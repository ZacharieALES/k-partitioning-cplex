package formulation.pcenters;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.Range;
import inequality_family.YKStarLinkInequalities;
import pcenters.PCResult;

public class PCRadiusIndex extends PCDistanceOrdered<PCenterParam>{

	IloNumVar kStar;

	public PCRadiusIndex(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {
		super(param);
	}

	public PCRadiusIndex(double[][] initialD, PCenterIndexedDistancesParam param, int p) throws Exception {
		super(initialD, param, p);
	}

	@Override
	protected void createConstraints() throws IloException {
		createAtLeastOneCenter();
		createAtMostPCenter();
		createKStartYLinkConstraints();
	}

	protected void createKStartYLinkConstraints() throws IloException{

		/* For each client */
		for(int i = 0 ; i < N ; i++)

			if(!isClientDominated(i))

				/* For each possible distance */
				for(int k = 1 ; k <= K ; ++k)

					/* If the inequality is not dominated by the next one */
					if(clientHasFactoryAtDk(i, k)) {
						getCplex().addRange(new YKStarLinkInequalities(this, i, k, D[k], d).createRange());
						Range r = new YKStarLinkInequalities(this, i, k, D[k], d).createRange();
						//						System.out.println(r.expr);
					}

	}

	@Override
	protected void createNoneFactoryVariables() throws IloException {
		if(param.isInt)
			kStar = getCplex().iloCplex.intVar(0, K);
		else
			kStar = getCplex().iloCplex.numVar(0, K);

		kStar.setName("k*");
	}

	@Override
	protected void createObjective() throws IloException {

		IloLinearNumExpr obj = getCplex().linearNumExpr();
		obj.addTerm(1.0, kStar);
		getCplex().iloCplex.addMinimize(obj);
	}



	public double getRadius() throws IloException {

		double value = getCplex().getObjValue();

		/* If the index is an int */
		if(param.isInt)
			return D[(int)Math.round(value)];

		/* If the index is not an int */
		else {

			/* Get the two distances of the two integer index around <value> */
			double floor = D[(int)Math.floor(value + 1E-6)];
			double ceil = D[(int)Math.ceil(value)];

			/* Get the decimal part of the value */
			double decimalIndex = value - Math.floor(value);

			/* Return the proportional radius of <floor> and <ceil> according to <decimalIndex> */ 
			return floor + (ceil - floor) * decimalIndex;

		}	
	}

	@Override
	public void displaySolution() throws UnknownObjectException, IloException {
		System.out.println(getMethodName() + " k* = " + cvg.getValue(kStar));
		displayYVariables(5);
	}

	public IloNumVar kStarVar() {
		return kStar;
	}

	@Override
	public String getMethodName() {
		return "pcrad";
	}

	public static PCResult solveLBStarFirst(PCenterIndexedDistancesParam param) throws Exception {

		NumberFormat nf = new DecimalFormat("#0.0");
		param = new PCenterIndexedDistancesParam(param); 

		/* Solve PCSCO relaxation first */
		param.isInt = false;
		PCSCOrdered pcsco = new PCSCOrdered(param);
		PCResult resLB = PCenter.solve(pcsco);	
		System.out.print("\tPCRad lbStar: " + nf.format(resLB.radius) + " ");

		double[][] currentD = pcsco.d;
		int p = pcsco.p;

		/* Solve successively PCRad with k* integer and y relaxed */
		param.isInt = true;
		param.isYInt = false;

		PCResult resLBStar = null;
		double time = resLB.time;

		param.initialLB = Math.ceil(resLB.radius);
		param.initialUB = pcsco.ub;
		double previousLB = -Double.MAX_VALUE;

		PCRadiusIndex pcrad;
		
		do{

			pcrad = new PCRadiusIndex(currentD, param, p);
			resLBStar = PCenter.solve(pcrad);
			System.out.print(resLBStar.radius + " ");
			time += resLBStar.time;
			
			currentD = pcrad.d;
			
					pcrad.displaySolution();
			previousLB = param.initialLB;
			param.initialLB = resLBStar.radius;

		}while(Math.abs(previousLB - param.initialLB) > 1E-4);

		System.out.println("(" + nf.format(resLB.time) + "/" + nf.format(time) + "s)");

		/* Solve PCRad with both k* and y integers */
		param.initialLB = (int)Math.ceil(resLBStar.radius);
		param.initialUB = pcrad.ub;
		param.isYInt = true;
		PCResult res = PCenter.solve(new PCRadiusIndex(currentD, param, p));
		res.time += time;
		res.methodName = "pcrad_lb*";


		//		System.out.println("\n\t" + nf.format(res.time) + "s\n");

		return res;

	}

}
