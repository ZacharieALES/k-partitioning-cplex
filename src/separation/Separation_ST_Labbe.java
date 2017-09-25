package separation;
import inequality_family.Abstract_Inequality;
import inequality_family.ST_Inequality;

import java.util.ArrayList;
import java.util.Random;

import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;


public class Separation_ST_Labbe extends Abstract_Separation{

	
	public Separation_ST_Labbe(Solution_Representative ucc) {
		super("ST_Labbe", ucc);
	}
	
	Random rgen;  // Random number generator

	/**
	 * Heuristic to separate two-partition inequalities.
	 * S is restricted to one node picked randomly.
	 * T is iteratively constructed until an violated inequality is found.
	 * If not another node is chosen for S.
	 */
	public ArrayList<Abstract_Inequality> separate(){

		rgen = new Random();
		
		/* Shuffle the n nodes */
		
		int[] nodes = new int[s.n()];
		 
		for (int i=0; i < nodes.length; i++) {
			nodes[i] = i;
		}
		 
		/* Shuffle by exchanging each element randomly */
		for (int i=0; i < nodes.length; i++) {
			int randomPosition = rgen.nextInt(nodes.length);
			int temp = nodes[i];
			nodes[i] = nodes[randomPosition];
			nodes[randomPosition] = temp;
		}
		
		/* Find a cut */
		boolean cutFound = false;

		int s = 0;
		ArrayList<Integer> t = new ArrayList<Integer>();
		
		/* Id of the node which will be equal to S */
		int s_id = 0;
		
		while(!cutFound && s_id < this.s.n()){
			
			s = nodes[s_id];
			t = new ArrayList<Integer>();
			ArrayList<Integer> w = new ArrayList<Integer>();
			
			/* Score of the cut */
			double score = 0.0;
			
			/* Find all the potential nodes to put in T */
			for(int i = 0 ; i < this.s.n() ; ++i){
//				System.out.println("i: " + i + " s: " + s);
				if(i != s && this.s.x(s,i) > 0 + eps)
					w.add(i);
			}
			
			int candidate_id = 0; 
			
			/* Until no violated cut is found try to put successively all the node of w in T */
			while(!cutFound && candidate_id < w.size()){
				
				int candidate = w.get(candidate_id);
				double scoreGap = scoreGap(s, t, candidate);
				
				if(scoreGap > 0 + eps){
					t.add(candidate);
					score += scoreGap;
					
					/* If the inequality is a violated one (i.e. if it's score is greater than 1 (since a 2-partition corresponds to : x(S,T) - x(S) - x(T) <= |S| and here |S| = 1) 
					 * and if |T| > 2 (since otherwise it corresponds to a triangle inequality) */
					if(t.size() > 2 && score > 1 + eps)
						cutFound = true;
				}
					
				candidate_id++;
			}
//System.out.println("score: " + score);
			++s_id;
		}
		
		ArrayList<Abstract_Inequality> r = new ArrayList<Abstract_Inequality>();
		
		if(cutFound)
			r.add(getInequality(s, t));

		return r;
		
	}

	/**
	 * Compute the gap on the 2-partition cut score when adding a node to the set T
	 * @param s The only node in S
	 * @param t The nodes in T
	 * @param candidate The node that we try to add in T
	 * @return
	 */
	private double scoreGap(int s, ArrayList<Integer> t, int candidate) {
		
		double result = this.s.x(s,candidate);
		
		for(int i = 0 ; i < t.size() ; ++i)
			result -= this.s.x(candidate,t.get(i));
		
		return result;
	}
	
	private Abstract_Inequality getInequality(int s, ArrayList<Integer> t){
	
		ST_Inequality ineq = new ST_Inequality(this.s);
		ineq.S.add(s);
		ineq.T = t;
		
		return ineq;
			
	}
	
}
