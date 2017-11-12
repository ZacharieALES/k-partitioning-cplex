package main.old;

import java.io.File;
import java.util.ArrayList;

import cplex.Cplex;
import formulation.Partition;
import formulation.PartitionWithTildes;
import formulation.TildeParam;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import main.Execution;
import results.ComputeResults;
import separation.AbstractSeparation;
import separation.SeparationSubRepresentativeExhaustive;

/**
 * Calcul de la relaxation linéaire obtenue avec la formulation tilde lorsque l'ensemble des coupes de type sous-ensembles représentant sont ajoutées
 * @author zach
 *
 */
public class ExecutionRrSousRep extends Execution{

	String pre = "results/";
	String pre2 = "relaxation_by_formulation/";
	String pre3 = "tilde_et_sous_representants/";
	String doneFolder = "done/";

	String s_path = pre + pre2 + pre3 + "subRepRelaxation4D-n-k-i-sign-sur-100-graphes.ser";
	String s_path_tilde = pre + doneFolder + pre2 + "tilde_relaxation_100_graphs_4D_n_k_i_signe.ser";
	String s_optimal_integer_solutions_path = pre + "exact_solution/optimal_resolution_4D_n_k_i_signe-sur_100_instances.ser";
	
	String bis = "_bis.ser";
	


	boolean saveInBisFile = false;

	
	double[][][][] relaxationWithSubRep;
	double[][][][] relaxationWithoutSubRep;

	double[][][][] integerSolutions;
	    

	public ExecutionRrSousRep(Cplex cplex) {
		super(cplex, 7, 20, 2, 10, 0, 99);
		
		relaxationWithSubRep = this.unserialize4DTable(s_path);		
		
		relaxationWithoutSubRep = ComputeResults.unserialize(s_path_tilde, double[][][][].class);
		integerSolutions = ComputeResults.unserialize(s_optimal_integer_solutions_path, double[][][][].class);
	}

	@Override
	public void execution() throws IloException {
		
		TildeParam param = new TildeParam(null, cplex, -1, false, false);
		param.isInt = false;
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
		gapValues.add(-250.0);
		gapValues.add(-500.0);
		
		for(int i = 0 ; i < gapValues.size() ; ++i){
		
			param.gapDiss = gapValues.get(i);
			
//			if(relaxationWithSubRep[c_n][c_k][c_i][i] == -Double.MAX_VALUE){
			
				PartitionWithTildes rep;

				param.cplexPrimalDual = false;
				param.cplexAutoCuts = false;
				
				rep = ((PartitionWithTildes)createPartition(param));
				relaxationWithSubRep[c_n][c_k][c_i][i] = rr_improved(rep, new SeparationSubRepresentativeExhaustive(rep, rep.variableGetter()));
				
				
				if(Math.abs(relaxationWithoutSubRep[c_n][c_k][c_i][i]
						- relaxationWithSubRep[c_n][c_k][c_i][i]) > 1E-4)
				System.out.println(ComputeResults.doubleToString(relaxationWithoutSubRep[c_n][c_k][c_i][i], 2) + " " +
						ComputeResults.doubleToString(relaxationWithSubRep[c_n][c_k][c_i][i], 2) + " " +
						ComputeResults.doubleToString(integerSolutions[c_n][c_k][c_i][i], 2)
						);
				
//				if(relaxationWithoutSubRep[c_n][c_k][c_i][i] > relaxationWithSubRep[c_n][c_k][c_i][i] + 0.001
//					|| relaxationWithSubRep[c_n][c_k][c_i][i] > integerSolutions[c_n][c_k][c_i][i] + 0.001){
//						System.err.println("Error in values");
//						System.exit(0);
//					}

		
//				System.out.println(ComputeResults.improvement(relaxationWithoutSubRep[c_n][c_k][c_i][i], relaxationWithSubRep[c_n][c_k][c_i][i]));
			
//				if(saveInBisFile || (c_n == 20 && c_k == 19 && c_i == 99)){
//					ComputeResults.serialize(relaxationWithSubRep, s_path);
//					saveInBisFile = false;
//				}
//				else{
//					ComputeResults.serialize(relaxationWithSubRep, s_path + bis);
//					saveInBisFile = true;
//				}
//			}
		}

	}
	
	
	public double rr_improved(Partition rep, AbstractSeparation<? extends IFormulation> sep) throws IloException{
		
		rep.getCplex().solve();
		
				ArrayList<AbstractInequality<? extends IFormulation>> al_ineq = sep.separate();
				for(AbstractInequality<? extends IFormulation> ineq : al_ineq)
					rep.getCplex().addRange(ineq.getRange());
				rep.getCplex().solve();		
		return rep.getCplex().getObjValue();
		
	}
	
