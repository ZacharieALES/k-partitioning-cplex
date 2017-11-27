package callback.heuristic_callback;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import callback.control_callback.IControlCallback;
import formulation.PartitionWithTildes;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.HeuristicCallback;
import mipstart.SolutionManagerTildes;
import variable.CallbackVariableGetter;
import variable.VariableGetter;

/**
 * Create an integer solution from a continuous solution. Find the K nodes with
 * the greatest value of their representative variables and select them as
 * representative. For each other node i put it with the representative r wich
 * maximize x_i,r
 * 
 * @author zach
 * 
 */
public class KClosestRepresentativesTildes extends HeuristicCallback implements IControlCallback{

	PartitionWithTildes formulation;
	SolutionManagerTildes sm;
	private CallbackVariableGetter rvg;

	public static boolean onlyRoot = true;

	public KClosestRepresentativesTildes(PartitionWithTildes p) throws IloException {
		this.formulation = p;
		rvg = new CallbackVariableGetter(formulation.getCplex(), this);
	}


	@Override
	protected void main() throws IloException {

		if(!onlyRoot || this.getNnodes() == 0) {
			this.sm = new SolutionManagerTildes(formulation);

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
					return rvg.getValue(formulation.nodeVar(i));
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

			while(it.hasNext() && addedRep < formulation.KMax()){

				int id = it.next();

				/* Create a new cluster with the node <id> in it */
				ArrayList<Integer> al = new ArrayList<Integer>();
				al.add(id);
				clusters.add(al);

				representative.add(id);

				addedRep ++;
				//System.out.println("is representative : " + id);			
			}



			/* Find for each node its cluster */	

			/* For each node */
			for(int i = 0 ; i < formulation.n() ; ++i){

				/* Find its best cluster */
				int bestCluster = 0;

				double bestValue = Double.MAX_VALUE;

				/* If i is not the representative of the first cluster */
				if(i != clusters.get(0).get(0))
					bestValue = rvg.getValue(formulation.edgeVar(i, clusters.get(0).get(0)));


				/* For each cluster */
				for(int j = 0 ; j < formulation.KMax() ; ++j){

					/* Get the cluster */
					ArrayList<Integer> al = clusters.get(j);

					/* Get the representative of the cluster */
					int rep = al.get(0);

					/* If <i> is the representative of cluster j */
					if(i == rep){
						bestValue = Double.MAX_VALUE;
					}
					else{
						double v = rvg.getValue(formulation.edgeVar(i, rep));

						if(v > bestValue){
							bestValue = v;
							bestCluster = j;
						}
					}
				}

				/* If i is not the representative of a cluster */
				if(bestValue != Double.MAX_VALUE) {

					/* Add i in the best cluster */
					List<Integer> cluster = clusters.get(bestCluster);
					cluster.add(i);

					/* Update the representative if necessary */
					if(representative.get(bestCluster) > i)
						representative.set(bestCluster, i);

				}

			}

			/* For each cluster */
			for(int i = 0 ; i < clusters.size() ; i++){

				List<Integer> cluster = clusters.get(i);

				/* Get its representative ... */
				int rep = representative.get(i);

				/* ... and set it */
				sm.setRep(rep, 1.0);

				it = cluster.iterator();

				/* For each other element of the cluster */			
				while(it.hasNext()) {

					int node = it.next();

					if(!(node == rep))

						/* Set corresponding node cluster variable to 1 */
						sm.setNC(node, rep, 1.0);
				}


			}


			/* Set the edge variables of nodes inside the same cluster to 1 */
			//		System.out.println("Clusters : ");		
			/* For each cluster */

			for(ArrayList<Integer> c : clusters){

				//			System.out.println(c.toString());
				for(int i = 0 ; i < c.size() ; ++i)
					for(int j = 0 ; j < i ; ++j){

						//					System.out.println(c.get(j) + " - " + c.get(i));


						sm.setEdge(c.get(j), c.get(i), 1.0);
					}
			}
			//
			//		System.out.println("Found int: " + Math.round(sm.evaluate()) + " (" + Math.round(objective) + ")");

			this.setSolution(sm.var, sm.val, sm.evaluate());
		}

	}


	@Override
	public double getBestObjValuePublic() throws IloException {
		return this.getBestObjValue();
	}

	@Override
	public double getObjValuePublic() throws IloException {
		return this.getObjValue();
	}

	@Override
	public double getValuePublic(IloNumVar var) throws IloException{
		return this.getValue(var);
	}


	@Override
	public VariableGetter variableGetter() {
		return rvg;
	}
}
