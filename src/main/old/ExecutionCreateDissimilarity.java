package main.old;

import cplex.Cplex;
import generate_input_file.CreateDiss;
import ilog.concert.IloException;
import main.Execution;

public class ExecutionCreateDissimilarity extends Execution{

	public ExecutionCreateDissimilarity(Cplex cplex, int nm, int nM2, int im, int iM2) {
		super(cplex, nm, nM2, 1, 1, im, iM2);
	}

	@Override
	public void execution() throws IloException {
		CreateDiss.createFile("data/input_root_relaxation_100/n_" + c_n + "_id_" + c_i + ".txt", c_n);
		
	}
}
