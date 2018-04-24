package formulation.pcenters;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import cplex.Cplex;
import formulation.interfaces.IFNodeVNodeBV;
import formulation.pcenters.PCenterIndexedDistancesParam.PCenterReturnType;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.YZLinkInequality;
import results.ComputeResults;

public class PCSC extends PCDistanceOrdered<PCenterIndexedDistancesParam> implements IFNodeVNodeBV{

	protected IloNumVar z[];


	public PCSC(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {

		super(param);

	}

	public PCSC(double[][] currentD, PCenterIndexedDistancesParam param, int p) throws Exception {
		super(currentD, param, p);
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

		//		int counter = 0;

		/* For each client */
		for(int i = 0 ; i < N ; i++) {

			if(!isClientDominated(i))
				/* For each possible distance */
				for(int k = 1 ; k <= K ; ++k) {
					//				counter++;
					getCplex().addRange(new YZLinkInequality(this, i, k, D[k], d).createRange());
				}
		}

		//		System.out.println("\nPCSC: added " + counter + " yz inequalities");

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
				obj.addTerm(1, z[k]);
			//TODO : see how to find the best coefficients in the objective (e.g., obj.addTerm(K - k, z[k]);)
			else
				obj.addTerm(1.0, z[k]);

			previous = newValue;
		}

		getCplex().iloCplex.addMinimize(obj);
	}

	@Override
	protected void createNoneFactoryVariables() throws IloException {

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

		NumberFormat nf = new DecimalFormat("#0.00");

		System.out.print("(D0=" + Math.round(D[0]) + ")\t");

		for(int i = 1 ; i <= K ; i++) {
			System.out.print("(D" + i + "=" + Math.round(D[i]) + ") z" + i + "=" + nf.format(cvg.getValue(z[i])) + "\t");
			if(i % numberByLine == numberByLine - 1)
				System.out.println();
		}
	}

