package callback.heuristic_callback;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import callback.control_callback.IControlCallback;
import formulation.PartitionXY;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.HeuristicCallback;
import ilog.cplex.IloCplex.UnknownObjectException;
import mipstart.SolutionManagerXY;
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
public class KClosestRepresentativesXY extends HeuristicCallback implements IControlCallback{

	PartitionXY formulation;
	SolutionManagerXY sm;
	private CallbackVariableGetter rvg;
	
	public static boolean onlyRoot = false;

	public KClosestRepresentativesXY(PartitionXY p) throws IloException {
		this.formulation = p;
		rvg = new CallbackVariableGetter(formulation.getCplex(), this);
	}


	@Override
	protected void main() throws IloException {
		

		if(this.getNnodes() == 0 || !onlyRoot) {

			this.sm = new SolutionManagerXY(formulation);

			/* Create empty clusters */
			ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();

			for(int i= 0 ; i < formulation.maxClusterId ; i++) {

				ArrayList<Integer> al = new ArrayList<Integer>();
				clusters.add(al);
			}

			/* Find for each node its cluster */	

			/* For each node */
			for(int i = 0 ; i < formulation.n() ; ++i){

				/* Find its best cluster */
				int bestCluster = 0;

				double bestValue = rvg.getValue(formulation.nodeInClusterVar(i, 0));

				/* For each cluster */
				for(int j = 1 ; j < formulation.maxClusterId ; ++j){

					double v = rvg.getValue(formulation.nodeInClusterVar(i, j));

					if(v > bestValue){
						bestValue = v;
						bestCluster = j;
					}
				}

				/* Add i in the best cluster */
				List<Integer> cluster = clusters.get(bestCluster);
				cluster.add(i);

			}

			avoidEmptyClusters(clusters);

			/* For each cluster */
			for(int i = 0 ; i < clusters.size() ; i++){

				List<Integer> cluster = clusters.get(i);

				/* For each node in this cluster */
				for(int j = 0 ; j < cluster.size() ; ++j) {

					int nodeJ = cluster.get(j);

					/* Set the node cluster variable to 1 */
					sm.setNC(nodeJ, i, 1.0);

					/* For each edge in the cluster */
					for(int k = 0 ; k < j ; k++) {

						/* Set the corresponding variable to 1 */
						int nodeK = cluster.get(k);
						sm.setEdge(nodeJ, nodeK, 1.0);
					}
				}

			}

			//
//			System.out.println("Found int: " + Math.round(sm.evaluate()));
			this.setSolution(sm.var, sm.val, sm.evaluate());
		}

	}


	/**
	 * For all clusters, if a cluster is empty, add a node into it.
	 * @param clusters
	 * @throws IloException 
	 * @throws UnknownObjectException 
	 */
	private void avoidEmptyClusters(ArrayList<ArrayList<Integer>> clusters) throws UnknownObjectException, IloException {

		/* List of the clusters to which we need to move a node */
		List<Integer> emptyClusters = new ArrayList<>();

		/* List of the nodes that cannot be moved */
		List<Integer> unmovableNodes = new ArrayList<>();

		/* Find the empty clusters and the nodes that cannot be moved */
		for(int i = 0 ; i < clusters.size() ; i++) {
			List<Integer> cluster = clusters.get(i);

			if(cluster.size() == 0)
				emptyClusters.add(i);

			if(cluster.size() == 1) 
				unmovableNodes.add(cluster.get(0));

		}

		/* For each empty cluster */
		for(Integer clusterId: emptyClusters) {

			int bestNodeId = -1;
			double bestValue = -Double.MAX_VALUE;

			/* For each movable node */
			for(int i = 0 ; i < formulation.n ; i++) {

				if(!unmovableNodes.contains(i)) {

					double v = rvg.getValue(formulation.nodeInClusterVar(i, clusterId));

					if(v > bestValue) {
						bestValue = v;
						bestNodeId = i;
					}
				}
			}

			/* Add the best node to the empty cluster */
			clusters.get(clusterId).add(bestNodeId);

			/* Find the cluster which contains the best node */
			boolean clusterFound = false;
			Iterator<ArrayList<Integer>> it = clusters.iterator();

			while(!clusterFound && it.hasNext()) {

				List<Integer> cluster = it.next();

				/* If the cluster contains the node that we want to move */
				if(cluster.contains(bestNodeId)) {
					clusterFound = true;

					/* Remove it */
					cluster.remove(Integer.valueOf(bestNodeId));

					/* Check that the cluster may not become empty */
					if(cluster.size() == 1)
						unmovableNodes.add(cluster.get(0));
				}


			}

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
