package formulation;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.Range;

/**
 * The difference with Partition_x_y is that y variables vary from 1 to n for the first and the second index.
 *  
 * Formulation of the K-partitioning problem which consist in minimizing the sum of the edge
 * weight in a K-partition
 * 
 * Notation : V : set of vertices ({1, 2, ..., n}) n : number of vertices (=
 * |V|) K : number of clusters
 * 
 * Variables : y_i_j = 1 if i is in cluster j (for i and j in V)
 * x_i_j = 1 if i and j are in the same cluster
 * 
 * Constraints : sum_j y_i_j = 1 (each vertex i is in exactly one cluster) y_i_j
 * + y_k_j - x_i_k <= 1 (if i and k are in cluster j then x_i_k = 1) (triangle
 * inequalities)
 * 
 * Note : The variables y_i_j for i < j can be set to zero (remove some
 * symmetry)
 * 
 * @author zach
 * 
 */
public class Partition_x_y_2 extends Partition_x_y {

	/**
	 * If true, we search integer points; otherwise search for fractional points
	 */
	public boolean isInt;

	/**
	 * Node/Cluster variables.
	 */
	public IloNumVar[][] v_nodeCluster;	


	public Partition_x_y_2(XYParam xyp) {
		this(readDissimilarityInputFile(xyp), xyp);
	}

	public Partition_x_y_2(double objectif[][], XYParam xyp) {
		super(objectif, xyp);
	}
	
	@Override
	protected void setMaxClusterId() {
		maxClusterId = n;
	}


	@Override
	protected void createConstraints() throws IloException {

		createUniqueClusterConstraints();
		createExactlyKClusterConstraints();
		createTriangleConstraints();
		createNonSymmetricConstraints();

		if(p.useNN_1)
			createNN_1Constraints();

	}

	private void createNonSymmetricConstraints() {
		
		for(int i = 0 ; i < maxClusterId ; ++i){
			for(int j = i+1 ; j < maxClusterId ; ++j){

				try {
					IloLinearNumExpr expr = getCplex().linearNumExpr();
					expr.addTerm(+1.0, nodeInClusterVar(i, j));
					getCplex().addRange(new Range(expr, 0.0));
				} catch (IloException e) {
					e.printStackTrace();
				}
			}

			for(int j = 0 ; j <= i-1 ; ++j){

				try {
					IloLinearNumExpr expr = getCplex().linearNumExpr();
					expr.addTerm(+1.0, nodeInClusterVar(i, j));
					expr.addTerm(-1.0, nodeInClusterVar(j, j));
					getCplex().addRange(new Range(expr, 0.0));
				} catch (IloException e) {
					e.printStackTrace();
				}
				
			}
			
		}
				
		
	}

	private void createExactlyKClusterConstraints() {
		
		try {
			
			IloLinearNumExpr expr = getCplex().linearNumExpr();

			for(int i = 0 ; i < n ; ++i){
				expr.addTerm(+1.0, nodeInClusterVar(i, i));
//				System.out.print("y" + i +"," + i + " + ");
			}
			
//			System.out.println("= " + K);
				
			getCplex().addRange(new Range(p.KMax, expr, p.KMax));
			
		} catch (IloException e) {
			e.printStackTrace();
		}

	}
	


}
