package formulation.interfaces;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;

/**
 * Interface of objects which associate variables to the nodes of a graph
 * and another set of variables to a second set of nodes
 * 
 * @author zach
 *
 */
public interface IFNodeVNodeBV extends IFNodeV{

	/**
	 * @return Number of nodes of the second type
	 */
	public int K();
	
	/** 
	 * Get the variable associated to a node of the second set.
	 * @param i Id of the node.
	 * @return The variable associated to node i
	 * @throws IloException
	 */
	public IloNumVar nodeBVar(int i) throws IloException;
	
}
