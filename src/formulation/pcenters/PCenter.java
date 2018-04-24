package formulation.pcenters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import cplex.Cplex;
import formulation.interfaces.IFNodeV;
import formulation.pcenters.pCenterCreator.PCRadCreator;
import formulation.pcenters.pCenterCreator.PCSCCreator;
import formulation.pcenters.pCenterCreator.PCSCOCreator;
import formulation.pcenters.pCenterCreator.PCenterCreator;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.Range;
import pcenters.PCResult;
import results.ComputeResults;
import variable.CplexVariableGetter;

public abstract class PCenter<CurrentParam extends PCenterParam> implements IFNodeV{

	/** Number of clients */
	public int N;

	/** Number of factories */
	public int M;

	/** Maximal number of built centers */
	public int p;

	/** Distance between the clients and the factories */
	public double[][] d;

	public IloNumVar[] y;

	protected CplexVariableGetter cvg;
	protected CurrentParam param;

	/* Best known lower bound after the initialization phase (i.e., before the problem is solved) */
	protected double lb = -Double.MAX_VALUE;

	/** Best known upper bound after the initialization phase (i.e., before the problem is solved)  */
	protected double ub = Double.MAX_VALUE;

	/** True if the first factory is dominated (only used for the first factory j as dominated factories are represented by setting d[0][j] = Double.MAX_VALUE but if d[0][0] is modified it also modify the first client) */
	private boolean isFirstClientDominated = false;

	/** True if the first client is dominated (only used for the first client j as dominated clients are represented by setting d[j][0] = Double.MAX_VALUE but if d[0][0] is modified it also modify the first factory) */
	private boolean isFirstFactoryDominated = false;


	public PCenter(double[][] d, CurrentParam param, int p) throws Exception {

		if(d == null)
			throw new Exception("d is null");

		if(d.length == 0)
			throw new Exception("d is empty");

		if(d[0] == null)
			throw new Exception("d[0] is null");

		if(d[0].length == 0)
			throw new Exception("d[0] is empty");

		N = d.length;
		M = d[0].length;
		this.p = p; 

		initialize(d, param);
	}

	/**
	 * Create a p-center formulation from an input file.
	 * The input file format is:
	 * - The first line contains 3 integers separated by a space, they respectively correspond to:
	 * 		- The number of client N
	 * 		- The number of factories M
	 * 		- The value of p (I guess...)
	 * - The N next lines contain M values such that the value on line i and column j is the distance between the client number i and the factory number j.
	 * These values must be separated by spaces
	 * @param inputFile
	 * @throws IOException 
	 * @throws InvalidPCenterInputFile 
	 */
	public PCenter(CurrentParam param) throws IOException, InvalidPCenterInputFile {

		InputStream ips=new FileInputStream(param.inputFile); 
		InputStreamReader ipsr=new InputStreamReader(ips);
		BufferedReader br=new BufferedReader(ipsr);

		/* Read the first line */
		String ligne = br.readLine();
		String[] sTemp = ligne.split(" ");

		if(sTemp.length < 3){
			br.close();
			ips.close();
			throw new InvalidPCenterInputFile(param.inputFile, "The first line contains less than three values.");
		}

		N = Integer.parseInt(sTemp[0]);
		M = Integer.parseInt(sTemp[1]);
		p = Integer.parseInt(sTemp[2]);

		d = new double[N][M];
		int clientNb = 1;

		/* Read the next lines */
		while ((ligne=br.readLine())!=null && clientNb <= M){
			sTemp = ligne.split(" ");

			if(sTemp.length < M){
				br.close();
				ips.close();
				throw new InvalidPCenterInputFile(param.inputFile, "Line n°" + clientNb + " contains less than " + M + " values separated by spaces.");
			}

			for(int j = 0 ; j < M ; j++){
				double cDouble = Double.parseDouble(sTemp[j]);
				d[clientNb - 1][j] = cDouble;
			}

			clientNb++;			  

		}

		br.close();
		ips.close();

		if(clientNb - 1 < M)
			throw new InvalidPCenterInputFile(param.inputFile, "The file only contains " + (clientNb-1) + " distances lines instead of " + M);

		initialize(d, param);
	}

