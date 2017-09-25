package main;

import cutting_plane.CP_Rep;
import formulation.CplexParam;
import formulation.Partition_with_tildes;
import formulation.RepParam.Triangle;
import formulation.TildeParam;

public class Execution_grotschell_data {

	public void solve(){

		Partition_with_tildes p = new Partition_with_tildes(7, "./data/grotschell/n_158_v1.txt", new CplexParam(false), new TildeParam(false, true, Triangle.USE_LAZY_IN_BC_ONLY, true, false, false));

//		Partition_with_tildes p = new Partition_with_tildes(4, "./data/grotschell/n_34_v1.txt", new CplexParam(false), new TildeParam(false, true, true, false, true, false, false));

		CP_Rep cprep = new CP_Rep(p, 500, 300,  -1, 750, true, 3600);
		cprep.solve();
		
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
