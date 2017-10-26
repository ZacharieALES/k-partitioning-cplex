package main;

import java.io.File;
import java.util.ArrayList;

import callback.branch_callback.Branch_EmptyCB;
import callback.cut_callback.CBAddSubRepInequalities;
import cplex.Cplex;
import formulation.PartitionWithRepresentative;
import formulation.PartitionWithTildes;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import ilog.concert.IloException;
import results.ComputeResults;
import separation.SeparationSubRepresentativeSansDoublon;

/**
 * Calcul de la relaxation linéaire obtenue avec la formulation tilde lorsque l'ensemble des coupes de type sous-ensembles représentant sont ajoutées
 * @author zach
 *
 */
public class ExecutionSubRepAddedWhenBranching extends Execution{

	String pre = "results/";
	String pre2 = "add_sub_rep_when_branching/";
	
	int maximalTime = 600;

	String s_path;
	String s_path_without;
	String s_path_old;
	String s_path_without_old;
	String s_path_with_initial;
	
	String bis = "_bis.ser";

	boolean saveInBisFile = false;

	
	double[][][][][] resultsWithSubRep;
	double[][][][][] resultsWithoutSubRep;
	double[][][][][] resultsWithInitialSubRep;	    

	public ExecutionSubRepAddedWhenBranching(Cplex cplex, int nbOfGraphs) {
		super(cplex, 20, 50, 2, 10, 0, nbOfGraphs);
		System.out.println("nm " + nm + " nM " + nM);
		this.nbGraph = nbOfGraphs;
		
		s_path = pre + pre2 + "resultsWithSubRep_5D-n-k-i-sign-param-sur-" + nbOfGraphs + "-graphes_ss_cplex_cut.ser";
		s_path_without = pre + pre2 + "resultsWithoutSubRep_5D-n-k-i-sign-param-sur-" + nbOfGraphs + "-graphes_ss_cplex_cut.ser";
		s_path_with_initial = pre + pre2 + "resultsWithInitialSubRep_5D-n-k-i-sign-param-sur-" + nbOfGraphs + "-graphes.ser";
		
		s_path_old = pre + pre2 + "resultsWithSubRep_5D-n-k-i-sign-param-sur-" + nbOfGraphs + "-graphes.ser";
		s_path_without_old = pre + pre2 + "resultsWithoutSubRep_5D-n-k-i-sign-param-sur-" + nbOfGraphs + "-graphes.ser";
		
		resultsWithSubRep = this.unserialize5DTable(s_path);
		resultsWithoutSubRep = this.unserialize5DTable(s_path_without);
		resultsWithInitialSubRep = this.unserialize5DTable(s_path_with_initial);

		
	}

