package formulation.pcenters;

import cplex.Cplex;
import formulation.Param;

public class PCenterParam extends Param{
	
	public PCenterParam(Param p) {
		super(p);
	}

	public PCenterParam(String inputFile, Cplex cplex){
		super(inputFile, cplex);
	}

}
