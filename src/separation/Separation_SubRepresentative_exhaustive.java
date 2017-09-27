package separation;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.LowerRep_Inequality;
import inequality_family.SubRepresentative_Inequality;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;


/**
 * Exhaustively add all the sub_representative inequalities xi,j <= sum_k=0,i
 * @author zach
 *
 */
public class Separation_SubRepresentative_exhaustive extends Abstract_Separation {

	public Separation_SubRepresentative_exhaustive(Solution_Representative rep) {
		super("Sub-representative exhaustif", rep);
	}

	@Override
	public ArrayList<Abstract_Inequality> separate(){
		
		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
		for(int i = 0 ; i < s.n() ; i++)
			for(int j = i+1 ; j < s.n() ; ++j)
				result.add(new SubRepresentative_Inequality(s, i, j));
				
		return result;
	}

}
