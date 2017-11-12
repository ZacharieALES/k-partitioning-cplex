package main.old;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import cplex.Cplex;
import formulation.PartitionWithRepresentative;
import formulation.PartitionXY;
import formulation.PartitionXY2;
import formulation.RepParam;
import formulation.TildeParam;
import formulation.XYParam;
import ilog.concert.IloException;
import main.Execution;
import results.ComputeResults;

public class ExecutionRrTildeEtSans extends Execution{

	public ExecutionRrTildeEtSans(Cplex cplex) {
		super(cplex, 15, 20, 2, 6, 0, 99);
//		super(15, 20, 2, 10, 0, 99);

//		res_tildes = this.unserialize4DTable(tildePath);
//		res_ss_tildes = this.unserialize4DTable(ssTildePath);
//		res_x_y_v1 = this.unserialize4DTable(xy1Path);
//		res_x_y_v2 = this.unserialize4DTable(xy2Path);
		res_tildes = new double[nM+1][kM+1][iM+1][3];
		res_ss_tildes = new double[nM+1][kM+1][iM+1][3];
		res_x_y_v1 = new double[nM+1][kM+1][iM+1][3];
		res_x_y_v2 = new double[nM+1][kM+1][iM+1][3];
		
	}

	double[][][][] res_tildes;
	double[][][][] res_ss_tildes;
	double[][][][] res_x_y_v1;
	double[][][][] res_x_y_v2;
	
	String pre = "results/relaxation_by_formulation/";
	String bis = "_bis.ser";
	String tildePath = pre + "tilde_relaxation_100_graphs_4D_n_k_i_signe" + ".ser";
	String ssTildePath = pre + "ss_tilde_relaxation_100_graphs_4D_n_k_i_signe" + ".ser";
	String xy1Path = pre + "xy1_relaxation_100_graphs_4D_n_k_i_signe" + ".ser";
	String xy2Path = pre + "xy2_relaxation_100_graphs_4D_n_k_i_signe" + ".ser";
	
	static boolean saveInBisFile = false;

	@Override
	public void execution() throws IloException {
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
//		gapValues.add(0.0);
//		gapValues.add(-250.0);
		gapValues.add(-500.0);
		
		TildeParam tp = new TildeParam(null, cplex, -1, false, false);
		RepParam rp = new RepParam(null, cplex, -1, false);
		XYParam xyp = new XYParam(null, cplex, -1, true);
		
		tp.isInt = false;
		rp.isInt = false;
		xyp.isInt = false;
		
		for(int i = 0 ; i < gapValues.size() ; ++i){
		
			tp.gapDiss = gapValues.get(i);
			rp.gapDiss = gapValues.get(i);
			xyp.gapDiss = gapValues.get(i);


//			if(res_tildes[c_n][c_k][c_i][i] == -Double.MAX_VALUE)
			{
				PartitionWithRepresentative rep = ((PartitionWithRepresentative)createPartition(tp));
				rep.getCplex().solve();
				res_tildes[c_n][c_k][c_i][i] = rep.getCplex().getObjValue();
//				System.out.println(res_tildes[c_n][c_k][c_i][i]);
			}
			
//			if(res_ss_tildes[c_n][c_k][c_i][i] == -Double.MAX_VALUE)
			{			
				PartitionWithRepresentative rep = ((PartitionWithRepresentative)createPartition(rp));
				rep.getCplex().solve();
				res_ss_tildes[c_n][c_k][c_i][i] = rep.getCplex().getObjValue();
			}

			xyp.isSecondXYFormulation= false;

//			if(res_x_y_v1[c_n][c_k][c_i][i] == -Double.MAX_VALUE)
			{
				PartitionXY rep_x_y = ((PartitionXY)createPartition(xyp));
				rep_x_y.getCplex().solve();
				res_x_y_v1[c_n][c_k][c_i][i] = rep_x_y.getCplex().getObjValue();
//				rep_x_y.displayNonNegativeCoefficientSolution();
				
//				System.out.println(res_x_y_v1[c_n][c_k][c_i][i]);
//				System.out.println("-----");
			}
			

			xyp.isSecondXYFormulation= true;

//			if(res_x_y_v2[c_n][c_k][c_i][i] == -Double.MAX_VALUE)
			{
				PartitionXY2 rep_x_y2 = ((PartitionXY2)createPartition(xyp));
				rep_x_y2.getCplex().solve();
				res_x_y_v2[c_n][c_k][c_i][i] = rep_x_y2.getCplex().getObjValue();

//				rep_x_y2.displayNonNegativeCoefficientSolution();
//				System.out.println(res_x_y_v2[c_n][c_k][c_i][i]);
//				System.out.println("-----");
			}

			System.out.println(Math.round(res_x_y_v1[c_n][c_k][c_i][i]) + " : " + Math.round(res_x_y_v2[c_n][c_k][c_i][i]) + " : " + Math.round(res_tildes[c_n][c_k][c_i][i]) + " : " + Math.round(res_ss_tildes[c_n][c_k][c_i][i]));
		}

//		if(saveInBisFile  || (c_n == 20 && c_k == 19 && c_i == 99)){
//			ComputeResults.serialize(res_tildes, tildePath);
//			ComputeResults.serialize(res_ss_tildes, ssTildePath);
//			ComputeResults.serialize(res_x_y_v1, xy1Path);
//			ComputeResults.serialize(res_x_y_v2, xy2Path);
//
//			saveInBisFile = false;
//		}
//		else {
//			ComputeResults.serialize(res_tildes, tildePath + bis);
//			ComputeResults.serialize(res_ss_tildes, ssTildePath + bis);
//			ComputeResults.serialize(res_x_y_v1, xy1Path + bis);
//			ComputeResults.serialize(res_x_y_v2, xy2Path + bis);
//			saveInBisFile = true;
//			
//		}
		
	}
	