	private void initialize(double[][] d, CurrentParam param) {

		this.d = d;
		this.param = param;

		double previousLB = Double.MAX_VALUE;
		double previousUB = -Double.MAX_VALUE;

		lb = param.initialLB;
		ub = param.initialUB;

		if(lb != -Double.MAX_VALUE)
			for(int i = 0; i < N; ++i)
				for(int j = 0; j < M; ++j)
					if(!(d[i][j] == Double.MAX_VALUE))
						d[i][j] = Math.max(d[i][j], lb);

		if(ub != Double.MAX_VALUE)
			for(int i = 0; i < N; ++i)
				for(int j = 0; j < M; ++j)
					if(!(d[i][j] == Double.MAX_VALUE))
						d[i][j] = Math.min(d[i][j], ub + 1);

		filterClientsAndFactories();

//				System.out.println();
//				for(int i = 0; i < N; ++i) {
//					if(!isClientDominated(i)) {
//						for(int j = 0; j < M; ++j)
//							if(!isFactoryDominated(j))
//								System.out.print(d[i][j] + "\t||");
//						System.out.println();
//					}
//				}



		//		System.out.print(" (b1: " + lb + "/" + ub + ") ");
		int iteration = 0;
		boolean boundImproved = Math.abs(ub - lb) > 1E-4;

		while(boundImproved
				&& (param.computeBoundsSeveralTimes || iteration == 0)
				&& iteration < 10){

			previousLB = lb;
			previousUB = ub;

			if(param.useLB0 || param.useLB1) {
				lb = computeLowerBound();
				lb = Math.max(previousLB, lb);

				if(lb != previousLB)
					for(int i = 0; i < N; ++i)
						for(int j = 0; j < M; ++j)
							if(!(d[i][j] == Double.MAX_VALUE))
								d[i][j] = Math.max(d[i][j], lb);
				//				if(d[i][j] < lb)
				//					d[i][j] = 0;
			}

			if(param.useUB0 || param.useUB1) {
				ub = computeUpperBound();
				ub = Math.min(previousUB, ub);

				if(ub != previousUB)
					for(int i = 0; i < N; ++i)
						for(int j = 0; j < M; ++j)
							if(!(d[i][j] == Double.MAX_VALUE))
								d[i][j] = Math.min(d[i][j], ub + 1);
			}

//						System.out.println(" (b: " + lb + "/" + ub + ") ");

			/* If a bound is improved and the two bounds are not equal */
			boundImproved = (Math.abs(previousLB - lb) > 1E-4
					|| Math.abs(previousUB - ub) > 1E-4)
					&& Math.abs(ub - lb) > 1E-4;

					if(boundImproved)
						filterClientsAndFactories();

					iteration++;



					//					for(int i = 0; i < N; ++i) {
					//						if(!isClientDominated(i)) {
					//							for(int j = 0; j < M; ++j)
					//								if(!isFactoryDominated(j))
					//									System.out.print(d[i][j] + "\t|");
					//							System.out.println();
					//						}
					//					}
		}
		//		System.out.println("\n--End of preprocessing");

		//		System.out.println("bounds: [" + previousLB + ", " + lb + "] [" + previousUB + " " + ub + "]");

		//		if(iteration > 2)
		//			System.out.print("!");

		ComputeResults.logSSLn("\tbounds: [" + lb + ", " + ub + "]");


		this.cvg = new CplexVariableGetter(getCplex());
	}

	private void filterClientsAndFactories() {

		if(param.filterDominatedClientsAndFactories) {

			int dominatedClientsOnLastIteration;
			int dominatedFactoriesOnLastIteration;

			do {

				dominatedClientsOnLastIteration = 0;
				dominatedFactoriesOnLastIteration = 0;

				for(int c1 = 0; c1 < N; ++c1) {

					if(!isClientDominated(c1))
						for(int c2 = c1+1; c2 < N; ++c2) {

							// Warning: Client c1 can become dominated in the c2 for loop
							if(!isClientDominated(c2) && !isClientDominated(c1)) {
								boolean c1LowerThanC2 = true;
								boolean c2LowerThanC1 = true;
								int j = 0;

								while((c1LowerThanC2 || c2LowerThanC1) && j < M) {

									if(!isFactoryDominated(j)) 
									{
										if(d[c2][j] > d[c1][j])
											c2LowerThanC1 = false;

										if(d[c1][j] > d[c2][j])
											c1LowerThanC2 = false;
									}
									j++;

								}

								if(c1LowerThanC2) {
									//									System.out.println("C" + c1 + " dominated by C" + c2);
									if(c1 > 0)
										d[c1][0] = Double.MAX_VALUE;
									else
										isFirstClientDominated = true;

									dominatedClientsOnLastIteration++;
								}

								else if(c2LowerThanC1) { 
									//									System.out.println("C" + c2 + " dominated by C" + c1);
									if(c2 > 0)
										d[c2][0] = Double.MAX_VALUE;
									else
										isFirstFactoryDominated = true;

									dominatedClientsOnLastIteration++;
								}
							}
						}
				}

				for(int f1 = 0; f1 < M; ++f1)
					if(!isFactoryDominated(f1))
						for(int f2 = f1+1; f2 < M; ++f2) {

							// Warning: Factory f1 can become dominated in the f2 for loop
							if(!isFactoryDominated(f2) && !isFactoryDominated(f1)) {

								boolean f1Dominates = true;
								boolean f2Dominates = true;
								int i = 0;

								while((f1Dominates || f2Dominates) && i < N) {

									if(!isClientDominated(i)) 
									{
										if(d[i][f1] > d[i][f2])
											f2Dominates = false;

										if(d[i][f2] > d[i][f1])
											f1Dominates = false;
									}

									i++;

								}

								if(f1Dominates) {

									//																System.out.println("is not dom: " + (d[0][f1] == Double.MAX_VALUE) + "/" + (d[0][f2] == Double.MAX_VALUE));

									//									System.out.println("F" + f1 + " dominated by F" + f2);
									//
									//								System.out.println("F" + f1);
									//								for(int t = 0; t < N; t++)
									//									System.out.print("\t" + d[t][f1]);
									//								System.out.println("\nF" + f2);
									//								for(int t = 0; t < N; t++)
									//									System.out.print("\t" + d[t][f2]);
									//								System.out.println();

									if(f1 > 0)
										d[0][f1] = Double.MAX_VALUE;
									else
										isFirstFactoryDominated = true;

									dominatedFactoriesOnLastIteration++;

									//					System.out.println("++factory " + f1 + " dominated");
								}

								else if(f2Dominates) {

									//								System.out.println("is not dom: " + (d[0][f1] == Double.MAX_VALUE) + "/" + (d[0][f2] == Double.MAX_VALUE));
									//
									//									System.out.println("F" + f2 + " dominated by F" + f1);
									//
									//								System.out.println("F" + f1);
									//								for(int t = 0; t < N; t++)
									//									System.out.print("\t" + d[t][f1]);
									//								System.out.println("\nF" + f2);
									//								for(int t = 0; t < N; t++)
									//									System.out.print("\t" + d[t][f2]);
									//								System.out.println();

									if(f2 > 0)
										d[0][f2] = Double.MAX_VALUE;
									else
										isFirstFactoryDominated = true;

									dominatedFactoriesOnLastIteration++;
									//					System.out.println("++factory " + f2 + " dominated");
								}
							}
						}


//										System.out.print(" (dom " + dominatedClientsOnLastIteration + "/" + dominatedFactoriesOnLastIteration + ")" );
			}while(dominatedClientsOnLastIteration > 0 || dominatedFactoriesOnLastIteration > 0);
		}
	}

