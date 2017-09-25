package main;

import ilog.concert.IloException;
import results.ComputeResults;

public class Execution_ecart_relatif extends Execution{

	public Execution_ecart_relatif(int nm, int nM2, int km, int kM2, int im,
			int iM2) {
		super(nm, nM2, km, kM2, im, iM2);
	}

	@Override
	public void execution() throws IloException {
		
		
	}
	
	public void display_results(){
		

		
		String prefix = "results/rr_dam/done/";

		double[][][] optimal_objective_value;
		
		try {
			optimal_objective_value = ComputeResults.unserialize("results/exact_solution/d1/3D_exact_obj_graph_0_to_19.ser", double[][][].class);
			double[][][] relaxation_ss_tildes = ComputeResults.unserialize("results/relaxation_by_formulation/ss_tilde_positifsgraph_0_to_99.ser", double[][][].class);
			double[][][] relaxation_avec_tildes = ComputeResults.unserialize("results/relaxation_by_formulation/tilde_positifsgraph_0_to_99.ser", double[][][].class);
			double[][][] relaxation_xy1 = ComputeResults.unserialize("results/relaxation_by_formulation/xy_v1_positifsgraph_0_to_99.ser", double[][][].class);
			double[][][] relaxation_xy2 = ComputeResults.unserialize("results/relaxation_by_formulation/xy_v2_positifsgraph_0_to_99.ser", double[][][].class);
			
			
			for(int i = 15 ; i < 21 ; ++i){
				String s1 = "";
				String s2 = "\\textbf{"  + i + "}";
				String s3 = "";
				
				for(int  j = 2 ; j <= 10 ; ++j){
					String mean_imp_ss_tildes = ComputeResults.meanImprovement(optimal_objective_value[i][j], relaxation_ss_tildes[i][j], 0);
					String mean_imp_tildes = ComputeResults.meanImprovement(optimal_objective_value[i][j], relaxation_avec_tildes[i][j], 0);
					String mean_imp_xy1= ComputeResults.meanImprovement(optimal_objective_value[i][j], relaxation_xy1[i][j], 0);
					
					s1 += " & " + mean_imp_ss_tildes;
					s2 += " & " + mean_imp_tildes;  
					s3 += " & " + mean_imp_xy1; 
				}
				
				System.out.println(s1 + "& $(F_1)$ \\\\");
				System.out.println(s2 + "& $(F_2)$ \\\\");
				System.out.println(s3 + "& $(F_{cr}$ \\\\");
				
				if(i != 20)
					System.out.println("\\hline");
				
			}
			
		}catch(Exception e){e.printStackTrace();}
		
//		
//		System.out.println("int: " + optimal_objective_value[15][2][0]);
//		System.out.println("ss tildes: " + relaxation_ss_tildes[15][2][0]);
//		System.out.println("tildes: " + relaxation_avec_tildes[15][2][0]);
//		System.out.println("xy1: " + relaxation_xy1[15][2][0]);
//		System.out.println("xy2: " + relaxation_xy2[15][2][0]);
		
		
	}

}
