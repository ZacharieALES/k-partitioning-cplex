package formulation;

import cplex.Cplex;

public class TildeParam extends RepParam{

	public boolean useLinear = true;
	
	public TildeParam(String inputFile, Cplex cplex, int K){
		super(inputFile, cplex, K);
		this.useLower = true;
		this.useUpper = false;
	}
	
	public TildeParam(String inputFile, Cplex cplex, int K, boolean useUpper, boolean useNN_1){
		super(inputFile, cplex, K, useNN_1);
		this.useUpper = useUpper;
		this.useLower = true;
	}
	
	public TildeParam(String inputFile, Cplex cplex, int K, boolean useNN_1, Triangle triangle, boolean useLower, boolean useUpper, boolean useLinear){
		super(inputFile, cplex, K, triangle, useNN_1, useLower, useUpper);
		this.useLinear = useLinear;
	}
	
	public TildeParam(TildeParam tp){
		super(tp);
		
		useLinear = tp.useLinear;
	}
}