	public boolean isClientDominated(int idI) {return idI > 0 ? d[idI][0] == Double.MAX_VALUE: isFirstClientDominated;}
	public boolean isFactoryDominated(int idJ) {return idJ > 0 ? d[0][idJ] == Double.MAX_VALUE: isFirstFactoryDominated;}

	public int numberOfDominatedFactories() {
		int result = 0;
		for(int i = 0; i < M; i++)
			if(isFactoryDominated(i))
				result++;
		return result;
	}

	public int numberOfDominatedClients() {
		int result = 0;
		for(int i = 0; i < N; i++)
			if(isClientDominated(i))
				result++;
		return result;
	}

	/**
	 * Compute the lower bounds:
	 * lb0 = max_i min_j d_ij
	 * lb1 = (M-p)th smallest value of (max_i min_{h != j} d_ih) for all j
	 */
	public double computeLowerBound() {

		int nbOfUnDominatedFactories = -1;

		/* Order for each client its distance to the factories */ 
		List<TreeSet<PositionedDistance>> orderedLines = new ArrayList<>();

		double minDist = Double.MAX_VALUE;
		double secondMinDist = Double.MAX_VALUE;

		for(int i = 0 ; i < N; ++i) {
			TreeSet<PositionedDistance> tree = null;

			if(!isClientDominated(i)) {
				tree = new TreeSet<>(new Comparator<PositionedDistance>() {

					@Override
					public int compare(PCenter<CurrentParam>.PositionedDistance o1,
							PCenter<CurrentParam>.PositionedDistance o2) {
						int value = (int)(o1.distance - o2.distance);
						if(value == 0)
							value = 1;

						return value;
					}
				});

				for(int j = 0; j < M; ++j)
					if(!isFactoryDominated(j)) {
						tree.add(new PositionedDistance(j, d[i][j]));

						if(d[i][j] < minDist) {
							secondMinDist = minDist;
							minDist = d[i][j];
						}
						else if(d[i][j] > minDist && d[i][j] < secondMinDist)
							secondMinDist = d[i][j];
					}

				if(nbOfUnDominatedFactories == -1)
					nbOfUnDominatedFactories = tree.size();
			}

			orderedLines.add(tree);
		}

		boolean moreThanPUndominatedFactories = nbOfUnDominatedFactories > p;

		double lb0 = -Double.MAX_VALUE;

		if(param.useLB0) {

			for(TreeSet<PositionedDistance> tree: orderedLines) {

				/* If the client is not dominated */
				if(tree != null) {
					Iterator<PositionedDistance> it = tree.iterator();
					double minValue = it.next().distance;

					if(minValue > lb0)
						lb0 = minValue;
				}

			}

			//						System.out.println("\nLB0: " + lb0);
		}

		double lb1 = -Double.MAX_VALUE;

		if(param.useLB1 && moreThanPUndominatedFactories) {

			/* Tree set that will contain for each factory j, max_i min_{h != j} d[i][h]
			 * (i.e., lb0 if we know that factory j is not built) */
			TreeSet<Double> gamma = new TreeSet<>(new Comparator<Double>() {

				@Override
				public int compare(Double arg0, Double arg1) {
					int value = (int)(arg0 - arg1);
					if(value == 0)
						value = 1;
					return value;
				}

			});

			//			System.out.println("\n---");

			/* For each factory */
			for(int j = 0 ; j < M; j++) {

				if(!isFactoryDominated(j)) {

					//					System.out.print("j: " + j + " ");

					double gammaJ = -Double.MAX_VALUE;

					/* For each client */
					for(TreeSet<PositionedDistance> tree: orderedLines) {

						//						System.out.print(tree + ", ");
						/* If the client is not dominated */
						if(tree != null) {
							Iterator<PositionedDistance> it = tree.iterator();
							PositionedDistance pd = it.next();


							if(pd.position == j)
								pd = it.next();

							double minValue = pd.distance;

							if(minValue > gammaJ)
								gammaJ = minValue;
						}

					}
					gamma.add(gammaJ);
				}

			}

			//			System.out.println("\nList of the gammas: " + gamma);

			/* Browse <gamma> until position M-p */
			if(p < M - p) 
			{
				Iterator<Double> it = gamma.descendingIterator();

				for(int i = 0; i <= Math.min(p, gamma.size() - 1); ++i)
					lb1 = it.next();
			}
			else {
				Iterator<Double> it = gamma.iterator();

				for(int i = 0; i  < Math.min(gamma.size() - p, gamma.size()); ++i)
					lb1 = it.next();
			}

			//			System.out.println("\nLB1: " + lb1);


			if(lb1 == lb) {

				/* Factories that must be built to have a solution equals to lb. 
				 * More precisely: each factory is such that there is a client at distance lb whose 
				 * only factory has 1 factory at distance lb */ 
				Set<Integer> factoriesToBuild = new TreeSet<>();
				List<Integer> clientsCovered = new ArrayList<>();

				int ci = 0;

				while(ci < N && factoriesToBuild.size() <= p) {

					if(!isClientDominated(ci)) {
						int firstFactoryAtDistanceLb = -1;
						int secondFactoryAtDistanceLb = -1;
						int fj = 0;

						while(fj < M && secondFactoryAtDistanceLb == -1) {

							if(!isFactoryDominated(fj))
								if(d[ci][fj] == lb)
									if(firstFactoryAtDistanceLb == -1)
										firstFactoryAtDistanceLb = fj;
									else
										secondFactoryAtDistanceLb = fj;

							fj++;
						}

						if(secondFactoryAtDistanceLb == -1 && firstFactoryAtDistanceLb != -1) {
							factoriesToBuild.add(firstFactoryAtDistanceLb);
							clientsCovered.add(ci);
						}
					}
					ci++;

				}

				//				if(factoriesToBuild.size() >= p)
				//					if(factoriesToBuild.size() > p) {
				//						lb1 = secondMinDist;
				//						System.out.println("\n>p factories to build (\" + factoriesToBuild + \"), lb = " + lb1);
				//					}
				//					else {
				//						double radius = -Double.MAX_VALUE;
				//
				//						for(int i = 0 ; i < N; i++)
				//							if(!isClientDominated(i)) {
				//								Iterator<Integer> itFactories = factoriesToBuild.iterator();
				//								double min = d[i][itFactories.next()];
				//
				//								while(itFactories.hasNext()) {
				//									double dist = d[i][itFactories.next()];
				//									if(dist < min)
				//										min = dist;
				//								}
				//
				//								if(radius < min)
				//									radius = min;
				//
				//							}
				//
				//						if(radius > minDist)
				//							lb1 = secondMinDist;
				//
				//						System.out.println("\np factories to build (" + factoriesToBuild + "), lb = " + lb1 + " (previous ub: " + ub + ")");
				//						System.out.println("min/second dist: " + minDist + "/" + secondMinDist);
				//						System.out.println("radius: " + radius);
				//						ub = Math.min(ub, radius);
				//					}
				//				else
				//					System.out.print(".");
			}

		}

		return Math.max(lb, Math.max(lb0, lb1));
		//		return Math.max(lb0, lb1);

	}	