	public void printResult(){

		/* results[n][k][0] : mean value for the first formulation x/y 
		 * results[n][k][1] : mean value for the second formulation x/y 
		 * results[n][k][2] : mean value for the formulation with representative
		 * results[n][k][3] : mean value for the formulation with tildes
		 * */
		double[][][] resultsDisplayed = new double[nM+1][kM+1][4];
		
		double[][][][] exact_solution  = unserialize4DTable("./results/exact_solution/optimal_resolution_4D_n_k_i_signe-sur_100_instances.ser");

		String content = "\\documentclass[15pt , a4paper]{article}\n\n\\usepackage[french]{babel}\n\\usepackage [utf8] {inputenc}\n\\usepackage{vmargin}\n\\usepackage{array}\n\\usepackage{booktabs}\n\\setmarginsrb{.5cm}{0cm}{0cm}{.5cm}{.5cm}{.5cm}{.5cm}{.5cm}\\usepackage{multirow}\n\\newcolumntype{M}[1]{>{\\centering}m{#1}}\n\\begin{document}";

		for(int set = 0 ; set < 3 ; ++set){
			for(int n = nm ; n <= nM ; ++n){
	//			System.out.print(n + " \t");
				for(int k = km ; k <= kM ; ++k){
					
					resultsDisplayed[n][k][0] = 0.0;
					resultsDisplayed[n][k][1] = 0.0;
					resultsDisplayed[n][k][2] = 0.0;
					resultsDisplayed[n][k][3] = 0.0;
					
					for(int i = im ; i <= iM; ++i){

//						resultsDisplayed[n][k][2] += ComputeResults.improvement(exact_solution[n][k][i][set], res_ss_tildes[n][k][i][set]);
//						resultsDisplayed[n][k][3] += ComputeResults.improvement(exact_solution[n][k][i][set], res_tildes[n][k][i][set]);
//						resultsDisplayed[n][k][1] += ComputeResults.improvement(exact_solution[n][k][i][set], res_x_y_v2[n][k][i][set]);
//						resultsDisplayed[n][k][0] += ComputeResults.improvement(exact_solution[n][k][i][set], res_x_y_v1[n][k][i][set]);
//						
						resultsDisplayed[n][k][2] += ComputeResults.improvement(res_ss_tildes[n][k][i][set], exact_solution[n][k][i][set]);
						resultsDisplayed[n][k][3] += ComputeResults.improvement(res_tildes[n][k][i][set], exact_solution[n][k][i][set]);
						resultsDisplayed[n][k][1] += ComputeResults.improvement(res_x_y_v2[n][k][i][set], exact_solution[n][k][i][set]);
						resultsDisplayed[n][k][0] += ComputeResults.improvement(res_x_y_v1[n][k][i][set], exact_solution[n][k][i][set]);
					}
					
	//				System.out.println(resultsDisplayed[n][k][2] + " " +
	//					resultsDisplayed[n][k][3] + " "+
	//					resultsDisplayed[n][k][1] + " " + 
	//					resultsDisplayed[n][k][0] );
					resultsDisplayed[n][k][0] /= (iM - im + 1.0);
					resultsDisplayed[n][k][1] /= (iM - im + 1.0);
					resultsDisplayed[n][k][2] /= (iM - im + 1.0);
					resultsDisplayed[n][k][3] /= (iM - im + 1.0);

					resultsDisplayed[n][k][0] *= 100;
					resultsDisplayed[n][k][1] *= 100;
					resultsDisplayed[n][k][2] *= 100;
					resultsDisplayed[n][k][3] *= 100;
					
	//				System.out.print("&\t" + Math.round(resultsDisplayed[n][k][0]) + ":" + Math.round(resultsDisplayed[n][k][1]) + ":" + Math.round(resultsDisplayed[n][k][2]) + ":" + Math.round(resultsDisplayed[n][k][3]) + "\t");
						
				}
				
			}
			
			content += displayTable(resultsDisplayed, "Graphes de $D_" + (set+1) + "$");
		}
		
		content += "\\end{document}\n";
		
		String save_file = "./tables_relaxation.tex";
		
		File f = new File(save_file);
		
		if(f.exists())
			f.delete();

		ComputeResults.writeInFile(save_file, content, false);
		
		System.out.println("done");
		
	}
	
