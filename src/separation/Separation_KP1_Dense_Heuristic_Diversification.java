package separation;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.DependentSet_Inequality;

import java.util.ArrayList;

import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;

public class Separation_KP1_Dense_Heuristic_Diversification extends Separation_Kp1_Dense_heuristic{

	public int nodeForcedInSet = 0;
	
	public Separation_KP1_Dense_Heuristic_Diversification(
			Solution_Representative ucc) {
		super(ucc);
	}
	
	@Override
	public ArrayList<Abstract_Inequality> separate() throws IloException {
		
		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
		boolean[] nodeToPutInSet = new boolean[s.n()];
		
		for(int i = 0 ; i < s.n(); ++i)
			nodeToPutInSet[i] = true;
		
		int i = 0;
		
		while(i < s.n()){
			
			if(nodeToPutInSet[i]){
//System.out.println("Node forced: " + i);

				nodeForcedInSet = i;
				
				ArrayList<Abstract_Inequality> al_ineq = super.separate();
				
				/* If an inequality has been found */
				if(al_ineq.size() > 0){
					
					/* Add it */
					result.addAll(al_ineq);
					
					DependentSet_Inequality ineq = (DependentSet_Inequality)al_ineq.get(0);
					
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
		int gap = Math.abs(rand.nextInt(s.n()));
		
		for(int i = 0 ; i < s.n() ; ++i){
			int g_id = (i + gap) % s.n();
			if(density[g_id] > max && g_id != nodeForcedInSet){
				best = g_id;
				max = density[g_id];
			}
		}
		
		return best;
			
	}

}
