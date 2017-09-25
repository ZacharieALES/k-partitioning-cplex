package main;

import generate_input_file.CreateDiss;
import ilog.concert.IloException;

public class Execution_Create_dissimilarity extends Execution{

	public Execution_Create_dissimilarity(int nm, int nM2, int im, int iM2) {
		super(nm, nM2, 1, 1, im, iM2);
	}

	@Override
	public void execution() throws IloException {
		CreateDiss.createFile("data/input_root_relaxation_100/n_" + c_n + "_id_" + c_i + ".txt", c_n);
		
	}
}
