package formulation;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.IntParam;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.Range;
import inequality_family.Triangle_Inequality_x_y1;
import inequality_family.Triangle_Inequality_x_y2;
import solution.Solution_Representative;

/**
 * The difference with Partition_x_y_2 is that here the y variables vary 1 to K for the second index.
 *  
 * Formulation of the problem which consist in minimizing the sum of the edge
 * weight in a K-partition
 * 
 * Notation : V : set of vertices ({1, 2, ..., n}) n : number of vertices (=
 * |V|) K : number of clusters
 * 
 * Variables : y_i_j = 1 if i is in cluster j (for i in V and j in {1,...,K})
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
public class Partition_x_y extends Partition implements Solution_Representative {

	public String dissimilarity_file = "empty_file";

	/**
	 * If true, we search integer points; otherwise search for fractional points
	 */
	public boolean isInt;

	public CplexParam cp;
	public Param xyp;

	/**
	 * Node/Cluster variables.
	 */
	public IloNumVar[][] v_nodeCluster;

	public Partition_x_y(int K, String dissimilarity_file, CplexParam cp,
			Param xyp) {

		this(K, dissimilarity_file, -1, cp, xyp);
			
	}
	


	public Partition_x_y(int K, String dissimilarity_file,
			int max_number_of_nodes, CplexParam cp, Param xyp) {

		this(K, readDissimilarityInputFile(dissimilarity_file,
				max_number_of_nodes,xyp.gapDiss), cp, xyp);
		this.dissimilarity_file = dissimilarity_file;

	}

	public Partition_x_y(int K, double objectif[][], CplexParam cp, Param xyp) {

		this.d = objectif;
		this.n = d.length;

		this.isInt = xyp.isInt;

		this.cp = cp;
		this.xyp = xyp;

		if (!cp.output)
			turnOffCPOutput();

		if (cp.autoCuts)
			removeAutomaticCuts();

		if (cp.primalDual)
			turnOffPrimalDualReduction();

		try {

			this.K = K;

			/* Create the model */
			cplex.clearModel();
			cplex.clearCallbacks();

			/* Reinitialize the parameters to their default value */
			cplex.setDefaults();

			if (cp.tilim != -1)
				cplex.setParam(IloCplex.DoubleParam.TiLim,
						Math.max(10, cp.tilim));

			// cplex.setParam(DoubleParam.WorkMem, 2000);
			// cplex.setParam(DoubleParam.TreLim, 500);
//			 cplex.setParam(IntParam.NodeFileInd, 3);

			/* Create the variables */
			createVariables();
			createObjectiveFunction();
			createConstraints();

			// Turn off preprocessing
			// cplex.setParam(IloCplex.BooleanParam.PreInd, false);

		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
			e.printStackTrace();
			System.exit(0);
		}
	}


	private void createConstraints() throws IloException {

		createUniqueClusterConstraints();
		createNonEmptyClusterConstraints();
		createTriangleConstraints();
		createNonSymmetricConstraints();
		
		if(xyp.useNN_1)
			createNN_1Constraints();

	}
	
	
	public void createNN_1Constraints(){

		try {
			int d = (int) Math.floor((n-1)/K);
			int mo = (n-1)%K;
			int n1 = (d+1) * d / 2;
			int n2 = d * (d-1) / 2;
			int righthand = n1 * mo + n2 * (K-mo);
			
			for(int j = 0 ; j < n ; ++j){
				IloLinearNumExpr expr;
					expr = cplex.linearNumExpr();
	
				for(int l = 0 ; l < n ; ++l)
					if(l != j)
						for(int m = l+1 ; m < n ; ++m){
							if(m != j)
								expr.addTerm(+1.0, v_edge[l][m]);
						}
			
				cplex.addGe(expr, righthand);
	
			}
	
			d = (int) Math.floor((n)/K);
			mo = (n)%K;
			n1 = (d+1) * d / 2;
			n2 = d * (d-1) / 2;
			righthand = n1 * mo + n2 * (K-mo);
			
			IloLinearNumExpr expr = cplex.linearNumExpr();
			for(int l = 0 ; l < n ; ++l)
				for(int m = l+1 ; m < n ; ++m){
					expr.addTerm(+1.0, v_edge[l][m]);
					}
			
			cplex.addGe(expr, righthand);

		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	private void createNonSymmetricConstraints() {
		
		for(int i = 0 ; i < K() ; ++i){
			for(int j = i+1 ; j < K() ; ++j){

				try {
					IloLinearNumExpr expr = linearNumExpr();
					expr.addTerm(+1.0, y_var(i, j));
					addRange(new Range(expr, 0.0));
				} catch (IloException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
				
		
	}

	private void createUniqueClusterConstraints() {

	//			addRange(new UniqueCluster_Inequality(this, i).createRange());
		
		try {
			for (int i = 0; i < n; ++i) {
				
				IloLinearNumExpr expr = linearNumExpr();
				for(int j = 0 ; j < K() ; ++j)
						expr.addTerm(+1.0, y_var(i, j));
				
				addRange(new Range(1.0, expr, 1.0));
					
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void createNonEmptyClusterConstraints() {

	//			addRange(new UniqueCluster_Inequality(this, i).createRange());
		
		try {
			for(int j = 0 ; j < K() ; ++j){
				
				IloLinearNumExpr expr = linearNumExpr();
			
				for (int i = 0; i < n; ++i) {
						expr.addTerm(+1.0, y_var(i, j));
				}
				
				addRange(new Range(1.0, expr));
					
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Add triangular constraints : xi,j + xi,k - yj,k <= 1 - if i is with j and
	 * k, then j and k are together
	 * 
	 * @param solution
	 * 
	 * @throws IloException
	 */
	void createTriangleConstraints() throws IloException {

		for (int i = 0; i < n ; ++i)
			for (int j = i + 1; j < n ; ++j)
				for (int k = 0; k < K(); ++k) {


					addRange(new Triangle_Inequality_x_y1(this, k, i, j)
							.createRange());

					addRange(new Triangle_Inequality_x_y2(this, k, i, j)
							.createRange());
					

					addRange(new Triangle_Inequality_x_y2(this, k, j, i)
							.createRange());
				}

	}

	private void createObjectiveFunction() throws IloException {

		IloLinearNumExpr obj = cplex.linearNumExpr();

		for (int i = 1; i < n; ++i)
			for (int j = 0; j < i; ++j)
				obj.addTerm(d[i][j], v_edge[i][j]);

		cplex.addMinimize(obj);
	}

	private void createVariables() throws IloException {

		if (this.isInt) {
			v_edge = new IloIntVar[n][];
			v_nodeCluster = new IloIntVar[n][];
		} else {
			v_edge = new IloNumVar[n][];
			v_nodeCluster = new IloNumVar[n][];
		}

		for (int i = 0; i < n; ++i) {
			if (isInt) {
				v_nodeCluster[i] = new IloIntVar[K()];
				v_edge[i] = new IloIntVar[n];
			} else {
				v_nodeCluster[i] = new IloNumVar[K()];
				v_edge[i] = new IloNumVar[n];
			}

			cplex.conversion(v_edge[i], IloNumVarType.Float);
			cplex.conversion(v_nodeCluster[i], IloNumVarType.Float);

			for (int k = 0; k < K(); ++k) {
				if (isInt)
					v_nodeCluster[i][k] = cplex.intVar(0, 1);
				else
					v_nodeCluster[i][k] = cplex.numVar(0, 1);

				v_nodeCluster[i][k].setName("y_" + i + "_" + k);

			}

			for (int j = 0; j < i; ++j) {
				if (isInt)
					v_edge[i][j] = cplex.intVar(0, 1);
				else
					v_edge[i][j] = cplex.numVar(0, 1);

				v_edge[i][j].setName("x_" + i + "_" + j);
			}

		}

		/* Link the symetric variables to their equivalent in the lower triangular part of v_edge
		 * Ex : v[1][0] = v[0][1]
		 */
		for (int i = 0 ; i < n; ++i){
			
			for(int j = i+1 ; j < n ; ++j)
				v_edge[i][j] = v_edge[j][i];
		}

	}

	@Override
	public void displaySolution() throws UnknownObjectException, IloException {
		System.out.println("Edges variables");
		displayEdgeVariables(6);
		displayYVariables(6);
	}

	/**
	 * Display the value of the edge variables which are equal to 1
	 * 
	 * @param numberOfElementsByLine
	 *            Number of variables displayed by line
	 * @throws UnknownObjectException
	 * @throws IloException
	 */
	public void displayYVariables(int numberOfElementsByLine)
			throws UnknownObjectException, IloException {

		int i = 0; // Index of the edge first node
		int j = 0; // Index of the edge first cluster

		/* While all the edges variables have not been displayed */
		while (i != n) {

			int k = 0; // Id of the last element displayed on the line

			/*
			 * While the line is not over (i.e. while it does not contain l
			 * elements or reach the last edge variable)
			 */
			while (k < numberOfElementsByLine && i != n) {

				double value = cplex.getValue(v_nodeCluster[i][j]);
//System.out.println(i + " : " + j);
System.out.print("cluster " + j + ": " + i + "(" + value + ")\t\t");

				/* If the value is not zero (or very close) */
				if ( !(value < 0.0+epsilon)) {
					/* If the value is one (or very close) */
					if (value > 1-epsilon) {

//						System.out.print("cluster " + j + ": " + i + "\t\t");
						++k;

					} else
						System.err.println("Error the edge variable " + j + "-"
								+ i + " is not equal to 0 or 1 (value " + value
								+ ")");

				}

				if (j != K()-1)
					++j;
				else {
					++i;
					j = 0;
				}
			}

			System.out.println(" ");
		}

	}

	
	@Override
	public int n() {
		return n();
	}

	@Override
	public int K() {
		return this.K;
	}

	@Override
	public double xt(int i, int j) {
		return 0;
	}

	@Override
	public double x(int i, int j) {
		try {
			return cplex.getValue(x_var(i, j));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("(i,j): (" + i + "," + j + ")");
			System.exit(0);
			return 1.0;
		}
	}

	@Override
	public double x(int i) {
		return 0;
	}

	@Override
	public IloLinearNumExpr linearNumExpr() {
		try {
			return cplex.linearNumExpr();
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IloNumVar xt_var(int i, int j) {
		return null;
	}

	@Override
	public IloNumVar x_var(int i, int j) {
		return v_edge[i][j];
	}

	@Override
	public IloNumVar x_var(int i) {
		return null;
	}

	@Override
	public IloNumVar y_var(int i, int j) {
		return v_nodeCluster[i][j];
	}

	@Override
	public double getBestObjValue2() {
		try {
			return cplex.getBestObjValue();
		} catch (IloException e) {
			e.printStackTrace();
			return -1.0;
		}
	}

	@Override
	public double getObjValue2() {
		try {
			return cplex.getObjValue();
		} catch (IloException e) {
			e.printStackTrace();
			return -1.0;
		}
	}

	@Override
	public boolean isTilde() {
		return false;
	}

	@Override
	public double d(int i, int j) {
		return d[i][j];
	}

	@Override
	public double y(int i, int j) {
		try {
			return cplex.getValue(y_var(i, j));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("(i,j): (" + i + "," + j + ")");
			System.exit(0);
			return 1.0;
		}
	}
	

	/**
	 * Display the value of the node-cluster variables which are equal to 1
	 * 
	 * @param numberOfElementsByLine
	 *            Number of variables displayed by line
	 * @throws UnknownObjectException
	 * @throws IloException
	 */
	public void displayNodeClusterVariables(int numberOfElementsByLine)
			throws UnknownObjectException, IloException {

		int i = 0; // Index of the edge first node
		int j = 0; // Index of the edge second node

		/* While all the node-cluster variables have not been displayed */
		while (i != n) {

			int k = 0; // Id of the last element displayed on the line

			/*
			 * While the line is not over (i.e. while it does not contain l
			 * elements or reach the last edge variable)
			 */
			while (k < numberOfElementsByLine && i != n) {

				double value = cplex.getValue(v_nodeCluster[i][j]);


if(value > 1E-4){
System.out.print("y" + i + "-" + j + "(" + value + ")\t\t");
++k;
}

//				/* If the value is not zero (or very close) */
//				if ( !(value < 0.0+epsilon)) {
//					/* If the value is one (or very close) */
//					if (value > 1-epsilon) {
//
//						System.out.print(i + "-" + j + "\t\t");
//						++k;
// 
//					} else
//						System.err.println("Error the node-cluster variable " + j + "-"
//								+ i + " is not equal to 0 or 1 (value " + value
//								+ ")");
//
//				}

				if (j != K() - 1)
					++j;
				else {
					++i;
					j = 0;
				}

			}

			System.out.println(" ");
		}
	}
	

	public void displayNonNegativeCoefficientSolution() {

		try {

			double obj = 0.0;
			/*
			 * Display the representative variables different from 0 (<l> by
			 * line)
			 */
			System.out.println(" ");
			System.out.println("XY variables");
			
			double value = 0.0;
			for (int m = 0; m < n ; ++m){
				for(int o = 0 ; o < K ; ++o){
					
					value = cplex.getValue(this.v_nodeCluster[m][o]);
					if(value > 0.0 + epsilon){
						System.out.println(m+ "," + o + " = " + value);
					}
				}
			}

			/* Display the edge variables different from 0 (<l> by line) */
			System.out.println(" ");
			System.out.println("Edges variables");

			/* While all the edges variables have not been displayed */
			for(int i = 1 ; i < n ; ++i)
				for(int j = 0 ; j < i ; ++j){

					value = cplex.getValue(v_edge[i][j]);

					if(value > 0.0 + epsilon){
						System.out.println(i + "-" + j + " : " + value);
						if(value == 1.0){
							obj += d[i][j];
						}
					}
					
				}
			
			System.out.println("Objective: "+ obj);
					
				System.out.println(" ");
			
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
		}
		
	}


}