	/**
	 * Display a table with K in columns and n in lines
	 * @param table
	 * @param caption
	 * @return
	 */
	public String displayTable(double[][][] table, String caption){
		
		int nbOfFormulations = 4;
		
		List<String> formulations = new ArrayList<String>();

		formulations.add("$(F_{nc1})$");
		formulations.add("$(F_{nc2})$");
		formulations.add("$(F_{er})$");
		formulations.add("$(F_{ext})$");
		

//		\multirow{4}{*}{\textbf{15}} 	& $(F_{nc1})$	
//		&

		String pre_table = "\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{"
				+ "M{0.5cm}"
				+ "c@{\\hspace{0.5cm}}"
				+ "*{10}{r@{\\hspace{0.5cm}}}"
				+ "r@{}}"
				+ "\\toprule\\multirow{2}{*}{\\textbf{n}} & \\multirow{2}{*}{\\textbf{Formulation}} & \\multicolumn{8}{c}{\\textbf{K}} \\\\"
				+ "& & \\textbf{2} & \\textbf{3} &\\textbf{4} &\\textbf{5} &\\textbf{6} &\\textbf{7} &\\textbf{8} &\\textbf{9} &\\textbf{10} \\tabularnewline\\hline";
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}";

	     NumberFormat formatter = new DecimalFormat();

		String result = pre_table + "\n";
		for(int n = nm ; n <= nM ; ++n){

			result +="\\multirow{" + formulations.size() + "}{*}{\\textbf{" + n + "}} \t";
			
			for(int i = 0 ; i < formulations.size() ; ++i){

				result += "&\t" + formulations.get(i);
				
				for(int k = km ; k <= kM ; ++k){
					
					Integer v =  (Integer) Math.round((float)table[n][k][i]);
					String s = v.toString();
					
					if(v > 1E4){

					     formatter = new DecimalFormat("0.##E0");
					     s = formatter.format(v);
					}
						
					result += "&\t" + s;
				}
				result += "\\\\\n ";
				
			}

			if(n == nM)
				result += "\n";
			else
				result += "\\hline\n";
		}
		
		result  += suf_table1;
		result += caption;
		result += suf_table2 + "\n";
		
		return result;
		
	}
	
//	public void unserialize(){
//		res_tildes = ComputeResults.unserialize("ISCO_res_tildes");
//		res_ss_tildes = ComputeResults.unserialize("ISCO_res_ss_tildes");
//	}

