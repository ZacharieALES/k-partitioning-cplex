package formulation.pcenters;

import cplex.Cplex;
import formulation.Param;

public class PCenterParam extends Param{

	double initialLB = -Double.MAX_VALUE;
	double initialUB = Double.MAX_VALUE;

	boolean useLB0 = true;
	boolean useLB1 = true;
	boolean useUB0 = true;
	boolean useUB1 = true;
	
	boolean computeBoundsSeveralTimes = true;

	/** Remove clients and factories which are dominated (i.e., that will not affect the optimal solution) */
	public boolean filterDominatedClientsAndFactories = true;

	/** True if the y variables are integer (if Param.isInt is false, this parameter is ignored) */
	boolean isYInt = true;

	public PCenterParam(PCenterParam p) {
		super(p);

		useLB0 = p.useLB0;
		useLB1 = p.useLB1;
		useUB0 = p.useUB0;
		useUB1 = p.useUB1;
		this.isYInt = p.isYInt;
		this.initialLB = p.initialLB;
		this.initialUB = p.initialUB;
		filterDominatedClientsAndFactories = p.filterDominatedClientsAndFactories; 
		computeBoundsSeveralTimes = p.computeBoundsSeveralTimes;

	}

	public PCenterParam(String inputFile, Cplex cplex){
		super(inputFile, cplex);
	}

	public void useBounds(boolean boundUsed) {
		useLB0 = boundUsed;
		useLB1 = boundUsed;
		useUB0 = boundUsed;
		useUB1 = boundUsed;
	}

}
