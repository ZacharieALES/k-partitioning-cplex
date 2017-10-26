package mipstart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import formulation.interfaces.IFEdgeVNodeVClusterNb;
import ilog.concert.IloException;

/**
 * Given a non integer solution, sort all the edges variables from the largest to the smallest.
 * For each edge (i,j) (starting with the largest) while less than n-K nodes have been regrouped
 * 		If i and j are not already in the same cluster
 * 			Regroup them
 * @author zach 
 *
 */
public class NMinusKFusion implements AbstractMIPStartGetter{

	int mergedNodes = 0;
	IFEdgeVNodeVClusterNb  s;
	
	public NMinusKFusion(IFEdgeVNodeVClusterNb s){
		this.s = s;
	}

	public SolutionManager getMIPStart() throws IloException{

		SolutionManager mip = new SolutionManager(s);

		TreeSet<Edge> t = new TreeSet<Edge>(new Comparator<Edge>(){


			Random random = new Random();

			@Override
			public int compare(Edge o1, Edge o2) {

				int result = 0;
				try {
					result = o2.getValue() - o1.getValue();
				} catch (IloException e) {
					e.printStackTrace();
				}

				if(result == 0){

					result = random.nextInt(3) - 1;
				}

				return result;
			}
		});

		for(int i = 0 ; i < s.n() ; ++i)
			for(int j = 0 ; j < i ; ++j)
				t.add(new Edge(i,j));

		Iterator<Edge> it = t.iterator();

		Node[] nodes = new Node[s.n()];

		for(int i = 0 ; i < s.n() ; ++i){
			nodes[i] = new Node(i);
		}

		/* Regroup the nodes until K clusters is reached */
		while(it.hasNext() && mergedNodes < s.n() - s.maximalNumberOfClusters()){
			Edge e = it.next();
			nodes[e.i].union(nodes[e.j]);
		}
		ArrayList<Integer> rep = new ArrayList<Integer>();		
		/* For each edge */
		for(int i = 0 ; i < s.n() ; ++i){

			/* Set the representative variable of the root of nodes[i] to 1 */
			mip.setRep(nodes[i].find().id, 1.0);

			for(int j = 0 ; j < i ; ++j)

				/* If they are in the same cluster */
				if(nodes[i].find() == nodes[j].find()){

					/* Set the variable x_i,j to 1 */
					mip.setEdge(i, j, 1.0);

				}

			if(!rep.contains(nodes[i].find().id))
				rep.add(nodes[i].find().id);
			System.out.println("Rep de " + i + " est " + nodes[i].find().id);
		}

		System.out.println("Rep: " + rep.toString());
		System.out.println("merged: " + mergedNodes);

		return mip;

	}


	public class Edge{

		int i;
		int j;

		public Edge(int i, int j){
			this.i = i;
			this.j = j;
		}

		/**
		 * Value of the edge in the current solution
		 */
		private int value = -1;

		public int getValue() throws IloException{

			if(value == -1)
				value  = (int)(1000 * s.variableGetter().getValue(s.edgeVar(i,j)));

			return value;
		}

	}

	public class Node{
		int id;
		Node parent = this;
		int rank = 0;

		public Node(int i){
			this.id = i;
		}


		public Node find(){

			if(parent != this)
				parent = parent.find();
			return parent;
		}

		public void union(Node e){
			System.out.println("merge: " + e.id + " " + this.id);			
			Node eRoot = e.find();
			Node nRoot = this.find();

			if(eRoot != nRoot){

				mergedNodes++;
				Node smaller = null;
				Node larger = null;

				if(eRoot.rank < nRoot.rank){
					smaller = eRoot;
					larger = nRoot;
				}
				else{
					smaller = nRoot;
					larger = eRoot;

					/* If the tree have the same size, the rank is incremented */
					if(eRoot.rank == nRoot.rank)
						larger.rank++;
				}

				smaller.parent = larger;

				/* Ensure that the lowest node is still at the top */
				if(smaller.id < larger.id){
					int temp = smaller.id;
					smaller.id = larger.id;
					larger.id = temp;
				}
			}
		}
	}
}