	/**
	 * Compute the upper bounds:
	 * ub0 = min_j max_i d_ij
	 * ub1 = greedy solution (at each step add the factory which reduces the radius)
	 */
	public double computeUpperBound() {

		Random r = new Random();

		/* List of the factories that are not currently in the greedy solution */
		List<Integer> remainingFactories = new ArrayList<>();

		for(int j = 0; j < M; ++j)
			if(!isFactoryDominated(j))
				remainingFactories.add(j);

		/* Distance of each client to its closest factory currently in the solution */
		double[] distToFactory = new double[N];

		for(int i = 0; i < N; ++i) 
			distToFactory[i] = Double.MAX_VALUE;

		/* Value of the current solution */
		double currentRadius = Double.MAX_VALUE;

		int nbOfSteps = 1;

		if(param.useUB1)
			nbOfSteps = p;
		//		System.out.print("!");

		/* For each step of the greedy algorithm */
		int step = 0;
		boolean isOver = false;

		while(step < nbOfSteps && !isOver) {

			/* Best factories currently found and their radius */
			List<Integer> bestCandidates = new ArrayList<>();
			double bestRadius = currentRadius;

			//			System.out.println("Remaining factories: " + remainingFactories);

			/* For each remaining factory */
			for(Integer j: remainingFactories) {

				//				System.out.print("\nj/d[0][j]: " + j + "/" + d[0][j]);
				/* New radius if j is added to the solution */
				Double radiusJ = null;

				for(int i = 0; i < N; ++i) 
					if(!isClientDominated(i)) // OK ?????????
						if(radiusJ == null)
							radiusJ = Math.min(distToFactory[i], d[i][j]);
						else
							radiusJ = Math.max(radiusJ, Math.min(distToFactory[i], d[i][j]));

				//				System.out.println("radiusJ: " + radiusJ);

				if(radiusJ != null && radiusJ <= bestRadius) {

					if(radiusJ < bestRadius) {
						bestCandidates = new ArrayList<>();
						bestCandidates.add(j);
						bestRadius = radiusJ;
					}
					else 
						bestCandidates.add(j);

				}	
			}

			//			System.out.println("Best candidates: " + bestCandidates);

			if(remainingFactories.size() == 0 || bestCandidates.size() == 0) 
				isOver = true;

			if(!isOver) {
				// Can bestCandidates be empty here ?
				int id = 0;
//				int id = r.nextInt(bestCandidates.size());

				/* Update the distances to the factories */
				for(int i = 0; i < N; ++i) 
					if(!isClientDominated(i))
						distToFactory[i] = Math.min(distToFactory[i], d[i][bestCandidates.get(id)]);

				/* Update the radius */
				currentRadius = bestRadius;

				remainingFactories.remove(Integer.valueOf(bestCandidates.get(id)));
			}

			step++;
		}


		//				System.out.println("Upper bound: " + currentRadius + " (factories not used: " + remainingFactories + ")");

		return Math.min(ub, currentRadius);
		//		return currentRadius;

	}

