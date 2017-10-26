package formulation.interfaces;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;

/**
 * Interface of objects which associate variables to the nodes of a graph.
 * 
 * @author zach
 *
 */
public interface IFNodeV extends IFormulation{

	/**
	 * @return Number of nodes in the graph 
	 */
	public int n();
	
	/** 
	 * Get the variable associated to a node.
	 * @param i Id of the node.
	 * @return The variable associated to node i
	 * @throws IloException
	 */
	public IloNumVar nodeVar(int i) throws IloException;
	
}
