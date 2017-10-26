package main;

import cplex.Cplex;
import cutting_plane.CP_Rep;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import ilog.concert.IloException;

public class ExecutionMotifData {

	public void solve(){
		
		
		int max_nb_node = 400;
		int K = 20;
		
		Cplex cplex = new Cplex();
		
		TildeParam tp = new TildeParam("./data/these_final/dissimilarity_final.txt", cplex, K, true, Triangle.USE_LAZY_IN_BC_ONLY, true, false, false);
		tp.maxNumberOfNodes = max_nb_node; 
//		Partition_with_tildes p = new Partition_with_tildes(K, "./data/these_final/input_371_patterns.txt", max_nb_node, new CplexParam(false), new TildeParam(false, true, true, false, true, false, false));

		try {
			CP_Rep cprep = new CP_Rep(tp, 500, -1, 10, 10, true, 360000);
			cprep.solve();
//			cplex.solve();
		} catch (IloException e) {
			e.printStackTrace();
		}
		
	}

}
