package main;

import java.util.ArrayList;

import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.RepParam;
import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import results.ComputeResults;
import separation.Abstract_Separation;
import separation.Separation_Paw_Inequalities_exhaustive;

public class remote_Execution_root_relaxation extends Execution{

	String pre = "results/relaxation_by_family";
	String suf = "graph_0_to_99";
	
//	double[][][] stRoot = ComputeResults.unserialize(pre + "st"  + suf);
//	double[][][] tccRoot = ComputeResults.unserialize(pre + "tcc"  + suf);
//	double[][][] kp1Root = ComputeResults.unserialize(pre + "kp1"  + suf);

	double[][][][] stRoot;
	double[][][][] tccRoot;
	double[][][][] kp1Root;
	double[][][][] triangleRepRoot;
	double[][][][] rootRelaxation;

	public remote_Execution_root_relaxation(int nm, int nM2, int km, int kM2, int im,
			int iM2) {
		super(nm, nM2, km, kM2, im, iM2);
		stRoot = new double[nM+1][kM+1][iM+1][3];
		tccRoot = new double[nM+1][kM+1][iM+1][3];
		kp1Root = new double[nM+1][kM+1][iM+1][3];
		triangleRepRoot = new double[nM+1][kM+1][iM+1][3];
		rootRelaxation = new double[nM+1][kM+1][iM+1][3];
	}

	@Override
	public void execution() throws IloException {
		
		RepParam param = new RepParam(null, -1, false);
		param.isInt = false;
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
//		gapValues.add(-250.0);
//		gapValues.add(-500.0);
		
		for(int i = 0 ; i < gapValues.size() ; ++i){
		
			param.gapDiss = gapValues.get(i);
			
			rootRelaxation[c_n][c_k][c_i][i] = this.getRootRelaxation(param);
			
			param.cplexAutoCuts = false;
			param.cplexPrimalDual = false;
			
			PartitionWithRepresentative rep;
//			
//			rep = ((PartitionWithRepresentative)createPartition(new CplexParam(true, false, false, -1), param));
//			kp1Root[c_n][c_k][c_i][i] = rr_improved(rep, new Separation_DependentSet_KL(rep, 10, true));
//			
//			rep = ((PartitionWithRepresentative)createPartition(new CplexParam(true, false, false, -1), param));
//			stRoot[c_n][c_k][c_i][i] = rr_improved(rep, new Separation_ST_Grotschell(rep, 5000));
//			
//			rep = ((PartitionWithRepresentative)createPartition(new CplexParam(true, false, false, -1), param));
//			tccRoot[c_n][c_k][c_i][i] = rr_improved(rep, new Separation_TCC_Muller_Improvement(rep));
	
			rep = ((PartitionWithRepresentative)createPartition(param));
			triangleRepRoot[c_n][c_k][c_i][i] = rr_improved(rep, new Separation_Paw_Inequalities_exhaustive(rep));

			System.out.println(ComputeResults.improvement(rootRelaxation[c_n][c_k][c_i][i], triangleRepRoot[c_n][c_k][c_i][i]));
			
//			System.out.println("rr " + Math.round(rootRelaxation[c_n][c_k][c_i][i]) + " - new " + Math.round(triangleRepRoot[c_n][c_k][c_i][i])   + " - tcc " + Math.round(tccRoot[c_n][c_k][c_i][i]) + " - st " + Math.round(stRoot[c_n][c_k][c_i][i]) +    " - dep " + Math.round(kp1Root[c_n][c_k][c_i][i]));
		}


		String prefix = "./results/rr_dam/rr_4d_root_n_7_to_20_i_min" + im + "_i_max" + iM;
//		ComputeResults.serialize(rootRelaxation, prefix);
//		ComputeResults.serialize(stRoot, prefix + "st");
//		ComputeResults.serialize(tccRoot, prefix + "tcc");
//		ComputeResults.serialize(kp1Root, prefix + "dependent");
//		ComputeResults.serialize(triangleRepRoot, prefix + "new2");

	}
	
	
	public double rr_improved(Partition rep, Abstract_Separation sep){
		
		rep.solve();
		
		boolean found = true;
		while(found){
			
			found = false;
			ArrayList<Abstract_Inequality> al_ineq;
			try {
				al_ineq = sep.separate();
				if(al_ineq.size() > 0){
					for(Abstract_Inequality ineq : al_ineq){
						rep.addRange(ineq.getRange());
					}
					rep.solve();			
					found = true;
				}
			} catch (IloException e) {
				e.printStackTrace();
			}
		}		
		
		return rep.getObjValue2();
		
	}
	

