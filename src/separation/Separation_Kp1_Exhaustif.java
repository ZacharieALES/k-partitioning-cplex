package separation;
import java.util.ArrayList;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.DependentSet_Inequality;
import solution.Solution_Representative;


/**
 * Add exhaustively the dependent set inequalities inequalities (improve a lot the root relaxation but is extremely long):
 * sum_k_k'_in_Z x_k_k' <= K+1
 * 
 * @author zach
 *
 */
public class Separation_Kp1_Exhaustif extends Abstract_Separation{
	
	
	public Separation_Kp1_Exhaustif(Solution_Representative rep){
		super("Kp1_exhaustif", rep);
	}
	
	@Override
	public ArrayList<Abstract_Inequality> separate() {
		
		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();

		for(int size = s.K()+1 ; size <= 2*s.K()-1 ; ++size){ 
			ArrayList<Integer> set = new ArrayList<Integer>();
			
			/* Create the first set */
			for(int i = 0 ; i < size+1 ; i++)
				set.add(i);
			
			while(set != null){
				Abstract_Inequality ineq;
				try {
					ineq = getInequality(set);
					if(ineq !=  null)
						result.add(ineq);
				} catch (IloException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
//				System.out.println("kp1 exhaustif: " + set);
				set = nextKp1Set(set);
			}
		}
			
		return result;
	
	}
	
	public Abstract_Inequality getInequality(ArrayList<Integer> set) throws IloException{
					
		Abstract_Inequality ineq = new DependentSet_Inequality(s, set, 1.0);
		
		if(ineq.getSlack() < 0.0001)
			return ineq;
		else 
			return null;
		
	}
	
	public ArrayList<Integer> nextKp1Set(ArrayList<Integer> set){
		
		ArrayList<Integer> result = null;
		int id = set.size()-1;
		int nextElement = set.get(id);
		if(nextElement !=s.n()-1){
			set.set(id, nextElement+1);
			result = set;
		}
		else{
			
			id--;
			int currentElement = set.get(id);
			
			boolean found = false;
					
			while(id>=0 && !found){
				
				if(currentElement + 1 != nextElement){
					found = true;
					set.set(id, currentElement+1);
					for(int i = 1 ; i < set.size() - id ; ++i)
						set.set(id + i, currentElement + i + 1);
					result = set;
				}
				
				id--;
				
				if(id != -1){
					nextElement = currentElement;
					currentElement = set.get(id);
				}

			}
				
		}
		
		return result;
		
	}
	
}

