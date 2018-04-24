package formulation.pcenters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import formulation.pcenters.pCenterCreator.PCenterCreator;
import results.ComputeResults;

public abstract class PCDistanceOrdered<CurrentParam extends PCenterParam> extends PCenter<CurrentParam>{

	protected int K;

	/** Ordered value of the existing distances in <d> */
	protected Double[] D;

	public PCDistanceOrdered(CurrentParam param) throws IOException, InvalidPCenterInputFile {

		super(param);
		initialize();
	}

	public PCDistanceOrdered(double[][] initialD, CurrentParam param, int p) throws Exception {
		super(initialD, param, p);
		initialize();
	}

	private void initialize() {

		TreeSet<Double> D = new TreeSet<>();

		for(int i = 0 ; i < N ; i++)
			if(!isClientDominated(i))
				for(int j = 0 ; j < M; j++)
					if(!isFactoryDominated(j))
						D.add(d[i][j]);

		this.K = D.size() - 1;

		ComputeResults.logSSLn("\tK : " + this.K);

		this.D = new Double[D.size()];
		this.D = D.toArray(this.D);

		//								System.out.println("\n-- K = " + this.K);
		//								for(int i = 0; i <= K; ++i)
		//									System.out.println("D[" + i + "] = " + this.D[i]);


		//								for(int i = 0 ; i < N ; i++) {
		//									for(int j = 0 ; j < M; j++)
		//				//						System.out.print(indexOfDistanceInD(d[i][j]) + "," + d[i][j] +"\t");
		//										System.out.print(d[i][j] +"\t");
		//									System.out.println();
		//								}
		//								System.out.println("--");
	}

	public int K() {
		return K;
	}