	public double radiusAssociatedToIndex(double value) throws UnknownObjectException, IloException {

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

	public double getRelaxationRadius() throws IloException {
		return(radiusAssociatedToIndex(getCplex().getBestObjValue()));
	}

	public double getRadius() throws IloException {
		return(radiusAssociatedToIndex(getCplex().getObjValue()));
	}

	public enum DataSet{
		RANDOM, ORLIB, TEST_RELAX, TSP, RANDOM_SOUROUR,TEST_MULTICHOTOMY
	}

	public static void main(String[] args) {

		Cplex cplex = new Cplex();
		try {


			DataSet dataSet;
			//			dataSet = DataSet.ORLIB;
			dataSet = DataSet.RANDOM_SOUROUR;
			//			dataSet = DataSet.RANDOM;
			//			dataSet = DataSet.TEST_RELAX;
			//			dataSet = DataSet.TSP;
			dataSet = DataSet.TEST_MULTICHOTOMY;

			switch(dataSet) {
			case RANDOM_SOUROUR:


				List<String> files = new ArrayList<>();

				for(int p = 5; p <= 15; p +=5)
					files.add("data/pcenters/allRandom/euc100_" + p + ".dat");

				for(int p = 5; p <= 15; p +=5)
					files.add("data/pcenters/allRandom/rand_100_" + p + ".dat");
				
				for(String path: files) {
					ComputeResults.log("\n\t" + path);

					BatchParam bp = new BatchParam(path);

					/* With bounds */
					bp.doPC = true;
					bp.doPCSCO = true; 
					bp.doPCSC = true;
					bp.doPCRad = true;

					/* With bounds and row/col reduction */
					bp.doPCSCOIt = false;
					bp.doPCRadIt = false;
					bp.doPCSCIt = false;

					bp.doPCRelax = false;
					bp.doPCSCORelax = false;
					bp.doPCSCRelax = false;
					bp.doPCRadRelax = false;
					bp.doPCRadURelax = false;

					bp.doPCRadLBInt = false;


					bp.doPCRadU = false;

					bp.doPCRUIt = false;

					bp.timeMaxInSeconds = 600;

					PCenter.batchSolve(bp, cplex);
				}


				break;

			case ORLIB:

				int iMin = 1;
				int iMax = 40;
				for(int i = iMin ; i <= iMax; i++) {

					String instanceName = "pcentre" + i;
					String filePath = "data/pcenters/sourour/" + instanceName + ".dat";

					ComputeResults.log("\n\t" + instanceName);

					BatchParam bp = new BatchParam(filePath);

					/* With bounds */
					bp.doPC = true;
					bp.doPCSCO = true; 
					bp.doPCSC = true;
					bp.doPCRad = true;

					/* With bounds and row/col reduction */
					bp.doPCSCOIt = false;
					bp.doPCRadIt = false;
					bp.doPCSCIt = false;

					bp.doPCRelax = false;
					bp.doPCSCORelax = false;
					bp.doPCSCRelax = false;
					bp.doPCRadRelax = false;
					bp.doPCRadURelax = false;

					bp.doPCRadLBInt = false;


					bp.doPCRadU = true;

					bp.doPCRUIt = false;

					bp.timeMaxInSeconds = 3600;

					PCenter.batchSolve(bp, cplex);

					//					System.exit(0);

				}

				break;
				

			case TEST_MULTICHOTOMY:

//				iMin = 40;
//				iMax = 40;
//				for(int i = iMin ; i <= iMax; i++) 
				{
					
					String instanceName = "u1817_90";
					String filePath = "data/pcenters/allTSP/" + instanceName + ".dat";
//					String instanceName = "pcentre" + i;
//					String filePath = "data/pcenters/sourour/" + instanceName + ".dat";

					ComputeResults.log("\n\t" + instanceName);

					BatchParam bp = new BatchParam(filePath);

					/* With bounds */
					bp.doPC = false;
					bp.doPCSCO = false; 
					bp.doPCSC = false;
					bp.doPCRad = false;

					/* With bounds and row/col reduction */
					bp.doPCSCOIt = false;
					bp.doPCRadIt = false;
					bp.doPCSCIt = false;

					bp.doPCRelax = false;
					bp.doPCSCORelax = false;
					bp.doPCSCRelax = false;
					bp.doPCRadRelax = false;
					bp.doPCRadURelax = false;

					bp.doPCRadLBInt = false;
					bp.doPCRadU = false;
					bp.doPCRUIt = false;
					
					bp.doPCSCOM = true;



					bp.timeMaxInSeconds = 3600;

					PCenter.batchSolve(bp, cplex);

										System.exit(0);

				}

				break;
				
			case TEST_RELAX:

				int nMin = 10;
				int nMax = 200;
				int pMin = 2;
				int pMax = 9;

				for(int n = nMin ; n <= nMax ; n+= 10) {

					for(int p = pMin ; p <= Math.min(pMax, n) ; p++) {

						for(int i = 1 ; i < 2; ++i) {

							//							if(p == 2 && (i == 22) || i == 44 || i == 61) 
							{

								System.out.println("\nn = "+ n + " p = " + p + " i = " + i);
								String filePath = "data/pcenters/random/pc_n" + n + "_p" + p + "_i_" + i + ".dat";

								BatchParam bp = new BatchParam(filePath);

								bp.doPC = false;
								bp.doPCSCO = false; 
								bp.doPCSC = false;
								bp.doPCRad = false;
								bp.doPCSCOIt = false;
								bp.doPCRadIt = false;
								bp.doPCRadLBInt = false;
								bp.doPCRelax = false;
								bp.doPCSCRelax = false;
								bp.doPCSCIt = false;
								bp.doPCRadRelax = false;


								bp.doPCRadU = true;
								bp.doPCRadURelax = false;

								bp.doPCSCORelax = false;
								bp.doPCORRelax = false;

								bp.doPCOR = false;
								bp.doPCSCO = true;


								PCenter.batchSolve(bp, cplex);
								//								System.exit(0);
							}
						}

					}
				}

				break;
			case TSP:

				String filePath = "data/pcenters/allTSP/u1060_10.dat";

				BatchParam bp = new BatchParam(filePath);

				/* With bounds */
				bp.doPC = false;
				bp.doPCSCO = false; 
				bp.doPCSC = false;
				bp.doPCRad = false;

				/* With bounds and row/col reduction */
				bp.doPCSCOIt = true;
				bp.doPCRadIt = true;

				bp.doPCRelax = false;
				bp.doPCSCORelax = false;
				bp.doPCSCRelax = false;
				bp.doPCRadRelax = false;

				bp.doPCSCIt = false;
				bp.doPCRadLBInt = false;

				bp.timeMaxInSeconds = 3600;

				PCenter.batchSolve(bp, cplex);

				break;
			case RANDOM:

				nMin = 10;
				nMax = 10;
				pMin = 2;
				pMax = 9;

				for(int n = nMin ; n <= nMax ; n+= 10) {

					for(int p = pMin ; p <= Math.min(pMax, n) ; p++) {

						for(int i = 1 ; i < 2; ++i) {

							//							if(p == 2 && (i == 22) || i == 44 || i == 61) 
							{

								System.out.println("\nn = "+ n + " p = " + p + " i = " + i);
								filePath = "data/pcenters/random/pc_n" + n + "_p" + p + "_i_" + i + ".dat";

								bp = new BatchParam(filePath);
								bp.doPCRadIt = false;
								bp.doPCRadLBInt = true;
								bp.doPCSCIt = false;

								bp.doPCSCO = false;
								bp.doPCRad = false;

								PCenter.batchSolve(bp, cplex);
							}
						}

					}
				}

				break;
			default:

				bp = new BatchParam("./data/pcenters/u1060_20.dat");
				bp.doPCRad = false;

				bp.doPCSCO = false;
				bp.doPCSCIt = false;

				PCenter.batchSolve(bp, cplex);

				break;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	@Override
	public String getMethodName() {
		return "pcsc";
	}

}
