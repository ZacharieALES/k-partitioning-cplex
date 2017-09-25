package main;

import cutting_plane.CP_Rep;
import formulation.CplexParam;
import formulation.Partition;
import formulation.Partition_with_tildes;
import formulation.TildeParam;
import formulation.RepParam.Triangle;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import results.ComputeResults;

public class Main {

	static IloCplex cplex;

	static int first_graph = 0;
	static int nm = 7;
	static int nM = 20;
	static int km = 2;
	static int kM = 20;
	static int numberOfGraphs = 80;
	static int last_graph = first_graph + numberOfGraphs - 1;

	static String folder = "../../../Dropbox/";
	static String pre = folder + ComputeResults.getDate() + "_";
	static String suf = "_" + numberOfGraphs + "_graphs";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Partition.start();

//		new Execution_sub_rep_added_when_branching(10).printResults(6);
		
//		/* A executer pour INOC */
//		new Execution_sub_rep_added_when_branching(10).execute();
//		new Execution_inoc_numero_special_tsp().solve();
//		new Execution_inoc_numero_special_new_graphs().printTablesTimeGapNodes();
//		new Execution_inoc_numero_special_tsp().solve(); // avec 1h et heuristiques rapides
//		new Execution_inoc_numero_special_v2_10_min_tmax_cutting_plane(20, 50, 2, 8, 0, 9).execute(); //avec heuristiques rapides

//		new Execution_inoc_numero_special_v2_10_min_tmax(20, 50, 2, 8, 0, 9).printTablesTimeGapNodes(20, 50);
//		new Execution_rr_sous_rep().execute();
//		new Execution_rr_sous_rep().printResults();
		
//		new Execution_exact_resolution().execute();
		
//		boolean useEmptyBC = false;
//		
//		Partition_with_tildes p = new Partition_with_tildes(8, "data/input_root_relaxation_100/n_19_id_0.txt", new CplexParam(true, true, true, -1), new TildeParam(true, true, true, true, true, true, true));
//		
//		try {
//			
//			if(useEmptyBC){
//				PartitionWithRepresentative.cplex.use(new Branch_EmptyCB());
//			}
//			else{
//				PartitionWithRepresentative.cplex.use(new Branch_DisplayInformations());
//				PartitionWithRepresentative.cplex.use(new CB_AddSubRep_inequalities(p));
//			}
//			p.solve();
//			p.displaySolution();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}


//		Partition_with_tildes p = new Partition_with_tildes(7, "./data/input_root_relaxation_100/n_36_id_6.txt", new CplexParam(false), new TildeParam(false, true, Triangle.USE_LAZY_IN_BC_ONLY, true, false, false));
//		CP_Rep cprep = new CP_Rep(p, 500, 300,  -1, 750, true, 3600);
//		cprep.solve();
		
		
//		new Execution_rr_tilde_et_sans().execute();
//		new Execution_rr_sous_rep().printResults();
		
		// new Execution_Create_dissimilarity(41, 50, 0, 4).execute();

		// new Execution(30, 30, 2, 4, 0, 0){
		// public void execution() throws IloException{
		//
		// Partition p = createPartition(0, true, true, false, true, true);
		// p.use(new Lazy_Separation_Triangle("lazy triangle", p, 500).lcc);
		// Result r = new Result(c_i, p, true);
		// }
		// }.execute();

//		int n = 50;
//		Partition_with_tildes p = new Partition_with_tildes(7, "./data/input_root_relaxation_100/n_" + n + "_id_10.txt", new CplexParam(false), new TildeParam(false, true, Triangle.USE_IN_BC_ONLY, true, false, false));
//		CP_Rep cprep = new CP_Rep(p, 500, 300,  -1, 750, true, 3600);
//		cprep.solve();
		
//		 new CP_Execution_Rep(n, n, 2, 2, 2, 2, 500, 100000000).execute();

		// new Execution_test(30, 50, 4, 10, 0, 4).execute();

		// new Execution_test_dual_simplex(30, 30, 4, 4, 4, 4).execute();

		// new Execution_experience_ISCO(30, 50, 2, 8, 0, 0).execute();
		// new Execution_experience_ISCO(30, 50, 4, 10, 5, 19).execute();

		// new Execution_experience_ISCO(50, 50, 4, 4, 4, 4).execute();

		// new Execution_experience_ISCO(0, 0, 0, 0, 0, 0).printResult();

