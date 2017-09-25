package separation;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.UpperRep_Inequality;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;

public class Separation_UpperRep extends Abstract_Separation {

	int MAXCUT;
	int MAXFOUND;

	public TreeSet<GapUpperRepInequality> foundIneq = new TreeSet<GapUpperRepInequality>(new Comparator<GapUpperRepInequality>(){

		@Override
		public int compare(GapUpperRepInequality o1, GapUpperRepInequality o2) {
			int value = o2.gap - o1.gap;
			if(value == 0)
				value = 1;
			return value;
		}
	});

	public Separation_UpperRep(Solution_Representative ucc, int MAXCUT) {
		super("Upper rep", ucc);
		this.MAXCUT = MAXCUT;
		MAXFOUND = 5*MAXCUT;
	}

	@Override
	public ArrayList<Abstract_Inequality> separate() throws IloException {

		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
		foundIneq.clear();	
		
		int j = 2;
		
		while(j < s.n() && foundIneq.size() < MAXFOUND){
			
			int i = 0;
			
			while(i < j && foundIneq.size() < MAXFOUND){
				addIfGapNegative(i, j);
				++i;
			}
			
			++j;
			
		}

		Iterator<GapUpperRepInequality> it = foundIneq.iterator();
		int nb = 0;
		
		while(it.hasNext() && nb < MAXCUT){
			result.add(it.next());
			nb++;
		}
		
		return result;
	}

	
	private void addIfGapNegative(int i, int j) {
		GapUpperRepInequality ineq = new GapUpperRepInequality(i, j);
		
		if(ineq.gap < -eps)
			foundIneq.add(ineq);
	}
	
	public class GapUpperRepInequality extends UpperRep_Inequality{
		
		public int gap;
		
		public GapUpperRepInequality(int i, int j) {
			super(Separation_UpperRep.this.s, i, j);
			this.gap = (int) (getSlack() * 1000);
		}
		
	}

}
