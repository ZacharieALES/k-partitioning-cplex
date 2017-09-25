package branch_callback;
import formulation.PartitionWithRepresentative;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.BranchCallback;
import ilog.cplex.IloCplex.BranchDirection;

public class BranchCB_Round extends BranchCallback{

	PartitionWithRepresentative rep;
	
	public BranchCB_Round(PartitionWithRepresentative rep){
		this.rep = rep;
	}
	
	public int nbNodes = 0;
	
	@Override
	protected void main() throws IloException {

		nbNodes++;
		
		NodeInfoR ni = null;
		
		/* If the node contains a NodeInfo (i.e. if the node corresponds to a representative variable which can be set to one) */
		if(this.getNodeData() instanceof NodeInfoR){
			ni = (NodeInfoR)this.getNodeData();	
		}
		else if(this.getNodeId().toString().compareTo("Node0") == 0){
			ni = new NodeInfoR();	
		}
			
		if(ni != null){
			/* Maximal value of a representative variable (which have not already been set to 1 in the previous branch) in the integer relaxation */
			double max = 0.0;
			int maxRep = -1;
			int i = 4;
			
			/* For each representative (from 4 to n) while no representative of value 1 (which have not been set in a previous branch) has been found */
			while(max != 1.0 &&  i < rep.n){
				
				/* If the representative has not been set in the previous parents */
				if(ni.statuts[i-1] == -1){
					
					/* If its value is greater than the best currently found */
					double value = this.getValue(rep.v_rep[i - 4]);
					if(value > max){
						max = value;
						maxRep = i;
					}
				}
					
				i++;			
				
			}
			
			/* If a representative variable which have not already been set to 1 has a value of 1 in the integer relaxation,
			 * Branch on it.
			 */
			if(max == 1.0)
				branchOnXi(maxRep, ni);
			
			/* Otherwise, check if x2 or x3 have not already been set and if they have a value greater than <max> */
			else{
				
				double v2 = -1.0;
				double v3 = -1.0;
				
				if(ni.statuts[1] == -1)
					v2 = this.getValue(rep.exprX2());
				if(ni.statuts[2] == -1)
					v3 = this.getValue(rep.exprX3());
				
				if(v2 > max && v2 > v3){			
					branchOnX2(ni);
				}
				else
					if(v3 > max){
						branchOnX3(ni);
					}
					else{
						
						/* If all the not set representative variable are equal to 0 */
						if(maxRep == -1){
							
							/* Take the first representative variable which has not been set */
							boolean found = false;
							int v = 2;
							
							while(!found){
								
								if(ni.statuts[v-1] == -1){
									found = true;
								}
								else
									v++;
								
							}
							
							if(v < 4)
								if(v == 2){						
									branchOnX2(ni);
								}
								else{	
									branchOnX3(ni);
								}
							else{	
								branchOnXi(v, ni);
							}
							
						}
						else{
							branchOnXi(maxRep, ni);
						}
					}
			}
		}
		
	}
	
	public void branchOnX2(NodeInfoR ni) throws IloException{
		
		
		/** Branch on x2 = 0 if possible **/
		
		/* If x2 can be set to 0
		 * i.e.: if the number of node which are not set is greater than the number of nodes which still have to be set to 1
		 * rep.n - ni.nbRepSet : number of nodes which are not set
		 * rep.K - ni.nbRepTo1 : number of nodes which still have to be set to 1
		 */
		if(rep.n - ni.nbRepSet > rep.K - ni.nbRepTo1){
			makeBranch(rep.x2Eq0(), this.getObjValue(), new NodeInfoR(2, 0, ni));		
		}
		else{
			//TODO set all the remaining nodes to 1 and their edge variables to 0
			makeBranch(rep.x2Eq0(), this.getObjValue());
		}
		
		
		/** Branch on x2 = 1 **/
		
		/* If x2 is not the last representative that will be set to 1 */
		if(ni.nbRepTo1 < rep.K-1){
			makeBranch(rep.x2Eq1(), this.getObjValue(), new NodeInfoR(2, 1, ni));
		}
		
		/* If this is the last representative to be set to 1 */
		else{
			//TODO fixer les autres representants à 0
			makeBranch(rep.x2Eq1(), this.getObjValue());
		}
		
	}
	
