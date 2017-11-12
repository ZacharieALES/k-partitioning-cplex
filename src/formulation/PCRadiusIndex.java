package formulation;

import java.io.IOException;

import formulation.pcenters.InvalidPCenterInputFile;
import formulation.pcenters.PCDistanceOrdered;
import formulation.pcenters.PCenterIndexedDistancesParam;
import formulation.pcenters.PCenterIndexedDistancesParam.PCenterReturnType;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.YKStarLinkInequalities;
import inequality_family.YZLinkInequality;

public class PCRadiusIndex extends PCDistanceOrdered{

	IloNumVar kStar;

	public PCRadiusIndex(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {
		super(param);
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

			/* For each possible distance */
			for(int k = 1 ; k <= K ; ++k)

				/* If the inequality is not dominated by the next one */
				if(clientHasFactoryAtDk(i, k))
					getCplex().addRange(new YKStarLinkInequalities(this, i, k, D[k], d).createRange());

	}

//	private void createKStartYLinkConstraints() throws IloException {
//
//		/* For each client */
//		for(int i = 0 ; i < N ; i++) {
//
//			/* For each possible distance */
//			for(int k = 1 ; k <= K ; ++k) {
//				getCplex().addRange(new YKStarLinkInequalities(this, i, k, D[k], d).createRange());
//			}
//		}
//	}

	@Override
	protected void createNoneClientVariables() throws IloException {
		if(param.isInt)
			kStar = getCplex().iloCplex.intVar(0, K);
		else
			kStar = getCplex().iloCplex.numVar(0, K);
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
			double floor = D[(int)Math.floor(value)];
			double ceil = D[(int)Math.ceil(value)];

			/* Get the decimal part of the value */
			double decimalIndex = value - Math.floor(value);

			/* Return the proportional radius of <floor> and <ceil> according to <decimalIndex> */ 
			return floor + (ceil - floor) * decimalIndex;

		}
	}
	


	@Override
	public void displaySolution() throws UnknownObjectException, IloException {
		displayYVariables(5);
		System.out.println("k star: " + cvg.getValue(kStar));
	}

	public IloNumVar kStarVar() {
		return kStar;
	}

}