	public abstract double getRadius() throws IloException;
	public abstract double getRelaxationRadius() throws IloException;

	private class PositionedDistance{
		int position;
		double distance;

		public PositionedDistance(int position, double distance) {
			this.position = position;
			this.distance = distance;
		}

		@Override
		public String toString() {return distance + "";}
	}

	public void createFormulation() throws IloException {

		if(!param.cplexOutput)
			getCplex().turnOffCPOutput();

		if(!param.useCplexAutoCuts)
			getCplex().removeAutomaticCuts();

		if(!param.useCplexPrimalDual)
			getCplex().turnOffPrimalDualReduction();

		/* Create the model */
		getCplex().iloCplex.clearModel();
		getCplex().iloCplex.clearCallbacks();

		/* Reinitialize the parameters to their default value */
		getCplex().setDefaults();

		if(param.tilim != -1)
			getCplex().setParam(IloCplex.DoubleParam.TiLim, Math.max(10,param.tilim));

		createFactoryVariables();
		createNoneFactoryVariables();
		createConstraints();
		createObjective();
	}

	private void createFactoryVariables() throws IloException {

		if(param.isInt && param.isYInt)
			y = new IloIntVar[M];
		else 
			y = new IloNumVar[M];

		for(int j = 0 ; j < M ; j++) {
			if(!isFactoryDominated(j)) {
				if(param.isInt && param.isYInt)
					y[j] = getCplex().iloCplex.intVar(0, 1);
				else
					y[j] = getCplex().iloCplex.numVar(0, 1);

				y[j].setName("y" + j);
			}
		}

	}

	protected abstract void createConstraints() throws IloException;

	protected abstract void createNoneFactoryVariables() throws IloException;

	protected abstract void createObjective() throws IloException;

	@Override
	public int n() {
		return N;
	}

	@Override
	public IloNumVar nodeVar(int i) throws IloException {
		return y[i];
	}

	@Override
	public Cplex getCplex() {
		return param.cplex;
	}

	@Override
	public CplexVariableGetter variableGetter() {
		return cvg;
	}

	@Override
	public void displaySolution() throws UnknownObjectException, IloException {
		displayYVariables(5);
	}

	protected void createAtLeastOneCenter() throws IloException {

		IloLinearNumExpr expr = getCplex().linearNumExpr();

		for(int m = 0 ; m < M ; m++)
			if(!isFactoryDominated(m))
				expr.addTerm(1.0, y[m]);

		getCplex().addRange(new Range(1.0, expr));
	}

	protected  void createAtMostPCenter() throws IloException {

		IloLinearNumExpr expr = getCplex().linearNumExpr();

		for(int m = 0 ; m < M ; m++)
			if(!isFactoryDominated(m)) {
				expr.addTerm(1.0, y[m]);
			}

		getCplex().addRange(new Range(expr, p));
	}

	/**
	 * Display the value of the y variables
	 * @param numberByLine Number of variable displayed on each line
	 * @throws IloException 
	 * @throws UnknownObjectException 
	 */
	protected void displayYVariables(int numberByLine) throws UnknownObjectException, IloException {

		NumberFormat nf = new DecimalFormat("#0.00");

		for(int i = 0 ; i < M ; i++) {
			if(!isFactoryDominated(i))
				System.out.print("y" + (i+1) + "=" + nf.format(cvg.getValue(y[i])) + "\t");
			else
				System.out.print("y" + (i+1) + "=" + "dominated" + "\t");

			if(i % numberByLine == numberByLine - 1)
				System.out.println();
		}
	}

