package mipstart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import formulation.interfaces.IFEdgeVNodeVClusterNbEdgeW;
import ilog.concert.IloException;

/**
 * Create an integer solution from a continuous solution. Find the K nodes with
 * the greatest value of their representative variables and select them as
 * representative. For each other node i put it with the representative r wich
 * maximize x_i,r
 * 
 * @author zach
 * 
 */
public class ClosestRep implements AbstractMIPStartGetter{

	IFEdgeVNodeVClusterNbEdgeW formulation;

	public ClosestRep(IFEdgeVNodeVClusterNbEdgeW s) {
		this.formulation = s;
	}

	public SolutionManagerRepresentative getMIPStart() throws IloException {

		SolutionManagerRepresentative mip = new SolutionManagerRepresentative(formulation);

		//		formulation.displaySolution();

		TreeSet<Integer> t = new TreeSet<Integer>(new Comparator<Integer>(){


			Random random = new Random();
			double rep2 = -1;

			@Override
			public int compare(Integer o1, Integer o2) {

				int result = 0;
				try {
					result = (int) (getRep(o2) - getRep(o1));
				} catch (IloException e) {
					e.printStackTrace();
				}

				if(result == 0){
					if(random.nextInt(2) == 0)
						result = -1;
					else
						result = 1;
				}

				return result;
			}

			public double getRep(int i) throws IloException {
				return formulation.variableGetter().getValue(formulation.nodeVar(i));
			}
		});

		/* Add all the nodes in the representative tree */
		for(int i = 0 ; i < formulation.n() ; ++i)
			t.add(i);

		/* Find the K representative by taking the K first values of <t> */		

		ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();

		Iterator<Integer> it = t.iterator();
		int addedRep = 0;

		List<Integer> representative = new ArrayList<>();
		
		while(it.hasNext() && addedRep < formulation.maximalNumberOfClusters()){

			int id = it.next();

			/* Create a new cluster with the node <id> in it */
			ArrayList<Integer> al = new ArrayList<Integer>();
			al.add(id);
			clusters.add(al);
			
			representative.add(id);

			addedRep ++;
			//			System.out.println("is representative : " + id);			
		}



		/* Find for each node its cluster */	

		/* For each node */
		for(int i = 0 ; i < formulation.n() ; ++i){

			/* Find its best cluster */
			int bestCluster = 0;

			double bestValue = Double.MAX_VALUE;

			/* If i is not the representative of the first cluster */
			if(i != clusters.get(0).get(0))
				bestValue = formulation.variableGetter().getValue(formulation.edgeVar(i, clusters.get(0).get(0)));


			/* For each cluster */
			for(int j = 0 ; j < formulation.maximalNumberOfClusters() ; ++j){

				/* Get the cluster */
				ArrayList<Integer> al = clusters.get(j);

				/* Get the representative of the cluster */
				int rep = al.get(0);

				/* If <i> is the representative of cluster j */
				if(i == rep){
					bestValue = Double.MAX_VALUE;
				}
				else{
					double v = formulation.variableGetter().getValue(formulation.edgeVar(i, rep));

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

			/* Update the representative if necessary */
			if(representative.get(bestCluster) > i)
				representative.set(bestCluster, i);

		}

		/* For each cluster */
		for(int i = 0 ; i < clusters.size() ; i++){

			/* Get its representative ... */
			int rep = representative.get(i);
			
			/* ... and set it */
			mip.setRep(rep, 1.0);

		}

		/* Set the edge variables of nodes inside the same cluster to 1 */
//		System.out.println("Clusters : ");		

		mip.evaluate();

		/* For each cluster */
		for(ArrayList<Integer> c : clusters){
//			System.out.println(c.toString());
			for(int i = 0 ; i < c.size() ; ++i)
				for(int j = 0 ; j < i ; ++j){
					mip.setEdge(c.get(j), c.get(i), 1.0);
				}
		}

		return mip;
	}
}
