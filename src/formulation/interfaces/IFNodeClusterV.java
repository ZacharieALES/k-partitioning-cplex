package formulation.interfaces;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;

/**
 * Interface of objects which associate variables to the fact that a node is inside a cluster 
 * 
 * @author zach
 *
 */
public interface IFNodeClusterV extends IFormulation{

	/**
	 * @return Number of nodes in the graph 
	 */
	public int n();
	
	/** 
	 * Get the variable associated to the fact that a node is inside a cluster.
	 * @param i Id of the node.
	 * @param k Id of the cluster.
	 * @return The variable associated to the fact that node i is in cluster k.
	 * @throws IloException
	 */
	public IloNumVar nodeInClusterVar(int i, int k) throws IloException;
	
}