	/**
	 * Generate an instance in which:
	 * - each client and each factory has (x,y) coordinates randomly generated between 0 and <maxCoordinateValue> 
	 * @param outputFile The path at which the output file will be created 
	 * @param seed The random seed
	 * @param n The number of clients
	 * @param m The number of factories
	 * @param maxCoordinateValue Maximal value of a coordinate of a factory
	 * @param factoriesEqualToClients True if the factories potential sites are the clients sites
	 */
	public static void generateInstance(String outputFile, int seed, int p, int n, int m, int maxCoordinateValue, boolean factoriesEqualToClients) {

		Random r = new Random(seed);

		/* Generate the client coordinates */
		int[][] clientCoordinates = new int[n][2];

		for(int i = 0 ; i < n ; i++) {
			clientCoordinates[i][0] = r.nextInt(maxCoordinateValue);
			clientCoordinates[i][1] = r.nextInt(maxCoordinateValue);
		}

		/* Generate the factories coordinates */
		int[][] factoriesCoordinates;

		if(factoriesEqualToClients) {
			factoriesCoordinates = clientCoordinates;
			m = n;
		}
		else {
			factoriesCoordinates = new int[m][2];
			for(int i = 0 ; i < m ; i++) {
				factoriesCoordinates[i][0] = r.nextInt(maxCoordinateValue);
				factoriesCoordinates[i][1] = r.nextInt(maxCoordinateValue);
			}
		}

		int[][] distances = new int[n][m];

		for(int i = 0 ; i < n ; i++)
			for(int j = 0 ; j < n ; j++)
				distances[i][j] = (int)Math.sqrt(Math.pow(clientCoordinates[i][0]-factoriesCoordinates[j][0], 2) + Math.pow(clientCoordinates[i][1]-factoriesCoordinates[j][1], 2));

		try{
			FileWriter fw = new FileWriter(outputFile, false); // True if the text is appened at the end of the file, false if the content of the file is removed prior to write in it
			BufferedWriter output = new BufferedWriter(fw);


			output.write(n + " " + m + " " + p + "\n");

			for(int i = 0 ; i < n ; i++) {
				for(int j = 0 ; j < m ; j++)
					output.write(distances[i][j] + " ");
				output.write("\n");
				output.flush();
			}

			output.close();
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}



	}

	public abstract String getMethodName();


	public static PCResult solve(PCenter<?> formulation) throws IloException {

		double time = formulation.getCplex().getCplexTime();
		formulation.createFormulation();

		formulation.getCplex().solve();
		time = formulation.getCplex().getCplexTime() - time;

//		ComputeResults.logSSLn("\tRelaxation: " + formulation.getRelaxationRadius());
//				System.out.println("\n---\n" + formulation.getMethodName() + "(relaxation: " + formulation.getRelaxationRadius() + ")");
//				formulation.displaySolution();
		
//		ComputeResults.logSSLn("\n\tcstr/var: " + formulation.getCplex().iloCplex.getNrows() + "/" + formulation.getCplex().iloCplex.getNcols());
		
		
		
//		formulation.getCplex().iloCplex.exportModel("test" + truc + ".lp");

		return new PCResult(formulation.getRadius(), time, formulation.getMethodName());
	}


