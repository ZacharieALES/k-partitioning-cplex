package mipstart;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import cutting_plane.Abstract_Cutting_Plane.CP_Separation;
import formulation.PartitionWithRepresentative;
import formulation.Partition_with_tildes;
import formulation.RepParam;
import formulation.TildeParam;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import inequality_family.Abstract_Inequality;
import inequality_family.Range;
import solution.Solution_Representative;

/**
 * Create an integer solution from a continuous solution. Find the K nodes with the greatest value of their representative variables and select them as representative.
 * Then repeat :
 * 
 * 		- For each edge between a representative <r> and a node <i> which is not yet assigned in a cluster : if x_r,i == 1, add i in the cluster of <r>
 * 		- If no such edge is found, merge the highest edge x_r,i
 * 		- If there is still unassigned nodes, solve the relaxation
 * 
 *  Until all the nodes are not assigned
 * @author zach
 *
 */
public class Rep_then_relaxations implements Abstract_MIPStartGetter{

	Solution_Representative s;

	public boolean[] clusterAssigned;
	public ArrayList<ArrayList<Integer>> clusters = new ArrayList<ArrayList<Integer>>();

	/**
	 * Inequalities added to the formulation (e.g. during a cutting plane)
	 */
	public ArrayList<CP_Separation> sep_algo;

	/**
	 * Parameters of the formulation
	 */
	RepParam param;

	/**
	 * Coefficients of the obecjtive function of the current problem 
	 */
	double[][] objective;

	/**
	 * Number of nodes which are not assigned to a cluster
	 */
	public int unassignedNodes;


	public Rep_then_relaxations(Solution_Representative s, ArrayList<CP_Separation> sep_algo, RepParam param, double[][] objective) {
		this.s = s;
		clusterAssigned = new boolean[s.n()];

		this.sep_algo = sep_algo;
		this.param = param;

		this.objective = objective;
	}

	@Override
	public MIPStart getMIPStart() throws IloException {

		MIPStart mip = new MIPStart(s);

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

			public double getRep(int i) throws IloException{

				double result = 0;

				if(i >= 3)
					result = s.x(i);
				else{
					if(i == 0)
						result = 1;
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

		clusters = new ArrayList<ArrayList<Integer>>();
		clusterAssigned = new boolean [s.n()];

		for(int i = 0 ; i < s.n(); ++i)
			clusterAssigned[i] = false;

		/* Number of nodes which are not assigned to a cluster */
		unassignedNodes = s.n();


		Iterator<Integer> it = t.iterator();
		int addedRep = 0;

		System.out.print("Rep: ");
		while(it.hasNext() && addedRep < s.K()){

			int id = it.next();
			System.out.print(id + " ");
			/* Create a new cluster with the node <id> in it */
			ArrayList<Integer> al = new ArrayList<Integer>();
			al.add(id);
			clusters.add(al);

			clusterAssigned[id] = true;

			mip.setRep(id, 1.0);
			addedRep ++;
			unassignedNodes--;

		}
		System.out.println("");		

		assignEdges(s);

		while(unassignedNodes > 0){

			PartitionWithRepresentative p = this.createPartition();
			p.solve();

			assignEdges(p);		

		}

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

	/**
	 * Given a non integer solution and a set of incomplete clusters.
	 * Set unassigned nodes in a cluster.
	 * @param s2 Non integer solution
	 * @throws IloException 
	 */
	private void assignEdges(Solution_Representative s2) throws IloException {

		double max_edge = -1.0;
		int max_node = -1;
		int max_cluster = -1;
		boolean assignmentDone = false;

		/* For each node which is not assigned to a cluster yet */
		for(int i = 0 ; i < s2.n() ; ++i)

			if(!clusterAssigned[i]){

				int j = 0;

				while(!clusterAssigned[i] && j < s2.K()){

					double value = s2.x(i, clusters.get(j).get(0));

					if(value > 1.0 - 1E-6){
						clusters.get(j).add(i);
						clusterAssigned[i] = true;
						assignmentDone = true;
						unassignedNodes--;
					}

					if(value > max_edge){
						max_edge = value;
						max_node = i;
						max_cluster = j;
					}

					++j;
				}

			}


		/* If no assignment was done, let x_i,r be the maximum edge such that :
		 * 	- i is a unassigned node
		 * 	- r is a representative
		 * Add i in the cluster of r
		 */
		if(!assignmentDone && max_edge != -1.0){
			clusters.get(max_cluster).add(max_node);
			clusterAssigned[max_node] = true;
			unassignedNodes--;
		}

		if(max_edge == -1.0){
			System.out.println("Error no more nodes to assign");
			System.exit(0);
		}

	}

	public PartitionWithRepresentative createPartition(){

		PartitionWithRepresentative p = null;
		param.cplexPrimalDual = false;
		param.cplexAutoCuts = false;
		param.tilim = 3600;
		param.K = s.K();

		if(this.param instanceof TildeParam)
			p = new Partition_with_tildes(objective, (TildeParam) param);
		else
			p = new PartitionWithRepresentative(objective, param);

		/* Add the original inequalities */ 
		for(CP_Separation sep : this.sep_algo)
			for(Abstract_Inequality ineq : sep.addedIneq){
				p.addRange(ineq.createRange());
			}

		/* Add the inequalities which correspond to x_i,j = 1 for each i and j in the same cluster */
		for(ArrayList<Integer> cluster : clusters)
			for(int i = 0 ; i < cluster.size() ; ++i)
				for(int j = 0 ; j < i ; ++j)
					p.addRange(new VarTo1Inequality(p, i, j).createRange());

		return p;
	}


	/**
	 * Inequality x_i,j >= 1
	 * @author zach
	 *
	 */
	private class VarTo1Inequality extends Abstract_Inequality{

		int i;
		int j;

		public VarTo1Inequality(Solution_Representative s, int i, int j) {
			super(s);
			this.i = i;
			this.j = j;
		}

		@Override
		public Range createRange() {

			try{

				IloLinearNumExpr expr = s.linearNumExpr();
				expr.addTerm(+1.0, this.s.x_var(i,j));

				return new Range(1.0, expr);

			}catch(IloException e){
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public Abstract_Inequality clone() {
			return new VarTo1Inequality(s, i, j);
		}

		@Override
		public double evaluate() throws IloException {
			return this.s.x(i,j);
		}

		@Override
		public double getSlack() throws IloException {
			return this.evaluate() - 1.0;
		}

		@Override
		public boolean useTilde() {
			return false;
		}

	}

}
