package separation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import formulation.interfaces.IFEdgeVNodeVClusterNb;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import inequality_family.UpperRepInequality;
import variable.VariableGetter;

public class SeparationUpperRep extends AbstractSeparation<IFEdgeVNodeVClusterNb> {

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

	public SeparationUpperRep(IFEdgeVNodeVClusterNb formulation, VariableGetter vg, int MAXCUT) {
		super("Upper rep", formulation, vg);
		
		this.MAXCUT = MAXCUT;
		MAXFOUND = 5*MAXCUT;
	}

	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate()  {

		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
		foundIneq.clear();	
		
		int j = 2;
		
		while(j < formulation.n() && foundIneq.size() < MAXFOUND){
			
			int i = 0;
			
			while(i < j && foundIneq.size() < MAXFOUND){
				try {
					addIfGapNegative(i, j);
				} catch (IloException e) {
					e.printStackTrace();
				}
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

	
	private void addIfGapNegative(int i, int j) throws IloException {
		GapUpperRepInequality ineq = new GapUpperRepInequality(i, j);
		
		if(ineq.gap < -eps)
			foundIneq.add(ineq);
	}
	
	public class GapUpperRepInequality extends UpperRepInequality{
		
		public int gap;
		
		public GapUpperRepInequality(int i, int j) throws IloException {
			super(SeparationUpperRep.this.formulation, i, j);
			this.gap = (int) (getSlack(vg) * 1000);
		}
		
	}

}