//}


//
//
//public Execution_rr_tilde_et_sans(int nm, int nM2, int km, int kM2, int im,
//		int iM2) {
//	super(nm, nM2, km, kM2, im, iM2);
//	
//	res_tildes1 = new double[nM+1][kM+1][iM+1];
//	res_ss_tildes1 = new double[nM+1][kM+1][iM+1];
//	res_x_y_v1_1 = new double[nM+1][kM+1][iM+1];
//	res_x_y_v2_1 = new double[nM+1][kM+1][iM+1];
//
//	res_tildes2 = new double[nM+1][kM+1][iM+1];
//	res_ss_tildes2 = new double[nM+1][kM+1][iM+1];
//	res_x_y_v1_2 = new double[nM+1][kM+1][iM+1];
//	res_x_y_v2_2 = new double[nM+1][kM+1][iM+1];
//
//	res_tildes3 = new double[nM+1][kM+1][iM+1];
//	res_ss_tildes3 = new double[nM+1][kM+1][iM+1];
//	res_x_y_v1_3 = new double[nM+1][kM+1][iM+1];
//	res_x_y_v2_3 = new double[nM+1][kM+1][iM+1];
//	
//	 suf = "graph_" + im + "_to_" + iM;
//}
//
//double[][][] res_tildes1;
//double[][][] res_ss_tildes1;
//double[][][] res_x_y_v1_1;
//double[][][] res_x_y_v2_1;
//
//double[][][] res_tildes2;
//double[][][] res_ss_tildes2;
//double[][][] res_x_y_v1_2;
//double[][][] res_x_y_v2_2;
//
//double[][][] res_tildes3;
//double[][][] res_ss_tildes3;
//double[][][] res_x_y_v1_3;
//double[][][] res_x_y_v2_3;
//
//String pre = "results/relaxation_by_formulation/";
//String suf;
//
//@Override
//public void execution() throws IloException {
//	
//	ArrayList<Double> gapValues = new ArrayList<Double>();
//	gapValues.add(0.0);
////	gapValues.add(-250.0);
////	gapValues.add(-500.0);
//	
//	for(int i = 0 ; i < gapValues.size() ; ++i){
//	
//		TildeParam tp = new TildeParam(false, true, false);
//		tp.gapDiss = gapValues.get(i);
//		
//		RepParam rp = new RepParam(false, true, false);
//		rp.gapDiss = gapValues.get(i);
//		
////		XYParam xyp = new XYParam(false, true);
////		xyp.gapDiss = gapValues.get(i);
//
//		PartitionWithRepresentative rep = ((PartitionWithRepresentative)createPartition(new CplexParam(false), tp));
//		rep.getCplex().solve();
//		switch(i){
//			case 0: res_tildes1[c_n][c_k][c_i] = rep.getCplex().getObjValue();break;
//			case 1: res_tildes2[c_n][c_k][c_i] = rep.getCplex().getObjValue();break;
//			case 2: res_tildes3[c_n][c_k][c_i] = rep.getCplex().getObjValue();break;
//		}
//		
//		
//		double v0 = rep.getCplex().getObjValue();
//		
//		rep = ((PartitionWithRepresentative)createPartition(new CplexParam(false), rp));
//		rep.getCplex().solve();
//		switch(i){
//			case 0: res_ss_tildes1[c_n][c_k][c_i] = rep.getCplex().getObjValue();break;
//			case 1: res_ss_tildes2[c_n][c_k][c_i] = rep.getCplex().getObjValue();break;
//			case 2: res_ss_tildes3[c_n][c_k][c_i] = rep.getCplex().getObjValue();break;
//		}	
//
//////		double v15 = rep.getCplex().getObjValue();
//////
////		xyp.isSecondXYFormulation= false;
////		Partition_x_y rep_x_y = ((Partition_x_y)createPartition(new CplexParam(false), xyp));
////		rep_x_y.solve();
////		switch(i){
////			case 0: res_x_y_v1_1[c_n][c_k][c_i] = rep_x_y.getObjValue2();break;
////			case 1: res_x_y_v1_2[c_n][c_k][c_i] = rep_x_y.getObjValue2();break;
////			case 2: res_x_y_v1_3[c_n][c_k][c_i] = rep_x_y.getObjValue2();break;
////		}	
////
////		double v1 = rep_x_y.getObjValue2();
////
////		rep_x_y.displayEdgeVariables(3);
////		rep_x_y.displayNodeClusterVariables(3);
////		
////		xyp.isSecondXYFormulation= true;
////		Partition_x_y_2 rep_x_y2 = ((Partition_x_y_2)createPartition(new CplexParam(false), xyp));
////		rep_x_y2.solve();
////		switch(i){
////			case 0: res_x_y_v2_1[c_n][c_k][c_i] = rep_x_y2.getObjValue2();break;
////			case 1: res_x_y_v2_2[c_n][c_k][c_i] = rep_x_y2.getObjValue2();break;
////			case 2: res_x_y_v2_3[c_n][c_k][c_i] = rep_x_y2.getObjValue2();break;
////		}	
////		rep_x_y2.displayEdgeVariables(3);
////		rep_x_y2.displayNodeClusterVariables(3);
////
////		System.out.println(Math.round(v1) + " : " + Math.round(rep_x_y2.getObjValue2()));// + " : " + Math.round(v15) + " : " + Math.round(v0));
//	}
//
//	ComputeResults.serialize(res_tildes1, pre + "PouetTilde_positifs" + suf + ".ser");
////	ComputeResults.serialize(res_tildes2, pre + "tilde_plus_et_moins" + suf + ".ser");
////	ComputeResults.serialize(res_tildes3, pre + "tilde_negatifs" + suf + ".ser");
//
//	ComputeResults.serialize(res_ss_tildes1, pre + "PouetSs_tilde_positifs" + suf + ".ser");
////	ComputeResults.serialize(res_ss_tildes2, pre + "ss_tilde_plus_et_moins" + suf + ".ser");
////	ComputeResults.serialize(res_ss_tildes3, pre + "ss_tilde_negatifs" + suf + ".ser");
////
////	ComputeResults.serialize(res_x_y_v1_1, pre + "xy_v1_positifs" + suf + ".ser");
////	ComputeResults.serialize(res_x_y_v1_2, pre + "xy_v1_plus_et_moins" + suf + ".ser");
////	ComputeResults.serialize(res_x_y_v1_3, pre + "xy_v1_negatifs" + suf + ".ser");
////
////	ComputeResults.serialize(res_x_y_v2_1, pre + "xy_v2_positifs" + suf + ".ser");
////	ComputeResults.serialize(res_x_y_v2_2, pre + "xy_v2_plus_et_moins" + suf + ".ser");
////	ComputeResults.serialize(res_x_y_v2_3, pre + "xy_v2_negatifs" + suf + ".ser");		
//
//	//		ComputeResults.serialize(res_tildes, "ISCO_res_tildes");
////	ComputeResults.serialize(res_ss_tildes, "ISCO_res_ss_tildes");
//	
////	if(c_k == kM && c_n == nM)
////		printResult();
//}
//
//public void printResult(){
//	
//
//	res_ss_tildes1 = ComputeResults.unserialize(pre + "ss_tilde_positifs" + suf + ".ser");
//	res_ss_tildes2 = ComputeResults.unserialize(pre + "ss_tilde_plus_et_moins" + suf + ".ser");
//	res_ss_tildes3 = ComputeResults.unserialize(pre + "ss_tilde_negatifs" + suf + ".ser");
//
////	res_tildes1 = ComputeResults.unserialize(pre + "tilde_positifs" + suf + ".ser");
////	res_tildes2 = ComputeResults.unserialize(pre + "tilde_plus_et_moins" + suf + ".ser");
////	res_tildes3 = ComputeResults.unserialize(pre + "tilde_negatifs" + suf + ".ser");
//
//	res_x_y_v1_1 = ComputeResults.unserialize(pre + "xy_v1_positifs" + suf + ".ser");
//	res_x_y_v1_2 = ComputeResults.unserialize(pre + "xy_v1_plus_et_moins" + suf + ".ser");
//	res_x_y_v1_3 = ComputeResults.unserialize(pre + "xy_v1_negatifs" + suf + ".ser");
//
//	res_x_y_v2_1 = ComputeResults.unserialize(pre + "xy_v2_positifs" + suf + ".ser");
//	res_x_y_v2_2 = ComputeResults.unserialize(pre + "xy_v2_plus_et_moins" + suf + ".ser");
//	res_x_y_v2_3 = ComputeResults.unserialize(pre + "xy_v2_negatifs" + suf + ".ser");
//	
//
//	/* results[n][k][0] : mean value for the first formulation x/y 
//	 * results[n][k][1] : mean value for the second formulation x/y 
//	 * results[n][k][2] : mean value for the formulation with representative
//	 * results[n][k][3] : mean value for the formulation with tildes
//	 * */
//	double[][][] resultsDisplayed = new double[nM+1][kM+1][4];
//
//	System.out.println("\\documentclass[15pt , a4paper]{article}\n\\usepackage{slashbox}\n\\usepackage[french]{babel}\n\\usepackage [utf8] {inputenc}\n\\usepackage{vmargin}\n\\usepackage{array}\n\\usepackage{booktabs}\n\\setmarginsrb{.5cm}{0cm}{0cm}{.5cm}{.5cm}{.5cm}{.5cm}{.5cm}\\usepackage{multirow}\n\\newcolumntype{M}[1]{>{\\centering}m{#1}}\n\\begin{document}");
//
//	for(int n = nm ; n <= nM ; ++n){
////		System.out.print(n + " \t");
//		for(int k = km ; k <= kM ; ++k){
//			
//			resultsDisplayed[n][k][0] = 0.0;
//			resultsDisplayed[n][k][1] = 0.0;
//			resultsDisplayed[n][k][2] = 0.0;
//			resultsDisplayed[n][k][3] = 0.0;
//			
//			for(int i = im ; i <= iM; ++i){
//				
//				resultsDisplayed[n][k][2] += res_ss_tildes1[n][k][i];
//				resultsDisplayed[n][k][3] += res_tildes1[n][k][i];
//				resultsDisplayed[n][k][1] += res_x_y_v2_1[n][k][i];
//				resultsDisplayed[n][k][0] += res_x_y_v1_1[n][k][i];
//				
//			}
//			
//			resultsDisplayed[n][k][0] /= (iM - im + 1);
//			resultsDisplayed[n][k][1] /= (iM - im + 1);
//			resultsDisplayed[n][k][2] /= (iM - im + 1);
//			resultsDisplayed[n][k][3] /= (iM - im + 1);
//			
////			System.out.print("&\t" + Math.round(resultsDisplayed[n][k][0]) + ":" + Math.round(resultsDisplayed[n][k][1]) + ":" + Math.round(resultsDisplayed[n][k][2]) + ":" + Math.round(resultsDisplayed[n][k][3]) + "\t");
//				
//		}
//		
//	}
//	
//	displayTable(resultsDisplayed, "Graphes de $D_1$");
//	
//	for(int n = nm ; n <= nM ; ++n){
//
//		for(int k = km ; k <= kM ; ++k){
//			
//			resultsDisplayed[n][k][0] = 0.0;
//			resultsDisplayed[n][k][1] = 0.0;
//			resultsDisplayed[n][k][2] = 0.0;
//			resultsDisplayed[n][k][3] = 0.0;
//			
//			for(int i = im ; i <= iM; ++i){
//				
//				resultsDisplayed[n][k][2] += res_ss_tildes2[n][k][i];
//				resultsDisplayed[n][k][3] += res_tildes2[n][k][i];
//				resultsDisplayed[n][k][1] += res_x_y_v2_2[n][k][i];
//				resultsDisplayed[n][k][0] += res_x_y_v1_2[n][k][i];
//				
//			}
//			
//			resultsDisplayed[n][k][0] /= (iM - im + 1);
//			resultsDisplayed[n][k][1] /= (iM - im + 1);
//			resultsDisplayed[n][k][2] /= (iM - im + 1);
//			resultsDisplayed[n][k][3] /= (iM - im + 1);
//			
////			System.out.print("&\t" + Math.round(resultsDisplayed[n][k][0]) + ":" + Math.round(resultsDisplayed[n][k][1]) + ":" + Math.round(resultsDisplayed[n][k][2]) + ":" + Math.round(resultsDisplayed[n][k][3]) + "\t");
//				
//		}
//		
//	}
//	
//	displayTable(resultsDisplayed, "Graphes de $D_2$");
//	
//	for(int n = nm ; n <= nM ; ++n){
//
//		for(int k = km ; k <= kM ; ++k){
//			
//			resultsDisplayed[n][k][0] = 0.0;
//			resultsDisplayed[n][k][1] = 0.0;
//			resultsDisplayed[n][k][2] = 0.0;
//			resultsDisplayed[n][k][3] = 0.0;
//			
//			for(int i = im ; i <= iM; ++i){
//				
//				resultsDisplayed[n][k][2] += res_ss_tildes3[n][k][i];
//				resultsDisplayed[n][k][3] += res_tildes3[n][k][i];
//				resultsDisplayed[n][k][1] += res_x_y_v2_3[n][k][i];
//				resultsDisplayed[n][k][0] += res_x_y_v1_3[n][k][i];
//				
//			}
//			
//			resultsDisplayed[n][k][0] /= (iM - im + 1);
//			resultsDisplayed[n][k][1] /= (iM - im + 1);
//			resultsDisplayed[n][k][2] /= (iM - im + 1);
//			resultsDisplayed[n][k][3] /= (iM - im + 1);
//			
////			System.out.print("&\t" + Math.round(resultsDisplayed[n][k][0]) + ":" + Math.round(resultsDisplayed[n][k][1]) + ":" + Math.round(resultsDisplayed[n][k][2]) + ":" + Math.round(resultsDisplayed[n][k][3]) + "\t");
//				
//		}
//		
//	}
//	
//	displayTable(resultsDisplayed, "Graphes de $D_3$");
//
//	System.out.println("\\end{document}");
//	
//	
//	
//}
//
//public void displayTable(double[][][] table, String caption){
//
//	String pre_table = "\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{M{0.5cm}*{19}{r@{\\hspace{0.5cm}}}r@{}}\\toprule\\multirow{2}{*}{\\textbf{n}} & \\multicolumn{8}{c}{\\textbf{K}} \\\\& \\textbf{2} & \\textbf{3} &\\textbf{4} &\\textbf{5} &\\textbf{6} &\\textbf{7} &\\textbf{8} &\\textbf{9} &\\textbf{10} \\tabularnewline\\hline";
//	String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
//	String suf_table2 = "}\\end{table}";
//
//	System.out.println(pre_table);
//	for(int n = nm ; n <= nM ; ++n){
//
//		System.out.print("\\multirow{2}{*}{\\textbf{" + n + "}} \t");
//		for(int k = km ; k <= kM ; ++k)
//			System.out.print("&\t" + Math.round(table[n][k][0]));
//		System.out.print("\\\\\n ");
////		for(int k = km ; k <= kM ; ++k)
////			System.out.print("&\t" + Math.round(table[n][k][1]));
////		System.out.print("\\\\\n ");
//		for(int k = km ; k <= kM ; ++k)
//			System.out.print("&\t" + Math.round(table[n][k][2]));
//		System.out.print("\\\\");
//		if(n == nM)
//			System.out.println();
//		else
//			System.out.println("\\hline");
//	}
//	
//	System.out.print(suf_table1);
//	System.out.print(caption);
//	System.out.println(suf_table2);
//	
//}
//
////public void unserialize(){
////	res_tildes = ComputeResults.unserialize("ISCO_res_tildes");
////	res_ss_tildes = ComputeResults.unserialize("ISCO_res_ss_tildes");
////}








	public double[][][][] unserialize4DTable(String path){
	
		double[][][][] array;
		
		/* If the file does not exist, create an empty array */
		if(!new File(path).exists()){
			
			System.out.println("Unable to find file \"" + path + "\". An empty array is generated");
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
				System.out.println("Unserialize file: " + path);
				array = ComputeResults.unserialize(path, double[][][][].class);
			}catch(Exception e){
				System.out.println("Unserialize bis file: " + path);
				/* If it does not work (probably because the program was interrupted while the file was created)
				 * unserialize the bis file */
				array = ComputeResults.unserialize(path + bis, double[][][][].class);
			}
		
		return array;
	}

}






