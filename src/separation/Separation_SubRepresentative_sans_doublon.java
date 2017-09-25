package separation;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.SubRepresentative_Inequality;

import java.util.ArrayList;
import java.util.List;

import cut_callback.Abstract_CutCallback;
import cut_callback.CB_AddSubRep_inequalities;

import solution.Solution_Representative;


/**
 * Add sub_representative inequalities xi,j <= sum_k=0,i when they are violated.
 * The inequalities are exhaustively stored initially in a list to avoid generating them twice
 * @author zach
 *
 */
public class Separation_SubRepresentative_sans_doublon extends Abstract_Separation {
	
	List<SubRepresentative_Inequality> ineq = null;

	public Separation_SubRepresentative_sans_doublon(Solution_Representative rep) {
		super("Sub-representative sans doublon", rep);
	}

	@Override
	public ArrayList<Abstract_Inequality> separate() throws IloException {
		
		if(ineq == null){
			ineq = new ArrayList<>();
			
			for(int i = 0 ; i < s.n() ; ++i)
				for(int j = i+1 ; j < s.n() ; ++j)
					ineq.add(new SubRepresentative_Inequality(s, i, j));
			
		}
		
		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
		for(int i = ineq.size() - 1 ; i >= 0 ; i--){
		
			SubRepresentative_Inequality sri = ineq.get(i);
			
			if(sri.evaluate() < 0.0 - 1E-5){
				result.add(sri);
				ineq.remove(i);
				CB_AddSubRep_inequalities.addedInequalities++;
			}
		}
				
		return result;
	}
}
