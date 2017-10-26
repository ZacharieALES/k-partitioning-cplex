package separation;

import java.util.ArrayList;

import formulation.interfaces.IFEdgeVNodeV;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import inequality_family.TriangleRepresentative;
import variable.VariableGetter;



/**
 * Separate triangle representative inequality exhaustively (add exhaustively all the violated inequalities)
 * @author zach
 *
 */
public class SeparationTriangleRepresentative extends AbstractSeparation<IFEdgeVNodeV>{
	
	/**
	 * True if only the triangle inequalities which are facet defining are separated  
	 */
	public boolean onlyFacets;
	
	public SeparationTriangleRepresentative(IFEdgeVNodeV formulation, VariableGetter vg, boolean separateOnlyFacets) {
		super("Triangle representative", formulation, vg);
		
		this.onlyFacets = separateOnlyFacets;
	}

	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate() {

		ArrayList<Integer> set = new ArrayList<Integer>();
		
		set.add(0);
		set.add(1);
		set.add(3);
		set.add(4);
			
		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
		while(set != null){
			result.addAll(getInequalities(set));
			set = nextSet(set);
		}
			
		return result;
	
	}
	
	public ArrayList<AbstractInequality<? extends IFormulation>> getInequalities(ArrayList<Integer> set){
		
		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
		TriangleRepresentative ineq1 = new TriangleRepresentative(formulation, set.get(0), set.get(1), set.get(2), set.get(3));
		TriangleRepresentative ineq2 = new TriangleRepresentative(formulation, set.get(1), set.get(0), set.get(2), set.get(3));
		
		try {
			if(ineq1.getSlack(vg) < -0.00001)
				result.add(ineq1);
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		try {
			if(ineq2.getSlack(vg) < -0.00001)
				result.add(ineq2);
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		/* Valid inequalities which are not facet defining */
		if(!onlyFacets){
			
			if(set.get(3) >= 3 && set.get(2) >= 3){
				TriangleRepresentative ineq3 = new TriangleRepresentative(formulation, set.get(0), set.get(1), set.get(3), set.get(2));
				
				try {
					if(ineq3.getSlack(vg) < -0.00001)
						result.add(ineq3);
				} catch (IloException e) {
					e.printStackTrace();
				}
			}
			
			if(set.get(3) >= 3 && set.get(1) >= 3){
				TriangleRepresentative ineq4 = new TriangleRepresentative(formulation, set.get(2), set.get(0), set.get(3), set.get(1));
				
				try {
					if(ineq4.getSlack(vg) < -0.00001)
						result.add(ineq4);
				} catch (IloException e) {
					e.printStackTrace();
				}
			}
		}
		
		return result;
		
	}
	
	public ArrayList<Integer> nextSet(ArrayList<Integer> set){
		
		ArrayList<Integer> result = null;
		int id = set.size()-1;
		int nextElement = set.get(id);
		if(nextElement !=formulation.n()-1){
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