		// new Execution_motif_data().solve();

		// new Execution_evaluation_ratio_KL(20, 30, 2, 10, 0, 4).execute();
		// new Execution_nn_1(20, 30, 2, 10, 0, 4).execute();

		// new Execution_root_relaxation(10, 10, 2, 3, 0, 0).execute();
		// new Execution_is_relaxation_optimal(7, 20, 2, 20, 0, 99).execute();

		 //new Execution_grotschell_data().solve();
		// new Execution_grotschell_data().createDissimilarity();
		 

			Partition_with_tildes p = new Partition_with_tildes(7, "./data/input_root_relaxation/n_20_id_19.txt", new CplexParam(false), new TildeParam(false, true, Triangle.USE_LAZY, true, false, false));
			CP_Rep cprep = new CP_Rep(p, 500, 300,  -1, 750, true, 3600);
			cprep.solve();
		 
//		Partition_with_tildes p = new Partition_with_tildes(K, "./data/these_final/dissimilarity_final.txt", max_nb_node, new CplexParam(false),  new TildeParam(false, true, Triangle.USE_LAZY_IN_BC_ONLY, true, false, false));
		
		// new Execution_rr_tilde_et_sans(7, 7, 2, 3, 0, 0).execute();
//		new Execution_rr_tilde_et_sans().execute();
//		new Execution_rr_tilde_et_sans().printResult();

		// new Execution_exact_resolution(10, 20, 2, 10, 0, 19).execute();
		// new Execution_rr_tilde_et_sans(10, 20, 2, 10, 0, 19).execute();
		
//		new Execution_motif_data().solve();
		
//		new Execution_ecart_relatif(10, 10, 10, 0, 0, 0).display_results();
		
//		new Execution_experience_INOC(30, 30, 2, 8, 0, 0).execute();
		
//		new Execution_motif_data().solve();
		
//		new Execution_experience_ISCO(30, 30, 2, 2, 0, 0).execute();
//		new Execution_root_relaxation(20, 20, 2, 10, 0, 5).execute();

//		double[][][] e = ComputeResults
//				.unserialize("results/exact_solution/d1/3D_exact_obj_graph_0_to_19.ser");
//		double[][][] t = ComputeResults
//				.unserialize("results/relaxation_by_formulation/PouetTilde_positifsgraph_0_to_19.ser");
//		double[][][] sst = ComputeResults
//				.unserialize("results/relaxation_by_formulation/PouetSs_tilde_positifsgraph_0_to_19.ser");
//
//		int[][] mean_st = new int[21][21];
//		int[][] mean_t = new int[21][21];
//		int[][] var_st = new int[21][21];
//		int[][] var_t = new int[21][21];
//		
//		for (int n = 10; n <= 20; ++n) {
//			for (int K = 2; K <= 10; ++K) {
//
//				double mean = 0.0;
//				double mean_sst = 0.0;
//
//				for (int i = 0; i < 20; ++i) {
//
//					mean += ComputeResults.improvement2(e[n][K][i], t[n][K][i]);
//					mean_sst += ComputeResults.improvement2(e[n][K][i],sst[n][K][i]);
//					
//				}
//				mean *= 100/20;
//				mean_sst *= 100/20;
//				
//				double var = 0.0;
//				double var_sst = 0.0;
//				for (int i = 0; i < 20; ++i) {
//					// System.out.println("n,k,i:" + n + "," + K + "," + i);
//					// System.out.println(Math.round(sst[n][K][i]) + " / " +
//					// Math.round(t[n][K][i]) + " / " + Math.round(e[n][K][i]));
//					double v1 = (mean - 100*ComputeResults.improvement2(e[n][K][i],
//							t[n][K][i]));
//					var += v1 * v1;
//
//					v1 = mean_sst
//							- 100 * ComputeResults.improvement2(e[n][K][i],
//									sst[n][K][i]);
//					var_sst += v1 * v1;
//				}
//				var /= 20;
//				var_sst /=  20;
//
//				mean_t[n][K] = (int) Math.round(mean);
//				mean_st[n][K] = (int) Math.round(mean_sst);
//				var_t[n][K] = (int) Math.round(var);
//				var_st[n][K] = (int) Math.round(var_sst);
//
//			}
//		}
//		
//		for(int n = 10 ; n <= 20 ; ++n){
//			String s2 = " & ";
//			for(int K = 2 ; K <= n-1 ; ++K){
//				System.out.print(var_t[n][K] + " " + var_st[n][K] + " & ");
//			}
//			System.out.println();
//		}
		// new
		// Execution_test_add_triangle_ineq_to_x_y(10,20,2,10,0,99).execute();

