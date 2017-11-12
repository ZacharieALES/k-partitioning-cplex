package main.old;

import cplex.Cplex;
import cutting_plane.CP_Rep;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import ilog.concert.IloException;

public class ExecutionGrotschellData {

	public void solve(Cplex cplex){
		TildeParam tp = new TildeParam("./data/grotschell/n_158_v1.txt", cplex, 7, true, Triangle.USE_LAZY_IN_BC_ONLY, true, false, false);
		tp.isInt = false;
//		Partition_with_tildes p = new Partition_with_tildes(4, "./data/grotschell/n_34_v1.txt", new CplexParam(false), new TildeParam(false, true, true, false, true, false, false));

		CP_Rep cprep;
		try {
			cprep = new CP_Rep(tp, 500, 300,  -1, 750, true, 3600);
			cprep.solve();
		} catch (IloException e) {
			e.printStackTrace();
		}
		
	}
	
	public void createDissimilarity(){

//		Grotschell_input_converter gr = new Grotschell_input_converter("./data/raw.csv");
//		ArrayList<Integer> al = new ArrayList<Integer>();
//		al.add(1);
//		al.add(2);
//		al.add(3);
//		gr.save("./dissim.txt", al);
	}
}
