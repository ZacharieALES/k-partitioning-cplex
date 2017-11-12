package formulation.pcenters;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import cplex.Cplex;
import formulation.PCRadiusIndex;
import formulation.interfaces.IFNodeVNodeBV;
import formulation.pcenters.PCenterIndexedDistancesParam.PCenterReturnType;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.YZLinkInequality;

public class PCSC extends PCDistanceOrdered implements IFNodeVNodeBV{

	protected IloNumVar z[];


	public PCSC(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {

		super(param);

	}

	@Override
	public IloNumVar nodeBVar(int i) throws IloException {
		return z[i];
	}

	@Override
	protected void createConstraints() throws IloException {
		createAtLeastOneCenter();
		createAtMostPCenter();
		createYZLinkConstraints();
	}

	protected void createYZLinkConstraints() throws IloException {

		/* For each client */
		for(int i = 0 ; i < N ; i++) {

			/* For each possible distance */
			for(int k = 1 ; k <= K ; ++k) {
				getCplex().addRange(new YZLinkInequality(this, i, k, D[k], d).createRange());
			}
		}

	}

	/**
	 * Get the index of a given distance in the array <D>
	 * @param dist The distance for which we want the index
	 * @return The index of <dist> in <D>; or -1 if dist is not int <D>
	 */
	protected int indexOfDistanceInD(double dist) {

		int result = -1;
		int i = 0;

		while(result == -1 && i < D.length) {

			if(D[i] == dist)
				result = i;
			else
				i++;
		}

		return result;

	}

	@Override
	protected void createObjective() throws IloException {

		IloLinearNumExpr obj = getCplex().linearNumExpr();

		double previous = D[0];

		for (int k = 1; k <= K; ++k){
			double newValue = D[k];

			/* If cplex returns the radius */
			if(param.returnType == PCenterReturnType.RADIUS)
				obj.addTerm(newValue - previous, z[k]);

			/* If cplex returns the radius index in <D> */
			else if(param.returnType == PCenterReturnType.RADIUS_INDEX)
				obj.addTerm(K - k, z[k]);
			else
				obj.addTerm(k, z[k]);

			previous = newValue;
		}

		getCplex().iloCplex.addMinimize(obj);
	}

	@Override
	protected void createNoneClientVariables() throws IloException {

		if(param.isInt)
			z = new IloIntVar[K+1];
		else
			z = new IloNumVar[K+1];

		for(int i = 1 ; i <= K ; i++) {
			if(param.isInt)
				z[i] = getCplex().iloCplex.intVar(0, 1);
			else
				z[i] = getCplex().iloCplex.numVar(0, 1);

			z[i].setName("z" + i);
		}

	}

	@Override
	public void displaySolution() throws UnknownObjectException, IloException {
		super.displaySolution();
		displayZVariables(5);
	}


	/**
	 * Display the value of the z variables
	 * @param numberByLine Number of variable displayed on each line
	 * @throws IloException 
	 * @throws UnknownObjectException 
	 */
	public void displayZVariables(int numberByLine) throws UnknownObjectException, IloException {

		System.out.print("(D0=" + Math.round(D[0]) + ")\t");

		for(int i = 1 ; i <= K ; i++) {
			System.out.print("(D" + i + "=" + Math.round(D[i]) + ") z" + i + "=" + cvg.getValue(z[i]) + "\t");
			if(i % numberByLine == numberByLine - 1)
				System.out.println();
		}
	}
	
	public double getRadius() throws IloException {

		double value = getCplex().getObjValue();

		/* If cplex directly returns the radius */
		if(param.returnType == PCenterReturnType.RADIUS)
			return value + D[0];

		/* If cplex returns the index of the radius */
		else {
			
			double radiusIndex = 0.0;
			
			for(int i = 1 ; i <= K() ; ++i)
				radiusIndex += this.cvg.getValue(z[i]);

			/* If the index is an int */
			if(param.isInt)
				return D[(int)Math.round(radiusIndex)];

		/* If the index is not an int */
			else {

				/* Get the two distances of the two integer index around <value> */
				double floor = D[(int)Math.floor(radiusIndex)];
				double ceil = D[(int)Math.ceil(radiusIndex)];

				/* Get the decimal part of the value */
				double decimalIndex = radiusIndex - Math.floor(radiusIndex);

				/* Return the proportional radius of <floor> and <ceil> according to <decimalIndex> */ 
				return floor + (ceil - floor) * decimalIndex;

			}
		}
	}

	public static void main(String[] args) {


		Cplex cplex = new Cplex();
		try {


			for(int i = 5 ; i < 60 ; i+= 5) {

				System.out.print("\ni = "+ i);

				for(int p = 2 ; p < Math.min(10, i+1) ; p++) {
					PCenterIndexedDistancesParam param = new PCenterIndexedDistancesParam("data/pcenters/random/pc_n" + i + "_p" + p + "_i_1.dat", cplex);

					param.returnType = PCenterReturnType.RADIUS_DECREASING_INDEX;
					param.isInt = false;
					PCSC pcsc = new PCSC(param);
					pcsc.createFormulation();
					cplex.solve();
					double pcscRelax = pcsc.getRadius();	

					pcsc.displayZVariables(5);								

					param.isInt = true;
					PCSC pcscInt = new PCSC(param);
					pcscInt.createFormulation();
					double timePCSC = cplex.getCplexTime();
					cplex.solve();
					timePCSC = cplex.getCplexTime() - timePCSC;
					double pcscOpt = pcscInt.getRadius();

//System.out.println("\n\n!int");
//					pcscInt.displayZVariables(5);	


					param.isInt = false;
					PCSCOrdered pcsco = new PCSCOrdered(param);
					pcsco.createFormulation();
					cplex.solve();
					double pcscoRelax = pcsco.getRadius();	

//					pcsc.displayZVariables(5);								

					param.isInt = true;
					PCSCOrdered pcscoInt = new PCSCOrdered(param);
					pcscoInt.createFormulation();
					double timePCSCO = cplex.getCplexTime();
					cplex.solve();
					timePCSCO = cplex.getCplexTime() - timePCSCO;
					double pcscoOpt = pcscoInt.getRadius();

//System.out.println("\n\n!int");
//					pcscInt.displayZVariables(5);
					
					param.returnType = PCenterReturnType.RADIUS_INDEX;
					param.isInt = false;
					pcsc = new PCSC(param);
					pcsc.createFormulation();
					cplex.solve();
					double pcsc2Relax = pcsc.getRadius();
					double pcsc2RelaxIndex = cplex.getObjValue();
					
					System.out.println("\n\n!coef (K-k):");
					pcsc.displayZVariables(5);						

					param.isInt = true;
					pcscInt = new PCSC(param);
					pcscInt.createFormulation();
					double timePCSC2 = cplex.getCplexTime();
					cplex.solve();
					timePCSC2 = cplex.getCplexTime() - timePCSC2;
					double pcsc2Opt = pcscInt.getRadius();
					double pcsc2OptIndex = cplex.getObjValue();
//System.out.println("\n\n!index int");
//					pcscInt.displayZVariables(5);	

//					pcscInt.displayZVariables(5);
					


					param.isInt = false;
					PCRadiusIndex pcRad = new PCRadiusIndex(param);
					pcRad.createFormulation();
					cplex.solve();
					double pcRadRelax = pcRad.getRadius();
					double pcRadRelaxIndex = cplex.getObjValue();
//					System.out.println("relax: " + pcRadRelaxIndex);
//					System.out.println("kstar: " + pcRad.cvg.getValue(pcRad.kStarVar()));
//					pcRad.displaySolution();
					
//					System.out.println("\n\n!index");
//					pcsc.displayZVariables(5);						

					param.isInt = true;
					PCRadiusIndex pcRadInt = new PCRadiusIndex(param);
					pcRadInt.createFormulation();
					double timePCRad = cplex.getCplexTime();
					cplex.solve();
					timePCRad = cplex.getCplexTime() - timePCRad;
					double pcRadOpt = pcRadInt.getRadius();
					double pcRadOptIndex = cplex.getObjValue();

					NumberFormat nf = new DecimalFormat("#0.0"); 
//					System.out.println("opt: " + pcRadOptIndex);
//					System.out.println("kstar: " + pcRadInt.cvg.getValue(pcRadInt.kStarVar()));
//					pcRadInt.displaySolution();
					
//					/* If the relaxation or the optimal value are different */
//					if(Math.abs(pcscRelax - pcsc2Relax) > 1E-4 || Math.abs(pcscOpt - pcsc2Opt) > 1E-4)
					{
						System.out.println("\n\tRelaxation pcsc/pcsc coef1/pcsc ordered/kstar: " + nf.format(pcscRelax) + "/" + nf.format(pcsc2Relax) + "/" + nf.format(pcscoRelax) + "/" + nf.format(pcRadRelax));
//						System.out.println("\tOptimum pcsc/pcsc new/pcsc ordered/star: " + pcscOpt + "/" + pcsc2Opt + "/" + pcscoOpt + "/" + pcRadOpt );
						System.out.println("\tTime pcsc/pcsc new/pcsc ordered/star: " + nf.format(timePCSC) + "/" + nf.format(timePCSC2) + "/" + nf.format(timePCSCO) + "/" + nf.format(timePCRad));
					}
					
//					System.exit(0);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public class Result{
		public double relaxation, objective, time, index;
		
		public Result(int relaxation, int objective, int time, int index) {
			this.relaxation = relaxation;
			this.objective = objective;
			this.time = time;
			this.index = index;
		}
	}

}
