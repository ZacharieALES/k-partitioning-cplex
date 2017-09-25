package separation;
import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.ST_Inequality;
import inequality_family.Triangle_Inequality;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

import separation.Separation_Triangle.GapTriangleInequality;
import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;


public class Separation_ST_Grotschell extends Abstract_Separation{

	int MAXCUT;
	int MAXFOUND;

	public Separation_ST_Grotschell(Solution_Representative ucc, int MAXCUT) {
		super("ST_Grotschell", ucc);
		this.MAXCUT = MAXCUT;
		MAXFOUND = 5*MAXCUT;
	}

	public TreeSet<GapSTInequality> foundIneq = new TreeSet<GapSTInequality>(new Comparator<GapSTInequality>(){

		@Override
		public int compare(GapSTInequality o1, GapSTInequality o2) {
			int value = o2.gap - o1.gap;
			if(value == 0)
				value = 1;
			return value;
		}
	});

	@Override
	public ArrayList<Abstract_Inequality> separate() throws IloException {
			
		foundIneq.clear();
			boolean heuristic1Over = false;
			int v = 0; 
			
			/* Search 2-partition inequalities such that S={v} (for all possible v) */
			while(foundIneq.size() < MAXFOUND && v < s.n()){
		
				/* Find neighbors j of v such that v_rep[v][j] != 0 and 1 */
				ArrayList<Integer> neighborV = new ArrayList<Integer>();
				
				for(int j = 0 ; j < v ; ++j){
					double value = s.x(v,j);
					if(value != 0 && value != 1)
						neighborV.add(j);			
				}
				
				for(int j = v+1 ; j < s.n() ; ++j){
					double value = s.x(j,v);
					if(value != 0 && value != 1)
						neighborV.add(j);
				}
	
				Collections.shuffle(neighborV);
				
				if(neighborV.size() > 0){
					
					/* Add the first neighbor in a set T */
					ArrayList<Integer> T = new ArrayList<Integer>();
					T.add(neighborV.get(0));
		
					/* For each neighbor w */
					for(int i = 1 ; i < neighborV.size() ; ++i){
					
						Integer w = neighborV.get(i);
						boolean w_valid = true;
						Iterator<Integer> t = T.iterator();

						/* If we consider the first heuristic */
						if(!heuristic1Over){
							
							/* If all the elements t of T verify  v_edge[w][t] = 0 */
							while(t.hasNext() && w_valid)	
								if(s.x(w,t.next()) < eps)
									w_valid = false;	
						}
						
						/* If we consider the second heuristic */
						else{
							
							double result = s.x(w,v);
							
							/* If all the elements t of T verify  v_edge[w][t] = 0 */
							while(t.hasNext() && result > (0 + eps))	
								result -= s.x(w,t.next());
							
							if(result < (0 - eps))
								w_valid = false;
//							else
//								System.out.println("Second valid");
							
						}
							
						if(w_valid){
							
							/* Add w in T */
							T.add(w);
						}
						
					}
					
					ArrayList<Integer> S = new ArrayList<Integer>();
					S.add(v);
					
					GapSTInequality ineq = new GapSTInequality(S, T);
					
					/* If the inequality is violated */
					if(ineq.gap < -eps){
						
						foundIneq.add(ineq);
					}
				}
				
				++v;
				
				/* If this is the end of the first heuristic and no cut has been found, start the second */
				if(v == s.n() && !heuristic1Over && foundIneq.size() == 0){
					heuristic1Over = true;
					v = 0;
				}
			}
			


			ArrayList<Abstract_Inequality> returned = new ArrayList<Abstract_Inequality>();
			
			Iterator<GapSTInequality> it = foundIneq.iterator();
			int nb = 0;
			
			while(it.hasNext() && nb < MAXCUT){
				returned.add(it.next());
				nb++;
			}
			
			return returned;
			
		}


	/**
	 * Represent a triangle inequality (x_s,t1 + x_s,t2 - x_t1,t2 <= 1)
	 * @author zach
	 *
	 */
	public class GapSTInequality extends ST_Inequality{
		
		/** Gap between the value of the inequality and it's upper bound (1) */
		public int gap;
		
		public GapSTInequality(ArrayList<Integer> s, ArrayList<Integer> t){
			super(Separation_ST_Grotschell.this.s);
			S = s;
			T = t;
			gap = (int) (getSlack() * 1000);
		}
		
	}

}