	protected boolean clientHasFactoryAtDk(int i, int k) {

		boolean found = false;
		int j = 0;

		while(j < M && !found) {

			if(!isFactoryDominated(j) && D[k].equals(d[i][j]))
				found = true;

			j++;

		}

		return found;

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

	/**
	 * Find the smallest distance greater than or equal to <dist>
	 * @param dist The lower bound on the sought distance
	 * @return The smallest distance in <D> greater than or equal to <dist>; or -Double.MAX_VALUE if no distance is greater than <dist>
	 */
	protected double lowestDistanceGreaterThan(double dist) {

		double result = -Double.MAX_VALUE;
		int i = 0;

		while(result == -Double.MAX_VALUE && i < D.length)
			if(D[i] >= dist)
				result = D[i];
			else
				++i;

		return result;

	}

	public static void solveMultichotomie(PCenterCreator creator, PCenterIndexedDistancesParam param, int K2){

		try {

			double time = param.cplex.getCplexTime();
			double resolutionTime = 0;
			param.filterDominatedClientsAndFactories = true;
			PCenter<?> formulation = creator.createFormulationObject(param);
			
			double optimalRadius = Double.MAX_VALUE;

			if(formulation instanceof PCDistanceOrdered) {

				PCDistanceOrdered<?> f = (PCDistanceOrdered<?>)formulation;

				boolean mustSolveRelaxation = true;

				while(K2 < f.D.length && optimalRadius == Double.MAX_VALUE) {

					int[] indexes = new int[K2];
					int gap = Math.max(1, f.D.length / K2);

					Map<Double, Double> repMap = new HashMap<>();

					System.out.println("\nD.length: " + f.D.length);
					System.out.println("K2: " + K2);
					System.out.println("Gap: " + gap);

					int rep = 0;

					/* List of the distances that are kept (named representative distances) */
					List<Double> representativeDistances = new ArrayList<>();

					/* List of the distance previous each representative
					 * If we know that the optimal radius is lower than a representative,
					 * then the next distance will constitute a lower bound (i.e., the previous distance)xs
					 */
					List<Double> previousRepDistances = new ArrayList<>();

					/**
					 * Number of distances represented by each representative 
					 */
					List<Integer> representativeSize = new ArrayList<>();

					for(int i = 0; i < K2; i++) {


						System.out.println("rep: " + f.D[rep]);
						representativeDistances.add(f.D[rep]);

						if(i == 0)
							previousRepDistances.add(null);
						else
							previousRepDistances.add(f.D[rep-1]);

						for(int j = rep; j < rep + gap; j++) {
							repMap.put(f.D[j], f.D[rep]);
							System.out.print(f.D[j] + " ");
						}

						if(i < f.D.length % K2) {
							// Ajouter 1 distance
							repMap.put(f.D[rep+gap], f.D[rep]);
							System.out.print(f.D[rep+gap] + " ");
							rep++;
							representativeSize.add(gap+1);
						}
						else
							representativeSize.add(gap);

						rep += gap;
						System.out.println();
//						System.out.println("(" + representativeSize.get(representativeDistances.size()-1) + ")");

					}

					System.out.println();

					double[][] newD = new double[f.d.length][];

					for(int i = 0; i < newD.length; i++) {

						//					if(!isClientDominated(i)) 
						{
							newD[i] = new double[f.d[i].length];

							for(int j = 0; j < newD[i].length; j++) 
								if(!f.isFactoryDominated(j) && !f.isClientDominated(i)){

									//								System.out.println((d[i] == null) + " " + (newD[i] == null) + " " + (repMap == null) + " " + (repMap.get(d[i][j])));
									//								System.out.println("i/j: " + i + "/" + j + " d[i][j]: " + d[i][j]);
									newD[i][j] = repMap.get(f.d[i][j]);
									//						System.out.print("in/out: " + d[i][j] + "/" + newD[i][j] + " ");
								}
								else
									newD[i][j] = Double.MAX_VALUE;
						}
					}

					double lb, ub;

					//					if(mustSolveRelaxation) 
					//					{
					//
					//						System.out.println("\n========= Continuous resolution of " + K2 + " distances");
					//
					//						/* Solve the relaxation with K2 distances until it is a Dk */
					//						RelaxationResult res = PCenter.computeRelaxationUntilEqualToADk(creator, param, newD, formulation.p);
					//
					////						/* Set the new distances */
					////						f.d = res.d;
					//
					//						/* Set the new lower bound */
					//						for(int i = 0; i < f.N; i++)
					//							if(!f.isClientDominated(i))
					//								for(int j = 0; j < f.M; ++j)
					//									if(!f.isFactoryDominated(j))
					//										if(f.d[i][j] < res.lb)
					//											f.d[i][j] = res.lb;
					//						
					//						System.out.println("Lower bound found: " + res.lb + "\n---");
					//					}
					//					else 
					{

						System.out.println("\n========= Integer resolution of " + K2 + " distances");

						/* Solve the integer problem with K2 distances */
						PCDistanceOrdered<?> p2 = (PCDistanceOrdered<?>)creator.createFormulationObject(newD, param, f.p);

						resolutionTime -= param.cplex.getCplexTime();
						PCenter.solve(p2);
						resolutionTime += param.cplex.getCplexTime();

						System.out.println("\nIteration radius: " + p2.getRadius());

						lb = p2.getRadius();

						int id = representativeDistances.indexOf(lb);

						if(representativeSize.get(id) != 1) { 

							/* If the radius found is represented by the last representative */
							if(id == representativeDistances.size() - 1)

								/* The upper bound is the last existing distance */
								ub = f.D[f.D.length - 1];

							else

								/* Otherwise, the new upper bound is the next representative -1 */
								ub = previousRepDistances.get(id+1)-1;

							System.out.println("lb/ub: " + lb + "/" + ub); 

							for(int i = 0; i < f.N; i++)
								if(!f.isClientDominated(i))
									for(int j = 0; j < f.M; ++j)
										if(!f.isFactoryDominated(j))
											if(f.d[i][j] < lb)
												f.d[i][j] = lb;
											else if(f.d[i][j] > ub)
												f.d[i][j] = ub + 1;


							System.out.println("Bounds found: [" + lb + ", " + ub + "]\n---");

						}
						/* If the found representative only corresponds to 1 distance, the optimum is obtained */
						else {
							optimalRadius = lb;
						}
					}

					mustSolveRelaxation = !mustSolveRelaxation;
					f.initialize();

				}

				/* Solve the last iteration */ 
				if(f.D.length > 1 && optimalRadius == Double.MAX_VALUE) {
					System.out.println("\n========= Last iteration " + K2 + " distances");

					resolutionTime -= param.cplex.getCplexTime();
					PCenter.solve(f);
					resolutionTime += param.cplex.getCplexTime();
					optimalRadius = f.getRadius();
				}

				System.out.println("\nFinal radius: " + optimalRadius);


				time = f.getCplex().getCplexTime() - time;

				System.out.println(((int)time) + "s (resolution: " + (int)resolutionTime + "s)");
			}
			else {
				System.err.println("Error: Cannot compute multichotomy on a formulation which does not contain the distances D");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void solveRelaxationMultichotomie(PCDistanceOrdered<?> originalFormulation, PCenterCreator creator, PCenterIndexedDistancesParam param, int K2){

		double time = originalFormulation.getCplex().getCplexTime();

		try {

			while(K2 < originalFormulation.D.length) {

				int[] indexes = new int[K2];
				int gap = Math.max(1, originalFormulation.D.length / K2);

				Map<Double, Double> repMap = new HashMap<>();

				System.out.println("\nD.length: " + originalFormulation.D.length);
				System.out.println("K2: " + K2);
				System.out.println("Gap: " + gap);

				int rep = 0;

				/* List of the distances that are kept (named representative distances) */
				List<Double> representativeDistances = new ArrayList<>();

				/* List of the distance previous each representative
				 * If we know that the optimal radius is lower than a representative,
				 * then the next distance will constitute a lower bound (i.e., the previous distance)xs
				 */
				List<Double> previousRepDistances = new ArrayList<>();

				for(int i = 0; i < K2; i++) {


					System.out.println("rep: " + originalFormulation.D[rep]);
					representativeDistances.add(originalFormulation.D[rep]);

					if(i == 0)
						previousRepDistances.add(null);
					else
						previousRepDistances.add(originalFormulation.D[rep-1]);

					for(int j = rep; j < rep + gap; j++) {
						repMap.put(originalFormulation.D[j], originalFormulation.D[rep]);
						System.out.print(originalFormulation.D[j] + " ");
					}

					if(i < originalFormulation.D.length % K2) {
						// Ajouter 1 distance
						repMap.put(originalFormulation.D[rep+gap], originalFormulation.D[rep]);
						System.out.print(originalFormulation.D[rep+gap] + " ");
						rep++;
					}

					rep += gap;
					System.out.println();

				}

				System.out.println();

				double[][] newD = new double[originalFormulation.d.length][];

				for(int i = 0; i < newD.length; i++) {

					//					if(!isClientDominated(i)) 
					{
						newD[i] = new double[originalFormulation.d[i].length];

						for(int j = 0; j < newD[i].length; j++) 
							if(!originalFormulation.isFactoryDominated(j) && !originalFormulation.isClientDominated(i)){

								//								System.out.println((d[i] == null) + " " + (newD[i] == null) + " " + (repMap == null) + " " + (repMap.get(d[i][j])));
								//								System.out.println("i/j: " + i + "/" + j + " d[i][j]: " + d[i][j]);
								newD[i][j] = repMap.get(originalFormulation.d[i][j]);
								//						System.out.print("in/out: " + d[i][j] + "/" + newD[i][j] + " ");
							}
							else
								newD[i][j] = Double.MAX_VALUE;
					}
				}

				PCDistanceOrdered<?> p2;
				p2 = (PCDistanceOrdered<?>)creator.createFormulationObject(newD, param, originalFormulation.p);


				PCenter.solve(p2);
				//				p2.getCplex().solve();
				System.out.println("\nIteration radius: " + p2.getRadius());

				double lb = p2.getRadius();

				int id = representativeDistances.indexOf(lb);

				double ub;

				if(id == representativeDistances.size() - 1)
					ub = originalFormulation.D[originalFormulation.D.length - 1];
				else
					ub = previousRepDistances.get(id+1)-1;

				System.out.println("lb/ub: " + lb + "/" + ub); 

				for(int i = 0; i < originalFormulation.N; i++)
					if(!originalFormulation.isClientDominated(i))
						for(int j = 0; j < originalFormulation.M; ++j)
							if(!originalFormulation.isFactoryDominated(j))
								if(originalFormulation.d[i][j] < lb)
									originalFormulation.d[i][j] = lb;
								else if(originalFormulation.d[i][j] > ub)
									originalFormulation.d[i][j] = ub + 1;

				originalFormulation.initialize();

			}

			/* Solve the last iteration */ 
			if(originalFormulation.D.length > 1) 
				PCenter.solve(originalFormulation);

			System.out.println("\nFinal radius: " + originalFormulation.getRadius());

			// resoudre le probleme sans reduire le nombre de distanes


			time = originalFormulation.getCplex().getCplexTime() - time;

			System.out.println((time) + "s");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
