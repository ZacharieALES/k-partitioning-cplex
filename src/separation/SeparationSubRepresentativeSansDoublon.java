package separation;

import java.util.ArrayList;
import java.util.List;

import callback.cut_callback.CBAddSubRepInequalities;
import formulation.interfaces.IFEdgeVNodeClusterV;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import inequality_family.SubRepresentativeInequality;
import variable.VariableGetter;




/**
 * Add sub_representative inequalities xi,j <= sum_k=0,i when they are violated.
 * The inequalities are exhaustively stored initially in a list to avoid generating them twice
 * @author zach
 *
 */
public class SeparationSubRepresentativeSansDoublon extends AbstractSeparation<IFEdgeVNodeClusterV> {
	
	List<SubRepresentativeInequality> ineq = null;
	
	public SeparationSubRepresentativeSansDoublon(IFEdgeVNodeClusterV formulation, VariableGetter vg) {
		super("Sub-representative sans doublon", formulation, vg);
		
	}

	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate(){
		
		if(ineq == null){
			ineq = new ArrayList<>();
			
			for(int i = 0 ; i < formulation.n() ; ++i)
				for(int j = i+1 ; j < formulation.n() ; ++j)
					ineq.add(new SubRepresentativeInequality(formulation, i, j));
			
		}
		
		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
		for(int i = ineq.size() - 1 ; i >= 0 ; i--){
		
			SubRepresentativeInequality sri = ineq.get(i);
			
			try {
				if(sri.evaluate(vg) < 0.0 - 1E-5){
					result.add(sri);
					ineq.remove(i);
					CBAddSubRepInequalities.addedInequalities++;
				}
			} catch (IloException e) {
				e.printStackTrace();
			}
		}
				
		return result;
	}
}