	@Override
	public void execution() throws IloException {
		

		ComputeResults.log("(n,k,i): (" + c_n + "," + c_k + "," +c_i + ")"); 
//		TildeParam param = new TildeParam(true, true, false);
		
		if(c_n%10 == 0 && c_k%2 == 0){
			
			int id_n = c_n/10 - nm/10;
			
			TildeParam param = new TildeParam(null, cplex, -1, false, Triangle.USE, true, true, true);
			ArrayList<Double> gapValues = new ArrayList<Double>();
			gapValues.add(0.0);
			gapValues.add(-250.0);
			gapValues.add(-500.0);
			
			for(int i = 0 ; i < gapValues.size() ; ++i){
			
				param.gapDiss = gapValues.get(i);
				param.cplexPrimalDual = false;
				param.cplexAutoCuts = false;
				param.tilim = maximalTime;
				
				try {
					
					if(resultsWithoutSubRep[id_n][c_k][c_i][i][0] == -Double.MAX_VALUE
							|| (id_n == 3 && i == 1) || (id_n == 1 && i == 5)
							){
						PartitionWithRepresentative rep = ((PartitionWithRepresentative)createPartition(param));
						rep.getCplex().use(new Branch_EmptyCB());
						resultsWithoutSubRep[id_n][c_k][c_i][i][0] = rep.getCplex().solve();
						resultsWithoutSubRep[id_n][c_k][c_i][i][1] = rep.getCplex().getBestObjValue();
						resultsWithoutSubRep[id_n][c_k][c_i][i][2] = rep.getCplex().getObjValue();
						resultsWithoutSubRep[id_n][c_k][c_i][i][3] = rep.getCplex().getNnodes();
					}
					
					System.out.print(Math.round(resultsWithoutSubRep[id_n][c_k][c_i][i][0]) + " ");
	
	
					if(resultsWithSubRep[id_n][c_k][c_i][i][0] == -Double.MAX_VALUE
							|| (id_n == 3 && i == 1) || (id_n == 1 && i == 5)
							){
						PartitionWithTildes rep = ((PartitionWithTildes)createPartition(param));
		//					rep = ((PartitionWithRepresentative)createPartition(new CplexParam(false, false, false, maximalTime), param));
//						PartitionWithRepresentative.cplex.use(new Branch_DisplayInformations());
//						PartitionWithRepresentative.cplex.use(new CB_AddSubRep_inequalities(rep));
												
						rep.getCplex().use(new SeparationSubRepresentativeSansDoublon(rep, rep.variableGetter()).createDefaultCallback(rep));
						resultsWithSubRep[id_n][c_k][c_i][i][0] = rep.getCplex().solve();
						resultsWithSubRep[id_n][c_k][c_i][i][1] = rep.getCplex().getBestObjValue();
						resultsWithSubRep[id_n][c_k][c_i][i][2] = rep.getCplex().getObjValue();
						resultsWithSubRep[id_n][c_k][c_i][i][3] = rep.getCplex().getNnodes();
						resultsWithSubRep[id_n][c_k][c_i][i][4] = CBAddSubRepInequalities.addedInequalities;
						CBAddSubRepInequalities.addedInequalities = 0;
					}
					
					System.out.print(Math.round(resultsWithSubRep[id_n][c_k][c_i][i][0]) + " ");
//					if(resultsWithInitialSubRep[id_n][c_k][c_i][i][0] == -Double.MAX_VALUE){
//						PartitionWithRepresentative rep = ((PartitionWithRepresentative)createPartition(new CplexParam(false, true, true, maximalTime), param));
//						PartitionWithRepresentative.cplex.use(new Branch_EmptyCB());
//	//					rep = ((PartitionWithRepresentative)createPartition(new CplexParam(false, false, false, maximalTime), param));
//		
//						
//						for(int id = 0 ; id < rep.n() ; id++)
//							for(int j = id+1 ; j < rep.n() ; ++j)
//								rep.addRange(new SubRepresentative_Inequality(rep, id, j).getRange());
//						
//						
//						resultsWithInitialSubRep[id_n][c_k][c_i][i][0] = rep.getCplex().solve();
//						resultsWithInitialSubRep[id_n][c_k][c_i][i][1] = rep.getCplex().getBestObjValue();
//						resultsWithInitialSubRep[id_n][c_k][c_i][i][2] = rep.getCplex().getObjValue();
//						resultsWithInitialSubRep[id_n][c_k][c_i][i][3] = rep.getCplex().getNnodes();
//					}
					
//					System.out.print(Math.round(resultsWithInitialSubRep[id_n][c_k][c_i][i][0]));
					System.out.print("s");
					
					String toLog = "";					
					double gap = /*Math.abs(resultsWithInitialSubRep[id_n][c_k][c_i][i][2] - resultsWithoutSubRep[id_n][c_k][c_i][i][2]) +*/ Math.abs(resultsWithSubRep[id_n][c_k][c_i][i][2] - resultsWithoutSubRep[id_n][c_k][c_i][i][2]) ;
					
					
					if(gap > 1-1E-5)
						toLog += " (gap [" + Math.round(resultsWithoutSubRep[id_n][c_k][c_i][i][1]) + ", " + Math.round(resultsWithoutSubRep[id_n][c_k][c_i][i][2]) + "]"
						+ " [" + Math.round(resultsWithSubRep[id_n][c_k][c_i][i][1]) + ", " + Math.round(resultsWithSubRep[id_n][c_k][c_i][i][2]) + "]"
//						+ " [" + Math.round(resultsWithInitialSubRep[id_n][c_k][c_i][i][1]) + ", " +  Math.round(resultsWithInitialSubRep[id_n][c_k][c_i][i][2]) + "]"
						+ ")";
					
					boolean isNode = resultsWithoutSubRep[id_n][c_k][c_i][i][3]  > 0  /*|| resultsWithInitialSubRep[id_n][c_k][c_i][i][0] > 0 */ || resultsWithSubRep[id_n][c_k][c_i][i][3] > 0;
					
					if(isNode){
						toLog += " (nodes ";
						toLog += Math.round(resultsWithoutSubRep[id_n][c_k][c_i][i][3]) + " ";
						toLog += Math.round(resultsWithSubRep[id_n][c_k][c_i][i][3]) + " ";
//						toLog += Math.round(resultsWithInitialSubRep[id_n][c_k][c_i][i][3]);
						toLog +=")";
					}
					
					if(resultsWithSubRep[id_n][c_k][c_i][i][4] > 0)
						toLog += ": + " + Math.round(resultsWithSubRep[id_n][c_k][c_i][i][4]) + "/" + Math.round(c_n*(c_n-1)/2.0);
					
					ComputeResults.log(toLog);
	
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(saveInBisFile || (c_n == this.nM && c_k == kM && c_i == iM)){
					ComputeResults.serialize(resultsWithSubRep, s_path);
					ComputeResults.serialize(resultsWithoutSubRep, s_path_without);
					ComputeResults.serialize(resultsWithInitialSubRep, s_path_with_initial);
					saveInBisFile = false;
				}
				else{
					ComputeResults.serialize(resultsWithSubRep, s_path + bis);
					ComputeResults.serialize(resultsWithoutSubRep, s_path_without + bis);
					ComputeResults.serialize(resultsWithInitialSubRep, s_path_with_initial + bis);
					saveInBisFile = true;
				}
		}
		}

	}
	
	
	/**
	 * Print 3 tables which represent each data set (D1, D2, D3) (positive, negative and both)
	 * In each table we represent for each formulation the time and the gap of the two configurations (with and without sub rep inequalities)
	 */
	public void printResults(int maximalSolvedInstance){
		
		/* Results to display in the tables. Definition of the dimensions:
		 * - 1 dimension : n
		 * - 2 dimension : K
		 * - 3 dimension : gap
		 * - 4 dimension : result (0: time,  1: gap)
		 * - 5 dimension : configuration (0: with sub-rep, 1: without sub-rep)
		 */
		double[][][][][] resultats = new double[nM/10-nm/10+1][kM/2][3][2][2];
		
		for(int signe = 0 ; signe <= 2 ; ++signe){
			for(int n = 0 ; n <= nM/10-nm/10 ; ++n){
				for(int k = 2 ; k <= 10 ; k += 2){

					if(k % 2 == 0){
						
						int id_k = k/2-1;
	//						System.out.println("\nsigne: "+ signe + " n: " + n + " k: " + k);
						resultats[n][id_k][signe][0][0] = 0.0;
						resultats[n][id_k][signe][0][1] = 0.0;
						resultats[n][id_k][signe][1][0] = 0.0;
						resultats[n][id_k][signe][1][1] = 0.0;
					
						int modif = 0;
					
						for(int i = 0 ; i < maximalSolvedInstance ; ++i){
							
							if( !(n == 3 && i == 1) && !(n == 1 && i == 5)){
								double minInt = Math.min(resultsWithSubRep[n][k][i][signe][2], resultsWithoutSubRep[n][k][i][signe][2]);
								if(minInt == -1.0){
									minInt = Math.min(resultsWithSubRep[n][k][i][signe][2], resultsWithoutSubRep[n][k][i][signe][2]);
								}
								
								if(minInt != -1.0){
	//								System.out.println("!" + resultsWithSubRep[n][k][i][signe][2]  + " " +  resultsWithoutSubRep[n][k][i][signe][2]);
									/* Compute the gaps */
									resultats[n][id_k][signe][1][0] += ComputeResults.improvement(minInt, resultsWithSubRep[n][k][i][signe][1]);
									resultats[n][id_k][signe][1][1] += ComputeResults.improvement(minInt, resultsWithoutSubRep[n][k][i][signe][1]);
									
									/* Get the times */
									resultats[n][id_k][signe][0][0] += resultsWithSubRep[n][k][i][signe][0];
									resultats[n][id_k][signe][0][1] += resultsWithoutSubRep[n][k][i][signe][0];
			
									if(resultsWithSubRep[n][k][i][signe][0] < -100000000)
										System.out.println("\n!1 n,k,signe,i: "  + n + ", " + k + ", " + signe + ", " + i + " relaxation: "+ resultsWithSubRep[n][k][i][signe][1]);
									if(resultsWithoutSubRep[n][k][i][signe][0] < -100000000)
										System.out.println("!1 n,k,signe,i: "  + n + ", " + k + ", " + signe + ", " + i + " relaxation: "+ resultsWithoutSubRep[n][k][i][signe][1]);
								}
								else
									modif++;
							}
							else{
								modif ++ ;
							}
							
						}
						resultats[n][id_k][signe][1][0] /= (maximalSolvedInstance - modif);
						resultats[n][id_k][signe][0][0] /= (maximalSolvedInstance - modif);
						resultats[n][id_k][signe][0][1] /= (maximalSolvedInstance - modif);
						resultats[n][id_k][signe][1][1] /= (maximalSolvedInstance - modif);
						
						System.out.print(ComputeResults.doubleToString(resultats[n][id_k][signe][1][0], 2) + "-" + ComputeResults.doubleToString(resultats[n][id_k][signe][1][1], 2) + "%\t");
						System.out.print(ComputeResults.doubleToString(resultats[n][id_k][signe][0][0], 0) + "-" + ComputeResults.doubleToString(resultats[n][id_k][signe][0][1], 0) + "s");
											
						System.out.print("\t");
					}
					
				}
				
				System.out.println();
			}
			
			System.out.println("\n");
		}
		
	}
	
	public double[][][][][] unserialize5DTable(String path){
		
		double[][][][][] array;
		
		/* If the file does not exist, create an empty array */
		if(!new File(path).exists()){
			array = new double[nM/10-nm/10+1][kM+1][nbGraph+1][3][5];
			
			for(int n = 0 ; n <= nM/10-nm/10 ; n++ )
				for(int k = 0 ; k <= kM ; ++k)
					for(int i = 0 ; i <= nbGraph ; ++i)
						for(int signe = 0 ; signe <= 2 ; ++signe)
							array[n][k][i][signe][0] = -Double.MAX_VALUE;
			System.out.println("\nFile not found: " + path);
		}
		
		/* If the file exist */
		else 
			
			/* Try to unserialize it */
			try{
				array = ComputeResults.unserialize(path, double[][][][][].class);
			}catch(Exception e){
				
				/* If it does not work (probably because the program was interrupted while the file was created)
				 * unserialize the bis file */
				array = ComputeResults.unserialize(path + bis, double[][][][][].class);
			}
		
		return array;
	}


}
