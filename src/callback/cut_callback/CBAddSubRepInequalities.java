package callback.cut_callback;

import java.util.List;

import callback.branch_callback.BranchDisplayInformations.EdgeVariableFixedToOne;
import formulation.interfaces.IFEdgeVNodeClusterV;
import ilog.concert.IloException;
import inequality_family.SubRepresentativeInequality;

public class CBAddSubRepInequalities extends AbstractCutCallback{

	public static int addedInequalities = 0;
	
	private IFEdgeVNodeClusterV specializedFormulation;
	
	public CBAddSubRepInequalities(IFEdgeVNodeClusterV formulation) {
		super(formulation);
		specializedFormulation = formulation;
		
	}

	@Override
	public void separates() throws IloException {
		
		Object o;
		try {
			o = this.getNodeData();
			
			/* If the node contains data in the shape of a list */
			if(o instanceof List<?>){
				
				List<?> l_o = (List<?>)o;
				
				/* For each element of the list */
				for(int i = l_o.size()-1 ; i >= 0 ; i--){
					
					Object o2 = l_o.get(i);
					
					/* If the element in the list is an EdgeVariableFixedToOne through the branch callback */
					if(o2 instanceof EdgeVariableFixedToOne){
						
						EdgeVariableFixedToOne fv = (EdgeVariableFixedToOne)o2;
						
						SubRepresentativeInequality sri = new SubRepresentativeInequality(specializedFormulation, fv.i, fv.j);

						this.addRange(sri.getRange(), -1);
//						System.out.println("Range ajoutée: " + fv.i + " " + fv.j);
						addedInequalities++;
					}
				}
				
				l_o.clear();
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	

}