	public double rr_improved_exhaustif(Partition rep, Abstract_Separation sep){
		
		rep.solve();
		
		ArrayList<Abstract_Inequality> al_ineq;
		try {
			al_ineq = sep.separate();
			if(al_ineq.size() > 0){
				System.out.println(sep.name + " : " + al_ineq.size());
				for(Abstract_Inequality ineq : al_ineq){
					rep.addRange(ineq.getRange());
				}
				rep.solve();			
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		return rep.getObjValue2();
		
	}
	
	public boolean isInt(double d){
		System.out.println(d);
		return (d-Math.round(d)) < 1E-4;
	}
	
	public void isIntResults(){

		
		String prefix = "results/rr_dam/";

		double[][] results1 = new double[21][11];
		double[][] results2 = new double[21][11];
		double[][] results3 = new double[21][11];
		
		for(int i = 0 ; i < 21 ; ++i)
			for(int j = 0 ; j < 11 ; ++j){
				results1[i][j] = 0.0;
				results2[i][j] = 0.0;
				results3[i][j] = 0.0;
			}

		double[][][][] root_0_to_19;
			root_0_to_19 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min0_i_max19", double[][][][].class);
			double[][][][] root_20_to_100 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min20_i_max100", double[][][][].class);
			double[][][][] root_n_7_to_10 = ComputeResults.unserialize(prefix + "rr_4d_root_n_7_to_10_i_min0_i_max100", double[][][][].class);
	
			for(int n = 7 ; n < 21 ; ++n)
				for(int k = 2 ; k < Math.min(n, 11) ; ++k)
					for(int i = 0 ; i < 100 ; ++i){
						
						if(n <= 10){
							if(isInt(root_n_7_to_10[n][k][i][0]))
								results1[n][k]++;
	
							if(isInt(root_n_7_to_10[n][k][i][1]))
								results2[n][k]++;
	
							if(isInt(root_n_7_to_10[n][k][i][2]))
								results3[n][k]++;
	
						}
						else{
						
							if(i < 20){
	
								if(isInt(root_0_to_19[n][k][i][0]))
									results1[n][k]++;
	
								if(isInt(root_0_to_19[n][k][i][1]))
									results2[n][k]++;
	
								if(isInt(root_0_to_19[n][k][i][2]))
									results3[n][k]++;
								
							}
							else{
	
								if(isInt(root_20_to_100[n][k][i][0]))
									results1[n][k]++;
	
								if(isInt(root_20_to_100[n][k][i][1]))
									results2[n][k]++;
	
								if(isInt(root_20_to_100[n][k][i][2]))
									results3[n][k]++;
								
							}
						}
					}
				
		
				
			        

//	System.out.println("\\documentclass[15pt , a4paper]{article}\n\\usepackage{slashbox}\n\\usepackage[french]{babel}\n\\usepackage [utf8] {inputenc}\n\\usepackage{vmargin}\n\\usepackage{array}\n\\usepackage{booktabs}\n\\setmarginsrb{.5cm}{0cm}{0cm}{.5cm}{.5cm}{.5cm}{.5cm}{.5cm}\\usepackage{multirow}\n\\newcolumntype{M}[1]{>{\\centering}m{#1}}\n\\begin{document}\n");
//
//	String prefix2 = "\\begin{table}\\renewcommand{\\arraystretch}{1.2}";
//	prefix2 += "\\centering \\begin{tabular}{M{0.5cm}*{9}{r@{\\hspace{0.5cm}}}r@{}}\\toprule";
//	prefix2 += "\\multirow{2}{*}{\\textbf{n}} & \\multicolumn{8}{c}{\\textbf{K}} \\\\& \\textbf{2} & \\textbf{3} &\\textbf{4} &\\textbf{5} &\\textbf{6} &\\textbf{7} &\\textbf{8} &\\textbf{9} &\\textbf{10} \\tabularnewline\\hline";
//	
//	String suffix1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
//	String suffix2 = "}\n\\label{";
//	String suffix3 = "}\n\\end{table}\n\n\n";
//	String[][] tab1;
//	
//	tab1 = ComputeResults.meanImprovement(rootRelaxation1, tccRoot1, 1);
//	System.out.println(prefix2);
//	ComputeResults.printDoubleTable(tab1, true);
//	System.out.println(suffix1);
//	System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de $2$-chorded cycles sur 100 graphes complets dont le poids des ar��tes est positif.");
//	System.out.println(suffix2);
//	System.out.println("tab:tcc_graph1");		
//	System.out.println(suffix3);
//	
//	
//	System.out.println("\n\\end{document}");

		for(int n = 7 ; n < 21 ; ++n){
			
			for(int k = 2 ; k < Math.min(10,n) ; ++k){
				System.out.print(results1[n][k] + "\t");
			}
			System.out.println();
		}
		
		for(int n = 7 ; n < 21 ; ++n){
			
			for(int k = 2 ; k < Math.min(10,n) ; ++k){
				System.out.print(results2[n][k] + "\t");
			}
			System.out.println();
		}
		
		for(int n = 7 ; n < 21 ; ++n){
			
			for(int k = 2 ; k < Math.min(10,n) ; ++k){
				System.out.print(results3[n][k] + "\t");
			}
			System.out.println();
		}


	}
	
	public void printResults(){
		
		String prefix = "results/rr_dam/done/";

		double[][][][] root_0_to_19;
		
			root_0_to_19 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min0_i_max19", double[][][][].class);
			double[][][][] root_n_7_to_10 = ComputeResults.unserialize(prefix + "rr_4d_root_n_7_to_10_i_min0_i_max100", double[][][][].class);
			
			double[][][][] dep_0_to_19 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min0_i_max19dependent", double[][][][].class);
			double[][][][] dep_20_to_100 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min20_i_max100dependent", double[][][][].class);
			double[][][][] dep_n_7_to_10 = ComputeResults.unserialize(prefix + "rr_4d_root_n_7_to_10_i_min0_i_max100dependent", double[][][][].class);
			
			double[][][][] new_0_to_19 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min0_i_max19new", double[][][][].class);
			double[][][][] new_20_to_100 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min20_i_max100new", double[][][][].class);
			double[][][][] new_n_7_to_10 = ComputeResults.unserialize(prefix + "rr_4d_root_n_7_to_10_i_min0_i_max100new", double[][][][].class);
			
			double[][][][] st_0_to_19 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min0_i_max19st", double[][][][].class);
			double[][][][] st_20_to_100 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min20_i_max100st", double[][][][].class);
			double[][][][] st_n_7_to_10 = ComputeResults.unserialize(prefix + "rr_4d_root_n_7_to_10_i_min0_i_max100st", double[][][][].class);
			
			double[][][][] tcc_0_to_19 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min0_i_max19tcc", double[][][][].class);
			double[][][][] tcc_20_to_39 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min20_i_max39tcc", double[][][][].class);
			double[][][][] tcc_40_to_59 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min40_i_max59tcc", double[][][][].class);
			double[][][][] tcc_60_to_79 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min60_i_max79tcc", double[][][][].class);
			double[][][][] tcc_n_7_to_10 = ComputeResults.unserialize(prefix + "rr_4d_root_n_7_to_10_i_min0_i_max100tcc", double[][][][].class);
	
			double[][][] stRoot1 = new double[21][11][100];
			double[][][] stRoot2 = new double[21][11][100];
			double[][][] stRoot3 = new double[21][11][100];
			double[][][] kp1Root1 = new double[21][11][100];
			double[][][] kp1Root2 = new double[21][11][100];
			double[][][] kp1Root3 = new double[21][11][100];
			double[][][] triangleRepRoot1 = new double[21][11][100];
			double[][][] triangleRepRoot2 = new double[21][11][100];
			double[][][] triangleRepRoot3 = new double[21][11][100];
			double[][][] rootRelaxation1 = new double[21][11][100];
			double[][][] rootRelaxation2 = new double[21][11][100];
			double[][][] rootRelaxation3 = new double[21][11][100];
			double[][][] tccRoot1 = new double[21][11][80];
			double[][][] tccRoot2 = new double[21][11][80];
			double[][][] tccRoot3 = new double[21][11][80];
			
	
			double[][][][] new_0_to_99 = ComputeResults.unserialize(prefix + "rr_4d_root_n_7_to_20_i_min0_i_max99new2", double[][][][].class);

			double[][][][] root_20_to_100 = ComputeResults.unserialize(prefix + "rr_4d_root_i_min20_i_max100", double[][][][].class);
			        
			for(int n = 7 ; n < 21 ; ++n)
				for(int k = 2 ; k < Math.min(n, 11) ; ++k)
					for(int i = 0 ; i < 100 ; ++i){
						
						if(n <= 10){
							rootRelaxation1[n][k][i] = root_n_7_to_10[n][k][i][0];
							rootRelaxation2[n][k][i] = root_n_7_to_10[n][k][i][1];
							rootRelaxation3[n][k][i] = root_n_7_to_10[n][k][i][2];
	
							kp1Root1[n][k][i] = dep_n_7_to_10[n][k][i][0];
							kp1Root2[n][k][i] = dep_n_7_to_10[n][k][i][1];
							kp1Root3[n][k][i] = dep_n_7_to_10[n][k][i][2];
	
							stRoot1[n][k][i] = st_n_7_to_10[n][k][i][0];
							stRoot2[n][k][i] = st_n_7_to_10[n][k][i][1];
							stRoot3[n][k][i] = st_n_7_to_10[n][k][i][2];
	
							if(i < 80){
							tccRoot1[n][k][i] = tcc_n_7_to_10[n][k][i][0];
							tccRoot2[n][k][i] = tcc_n_7_to_10[n][k][i][1];
							tccRoot3[n][k][i] = tcc_n_7_to_10[n][k][i][2];
							}
							
	//						triangleRepRoot1[n][k][i] = new_n_7_to_10[n][k][i][0];
	//						triangleRepRoot2[n][k][i] = new_n_7_to_10[n][k][i][1];
	//						triangleRepRoot3[n][k][i] = new_n_7_to_10[n][k][i][2];
							
							triangleRepRoot1[n][k][i] = new_0_to_99[n][k][i][0];
							triangleRepRoot2[n][k][i] = new_0_to_99[n][k][i][1];
							triangleRepRoot3[n][k][i] = new_0_to_99[n][k][i][2];
							
						}
						else{
							if(i < 20){
								
								rootRelaxation1[n][k][i] = root_0_to_19[n][k][i][0];
								rootRelaxation2[n][k][i] = root_0_to_19[n][k][i][1];
								rootRelaxation3[n][k][i] = root_0_to_19[n][k][i][2];						
								
								stRoot1[n][k][i] = st_0_to_19[n][k][i][0];
								stRoot2[n][k][i] = st_0_to_19[n][k][i][1];
								stRoot3[n][k][i] = st_0_to_19[n][k][i][2];
		
								tccRoot1[n][k][i] = tcc_0_to_19[n][k][i][0];
								tccRoot2[n][k][i] = tcc_0_to_19[n][k][i][1];
								tccRoot3[n][k][i] = tcc_0_to_19[n][k][i][2];
		
								kp1Root1[n][k][i] = dep_0_to_19[n][k][i][0];
								kp1Root2[n][k][i] = dep_0_to_19[n][k][i][1];
								kp1Root3[n][k][i] = dep_0_to_19[n][k][i][2];
								
								triangleRepRoot1[n][k][i] = new_0_to_99[n][k][i][0];
								triangleRepRoot2[n][k][i] = new_0_to_99[n][k][i][1];
								triangleRepRoot3[n][k][i] = new_0_to_99[n][k][i][2];
								
		
							}
							else{
								
								rootRelaxation1[n][k][i] = root_20_to_100[n][k][i][0];
								rootRelaxation2[n][k][i] = root_20_to_100[n][k][i][1];
								rootRelaxation3[n][k][i] = root_20_to_100[n][k][i][2];						
								
								stRoot1[n][k][i] = st_20_to_100[n][k][i][0];
								stRoot2[n][k][i] = st_20_to_100[n][k][i][1];
								stRoot3[n][k][i] = st_20_to_100[n][k][i][2];
		
								triangleRepRoot1[n][k][i] = new_0_to_99[n][k][i][0];
								triangleRepRoot2[n][k][i] = new_0_to_99[n][k][i][1];
								triangleRepRoot3[n][k][i] = new_0_to_99[n][k][i][2];
		
								kp1Root1[n][k][i] = dep_20_to_100[n][k][i][0];
								kp1Root2[n][k][i] = dep_20_to_100[n][k][i][1];
								kp1Root3[n][k][i] = dep_20_to_100[n][k][i][2];
		
								if(i >= 20 && i <= 39){
									tccRoot1[n][k][i] = tcc_20_to_39[n][k][i][0];
									tccRoot2[n][k][i] = tcc_20_to_39[n][k][i][1];
									tccRoot3[n][k][i] = tcc_20_to_39[n][k][i][2];				
								}
								if(i >= 40 && i <= 59){
									tccRoot1[n][k][i] = tcc_40_to_59[n][k][i][0];
									tccRoot2[n][k][i] = tcc_40_to_59[n][k][i][1];
									tccRoot3[n][k][i] = tcc_40_to_59[n][k][i][2];				
								}
								if(i >= 60 && i <= 79){
									tccRoot1[n][k][i] = tcc_60_to_79[n][k][i][0];
									tccRoot2[n][k][i] = tcc_60_to_79[n][k][i][1];
									tccRoot3[n][k][i] = tcc_60_to_79[n][k][i][2];				
								}
							}
						}
						
						
					}
						
				        

			System.out.println("\\documentclass[15pt , a4paper]{article}\n\\usepackage{slashbox}\n\\usepackage[french]{babel}\n\\usepackage [utf8] {inputenc}\n\\usepackage{vmargin}\n\\usepackage{array}\n\\usepackage{booktabs}\n\\setmarginsrb{.5cm}{0cm}{0cm}{.5cm}{.5cm}{.5cm}{.5cm}{.5cm}\\usepackage{multirow}\n\\newcolumntype{M}[1]{>{\\centering}m{#1}}\n\\begin{document}\n");
	//		String prefix2 = "\\setlength{\\extrarowheight}{1pt}\n";
	//		prefix2 += "\\begin{tabular}{c*{10}{p{0.5cm}@{\\hspace{0.4cm}}}}\\toprule\n";
	//		prefix2 += "\\backslashbox{$n$}{$K$} & 2& 3& 4& 5& 6& 7& 8& 9& 10\\\\\\midrule\n";
	
			String prefix2 = "\\begin{table}\\renewcommand{\\arraystretch}{1.2}";
			prefix2 += "\\centering \\begin{tabular}{M{0.5cm}*{9}{r@{\\hspace{0.5cm}}}r@{}}\\toprule";
			prefix2 += "\\multirow{2}{*}{\\textbf{n}} & \\multicolumn{8}{c}{\\textbf{K}} \\\\& \\textbf{2} & \\textbf{3} &\\textbf{4} &\\textbf{5} &\\textbf{6} &\\textbf{7} &\\textbf{8} &\\textbf{9} &\\textbf{10} \\tabularnewline\\hline";
			
			String suffix1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
			String suffix2 = "}\n\\label{";
			String suffix3 = "}\n\\end{table}\n\n\n";
			String[][] tab1;
			
			tab1 = ComputeResults.meanImprovement(rootRelaxation1, tccRoot1, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab1, true);
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de $2$-chorded cycles sur 100 graphes complets dont le poids des ar��tes est positif.");
			System.out.println(suffix2);
			System.out.println("tab:tcc_graph1");		
			System.out.println(suffix3);
	
			tab1 = ComputeResults.meanImprovement(rootRelaxation1, stRoot1, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab1, true);
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de $2$-partition sur 100 graphes complets dont le poids des ar��tes est positif.");
			System.out.println(suffix2);
			System.out.println("tab:st_graph1");		
			System.out.println(suffix3);
		
			tab1 = ComputeResults.meanImprovement(rootRelaxation1, kp1Root1, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab1, true);
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de clique g��n��ralis��es sur 100 graphes complets dont le poids des ar��tes est positif.");
			System.out.println(suffix2);
			System.out.println("tab:dep_graph1");		
			System.out.println(suffix3);
			
	
			tab1 = ComputeResults.meanImprovement(rootRelaxation1, triangleRepRoot1, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab1, true);
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de triangles repr��sentatifs sur 100 graphes complets dont le poids des ar��tes est positif.");
			System.out.println(suffix2);
			System.out.println("tab:new_graph1");		
			System.out.println(suffix3);
	
			String[][] tab2; 
			
			tab2 = ComputeResults.meanImprovement(rootRelaxation2, tccRoot2, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab2, true);System.out.println("\n");
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de $2$-chorded cycle sur 100 graphes complets dont le poids des ar��tes peut\\^etre positif ou n��gatif.");
			System.out.println(suffix2);
			System.out.println("tab:tcc_graph2");		
			System.out.println(suffix3);
					
			tab2 = ComputeResults.meanImprovement(rootRelaxation2, stRoot2, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab2, true);System.out.println("\n");
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de $2$-partition sur 100 graphes complets dont le poids des ar��tes peut \\^etre positif ou n��gatif.");
			System.out.println(suffix2);
			System.out.println("tab:st_graph2");		
			System.out.println(suffix3);
			
			tab2 = ComputeResults.meanImprovement(rootRelaxation2, kp1Root2, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab2, true);System.out.println("\n");
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de clique g��n��ralis��es sur 100 graphes complets dont le poids des ar��tes peut \\^etre positif ou n��gatif.");
			System.out.println(suffix2);
			System.out.println("tab:dep_graph2");		
			System.out.println(suffix3);
			
			tab2 = ComputeResults.meanImprovement(rootRelaxation2, triangleRepRoot2, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab2, true);System.out.println("\n");
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s triangulaire repr��sentatifs sur 100 graphes complets dont le poids des ar��tes peut \\^etre positif ou n��gatif.");
			System.out.println(suffix2);
			System.out.println("tab:new_graph2");		
			System.out.println(suffix3);
	
			String[][] tab3 = ComputeResults.meanImprovement(rootRelaxation3, kp1Root3, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab3, true);System.out.println("\n");
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de clique g��n��ralis��es sur 100 graphes complets dont le poids des ar��tes est n��gatif.");
			System.out.println(suffix2);
			System.out.println("tab:dep_graph3");		
			System.out.println(suffix3);
			
			tab3 = ComputeResults.meanImprovement(rootRelaxation3, stRoot3, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab3, true);System.out.println("\n");
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de $2$-partition sur 100 graphes complets dont le poids des ar��tes est n��gatif.");
			System.out.println(suffix2);
			System.out.println("tab:st_graph3");		
			System.out.println(suffix3);
		
			tab3 = ComputeResults.meanImprovement(rootRelaxation3, tccRoot3, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab3, true);System.out.println("\n");
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s de $2$-chorded cycle sur 100 graphes complets dont le poids des ar��tes est n��gatif.");
			System.out.println(suffix2);
			System.out.println("tab:tcc_graph3");		
			System.out.println(suffix3);
		
			tab3 = ComputeResults.meanImprovement(rootRelaxation3, triangleRepRoot3, 1);
			System.out.println(prefix2);
			ComputeResults.printDoubleTable(tab3, true);System.out.println("\n");
			System.out.println(suffix1);
			System.out.println("Am��lioration moyenne de la relaxation lin��aire par utilisation d'in��galit��s triangulaire repr��sentatifs sur 100 graphes complets dont le poids des ar��tes est n��gatif.");
			System.out.println(suffix2);
			System.out.println("tab:new_graph3");		
			System.out.println(suffix3);
	
			
			
			
			System.out.println("\n\\end{document}");

	}


}
