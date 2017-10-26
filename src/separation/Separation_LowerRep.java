package separation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import formulation.interfaces.IFEdgeVNodeVClusterNb;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import inequality_family.LowerRepInequality;
import variable.VariableGetter;



public class Separation_LowerRep extends AbstractSeparation<IFEdgeVNodeVClusterNb> {

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

	public Separation_LowerRep(IFEdgeVNodeVClusterNb formulation, VariableGetter vg, int MAXCUT) {
		super("Lower rep", formulation, vg);
		
		this.MAXCUT = MAXCUT;
		MAXFOUND = 5*MAXCUT;
	}

	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate() {
		
		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
		foundIneq.clear();	
		int l = 2;
		
		while(l < formulation.n() && foundIneq.size() < MAXFOUND){

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
	
	public class GapLowerRepInequality extends LowerRepInequality{
		
		public int gap;
		
		public GapLowerRepInequality(int l) throws IloException {
			super(Separation_LowerRep.this.formulation, l);
			this.gap = (int) (getSlack(vg) * 1000);
		}
		
	}

}
