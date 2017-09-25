package formulation;

public class Param {
	
	/**
	 * Value added to all the weight of each edge in the graph (enable to quickly change the value of all the edges of a graph without generating a new input file)
	 */
	public double gapDiss = 0.0;


	/** Display cplex output */
	public boolean cplexOutput = false;
	
	/** Use cplex primal dual heuristic */
	public boolean cplexPrimalDual = true;
	
	/** Use cplex automatic cuts */
	public boolean cplexAutoCuts = true;
	
	/** Cplex time limit */
	public double tilim = -1;
	
	public boolean isInt = true;
	public boolean useNN_1 = false;
	
	/** Input file which contains the weight value of the considered graph */
	public String inputFile = null;
	
	/** Maximal number of nodes read into the input file */
	public int maxNumberOfNodes = -1;
	
	/** Number of clusters */
	public int K = -1;
	
	public Param(String inputFile, int K){
		this.inputFile = inputFile;
		this.K = K;
	}
	
	public Param(Param p){
		gapDiss = p.gapDiss;
		cplexOutput = p.cplexOutput;
		cplexPrimalDual = p.cplexPrimalDual;
		cplexAutoCuts = p.cplexAutoCuts;
		tilim = p.tilim;
		isInt = p.isInt;
		useNN_1 = p.useNN_1;
		inputFile = p.inputFile;
		maxNumberOfNodes = p.maxNumberOfNodes;
		K = p.K;
	}
}
