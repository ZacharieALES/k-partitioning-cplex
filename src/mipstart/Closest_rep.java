package mipstart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import solution.Solution_Representative;

/**
 * Create an integer solution from a continuous solution. Find the K nodes with
 * the greatest value of their representative variables and select them as
 * representative. For each other node i put it with the representative r wich
 * maximize x_i,r
 * 
 * @author zach
 * 
 */
public class Closest_rep implements Abstract_MIPStartGetter{

	Solution_Representative s;

	public Closest_rep(Solution_Representative s) {
		this.s = s;
	}

	public MIPStart getMIPStart(){
		
		MIPStart mip = new MIPStart(s);
		
		TreeSet<Integer> t = new TreeSet<Integer>(new Comparator<Integer>(){
			

			Random random = new Random();
			double rep2 = -1;
				
	       @Override
	       public int compare(Integer o1, Integer o2) {
	    	   
	    	   int result = (int) (getRep(o2) - getRep(o1));
	    	   
	    	   if(result == 0){
	    		   if(random.nextInt(2) == 0)
	    			   result = -1;
	    		   else
	    			   result = 1;
	    	   }
	    	   
	           return result;
	       }
		           
		   public double getRep(int i){
			   
			   double result = 0;
			   
			   if(i >= 3)
				   result = s.x(i);
			   else{
				   if(i == 0)
					   result = 2;
				   else
					   if(i == 1){
						   result = 1 - s.x(0,1);
					   }
					   else if(rep2 == -1 ){
						   result = s.K() - 2 + s.x(0,1);
						   
						   for(int j = 3 ; j < s.n() ; ++j)
							   result -= s.x(j);
					   }
					   else
						   result = rep2;
			   }
			   
			   return result;
			   
		   }
		});
		
		/* Add all the nodes in the representative tree */
		for(int i = 0 ; i < s.n() ; ++i)
			t.add(i);
		
		/* Find the K representative by taking the K first values of <t> */		

		ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();
		
		Iterator<Integer> it = t.iterator();
		int addedRep = 0;
		
		while(it.hasNext() && addedRep < s.K()){
			
			int id = it.next();
			
			/* Create a new cluster with the node <id> in it */
			ArrayList<Integer> al = new ArrayList<Integer>();
			al.add(id);
			clusters.add(al);
			
			mip.setRep(id, 1.0);
			addedRep ++;
//System.out.println("is representative : " + id);			
		}
		
		
		
		/* Find for each node its cluster */	
		
		/* For each node */
		for(int i = 0 ; i < s.n() ; ++i){
			
			/* Find its best cluster */
			int bestCluster = 0;
			
			double bestValue = Double.MAX_VALUE;
			
			/* If i is not the representative of the first cluster */
			if(i != clusters.get(0).get(0))
				bestValue = s.x(i, clusters.get(0).get(0));

			/* For each cluster */
			for(int j = 0 ; j < s.K() ; ++j){
				
				/* Get the cluster */
				ArrayList<Integer> al = clusters.get(j);
				
				/* Get the representative of the cluster */
				int rep = al.get(0);
				
				/* If <i> is the representative of cluster j */
				if(i == rep){
					bestValue = Double.MAX_VALUE;
				}
				else{
					double v = s.x(i, rep);
					
					if(v > bestValue){
						bestValue = v;
						bestCluster = j;
					}
				}
			}
			
			/* If i is not the representative of a cluster */
			if(bestValue != Double.MAX_VALUE)
				
				/* Add i in the best cluster */
				clusters.get(bestCluster).add(i);
						
		}
		
		/* Set the edge variables of nodes inside the same cluster to 1 */
//System.out.println("Clusters : ");		
		/* For each cluster */
		mip.evaluation = 0.0;
		
		for(ArrayList<Integer> c : clusters){
			
//System.out.println(c.toString());
			for(int i = 0 ; i < c.size() ; ++i)
				for(int j = 0 ; j < i ; ++j){
					if(c.get(j) > c.get(i))
						mip.evaluation += s.d(c.get(j), c.get(i));
					else
						mip.evaluation += s.d(c.get(i), c.get(j));
					mip.setEdge(c.get(j), c.get(i), 1.0);
				}
		}
		
		return mip;
	}
}