	public static List<List<PCResult>> batchSolve(BatchParam bParam, Cplex cplex) throws IloException, IOException, InvalidPCenterInputFile {

		NumberFormat nf = new DecimalFormat("#0.00"); 

		/* List of parameters */
		PCenterIndexedDistancesParam paramPCSCInt = new PCenterIndexedDistancesParam(bParam.filePath, cplex);
		paramPCSCInt.tilim = bParam.timeMaxInSeconds;
		paramPCSCInt.filterDominatedClientsAndFactories = true;
		//				paramPCSCInt.computeBoundsSeveralTimes = false;

		PCenterIndexedDistancesParam paramPCSCRelax = new PCenterIndexedDistancesParam(bParam.filePath, cplex);
		paramPCSCRelax.isInt = false;
		paramPCSCRelax.filterDominatedClientsAndFactories = true;
		//				paramPCSCRelax.computeBou ndsSeveralTimes = false;

		/* Resolution */
		List<List<PCResult>> returnedResults = new ArrayList<>();
		List<PCResult> resultsOpt = new ArrayList<>();
		List<PCResult> resultsRelax = new ArrayList<>();
		returnedResults.add(resultsOpt);
		returnedResults.add(resultsRelax);
		


		paramPCSCRelax.useLB0 = true;
		paramPCSCRelax.useLB1 = true;
		paramPCSCRelax.useUB0 = true;
		paramPCSCRelax.useUB1 = true;
		paramPCSCRelax.computeBoundsSeveralTimes = true;

		paramPCSCInt.useLB0 = true;
		paramPCSCInt.useLB1 = true;
		paramPCSCInt.useUB0 = true;
		paramPCSCInt.useUB1 = true;
		paramPCSCInt.computeBoundsSeveralTimes = true;

		/* Relaxations */

		if(bParam.doPCRadRelax) {
			ComputeResults.logSSLn("--- PCRr: ");
			resultsRelax.add(solve(new PCRadiusIndex(paramPCSCRelax)));
			ComputeResults.log("\t" + nf.format(resultsRelax.get(resultsRelax.size() - 1).radius));
		}

		if(bParam.doPCRelax) {
			ComputeResults.logSSLn("--- PCr: "); 
			resultsRelax.add(solve(new PC(paramPCSCRelax)));
			ComputeResults.log("\t" + nf.format(resultsRelax.get(resultsRelax.size() - 1).radius));
		}

		if(bParam.doPCSCRelax) {
			ComputeResults.logSSLn("--- PCSCr: ");
			resultsRelax.add(solve(new PCSC(paramPCSCRelax)));
			ComputeResults.log("\t" + nf.format(resultsRelax.get(resultsRelax.size() - 1).radius));
		}

		if(bParam.doPCSCORelax) {
			ComputeResults.logSSLn("--- PCSCOr: ");
			resultsRelax.add(solve(new PCSCOrdered(paramPCSCRelax)));
			ComputeResults.log("\t" + nf.format(resultsRelax.get(resultsRelax.size() - 1).radius));
		}

		if(bParam.doPCORRelax) {
			ComputeResults.logSSLn("--- PCORr: ");
			resultsRelax.add(solve(new PCOR(paramPCSCRelax)));
			ComputeResults.log("\t" + nf.format(resultsRelax.get(resultsRelax.size() - 1).radius));
		}

		if(bParam.doPCRadURelax) {
			ComputeResults.logSSLn("--- PCRUr: ");
			resultsRelax.add(solve(new PCRadiusCalik(paramPCSCRelax)));
			ComputeResults.log("\t" + nf.format(resultsRelax.get(resultsRelax.size() - 1).radius));
		}
		
		/* Optimal resolutions */
		if(bParam.doPC) {
			ComputeResults.logSSLn("--- PC:   ");
			resultsOpt.add(PCenter.solve(new PC(paramPCSCInt)));
			ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
		}

		if(bParam.doPCSC) {
			ComputeResults.logSSLn("--- PCSC: ");
			resultsOpt.add(PCenter.solve(new PCSC(paramPCSCInt)));
			ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
		}

		if(bParam.doPCSCOM) {
			ComputeResults.logSSLn("--- PCSCO-M");
			PCDistanceOrdered.solveMultichotomie(new PCSCOCreator(), paramPCSCInt, 5);
//			resultsOpt.add(PCenter.solve(new PCSCOrdered(paramPCSCInt)));
//			ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
		}

		if(bParam.doPCSCO) {
			ComputeResults.logSSLn("--- PCSCO");
			resultsOpt.add(PCenter.solve(new PCSCOrdered(paramPCSCInt)));
			ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
		}

		if(bParam.doPCRad) {
			ComputeResults.logSSLn("--- PCR:   ");
			resultsOpt.add(PCenter.solve(new PCRadiusIndex(paramPCSCInt)));
			ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
		}

		if(bParam.doPCOR) {
			ComputeResults.logSSLn("--- PCOR:   ");
			resultsOpt.add(PCenter.solve(new PCOR(paramPCSCInt)));
			ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
		}

		if(bParam.doPCRadU) {
			ComputeResults.logSSLn("--- PCRU:   ");
			resultsOpt.add(PCenter.solve(new PCRadiusCalik(paramPCSCInt)));
			ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
		}

		if(bParam.doPCRadLBInt)
			try {
				ComputeResults.logSSLn("--- PCRIt");
				resultsOpt.add(PCRadiusIndex.solveLBStarFirst(paramPCSCInt));
				ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
			} catch (Exception e1) {
				e1.printStackTrace();
			}


				/* With iterative relaxations */
		//		//		paramPCSCInt.useLB0 = false;
		//		//		paramPCSCInt.useLB1 = false;
		//		//		paramPCSCInt.useBounds(false);

		if(bParam.doPCSCIt)
			try {
				ComputeResults.logSSLn("--- PCSCIt");
				resultsOpt.add(PCenter.solveIteratively(new PCSCCreator(), paramPCSCInt));
				ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
			} catch (Exception e) {
				e.printStackTrace();
			}

		if(bParam.doPCSCOIt)
			try {
				ComputeResults.logSSLn("--- PCSCOIt");
				resultsOpt.add(PCenter.solveIteratively(new PCSCOCreator(), paramPCSCInt));
				ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
			} catch (Exception e) {
				e.printStackTrace();
			}

		if(bParam.doPCRadIt)
			try {
				ComputeResults.logSSLn("--- PCRadIt");
				resultsOpt.add(PCenter.solveIteratively(new PCRadCreator(), paramPCSCInt));
				ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
			} catch (Exception e) {
				e.printStackTrace();
			}

		if(bParam.doPCRUIt)
			try {
				ComputeResults.logSSLn("--- PCRUIt");
				resultsOpt.add(PCenter.solveIteratively(new PCRadCreator(), paramPCSCInt));
				ComputeResults.log("\t" + nf.format(resultsOpt.get(resultsOpt.size() - 1).radius) + " " + nf.format(resultsOpt.get(resultsOpt.size() - 1).time) + "s");
			} catch (Exception e) {
				e.printStackTrace();
			}
		//		//		resultsOpt.add(PCenter.solveIteratively(new PCRadCreator(), new PCSCOCreator(), paramPCSCInt));

		//		/* 1 - Display the relaxations */
		//		if(resultsRelax.size() > 0) {
		//			System.out.print("\n\tRelaxation ");
		//
		//			for(PCResult res: resultsRelax)
		//				System.out.print(res.methodName + "/");
		//			System.out.print(": ");
		//
		//			for(PCResult res: resultsRelax)
		//				System.out.print(nf.format(res.radius) + "/");
		//		}
		//
		//		/* 2 - Display the resolution times (and optionally the radiuses if there are differences) */
		//		System.out.println("\n\tTime ");
		//
		//		//		Collections.sort(resultsOpt, new Comparator<PCResult>() {
		//		//
		//		//			@Override
		//		//			public int compare(PCResult o1, PCResult o2) {
		//		//				return (int)(1000 * (o1.time - o2.time));
		//		//			}
		//		//
		//		//		});
		//
		//		for(PCResult res: resultsOpt)
		//			System.out.println("\t\t" + res.methodName + ": " + nf.format(res.time) + "s");
		//
		//		String optRadiuses = "";
		//		double optRadius = -Double.MAX_VALUE;
		//
		//		for(PCResult res: resultsOpt) {
		//			//			System.out.print(nf.format(res.time) + "/");
		//
		//			optRadiuses += res.radius + "/";
		//
		//			/* If it is the first method */
		//			if(optRadius == -Double.MAX_VALUE)
		//				optRadius = res.radius;
		//
		//			/* If the results are not coherent with the previous methods */
		//			else if(Math.abs(optRadius - res.radius) > 1E-4)
		//				optRadius = Double.MAX_VALUE;
		//
		//		}
		//
		//
		//		if(optRadius == Double.MAX_VALUE) {
		//			System.out.println("\n!!! Different optimum solutions obtained: " + optRadiuses);
		//			System.exit(0);
		//		}
		//		else		
		//			System.out.println("\n\tOptimal radius: " + nf.format(optRadius) + "\n");


		//		if(Math.abs(PCenter.LBStar - PCRadiusIndex.PCRad_LB) > 1E-4) {
		//			System.out.println("LB* = " + PCenter.LBStar + ", PCRad LB = " + PCRadiusIndex.PCRad_LB);
		//			System.exit(0);
		//		}

		return returnedResults;

	}

