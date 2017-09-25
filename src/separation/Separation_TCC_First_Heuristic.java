package separation;
import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.TCC_Inequality;

import java.util.ArrayList;
import java.util.Random;

import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;


/**
 * Find an unsatisfied two-chorded cycle cut (x(C) - x(Cb) <= p)
 * 	- C : a cycle
 * 	- Cb : two-chorded cycle
 * 	- |C| = 2p + 1
 * 
 * The heuristic take random node i and try to expand the cycle
 * 
 * @author zach
 *
 */
public class Separation_TCC_First_Heuristic extends Abstract_Separation{

	public Separation_TCC_First_Heuristic(Solution_Representative ucc) {
		super("TCC_first_heuristic", ucc);
	}

	@Override
	public ArrayList<Abstract_Inequality> separate() throws IloException {
			
		boolean cutFound = false;
		
		/* First node of the cycle (goes from 0 to n since no valid cycle have been found) */
		int c1 = 0;

		ArrayList<Integer> cycle = new ArrayList<Integer>();
		
		/* For each node c1 while no cut has been found 
		 * try to create a cycle which starts from c1 */
		while( !cutFound && c1 < s.n()){
			
			/* Shuffled id between 0 to n-1 */
			int[] id = new int[s.n()]; 
			for(int i = 0 ; i < s.n() ; ++i)
				id[i] = i;
			id[c1] = -1;
			shuffleArray(id);
			
			/* Start the cycle with c1 */
			cycle.add(c1);
			
			/* Value of the inequality x(C) - x(Cb) */
			double cycle_value = 0.0;

			/* Next node to try adding in the cycle */
			int idNext = 0;
			
			/* Find an optimal second node to add in the cycle (it is optimal if x_c1,id[idNext] = 1) */
			boolean optimalSecondFound = false;
			
			while(!optimalSecondFound && idNext < s.n()){
				
				/* If the node is not already in the cycle */
				if(id[idNext] != -1){
					
					double value = s.x(c1,id[idNext]);
				
					if(value == 1){
						optimalSecondFound = true;
						cycle.add(id[idNext]);
						id[idNext] = -1;
						cycle_value +=value;
					}
				}

				idNext++;
			}
			
			/* Continue only if a second optimal node has been found (otherwise try the next node) */
			if(optimalSecondFound){
				
				/* cM : last node of the cycle
				 * cM1 : antepenultimate node of the cycle
				 */
				int cM = cycle.get(1);
				int cM1 = cycle.get(0);
				
				boolean processOver = false;

				/* While the process is not over.
				 * The process is over if :
				 * 	- a violated cycle have been found (a cycle which violate the corresponding two-chorded cycle inequality) -> cutFound == true
				 * 	- no valid node can be added in the cycle and it can't be closed -> cutFound == false
				 */
				while(!processOver){
				
					/* Find a valid node to add in the cycle (an optimal one if possible) 
					 * A node c is optimal if x_cM,c - x_cM1,c = 1.
					 * A node is valid if this value is > 0.
					 */
					boolean optimalNodeFound = false;
					double bestValidValue = 0;
					int bestValidIndex = -1;
					
					idNext = 0;
					
					/* While no optimal node has been found.
					 */
					while(!optimalNodeFound && idNext < s.n()){
						
						/* If the node is not already in the cycle */
						if(id[idNext] != -1){
							
							double value = s.x(cM,id[idNext]) - s.x(cM1,id[idNext]);

							/* If a better valid value is found */
							if(value > bestValidValue){
								
								bestValidIndex = idNext;
								bestValidValue = value;
								
								if(value == 1)
									optimalNodeFound = true;
							}
							
						}
							
						/***************************** */
						idNext++;
						
					} /* END : while(!optimalNeighborFound && cNext < n) */
					
					/* If we found a valid node */
					if(bestValidValue > 0){

						/* Add it in the cycle */
						cycle.add(id[bestValidIndex]);
						id[bestValidIndex] = -1;
						cycle_value += bestValidValue;
						cM1 = cM;
						cM = id[bestValidIndex];
						
						int size = cycle.size();
						
						/* If the number of nodes in the cycle is odd and >= 5 */
						if(size%2 == 1 && size >= 5){

							/* Try to close the cycle */
							int p = size / 2;
							
							/* If the inequality is violated 
							 * (i.e. if the value of the cycle is greater than <p> if we close it now) */
							if(cycle_value + s.x(cM,id[cycle.get(0)]) - s.x(cM,id[cycle.get(1)]) > p){
								processOver = true;
								cutFound = true;
							}
							
						}

						
						/* If the process is not over, start from the beginning to find another node to add  */
						if(!processOver)
							idNext = 0;
						
					}
					
					/* If no node is valid, try the next one */
					else{
						
						idNext++;

						/* If all the node have been tested and no valid cycle have been found */
						if(idNext == s.n())
							processOver = true;
						
					}
					
				} /* END : while(!processOver) */
				
			} /* END : if(validSecondFound) */
			
			c1++;
			
		} /* END : while( !cutFound && c1 < n) */
		
		ArrayList<Abstract_Inequality> r = new ArrayList<Abstract_Inequality>();
		
		if(cutFound)
			r.add(getInequality(cycle));
		
		return r;
		
	}

	/**
	 * Create the two-chorded-cycle inequality which corresponds the set <cycle>
	 * (x(C) - x(Cb) <= p)
	 * 	- C : a cycle
	 * 	- Cb : two-chorded cycle
	 * 	- |C| = 2p + 1
	 * @param cycle The cycle
	 * @return
	 */
	public Abstract_Inequality getInequality(ArrayList<Integer> cycle){

		int[] C = new int[cycle.size()];
		
		for(int i = 0 ; i < cycle.size() ; ++i)
			C[i] = cycle.get(i);
			
		TCC_Inequality tcc = new TCC_Inequality(s, cycle.size());
		tcc.C = cycle;
		
		return tcc;
		
	}

	  // Implementing Fisher–Yates shuffle
	  static void shuffleArray(int[] ar)
	  {
	    Random rnd = new Random();
	    for (int i = ar.length - 1; i >= 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	  }

	
}
