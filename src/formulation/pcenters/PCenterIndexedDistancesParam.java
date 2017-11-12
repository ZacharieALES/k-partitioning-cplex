package formulation.pcenters;

import cplex.Cplex;
import formulation.Param;

public class PCenterIndexedDistancesParam extends PCenterParam{

	/**
	 * Characterize the value returned by cplex:
	 * - RADIUS: the radius
	 * - RADIUS_INDEX: the index of the radius in the list of ordered distances dij
	 * - RADIUS_DECREASING_INDEX: sum_k (K-k-1) zk 
	 * @author zach
	 *
	 */
	public enum PCenterReturnType{RADIUS, RADIUS_INDEX, RADIUS_DECREASING_INDEX}
	
	public PCenterReturnType returnType = PCenterReturnType.RADIUS;
	
	public PCenterIndexedDistancesParam(Param p) {
		super(p);
	}

	public PCenterIndexedDistancesParam(String inputFile, Cplex cplex){
		super(inputFile, cplex);
	}

}