	public static double LBStar = 0;

	public static RelaxationResult computeRelaxationUntilEqualToADk(PCenterCreator creator, PCenterIndexedDistancesParam param) throws Exception {
		return computeRelaxationUntilEqualToADk(creator, param, null, -1);
	}

		public static RelaxationResult computeRelaxationUntilEqualToADk(PCenterCreator creator, PCenterIndexedDistancesParam param, double[][] currentD, int p) throws Exception {
			
		double lastRelaxation = Double.MAX_VALUE;
		double previousRelaxation = -Double.MAX_VALUE;

		param.isInt = false;
		
		double initialTime = param.cplex.getCplexTime();

		//		System.out.print("\tRelax " + creatorRelaxation.getMethodName() + ": ");
		while(Math.abs(lastRelaxation - previousRelaxation) > 1E-4) {

			PCenter<?> relax;
			if(currentD == null || p == -1)
				relax = creator.createFormulationObject(param);
			else
				relax = creator.createFormulationObject(currentD, param, p);

			currentD = relax.d;
			p = relax.p;

			relax.createFormulation();
			relax.getCplex().solve();
			double radius = relax.getRadius();

			//			System.out.print(nf.format(radius) + " ");

			previousRelaxation = lastRelaxation;
			lastRelaxation = radius;

			LBStar = radius;
			
			param.initialLB = (int)Math.ceil(radius);
			param.initialUB = relax.ub;

			//			if(Math.abs(lastRelaxation - previousRelaxation) <= 1E-4)
			//				relax.displaySolution();
			// Does not improve the results
			//			param.initialLB = relax.lowestDistanceGreaterThan(radius);

			System.out.println("lb/ub: " + param.initialLB + "/" + param.initialUB);
		}
		
		RelaxationResult res = new RelaxationResult(param.cplex.getCplexTime() - initialTime, p, currentD);
		res.lb = param.initialLB;
		return res;
	}


	public static PCResult solveIteratively(PCenterCreator creatorRelaxation, PCenterCreator creatorIntegerResolution, PCenterIndexedDistancesParam param) throws Exception {

		NumberFormat nf = new DecimalFormat("#0.0");

		double time = param.cplex.getCplexTime();

		/* Step 1 - solve the relaxation until it is a Dk */
		RelaxationResult relaxationRes = computeRelaxationUntilEqualToADk(creatorRelaxation, param);
		
		ComputeResults.logSSLn("\tFirst step: " + nf.format((relaxationRes.time)) + " s");

		/* Step 2 - solve the remaining problem */
		param.isInt = true;

		PCenter<?> pcsc = creatorIntegerResolution.createFormulationObject(relaxationRes.d, param, relaxationRes.p);
		pcsc.createFormulation();

		//		System.out.println("\tK: " + ((PCDistanceOrdered)pcsc).K);

		pcsc.getCplex().solve();

		ComputeResults.logSSLn("\tRelaxation: " + pcsc.getRelaxationRadius());

		ComputeResults.logSSLn("\tdominated factories/clients: " + pcsc.numberOfDominatedFactories() + "/" + pcsc.numberOfDominatedClients()) ;

		//		System.out.println("\n---\n" + pcsc.getMethodName());
		//		pcsc.displaySolution();
		time = pcsc.getCplex().getCplexTime() - time;

		String method = creatorRelaxation.getMethodName();

		if(!creatorRelaxation.getMethodName().equals(creatorIntegerResolution.getMethodName()))
			method += "_" + creatorIntegerResolution.getMethodName();

		//		pcsc.displaySolution();
		return new PCResult(pcsc.getRadius(), time, method.toLowerCase() + "_it");


	}

	public static PCResult solveIteratively(PCenterCreator creator, PCenterIndexedDistancesParam param) throws Exception {
		return solveIteratively(creator, creator, param);	
	}

	//	public static void main(String[] args) {
	//
	//
	//							for(int n = 10 ; n < 101 ; n+= 10)
	//								for(int p = 2 ; p < Math.min(10, n+1) ; p++)
	//									for(int i = 0; i < 10; ++i)
	//									PCenter.generateInstance("data/pcenters/random/pc_n"+n+"_p"+p+"_i_" + i + ".dat", i, p, n, n, 1000, true);
	//
	//	}
}
