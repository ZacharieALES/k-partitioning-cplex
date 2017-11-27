package callback.branch_callback;
import formulation.PartitionWithRepresentative;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex.BranchCallback;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Branch first on the representative variables and then let cplex do the branching
 * @author zach
 *
 */
public class BranchCBRepFirstAscending extends BranchCallback{

	PartitionWithRepresentative rep;

	private static final Logger logger = Logger.getLogger("");

	public BranchCBRepFirstAscending(PartitionWithRepresentative rep){
		this.rep = rep;
	}


	protected void main() throws IloException {


		logger.setLevel(Level.ALL);
		logger.log(Level.FINER, "---------");
		logger.log(Level.FINER, "Node ID: " + this.getNodeId().toString());


		/* If the node contains a NodeInfo (i.e. if the node corresponds to a representative variable which can be set to one) */
		if(this.getNodeData() instanceof NodeInfo){

			NodeInfo ni = (NodeInfo)this.getNodeData();
			logger.log(Level.FINER, "NbRepTo1: " + ni.nbRepTo1);
			logger.log(Level.FINER, "CurrentNode: " + ni.currentNode);			
			logger.log(Level.FINER, "Last Node to 1 : " + ni.lastNodeTo1);


			/* Branch on the next node representative variable */
			int nextNode = ni.currentNode + 1;

			IloLinearNumExpr expr = rep.getCplex().linearNumExpr();
			//System.out.println("Current node : " + ni.currentNode);
			//System.out.println("Nb Rep to 1  : " + ni.nbRepTo1);
			//System.out.println("K : " + rep.K);
			//System.out.println("Last node to 1 : " + ni.lastNodeTo1 +"\n--------");

			expr.addTerm(1.0, rep.v_rep[nextNode-4]);

			logger.log(Level.FINER, "Branching on x" + nextNode);

			/* Branch on x_nextNode = 1 
			 * (always possible since whenever the number of representative is equal to K, the next nodes doesn't contain any NodeInfo) */
			logger.log(Level.FINER, "Branching on x" + nextNode + " = 1");

			IloRange[] c_range = null;

			/* If nextNode is the last representative :
			 * - x_i_nextNode = 0 (i < nextNode)
			 * - x_nextNode = 1
			 * - x_i = 0 (i > nextNode) 
			 * -> rep.n constraints */
			if(ni.nbRepTo1 == rep.KMax()-1){

				c_range = new IloRange[rep.n];

				/* x_i_nextNode = 0 */
				for(int i = 1 ; i < nextNode ; ++i){
					IloLinearNumExpr expr_i_nn = rep.getCplex().linearNumExpr();
					expr_i_nn.addTerm(1.0,rep.v_edge[nextNode-1][i-1]);
					c_range[i-1] = rep.getCplex().range(0.0, expr_i_nn, 0.0);
				}

				/* x_nextNode = 1 */
				c_range[nextNode-1] = rep.getCplex().range(1.0, expr, 1.0);

				/* x_i = 0 */
				for(int i = nextNode + 1 ; i <= rep.n ; ++i){
					IloLinearNumExpr expr_i = rep.getCplex().linearNumExpr();
					expr_i.addTerm(1.0,rep.v_rep[i - 4]);
					c_range[i-1] = rep.getCplex().range(0.0, expr_i, 0.0);		
				}

				/* Don't pass any object to the node (important in order not to apply this algorithm on the child) */
				this.makeBranch(c_range, this.getObjValue());

			}

			/* If nextNode is not the last representative :
			 * - x_i_nextNode = 0 (i < nextNode)
			 * - x_nextNode = 1
			 * -> nextNode constraints */
			else{

				c_range = new IloRange[nextNode];

				/* x_i_nextNode = 0 */
				for(int i = 1 ; i < nextNode ; ++i){
					IloLinearNumExpr expr_i_nn = rep.getCplex().linearNumExpr();
					expr_i_nn.addTerm(1.0,rep.v_edge[nextNode-1][i-1]);
					c_range[i-1] = rep.getCplex().range(0.0, expr_i_nn, 0.0);
				}

				/* x_nextNode = 1 */
				c_range[nextNode-1] = rep.getCplex().range(1.0, expr, 1.0);

				NodeInfo new_ni1 = new NodeInfo(nextNode, ni.nbRepTo1 + 1, true);

				this.makeBranch(c_range, this.getObjValue(), (Object)new_ni1);
			}
			/* END : Branch on x_nextNode = 1 */

			/* If setting x_nextNode to 0 is possible (i.e. : if it will still be possible to have K clusters) 
			 * rep.n - nextNode : number of remaining representative after nextNode
			 * rep.K - ni.nbRepTo1 : number of remaining representative that have to be set to 1 */
			if(rep.n - nextNode >= rep.KMax() - ni.nbRepTo1){

				/* Branch on x_nextNode = 0 */
				logger.log(Level.FINER, "Branching on x" + nextNode + " = 0");
				NodeInfo new_ni = new NodeInfo(nextNode, ni.nbRepTo1, false);

				this.makeBranch(rep.getCplex().range(0.0, expr, 0.0), this.getObjValue(), (Object)new_ni);

			}

		} // END : If the node correspond to a representative

		else{

			/* If we are at the root */
			if(this.getNodeId().toString().compareTo("Node0") == 0){

				/* Branch on x2 <=> 1-x12 */

				System.out.println("BranchCBRepFirst.java: need to adapt");
				logger.log(Level.FINER, "Branching on x2");

//				/* Branch on x12 = 0, 
//				 * in that case there is two representative among the two first node since 1 is always a representative */
//				NodeInfo new_ni = new NodeInfo(2, 2, true);
//				IloLinearNumExpr expr = rep.getCplex().linearNumExpr();
//
//				expr.addTerm(1.0, rep.v_edge[1][0]);
//				if(rep.KMax() > 2){
//
//					this.makeBranch(rep.getCplex().range(0.0, expr, 0.0), this.getObjValue(), (Object)new_ni);
//				}
//
//				/* If K = 2 set all the next representative variables to 0 */
//				else{
//					IloRange[] c_range = new IloRange[rep.n-1];
//
//					c_range[0] = x2Eq1();
//					c_range[1] = x3Eq0();
//
//					for(int i = 4 ; i <= rep.n ; ++i){
//
//						IloLinearNumExpr expr2 = rep.getCplex().linearNumExpr();
//						expr2.addTerm(1.0,rep.v_rep[i-4]);
//						c_range[i-2] = rep.getCplex().range(0.0, expr2, 0.0);
//					}
//
//					this.makeBranch(c_range, this.getObjValue());
//				}
//
//				/* Branch on x12 = 1 
//				 * in that case there is one representative among the two first node since 1 is always a representative */
//				NodeInfo new_ni1 = new NodeInfo(2, 1, false);
//				this.makeBranch(rep.getCplex().range(1.0, expr, 1.0), this.getObjValue(), (Object)new_ni1);	

			} /* END : if(this.getNodeId().toString().compareTo("Node0") == 0){ */

		} // END : if(this.getNodeData() instanceof NodeInfo){ (if the node does not correspond to a representative)

	} // END : main

	public class NodeInfo{

		boolean lastNodeTo1 = false;

		/**
		 * Number of representative which have been set to 1
		 */
		int nbRepTo1 = 0;

		/**
		 * Representative which corresponds to the current node. It is equal to :
		 * 	- "-1" if the node does not correspond to a representative (i.e. all the representative variables have already been set to 0)
		 */
		public int currentNode;

		public NodeInfo(int currentRep, int nbRepTo1, boolean lastNodeTo1){

			this.currentNode = currentRep;
			this.nbRepTo1 = nbRepTo1;
			this.lastNodeTo1 = lastNodeTo1;

		}

	}
}