		// new Execution_test_formulations(10, 10, 2, 3, 0, 4).execute();

		// new Execution_root_relaxation(7, 20, 2, 10, 0, 19).execute();

		// new Execution_ST_KL_Root(20, 20, 4, 4, 1).execute();

		// new Execution_tilde_n_n_1_and_without(20, 20, 5, 20, 0, 4).execute();
		// new Execution_tilde_n_n_1_and_without(20, 20, 5, 20, 0,
		// 4).displayResults();

		Partition.end();

	}

	public static void displayCutUsed(IloCplex cplex) throws IloException {

		if (cplex.getNcuts(IloCplex.CutType.CliqueCover) != 0)
			System.out.println("Nb clique cuts: "
					+ cplex.getNcuts(IloCplex.CutType.CliqueCover));

		if (cplex.getNcuts(IloCplex.CutType.Cover) != 0)
			System.out.println("Nb cover cuts: "
					+ cplex.getNcuts(IloCplex.CutType.Cover));

		if (cplex.getNcuts(IloCplex.CutType.Disj) != 0)
			System.out.println("Nb Disj cuts: "
					+ cplex.getNcuts(IloCplex.CutType.Disj));

		if (cplex.getNcuts(IloCplex.CutType.FlowCover) != 0)
			System.out.println("Nb FlowCover cuts: "
					+ cplex.getNcuts(IloCplex.CutType.FlowCover));

		if (cplex.getNcuts(IloCplex.CutType.FlowPath) != 0)
			System.out.println("Nb FlowPath cuts: "
					+ cplex.getNcuts(IloCplex.CutType.FlowPath));

		if (cplex.getNcuts(IloCplex.CutType.Frac) != 0)
			System.out.println("Nb Frac cuts: "
					+ cplex.getNcuts(IloCplex.CutType.Frac));

		if (cplex.getNcuts(IloCplex.CutType.GUBCover) != 0)
			System.out.println("Nb GUB cuts: "
					+ cplex.getNcuts(IloCplex.CutType.GUBCover));

		if (cplex.getNcuts(IloCplex.CutType.ImplBd) != 0)
			System.out.println("Nb Impl cuts: "
					+ cplex.getNcuts(IloCplex.CutType.ImplBd));

		if (cplex.getNcuts(IloCplex.CutType.LocalCover) != 0)
			System.out.println("Nb LocalCover cuts: "
					+ cplex.getNcuts(IloCplex.CutType.LocalCover));

		if (cplex.getNcuts(IloCplex.CutType.MCF) != 0)
			System.out.println("Nb MCF cuts: "
					+ cplex.getNcuts(IloCplex.CutType.MCF));

		if (cplex.getNcuts(IloCplex.CutType.MIR) != 0)
			System.out.println("Nb MIR cuts: "
					+ cplex.getNcuts(IloCplex.CutType.MIR));

		if (cplex.getNcuts(IloCplex.CutType.ObjDisj) != 0)
			System.out.println("Nb ObjDisj cuts: "
					+ cplex.getNcuts(IloCplex.CutType.ObjDisj));

		if (cplex.getNcuts(IloCplex.CutType.SolnPool) != 0)
			System.out.println("Nb SolnPool cuts: "
					+ cplex.getNcuts(IloCplex.CutType.SolnPool));

		if (cplex.getNcuts(IloCplex.CutType.Table) != 0)
			System.out.println("Nb Table cuts: "
					+ cplex.getNcuts(IloCplex.CutType.Table));

		if (cplex.getNcuts(IloCplex.CutType.Tighten) != 0)
			System.out.println("Nb Tighten cuts: "
					+ cplex.getNcuts(IloCplex.CutType.Tighten));

		if (cplex.getNcuts(IloCplex.CutType.User) != 0)
			System.out.println("Nb User cuts: "
					+ cplex.getNcuts(IloCplex.CutType.User));

		if (cplex.getNcuts(IloCplex.CutType.ZeroHalf) != 0)
			System.out.println("Nb ZeroHalf cuts: "
					+ cplex.getNcuts(IloCplex.CutType.ZeroHalf));

	}

}
