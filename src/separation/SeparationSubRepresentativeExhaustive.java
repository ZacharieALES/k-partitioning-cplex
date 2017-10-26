package separation;

import java.util.ArrayList;

import formulation.interfaces.IFEdgeVNodeClusterV;
import formulation.interfaces.IFormulation;
import inequality_family.AbstractInequality;
import inequality_family.SubRepresentativeInequality;
import variable.VariableGetter;


/**
 * Exhaustively add all the sub_representative inequalities xi,j <= sum_k=0,i
 * @author zach
 *
 */
public class SeparationSubRepresentativeExhaustive extends AbstractSeparation<IFEdgeVNodeClusterV> {

	public SeparationSubRepresentativeExhaustive(IFEdgeVNodeClusterV formulation, VariableGetter vg) {
		super("Sub-representative exhaustif", formulation, vg);
		
	}

	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate(){
		
		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
		for(int i = 0 ; i < formulation.n() ; i++)
			for(int j = i+1 ; j < formulation.n() ; ++j)
				result.add(new SubRepresentativeInequality(formulation, i, j));
				
		return result;
	}

}