	public void branchOnX3(NodeInfoR ni) throws IloException{
		
		
		/** Branch on x3 = 0 if possible **/
		
		/* If x3 can be set to 0
		 * i.e.: if the number of node which are not set is greater than the number of nodes which still have to be set to 1
		 * rep.n - ni.nbRepSet : number of nodes which are not set
		 * rep.K - ni.nbRepTo1 : number of nodes which still have to be set to 1
		 */
		if(rep.n - ni.nbRepSet > rep.K - ni.nbRepTo1)
			makeBranch(rep.x3Eq0(), this.getObjValue(), new NodeInfoR(3, 0, ni));
		else{
			//TODO set all the remaining nodes to 1 and their edge variables to 0
			makeBranch(rep.x3Eq0(), this.getObjValue());
		}
		
		
		/** Branch on x3 = 1 **/
		
		/* If x3 is not the last representative that will be set to 1 */
		if(ni.nbRepTo1 < rep.K-1)
			makeBranch(rep.x3Eq1(), this.getObjValue(), new NodeInfoR(3, 1, ni));
		
		/* If this is the last representative to be set to 1 */
		else{
			//TODO fixer les autres representants à 0
			makeBranch(rep.x3Eq1(), this.getObjValue());
		}
		
	}
	
	public void branchOnXi(int i, NodeInfoR ni) throws IloException{
		
		/** Branch on xi = 0 if possible **/
		
		/* If xi can be set to 0
		 * i.e.: if the number of node which are not set is greater than the number of nodes which still have to be set to 1
		 * rep.n - ni.nbRepSet : number of nodes which are not set
		 * rep.K - ni.nbRepTo1 : number of nodes which still have to be set to 1
		 */
		if(rep.n - ni.nbRepSet > rep.K - ni.nbRepTo1)
			makeBranch(rep.v_rep[i - 4], 0.0, BranchDirection.Down, this.getObjValue(), new NodeInfoR(i, 0, ni));
		else{
			//TODO set all the remaining nodes to 1 and their edge variables to 0
			makeBranch(rep.v_rep[i - 4], 0.0, BranchDirection.Down, this.getObjValue());
		}
		
		/** Branch on xi = 1 **/
		
		/* If xi is not the last representative that will be set to 1 */
		if(ni.nbRepTo1 < rep.K-1)
			makeBranch(rep.v_rep[i - 4], 1.0, BranchDirection.Up, this.getObjValue(), new NodeInfoR(i, 1, ni));
		
		/* If this is the last representative to be set to 1 */
		else{
			//TODO fixer les autres representants à 0
			makeBranch(rep.v_rep[i - 4], 1.0, BranchDirection.Up, this.getObjValue());
		}
		
	}

	
	public class NodeInfoR{
		
		/** Statuts of each representative:
		 * -1 : not set
		 *  0 : set to 0
		 *  1 : set to 1
		 */
		int[] statuts = new int[rep.n];
		
		/* Number of representative set to 1 (start at 1 since 1 is always a representative) */
		int nbRepTo1 = 1;
		
		int nbRepSet = 1;
		
		public NodeInfoR(){
			
			for(int i = 0 ; i < rep.n ; i++){
				statuts[i] = -1;
			}
			
		}
		
		/**
		 * 
		 * @param repSet Representative variable which is set in this branch
		 * @param repValue 1 if the representative is set to 1 ; 0 otherwise
		 * @param parent Node information of the parent node
		 */
		public NodeInfoR(int repSet, int repValue, NodeInfoR parent){

			for(int i = 0 ; i < rep.n ; i++){
				statuts[i] = parent.statuts[i];
			}
			
			statuts[repSet-1] = repValue;	

			nbRepTo1 = parent.nbRepTo1 + repValue;
			nbRepSet = parent.nbRepSet + 1;
			
		}
		
	}
	
}
