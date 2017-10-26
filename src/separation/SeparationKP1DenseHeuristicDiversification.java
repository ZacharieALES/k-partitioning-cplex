package separation;

import java.util.ArrayList;

import formulation.interfaces.IFEdgeVClusterNb;
import formulation.interfaces.IFormulation;
import inequality_family.AbstractInequality;
import inequality_family.DependentSetInequality;
import variable.VariableGetter;


public class SeparationKP1DenseHeuristicDiversification extends SeparationKp1DenseHeuristic{

	public int nodeForcedInSet = 0;
	
	public SeparationKP1DenseHeuristicDiversification(IFEdgeVClusterNb formulation, VariableGetter vg) {
		super(formulation, vg);
	}
	
	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate(){
		
		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
		boolean[] nodeToPutInSet = new boolean[formulation.n()];
		
		for(int i = 0 ; i < formulation.n(); ++i)
			nodeToPutInSet[i] = true;
		
		int i = 0;
		
		while(i < formulation.n()){
			
			if(nodeToPutInSet[i]){
//System.out.println("Node forced: " + i);

				nodeForcedInSet = i;
				
				ArrayList<AbstractInequality<? extends IFormulation>> al_ineq = super.separate();
				
				/* If an inequality has been found */
				if(al_ineq.size() > 0){
					
					/* Add it */
					result.addAll(al_ineq);
					
					DependentSetInequality ineq = (DependentSetInequality)al_ineq.get(0);
					
					/* Remove all the nodes in the set Z from the list of nodes to search in a set */
					for(Integer n : ineq.Z)
						nodeToPutInSet[n] = false;
				}
			}
			
			++i;
		}
		
		return result;
			
	}
	
	@Override
	public int getDensestNode(){
		
		int best = -1;
		double max = -1.0;
		int gap = Math.abs(rand.nextInt(formulation.n()));
		
		for(int i = 0 ; i < formulation.n() ; ++i){
			int g_id = (i + gap) % formulation.n();
			if(density[g_id] > max && g_id != nodeForcedInSet){
				best = g_id;
				max = density[g_id];
			}
		}
		
		return best;
			
	}

}
