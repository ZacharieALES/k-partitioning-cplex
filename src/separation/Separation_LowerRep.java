package separation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.LowerRep_Inequality;
import solution.Solution_Representative;


public class Separation_LowerRep extends Abstract_Separation {

	int MAXCUT;
	int MAXFOUND;

	public TreeSet<GapLowerRepInequality> foundIneq = new TreeSet<GapLowerRepInequality>(new Comparator<GapLowerRepInequality>(){

		@Override
		public int compare(GapLowerRepInequality o1, GapLowerRepInequality o2) {
			int value = o2.gap - o1.gap;
			if(value == 0)
				value = 1;
			return value;
		}
	});

	public Separation_LowerRep(Solution_Representative rep, int MAXCUT) {
		super("Lower rep", rep);
		this.MAXCUT = MAXCUT;
		MAXFOUND = 5*MAXCUT;
	}

	@Override
	public ArrayList<Abstract_Inequality> separate() {
		
		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
		foundIneq.clear();	
		int l = 2;
		
		while(l < s.n() && foundIneq.size() < MAXFOUND){

			try {
				addIfGapNegative(l);
			} catch (IloException e) {
				e.printStackTrace();
			}
			++l;
			
		}

		Iterator<GapLowerRepInequality> it = foundIneq.iterator();
		int nb = 0;
		
		while(it.hasNext() && nb < MAXCUT){
			result.add(it.next());	
			nb++;
		}
		
		return result;
	}

	
	private void addIfGapNegative(int l) throws IloException {
		GapLowerRepInequality ineq = new GapLowerRepInequality(l);
		
		if(ineq.gap < -eps)
			foundIneq.add(ineq);
	}
	
	public class GapLowerRepInequality extends LowerRep_Inequality{
		
		public int gap;
		
		public GapLowerRepInequality(int l) throws IloException {
			super(Separation_LowerRep.this.s, l);
			this.gap = (int) (getSlack() * 1000);
		}
		
	}

}
