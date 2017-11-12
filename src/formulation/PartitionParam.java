package formulation;

import cplex.Cplex;

public class PartitionParam extends Param{
	
	public boolean useNN_1 = false;
	
	/** Maximal number of nodes read into the input file */
	public int maxNumberOfNodes = -1;
	
	/** Maximal number of clusters */
	public int KMax = -1;
	
	/** Maximal number of clusters */
	public int KMin = -1;
	
	public PartitionParam(String inputFile, Cplex cplex, int KMin, int KMax){
		super(inputFile, cplex);
		this.inputFile = inputFile;
		this.KMax = KMin;
		this.KMin = KMax;
		this.cplex = cplex;
	}
	
	public PartitionParam(String inputFile, Cplex cplex, int K){
		this(inputFile, cplex, K, K);
	}

	public PartitionParam(PartitionParam p){
		super(p);
		
		useNN_1 = p.useNN_1;
		maxNumberOfNodes = p.maxNumberOfNodes;
		KMax = p.KMax;
		KMin = p.KMin;
	}
}