	/**
	 * Print 3 tables which represent each data set (D1, D2, D3) (positive, negative and both)
	 * In each table we represent:
	 * 	- the gap between the tilde relaxation and the optimal integer solution;
	 *  - the improvement of the gap obtained thanks to the sub representative inequalities
	 */
	public void printResults(){
		
		double[][][] tildeGap = new double[21][20][3];
		double[][][] improvementSubRep = new double[21][20][3];
		
		double maximalSolvedInstance = 92.0;
		    
		for(int signe = 0 ; signe <= 2 ; ++signe){
			for(int n = 7 ; n < 21 ; ++n){
				for(int k = 2 ; k < Math.min(n, 11) ; ++k){

					tildeGap[n][k][signe] = 0.0;
					improvementSubRep[n][k][signe] = 0.0;
				
					for(int i = 0 ; i < maximalSolvedInstance ; ++i){
						
						tildeGap[n][k][signe] += ComputeResults.improvement(integerSolutions[n][k][i][signe], relaxationWithoutSubRep[n][k][i][signe]);
						improvementSubRep[n][k][signe] += ComputeResults.improvement(integerSolutions[n][k][i][signe], this.relaxationWithSubRep[n][k][i][signe]) - ComputeResults.improvement(integerSolutions[n][k][i][signe], relaxationWithoutSubRep[n][k][i][signe]);
						
//						System.out.println("relax without sub rep: " + relaxationWithoutSubRep[n][k][i][signe]);
//						System.out.println("relax with    sub rep: " + relaxationWithSubRep[n][k][i][signe]);
//						System.out.println("integer solution: " + integerSolutions[n][k][i][signe]);
//						System.out.println("gap without rep: " + tildeGap[n][k][signe]);
//						System.out.println("gap with    rep: " + improvementSubRep[n][k][signe]);
//						System.out.println();
						
					}
					
					
					tildeGap[n][k][signe] /= maximalSolvedInstance;
					improvementSubRep[n][k][signe] /= maximalSolvedInstance;
					
					System.out.print(ComputeResults.doubleToString(tildeGap[n][k][signe], 3));
					
					if(Math.abs(improvementSubRep[n][k][signe]) > 0.01)
						System.out.print("(" + ComputeResults.doubleToString(improvementSubRep[n][k][signe], 2) + ")");
					
					System.out.print("\t");
					
				}
				
				System.out.println();
			}
			
			System.out.println("\n");
		}
		
	}
	
	public double[][][][] unserialize4DTable(String path){
		
		double[][][][] array;
		
		/* If the file does not exist, create an empty array */
		if(!new File(path).exists()){
			array = new double[21][20][100][3];
			
			for(int n = 0 ; n <= 20 ; n++ )
				for(int k = 0 ; k <= 19 ; ++k)
					for(int i = 0 ; i <= 99 ; ++i)
						for(int signe = 0 ; signe <= 2 ; ++signe)
							array[n][k][i][signe] = -Double.MAX_VALUE;
		}
		
		/* If the file exist */
		else 
			
			/* Try to unserialize it */
			try{
				array = ComputeResults.unserialize(path, double[][][][].class);
			}catch(Exception e){
				
				/* If it does not work (probably because the program was interrupted while the file was created)
				 * unserialize the bis file */
				array = ComputeResults.unserialize(path + bis, double[][][][].class);
			}
		
		return array;
	}


}
