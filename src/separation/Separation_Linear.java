package separation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.Linear_First_Inequality;
import inequality_family.Linear_Second_Inequality;
import inequality_family.Linear_Third_Inequality;
import solution.Solution_Representative;


public class Separation_Linear extends Abstract_Separation {

	int MAXCUT;
	int MAXFOUND;

	public TreeSet<GapFirstInequality> foundIneq1 = new TreeSet<GapFirstInequality>(new Comparator<GapFirstInequality>(){

		@Override
		public int compare(GapFirstInequality o1, GapFirstInequality o2) {
			int value = o2.gap - o1.gap;
			if(value == 0)
				value = 1;
			return value;
		}
	});

	public TreeSet<GapSecondInequality> foundIneq2 = new TreeSet<GapSecondInequality>(new Comparator<GapSecondInequality>(){

		@Override
		public int compare(GapSecondInequality o1, GapSecondInequality o2) {
			int value = o2.gap - o1.gap;
			if(value == 0)
				value = 1;
			return value;
		}
	});

	public TreeSet<GapThirdInequality> foundIneq3 = new TreeSet<GapThirdInequality>(new Comparator<GapThirdInequality>(){

		@Override
		public int compare(GapThirdInequality o1, GapThirdInequality o2) {
			int value = o2.gap - o1.gap;
			if(value == 0)
				value = 1;
			return value;
		}
	});

	public Separation_Linear(Solution_Representative ucc, int MAXCUT) {
		super("Linear inequality", ucc);
		this.MAXCUT = MAXCUT;
		MAXFOUND = 5*MAXCUT;
	}

	@Override
	public ArrayList<Abstract_Inequality> separate(){

		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();

		foundIneq1.clear();	
		foundIneq2.clear();	
		foundIneq3.clear();	

		int i = 0;
		int totalFound = 0;

		while(i < s.n() && totalFound < MAXFOUND){

			int j = i+1;

			while(j < s.n()){

				//				addFirstIfGapNegative(i,j);

				if(i > 0){
					try {
						addSecondIfGapNegative(i,j);
					} catch (IloException e) {
						e.printStackTrace();
					}
					try{
						addThirdIfGapNegative(i,j);
					} catch (IloException e) {
						e.printStackTrace();
					}
				}

				++j;
			}

			++i;
		}

		int max = Math.max(Math.max(foundIneq1.size(), foundIneq2.size()), foundIneq3.size());

		int nb = 0;

		Iterator<GapFirstInequality> it1 = foundIneq1.iterator();
		Iterator<GapSecondInequality> it2 = foundIneq2.iterator();
		Iterator<GapThirdInequality> it3 = foundIneq3.iterator();

		boolean over = false;;

		while(!over && nb < MAXCUT){

			over = true;

			//			if(it1.hasNext()){
			//				result.add(it1.next());
			//				nb++;
			//				over = false;
			//			}
			//			
			if(it2.hasNext()){
				result.add(it2.next());
				over = false;	

				//System.out.println("Second: " + ((GapSecondInequality)result.get(result.size()-1)).i + " - "  + ((GapSecondInequality)result.get(result.size()-1)).j);				
			}

			if(it3.hasNext()){
				result.add(it3.next());
				nb++;
				over = false;		
				//System.out.println("Third: " + ((GapThirdInequality)result.get(result.size()-1)).i + " - "  + ((GapThirdInequality)result.get(result.size()-1)).j);	
			}

		}


		return result;
	}

	private void addFirstIfGapNegative(int i, int j) throws IloException {
		GapFirstInequality ineq = new GapFirstInequality(i, j);

		if(ineq.gap < -eps)
			foundIneq1.add(ineq);
	}

	private void addSecondIfGapNegative(int i, int j)  throws IloException{
		GapSecondInequality ineq = new GapSecondInequality(i, j);

		if(ineq.gap < -eps)
			foundIneq2.add(ineq);
	}

	private void addThirdIfGapNegative(int i, int j) throws IloException {
		GapThirdInequality ineq = new GapThirdInequality(i, j);

		if(ineq.gap < -eps)
			foundIneq3.add(ineq);
	}



	public class GapFirstInequality extends Linear_First_Inequality{

		/** Gap between the value of the inequality and it's upper bound */
		public int gap;

		public GapFirstInequality(int i, int j) throws IloException{
			super(Separation_Linear.this.s, i, j);
			gap = (int) (getSlack() * 1000);
		}

	}

	public class GapSecondInequality extends Linear_Second_Inequality{

		/** Gap between the value of the inequality and it's upper bound */
		public int gap;

		public GapSecondInequality(int i, int j) throws IloException{
			super(Separation_Linear.this.s, i, j);
			gap = (int) (getSlack() * 1000);
		}

	}

	public class GapThirdInequality extends Linear_Third_Inequality{

		/** Gap between the value of the inequality and it's upper bound */
		public int gap;

		public GapThirdInequality(int i, int j) throws IloException{
			super(Separation_Linear.this.s, i, j);
			gap = (int) (getSlack() * 1000);
		}

	}

}
