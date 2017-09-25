package main;

import java.io.File;
import java.util.ArrayList;

import cutting_plane.CP_Rep;
import formulation.CplexParam;
import formulation.PartitionWithRepresentative;
import formulation.TildeParam;
import formulation.RepParam.Triangle;
import ilog.concert.IloException;
import results.ComputeResults;

public class Execution_exact_resolution extends Execution{


	double[][][][] objective_value_exact_solution;
	
	public Execution_exact_resolution() {
		super(30, 30, 2, 19, 0, 99);
		
//		if(!new File(s_path).exists()){
//
//			objective_value_exact_solution = new double[21][20][100][3];
//			for(int n = 0 ; n <= 20 ; n++ )
//				for(int k = 0 ; k <= 19 ; ++k)
//					for(int i = 0 ; i <= 99 ; ++i)
//						for(int signe = 0 ; signe <= 2 ; ++signe)
//							objective_value_exact_solution[n][k][i][signe] = -Double.MAX_VALUE;
//		}
//		else
//			try{
//				objective_value_exact_solution = ComputeResults.unserialize4D(s_path);
//			}catch(Exception e){
//				objective_value_exact_solution = ComputeResults.unserialize4D(s_path_bis);
//			}
	}

	String s_path = "results/exact_solution/optimal_resolution_4D_n_k_i_signe-sur_100_instances.ser";
	
	/* Second save file which will be used one over two times (the save file may be corrupted when stopping the programm while it is serializing) */
	String s_path_bis = "results/exact_solution/optimal_resolution_4D_n_k_i_signe-sur_100_instances_bis.ser";
	
	static boolean saveInBisFile = false;
	
	@Override
	public void execution() throws IloException {
		

//		if(objective_value_exact_solution[c_n][c_k][c_i][0] == -Double.MAX_VALUE)
//			ComputeResults.log("(" + c_n + "," + c_k + "," + c_i + ")") ;
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
//		gapValues.add(0.0);
//		gapValues.add(-250.0);
		gapValues.add(-500.0);
		
		for(int i = 0 ; i < gapValues.size() ; ++i){

//			if(objective_value_exact_solution[c_n][c_k][c_i][i] == -Double.MAX_VALUE)
			{
				TildeParam ti = new TildeParam(false, true, Triangle.USE_LAZY_IN_BC_ONLY, true, false, false);
				ti.gapDiss = gapValues.get(i);
				/* Cutting Plane */
				PartitionWithRepresentative rep = ((PartitionWithRepresentative)createPartition(new CplexParam(false, true, true, -1), ti));
				CP_Rep cprep = new CP_Rep(rep, 500, c_i, 10, 10, true, 3600);		
				cprep.solve();
				
//				objective_value_exact_solution[c_n][c_k][c_i][i] = cprep.cpresult.bestInt;
				
//				if(saveInBisFile){
//					ComputeResults.serialize(objective_value_exact_solution, s_path_bis);
//					saveInBisFile = false;
//				}
//				else{
//					ComputeResults.serialize(objective_value_exact_solution, s_path);
//					saveInBisFile = true;
//				}
				
				System.out.println(objective_value_exact_solution[c_n][c_k][c_i]);
				ComputeResults.log(objective_value_exact_solution[c_n][c_k][c_i][i]+ "");
			}
		}
		
	}

}
