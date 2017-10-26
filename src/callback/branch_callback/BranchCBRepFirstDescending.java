package callback.branch_callback;
import formulation.PartitionWithRepresentative;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.BranchCallback;

/**
 * Branch first on the representative variables and then let cplex do the branching
 * @author zach
 *
 */
public class BranchCBRepFirstDescending extends BranchCallback{
	
	PartitionWithRepresentative rep;
	
	public BranchCBRepFirstDescending(PartitionWithRepresentative rep){
		this.rep = rep;
	}

	protected void main() throws IloException {
	
//		
//		logger.setLevel(Level.INFO);
//		logger.log(Level.FINER, "---------");
//		logger.log(Level.FINER, "Node ID: " + this.getNodeId().toString());
//			
//			
//		/* If the node contains a NodeInfo (i.e. if the node corresponds to a representative variable which can be set to one) */
//		if(this.getNodeData() instanceof NodeInfo){
//			
//			NodeInfo ni = (NodeInfo)this.getNodeData();
//			logger.log(Level.FINER, "RepTo1: " + ni.nbRepTo1);
//			logger.log(Level.FINER, "CurrentNode: " + ni.currentNode);			
//
//			/* If the current node is 4 */
//			if(ni.currentNode == 4){
//				
//				logger.log(Level.FINER, "Branching on x3");
//				
//				/* Branch on x2 = 0 (-> x3 = 1) 
//				 * We have : x13 = x23 = 0*/
//				IloRange[] c_range = new IloRange[3];
//
//				IloLinearNumExpr expr12 = rep.cplex.linearNumExpr();
//				expr12.addTerm(1.0, rep.v_edge[1][0]);
//				c_range[0] = rep.cplex.eq(1.0, expr12);
//
//				IloLinearNumExpr expr13 = rep.cplex.linearNumExpr();
//				expr13.addTerm(1.0, rep.v_edge[2][0]);
//				c_range[1] = rep.cplex.eq(0.0, expr13);
//
//				IloLinearNumExpr expr23 = rep.cplex.linearNumExpr();
//				expr23.addTerm(1.0, rep.v_edge[2][1]);
//				c_range[2] = rep.cplex.eq(0.0, expr23);
//				
//				this.makeBranch(c_range, this.getObjValue());
//				
////				IloNumVar[] vars = new IloNumVar[3];
////				double[] bounds = new double[3];
////				BranchDirection[] dirs = new BranchDirection[3];
////				
////				vars[0] = rep.v_edge[1][0];
////				bounds[0] = 1.0;
////				dirs[0] = BranchDirection.Up;
////				
////				vars[1] = rep.v_edge[2][0];
////				bounds[1] = 0.0;
////				dirs[1] = BranchDirection.Down;
////				
////				vars[2] = rep.v_edge[2][1];
////				bounds[2] = 0.0;
////				dirs[2] = BranchDirection.Down;
////				
////				this.makeBranch(vars, bounds, dirs, this.getObjValue());
//
//				/* Branch on x2 = 1 (-> x3 = 0) */
//				this.makeBranch(rep.cplex.eq(expr12, 0.0), this.getObjValue());
////				this.makeBranch(rep.v_edge[1][0], 0.0, BranchDirection.Down, this.getObjValue());
//				
//
//				
//			} // END : if the current node is lower than or equal to 4 
//			
//			/* If the current node is greater than four */
//			else{
//				
//				/* Branch on the next node representative variable */
//				int nextNode = ni.currentNode - 1;
//
//				IloLinearNumExpr expr = rep.cplex.linearNumExpr();
//				expr.addTerm(1.0, rep.v_rep[nextNode-4]);
//				
//				logger.log(Level.FINER, "Branching on x" + nextNode);
//				
//				/* Branch on x_nextNode = 1 
//				 * (always possible since whenever the number of representative is equal to K, the next nodes doesn't contain any NodeInfo) */
//				logger.log(Level.FINER, "Branching on x" + nextNode + " = 1");
//				
//				IloRange[] c_range = null;
//				
//				/* If nextNode is the last (lowest) representative (except 1 which is always a representative) :
//				 * - x_i_nextNode = 0 (1 <= i < nextNode) -> nextNode - 1 constraints 
//				 * - x_nextNode = 1 -> 1 constraints
//				 * - x_i = 0 ( 1 < i < nextNode, i != 3)  -> nextNode - 4 (we don't set x2 since x12 will be set, we don't set x3 since x3 is determined by x12 and the other xi which will all be set)
//				 * - x_i_j = 1 (1 <= i < j <= nextNode - 1) -> (nextNode - 1) * (nextNode - 2) / 2 constraints 
//				 */
//				if(ni.nbRepTo1 == rep.K-1){
//			
//					c_range = new IloRange[2*nextNode - 4 + (nextNode - 1) * (nextNode - 2) / 2];
////					IloNumVar[] vars = new IloNumVar[2*nextNode - 4 + (nextNode - 1) * (nextNode - 2) / 2];
////					double[] bounds = new double[2*nextNode - 4 + (nextNode - 1) * (nextNode - 2) / 2];
////					BranchDirection[] dirs = new BranchDirection[2*nextNode - 4 + (nextNode - 1) * (nextNode - 2) / 2];
//
//					/* x_i_nextNode = 0 */
//					for(int i = 1 ; i < nextNode ; ++i){
////						vars[i-1] = rep.v_edge[nextNode-1][i-1];
////						bounds[i-1] = 0.0;
////						dirs[i-1] = BranchDirection.Down;
//						IloLinearNumExpr expr_i_nn = rep.cplex.linearNumExpr();
//						expr_i_nn.addTerm(1.0,rep.v_edge[nextNode-1][i-1]);
//						c_range[i-1] = rep.cplex.eq(0.0, expr_i_nn);
//					}
//					
//					/* x_nextNode = 1 */
////					vars[nextNode-1] = rep.v_rep[nextNode-4];
////					bounds[nextNode-1] = 1.0;
////					dirs[nextNode-1] = BranchDirection.Up;
//					c_range[nextNode-1] = rep.cplex.eq(1.0, expr);
//					
//					int v = 2 * nextNode - 4;
//
//					/* x12 = x13 = x23 = 0 */
////					vars[v] = rep.v_edge[1][0];
////					bounds[v] = 1.0;
////					dirs[v] = BranchDirection.Up;
//					IloLinearNumExpr expr_12 = rep.cplex.linearNumExpr();
//					expr_12.addTerm(1.0,rep.v_edge[1][0]);
//					c_range[v] = rep.cplex.eq(1.0, expr_12);
//					v++;
//
////					vars[v] = rep.v_edge[2][0];
////					bounds[v] = 1.0;
////					dirs[v] = BranchDirection.Up;
//					IloLinearNumExpr expr_13 = rep.cplex.linearNumExpr();
//					expr_13.addTerm(1.0,rep.v_edge[2][0]);
//					c_range[v] = rep.cplex.eq(1.0, expr_13);
//					v++;
//
////					vars[v] = rep.v_edge[2][1];
////					bounds[v] = 1.0;
////					dirs[v] = BranchDirection.Up;
//					IloLinearNumExpr expr_23 = rep.cplex.linearNumExpr();
//					expr_23.addTerm(1.0,rep.v_edge[2][1]);
//					c_range[v] = rep.cplex.eq(1.0, expr_23);
//					v++;
//					
//					/* x_i = 0 (4 <= i < nextNode) */
//					for(int i = 4 ; i < nextNode ; ++i){
////						vars[nextNode + i - 4] = rep.v_rep[i - 4];
////						bounds[nextNode + i - 4] = 0.0;
////						dirs[nextNode + i - 4] = BranchDirection.Down;
//						IloLinearNumExpr expr_i = rep.cplex.linearNumExpr();
//						expr_i.addTerm(1.0,rep.v_rep[i - 4]);
//						c_range[nextNode + i - 4] = rep.cplex.eq(0.0, expr_i);		
//						
//						for(int j = 1 ; j < i ; ++j){
//
////							vars[v] = rep.v_edge[i-1][j-1];
////							bounds[v] = 1.0;
////							dirs[v] = BranchDirection.Up;
//							IloLinearNumExpr expr_i_j = rep.cplex.linearNumExpr();
//							expr_i_j.addTerm(1.0,rep.v_edge[i-1][j-1]);
//							c_range[v] = rep.cplex.eq(1.0, expr_i_j);
//							
//							v++;
//							
//						}
//					}
//
//					/* Don't pass any object to the node (important in order not to apply this algorithm on the child) */
//					this.makeBranch(c_range, this.getObjValue());
////					this.makeBranch(vars, bounds, dirs, this.getObjValue());
//				}
//				
//				/* If nextNode is not the last representative :
//				 * - x_i_nextNode = 0 (1 <= i < nextNode) -> nextNode - 1 constraints 
//				 * - x_nextNode = 1 -> 1 constraint
//				 */
//				else{
//
//					
////					IloNumVar[] vars = new IloNumVar[nextNode];
////					double[] bounds = new double[nextNode];
////					BranchDirection[] dirs = new BranchDirection[nextNode];
//					c_range = new IloRange[nextNode];
//					
//
//					/* x_i_nextNode = 0 */
//					for(int i = 1 ; i < nextNode ; ++i){
////						vars[i-1] = rep.v_edge[nextNode-1][i-1];
////						bounds[i-1] = 0.0;
////						dirs[i-1] = BranchDirection.Down;
//						IloLinearNumExpr expr_i_nn = rep.cplex.linearNumExpr();
//						expr_i_nn.addTerm(1.0,rep.v_edge[nextNode-1][i-1]);
//						c_range[i-1] = rep.cplex.eq(0.0, expr_i_nn);
//					}
//
//					/* x_nextNode = 1 */
////					vars[nextNode-1] = rep.v_rep[nextNode-4];
////					bounds[nextNode-1] = 1.0;
////					dirs[nextNode-1] = BranchDirection.Up;
//					c_range[nextNode-1] = rep.cplex.eq(1.0, expr);
//
//					NodeInfo new_ni1 = new NodeInfo(nextNode, ni.nbRepTo1 + 1, true);
//
//					this.makeBranch(c_range, this.getObjValue(), (Object)new_ni1);
////					this.makeBranch(vars, bounds, dirs, this.getObjValue(), (Object)new_ni1);
//				}
//				
//				/* If setting x_nextNode to 0 is possible (i.e. : if it will still be possible to have K clusters) and if in that case at least one rep will be set to zero
//				 * nextNode - 2 : number of remaining representative which are not set (from 2 to nextNode - 1)
//				 * rep.K - ni.nbRepTo1 : number of remaining representative that have to be set to 1 */
//				int value = (nextNode - 2)  - (rep.K - ni.nbRepTo1);
//				
//				if(value > 0 ){
//					
//					/* Branch on x_nextNode = 0 */
//					logger.log(Level.FINER, "Branching on x" + nextNode + " = 0");
//					NodeInfo new_ni = new NodeInfo(nextNode, ni.nbRepTo1, false);
//
//					this.makeBranch(rep.cplex.eq(0.0, expr), this.getObjValue(), (Object)new_ni);
////					this.makeBranch(rep.v_rep[nextNode-4], 0.0, BranchDirection.Down, this.getObjValue(), (Object)new_ni);
//				
//				}
//				
//				else{
//					
//					/* If all the remaining representative have to be set to 1 when x_nextNode is set to 0
//					 * - x_i = 1 (3 < i < nextNode) -> nextNode - 4 constraints 
//					 * - x_nextNode = 0 -> 1 constraint
//					 * - x_i_j = 0 (1 <= j < i < nextNode) -> (nextNode - 1) (nextNode - 2) / 2 constraints
//					 */
//					if(value == 0){
//
////						IloNumVar[] vars = new IloNumVar[nextNode - 3 + (nextNode - 1) * (nextNode - 2) / 2];
////						double[] bounds = new double[nextNode - 3 + (nextNode - 1) * (nextNode - 2) / 2];
////						BranchDirection[] dirs = new BranchDirection[nextNode - 3 + (nextNode - 1) * (nextNode - 2) / 2];
//						IloRange[] c_range2 = new IloRange[nextNode - 3 + (nextNode - 1) * (nextNode - 2) / 2];
//												
//						int v = nextNode - 3;
//
////						vars[v] = rep.v_edge[1][0];
////						bounds[v] = 0.0;
////						dirs[v] = BranchDirection.Down;
//						IloLinearNumExpr expr_12 = rep.cplex.linearNumExpr();
//						expr_12.addTerm(1.0,rep.v_edge[1][0]);
//						c_range2[v] = rep.cplex.eq(0.0, expr_12);
//						v++;
//
//
////						vars[v] = rep.v_edge[2][0];
////						bounds[v] = 0.0;
////						dirs[v] = BranchDirection.Down;
//						IloLinearNumExpr expr_13 = rep.cplex.linearNumExpr();
//						expr_13.addTerm(1.0,rep.v_edge[2][0]);
//						c_range2[v] = rep.cplex.eq(0.0, expr_13);
//						v++;
//
////						vars[v] = rep.v_edge[2][1];
////						bounds[v] = 0.0;
////						dirs[v] = BranchDirection.Down;
//						IloLinearNumExpr expr_23 = rep.cplex.linearNumExpr();
//						expr_23.addTerm(1.0,rep.v_edge[2][1]);
//						c_range2[v] = rep.cplex.eq(0.0, expr_23);
//						v++;
//						
//						for(int i = 4 ; i < nextNode ; ++i){
////							vars[i - 4] = rep.v_rep[i - 4];
////							bounds[i - 4] = 1.0;
////							dirs[i - 4] = BranchDirection.Up;
//							IloLinearNumExpr expr_i = rep.cplex.linearNumExpr();
//							expr_i.addTerm(1.0,rep.v_rep[i - 4]);
//							c_range2[i - 4] = rep.cplex.eq(1.0, expr_i);
//							
//							for(int j = 1 ; j < i ; ++j){
//
////								vars[v] = rep.v_edge[i-1][j-1];
////								bounds[v] = 0.0;
////								dirs[v] = BranchDirection.Down;
//								IloLinearNumExpr expr_i_nn = rep.cplex.linearNumExpr();
//								expr_i_nn.addTerm(1.0,rep.v_edge[i-1][j-1]);
//								c_range2[v] = rep.cplex.eq(0.0, expr_i_nn);
//								v++;
//								
//							}
//						}
//
////						vars[nextNode - 4] = rep.v_rep[nextNode-4];
////						bounds[nextNode - 4] = 0.0;
////						dirs[nextNode - 4] = BranchDirection.Down;
//						c_range2[nextNode - 4] = rep.cplex.eq(0.0, expr);
//						
//						/* Branch on x_nextNode = 0 */
//						logger.log(Level.FINER, "Branching on x" + nextNode + " = 0");
// 
////						this.makeBranch(vars, bounds, dirs, this.getObjValue());
//						this.makeBranch(c_range2, this.getObjValue());
//						
//					}
//					
//				}
//			}
//			
//
//
//				
//		} // END : If the node correspond to a representative
//		
//		else{
//			
//			/* If we are at the root */
//			if(this.getNodeId().toString().compareTo("Node0") == 0){
//				
//				/* Branch on xn */
//
//				logger.log(Level.FINER, "Branching on xn");
//				
//				/* Branch on xn = 1, 
//				 * in that case there are two representative (1 and n) */
//				NodeInfo new_ni = new NodeInfo(rep.n, 2, true);
//				
//				IloLinearNumExpr expr = rep.cplex.linearNumExpr();
//				expr.addTerm(1.0, rep.v_rep[rep.n - 4]);
//				
////				this.makeBranch(rep.v_rep[rep.n - 4], 1.0, BranchDirection.Up, this.getObjValue(), (Object)new_ni);
//				this.makeBranch(rep.cplex.eq(1.0, expr), this.getObjValue(), (Object)new_ni);
//
//				/* Branch on xn = 0 
//				 * in that case there is only one representative (1) */
//				NodeInfo new_ni1 = new NodeInfo(rep.n, 1, false);
//				
////				this.makeBranch(rep.v_rep[rep.n - 4], 0.0, BranchDirection.Down, this.getObjValue(), (Object)new_ni1);
//				this.makeBranch(rep.cplex.eq(0.0, expr), this.getObjValue(), (Object)new_ni1);	
//				
//			} /* END : if(this.getNodeId().toString().compareTo("Node0") == 0){ */
//						
//		} // END : if(this.getNodeData() instanceof NodeInfo){ (if the node does not correspond to a representative)

	} // END : main
	
//	public class NodeInfo{
//		
//		boolean lastNodeTo1 = false;
//		
//		/**
//		 * Number of representative which have been set to 1
//		 */
//		int nbRepTo1 = 0;
//	
//		/**
//		 * Representative which corresponds to the current node. It is equal to :
//		 * 	- "-1" if the node does not correspond to a representative (i.e. all the representative variables have already been set to 0)
//		 */
//		public int currentNode;
//		
//		public NodeInfo(int currentRep, int nbRepTo1, boolean lastNodeTo1){
//			
//			this.currentNode = currentRep;
//			this.nbRepTo1 = nbRepTo1;
//			this.lastNodeTo1 = lastNodeTo1;
//				
//		}
//		
//	}
//	


}
