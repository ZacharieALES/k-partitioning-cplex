package node_callback;

import formulation.PartitionWithRepresentative;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.NodeCallback;

/**
 * NodeCallback to use with BranchCB_Rep_first branch callback.
 * By order of priorities :
 * - a node which has no NodeInfo
 * - a node whose NodeInfo got the higher nbRepTo1 attribute
 * 
 * @author zach
 *
 */
public class NodeCB_Rep1_first extends NodeCallback{

	PartitionWithRepresentative rep;
	
	public NodeCB_Rep1_first(PartitionWithRepresentative rep){
		this.rep = rep;
	}
	

	@Override
	protected void main() throws IloException {
	
//		int nbNodes = this.getNremainingNodes();
//		
//		boolean found = false;
//		
//		int i = 0;
//		int maxNode = -1;
//		int maxRep = 0;
//		
//		while( i < nbNodes && !found){
//			
//			Object o = this.getNodeData(i);
//			
//			if(o != null && o instanceof BranchCB_Rep_first_descending.NodeInfo){
//				
//				BranchCB_Rep_first_descending.NodeInfo ni = (BranchCB_Rep_first_descending.NodeInfo) o;
//				
//				if(ni.nbRepTo1 == rep.K){
//					found = true;
//				}
//				else{
//					
//					if(ni.lastNodeTo1)
//						found = true;
//					else 
//						++i;
////					if(ni.nbRepTo1 > maxRep){
////						
////						maxNode = i;
////						maxRep = ni.nbRepTo1;
////						
////					}
//	
//				}
//			}
//			else{
//				found = true;
//			}
//		
//		}
//		
//		if(found){
//			selectNode(i);
//		}
//		else if(maxNode != -1){
//			selectNode(maxNode);
//		}
//		else
//			System.out.println("ERROR : no node without info and no node with info");
		
	}
	
	

}
