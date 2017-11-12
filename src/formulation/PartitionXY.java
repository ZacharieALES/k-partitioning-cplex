package formulation;

import callback.cut_callback.AbstractCutCallback;
import formulation.interfaces.IFEdgeVNodeClusterV;
import formulation.interfaces.IFNodeClusterV;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.Range;
import inequality_family.TriangleInequalityXY1;
import inequality_family.TriangleInequalityXY2;
import variable.CplexVariableGetter;

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
public class PartitionXY extends Partition implements IFEdgeVNodeClusterV{

	/**
	 * If true, we search integer points; otherwise search for fractional points
	 */
	public boolean isInt;
	
	public int maxClusterId;

	/**
	 * Node/Cluster variables.
	 */
	public IloNumVar[][] v_nodeCluster;

	public PartitionXY(XYParam xyp) {
		this(readDissimilarityInputFile(xyp), xyp);
	}

	public PartitionXY(double objectif[][], XYParam xyp) {

		super(xyp);
		
		this.d = objectif;
		this.n = d.length;
		setMaxClusterId();

		this.isInt = xyp.isInt;

		this.p = new XYParam(xyp);

		if (!xyp.cplexOutput)
			getCplex().turnOffCPOutput();

		if (xyp.cplexAutoCuts)
			getCplex().removeAutomaticCuts();

		if (xyp.cplexPrimalDual)
			getCplex().turnOffPrimalDualReduction();

		try {

			/* Create the model */
			getCplex().iloCplex.clearModel();
			getCplex().iloCplex.clearCallbacks();

			/* Reinitialize the parameters to their default value */
			getCplex().setDefaults();

			if (xyp.tilim != -1)
				getCplex().setParam(IloCplex.DoubleParam.TiLim,
						Math.max(10, xyp.tilim));

			// cplex.setParam(DoubleParam.WorkMem, 2000);
			// cplex.setParam(DoubleParam.TreLim, 500);
			// cplex.setParam(IntParam.NodeFileInd, 2);

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


	protected void setMaxClusterId() {
		maxClusterId = KMax();
	}

	protected void createConstraints() throws IloException {

		createUniqueClusterConstraints();
		createNonEmptyClusterConstraints();
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
				
		
	}

	protected void createUniqueClusterConstraints() {

	//			addRange(new UniqueCluster_Inequality(this, i).createRange());
		
		try {
			for (int i = 0; i < n; ++i) {
				
				IloLinearNumExpr expr = getCplex().linearNumExpr();
				for(int j = 0 ; j < this.maxClusterId ; ++j)
						expr.addTerm(+1.0, nodeInClusterVar(i, j));
				
				getCplex().addRange(new Range(1.0, expr, 1.0));
					
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void createNonEmptyClusterConstraints() {

	//			addRange(new UniqueCluster_Inequality(this, i).createRange());
		
		try {
			for(int j = 0 ; j < KMax() ; ++j){
				
				IloLinearNumExpr expr = getCplex().linearNumExpr();
			
				for (int i = 0; i < n; ++i) {
						expr.addTerm(+1.0, nodeInClusterVar(i, j));
				}
				
				getCplex().addRange(new Range(1.0, expr));
					
			}
		} catch (IloException e) {
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
				for (int k = 0; k < maxClusterId; ++k) {


					getCplex().addRange(new TriangleInequalityXY1(this, k, i, j)
							.createRange());

					getCplex().addRange(new TriangleInequalityXY2(this, k, i, j)
							.createRange());
					

					getCplex().addRange(new TriangleInequalityXY2(this, k, j, i)
							.createRange());
				}

	}

	private void createObjectiveFunction() throws IloException {

		IloLinearNumExpr obj = getCplex().linearNumExpr();

		for (int i = 1; i < n; ++i)
			for (int j = 0; j < i; ++j)
				obj.addTerm(d[i][j], v_edge[i][j]);

		getCplex().iloCplex.addMinimize(obj);
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
				v_nodeCluster[i] = new IloIntVar[maxClusterId];
				v_edge[i] = new IloIntVar[n];
			} else {
				v_nodeCluster[i] = new IloNumVar[maxClusterId];
				v_edge[i] = new IloNumVar[n];
			}

			getCplex().iloCplex.conversion(v_edge[i], IloNumVarType.Float);
			getCplex().iloCplex.conversion(v_nodeCluster[i], IloNumVarType.Float);

			for (int k = 0; k < maxClusterId; ++k) {
				if (isInt)
					v_nodeCluster[i][k] = getCplex().iloCplex.intVar(0, 1);
				else
					v_nodeCluster[i][k] = getCplex().iloCplex.numVar(0, 1);

				v_nodeCluster[i][k].setName("y_" + i + "_" + k);

			}

			for (int j = 0; j < i; ++j) {
				if (isInt)
					v_edge[i][j] = getCplex().iloCplex.intVar(0, 1);
				else
					v_edge[i][j] = getCplex().iloCplex.numVar(0, 1);

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

				double value = cvg.getValue(v_nodeCluster[i][j]);
//System.out.println(i + " : " + j);
System.out.print("cluster " + j + ": " + i + "(" + value + ")\t\t");

				/* If the value is not zero (or very close) */
				if ( !(value < 0.0+getCplex().PRECISION)) {
					/* If the value is one (or very close) */
					if (value > 1-getCplex().PRECISION) {

//						System.out.print("cluster " + j + ": " + i + "\t\t");
						++k;

					} else
						System.err.println("Error the edge variable " + j + "-"
								+ i + " is not equal to 0 or 1 (value " + value
								+ ")");

				}

				if (j != maxClusterId-1)
					++j;
				else {
					++i;
					j = 0;
				}
			}

			System.out.println(" ");
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

				double value = cvg.getValue(v_nodeCluster[i][j]);


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

				if (j != maxClusterId - 1)
					++j;
				else {
					++i;
					j = 0;
				}

			}

			System.out.println(" ");
		}
	}


	
	public void displayAllCoefficientSolution() {

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
				for(int o = 0 ; o < maxClusterId ; ++o){
					value = cvg.getValue(this.v_nodeCluster[m][o]);
					System.out.println(m+ "," + o + " = " + value);
				}
			}

			/* Display the edge variables different from 0 (<l> by line) */
			System.out.println(" ");
			System.out.println("Edges variables");

			/* While all the edges variables have not been displayed */
			for(int i = 1 ; i < n ; ++i)
				for(int j = 0 ; j < i ; ++j){

					value = cvg.getValue(v_edge[i][j]);
					
					System.out.println(i + "-" + j + " : " + value);
					if(value == 1.0){
						obj += d[i][j];
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
				for(int o = 0 ; o < maxClusterId ; ++o){
					
					value = cvg.getValue(this.v_nodeCluster[m][o]);
					if(value > 0.0 + getCplex().PRECISION){
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

					value = cvg.getValue(v_edge[i][j]);

					if(value > 0.0 + getCplex().PRECISION){
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

	@Override
	public IloNumVar nodeInClusterVar(int i, int k) throws IloException {
		return v_nodeCluster[i][k];
	}



}
