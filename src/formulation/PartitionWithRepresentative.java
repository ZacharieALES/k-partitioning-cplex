package formulation;
import callback.lazy_callback.LazyCBTriangle;
import formulation.RepParam.Triangle;
import formulation.interfaces.IFEdgeVNodeVClusterNbEdgeW;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.DoubleParam;
import ilog.cplex.IloCplex.IntParam;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.LowerRepInequality;
import inequality_family.Range;
import inequality_family.Triangle_Inequality;
import inequality_family.UpperRepInequality;

/**
 * Program which use cplex to solve a k-partition problem of n nodes (0, 1, ...,
 * n-1)
 * 
 * 
 * The 0-1 variables are:
 * 
 * - xi,j = 1 if the edge (i,j) is in the partition; 0 otherwise
 * 
 * - xi = 1 if i the representative of its cluster (i.e. the point with lower
 * index); 0 otherwise
 * 
 * 
 * 
 * Three families of constraints are considered:
 * 
 * - triangular inequalities (if i is with j and k, j and k are together)
 * 
 * - upper representative (no more than 1 representative by cluster)
 * 
 * - lower representative (at least one representative by cluster)
 * 
 * @author zach
 * 
 */
public class PartitionWithRepresentative extends Partition implements IFEdgeVNodeVClusterNbEdgeW{



	/**
	 * Representative variables Array of n-3 elements. v_rep[i] contains the
	 * value of xi+3 (1=0..n-4)
	 */
	public IloNumVar[] v_rep;

	public PartitionWithRepresentative(RepParam rp){
		this(readDissimilarityInputFile(rp), rp);
	}

	public static boolean test = true;

	public PartitionWithRepresentative(double objectif[][], RepParam rp){

		super(rp);

		this.d = objectif;
		this.n = d.length;

		if(rp instanceof TildeParam)
			this.p = new TildeParam((TildeParam)rp);
		else
			this.p = new RepParam(rp);

		if(!rp.cplexOutput)
			getCplex().turnOffCPOutput();

		if(!rp.useCplexAutoCuts)
			getCplex().removeAutomaticCuts();

		if(!rp.useCplexPrimalDual)
			getCplex().turnOffPrimalDualReduction();

		try {

			/* Create the model */
			getCplex().iloCplex.clearModel();
			getCplex().iloCplex.clearCallbacks();

			/* Reinitialize the parameters to their default value */
			getCplex().setDefaults();

			if(rp.tilim != -1)
				getCplex().setParam(IloCplex.DoubleParam.TiLim, Math.max(10,rp.tilim));

			getCplex().setParam(DoubleParam.WorkMem, 5000);
			getCplex().setParam(DoubleParam.TreLim, 4000);
			getCplex().setParam(IntParam.NodeFileInd, 3);

			/* Create the variables */
			createVariables();
			createObjectiveFunction();
			createConstraints(rp.triangle, rp.useUpper, rp.useLower);


			//Turn off preprocessing
			//			cplex.setParam(IloCplex.BooleanParam.PreInd, false);

		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void createConstraints(Triangle triangle , boolean useUpper, boolean useLower) throws IloException{

		/*
		 * Add triangular constraints : xi,j + xi,k - xj,k <= 1
		 * 
		 * - if i is with j and k, then j and k are together
		 */
		if(triangle == Triangle.USE 
				|| (triangle == Triangle.USE_IN_BC_ONLY && p.isInt == true)){
			createTriangleConstraints();
			//			System.out.println("\n!!Add triangle constraints to the model");
		}
		else if(triangle == Triangle.USE_LAZY
				|| (triangle == Triangle.USE_LAZY_IN_BC_ONLY && p.isInt == true)){
			System.out.println("\n!!Add lazy CB in BC");
			getCplex().use(new LazyCBTriangle(this, 500));
		}
		else
			//			System.out.println("\n!!Don't add triangle or lazy callback for triangles");


			/*
			 * Add the upper representative constraints (i.e. no more than 1
			 * representative by cluster) : xj + xi,j <= 1 (i < j)
			 * 
			 * - if j is representative, its cluster doesn't contain any node
			 * lower than j;
			 * 
			 * - if j is in the same cluster than i with i<j then j is not a
			 * representative.
			 */
			if(useUpper)
				createUpperRepresentativeConstraints();

		/*
		 * Add the lower representative constraints (i.e. at least 1
		 * representative by cluster) : xj + sum(i = 0:j-1) xi,j >= 1
		 * 
		 * - if j in with any lower node him, it is a representative;
		 * 
		 * - if j is not a representative, it is at least linked with one
		 * lower node.
		 */
		if(useLower)
			createLowerRepresentativeConstraints();

		if(p.useNN_1)
			createNN_1Constraints();
		
		createNumberOfClusterConstraint();

	}

	private void createNumberOfClusterConstraint() throws UnknownObjectException,
	IloException  {

		
		IloLinearNumExpr expr = getCplex().linearNumExpr();
		
		for(int i = 0 ; i < n ; i++)
			expr.addTerm(1.0, this.nodeVar(i));
		
		if(p.KMax == p.KMin)
			getCplex().addRange(new Range(p.KMax, expr, p.KMax));
		
		else {
			
			if(p.KMax < n) 
				getCplex().addRange(new Range(expr, p.KMax));
			
			if(p.KMin > 1) 
				getCplex().addRange(new Range(p.KMin, expr));
		}
	}

	/**
	 * Display the value of the representative variables which are different
	 * from zero
	 * 
	 * @param numberOfElementsByLine
	 *            Number of variables displayed by line
	 * @throws UnknownObjectException
	 * @throws IloException
	 */
	public void displayRepresentativeVariables(
			int numberOfElementsByLine) throws UnknownObjectException,
	IloException {

		int i = 0; // Index of the node

		/* First node: x0 = 1 */
		System.out.print("0\t");

		/* Second node: x1 = 1 - x01 */
		double value = 1 - cvg.getValue(v_edge[1][0]);

		/* If the value is not zero (or very close) */
		if ( !(value < 0.0+getCplex().PRECISION)) {

			/* If the value is one (or very close) */
			if (value > 1.0-getCplex().PRECISION)
				System.out.print("1\t");
			else
				System.err
				.println("Error the representative variable number 1 is not equal to 0 or 1 (value "
						+ value + ")");

		}

		/* Third node: x2 = K - 2 + x01 - sum xi */
		value = p.KMax - 2 + cvg.getValue(v_edge[1][0]);

		for (int m = 0; m < n; ++m)
			value -= cvg.getValue(v_rep[m]);

		/* If the value is not zero (or very close) */
		if ( !(value < 0.0+getCplex().PRECISION)) {
			/* If the value is one (or very close) */
			if (value > 1.0-getCplex().PRECISION)
				System.out.print("2\t");
			else
				System.err
				.println("Error the representative variable number 2 is not equal to 0 or 1 (value "
						+ value + ")");

		}

		/* While all the representative variables have not been displayed */
		while (i < n - 3) {

			int k = 0; // Id of the last element displayed on the line

			/*
			 * While the line is not over and all the representative variables
			 * have not been displayed
			 */
			while (k < numberOfElementsByLine && i < n) {

				value = cvg.getValue(v_rep[i]);

				/* If the value is not zero (or very close) */
				if ( !(value < 0.0+getCplex().PRECISION)) {
					/* If the value is one (or very close) */
					if (value > 1.0-getCplex().PRECISION){
						System.out.print((i + 3) + "\t");
						++k;

					} else
						System.err
						.println("Error the representative variable number "
								+ (i+3)
								+ " is not equal to 0 or 1 (value "
								+ value + ")");

				}

				++i;

			}

			System.out.println(" ");
		}

	}

	public void displaySolution(){

		//		if(isSolved)

		try {
			int l = 6;

			/*
			 * Display the representative variables different from 0 (<l> by
			 * line)
			 */
			System.out.println(" ");
			System.out.println("Representative variables");
			displayRepresentativeVariables(l);

			/* Display the edge variables different from 0 (<l> by line) */
			System.out.println(" ");
			System.out.println("Edges variables");
			displayEdgeVariables(l);

		} catch (UnknownObjectException e) {
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}



	/**
	 * Create the objective function
	 * 
	 * @throws IloException
	 */
	void createObjectiveFunction() throws IloException {

		IloLinearNumExpr obj = getCplex().linearNumExpr();

		for (int i = 1; i < n; ++i)
			for (int j = 0; j < i; ++j)
				obj.addTerm(d[i][j], v_edge[i][j]);

		getCplex().iloCplex.addMinimize(obj);

	}

	/**
	 * Add : n-3 variables xi which represent the fact that i is representative
	 * of its cluster (i in [3,n-1]) n * n-1 / 2 variables xi,j (i in [0,n-2], j
	 * in [i+1,n-1])
	 */
	void createVariables() throws IloException {

		v_rep = new IloNumVar[n];
		if(p.isInt)
			v_edge = new IloIntVar[n][];
		else
			v_edge = new IloNumVar[n][];

		v_rep = getCplex().iloCplex.numVarArray(n, 0.0, 1.0);

		for(int i = 0 ; i < n; i++)
			v_rep[i].setName("r_" + i);

		//		for (int i = 1; i < n; ++i)
		//			v_edge[i] = cplex.intVarArray(i, 0, 1);

		/* Create the edge variables (lower triangular part of v_edge) */
		for (int i = 0 ; i < n; ++i){
			if(p.isInt)
				v_edge[i] = new IloIntVar[n];
			else
				v_edge[i] = new IloNumVar[n];

			getCplex().iloCplex.conversion(v_edge[i], IloNumVarType.Float);

			for(int j = 0 ; j < i ; ++j){
				if(p.isInt)
					v_edge[i][j] = getCplex().iloCplex.intVar(0, 1);
				else
					v_edge[i][j] = getCplex().iloCplex.numVar(0,1);

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

	/**
	 * Add triangular constraints : xi,j + xi,k - xj,k <= 1 - if i is with j and
	 * k, then j and k are together
	 * @param solution 
	 * 
	 * @throws IloException
	 */
	void createTriangleConstraints() throws IloException {

		for (int i = 0; i < n - 2; ++i)
			for (int j = i + 1; j < n - 1; ++j)
				for (int k = j + 1; k < n; ++k) {

					getCplex().addRange(new Triangle_Inequality(this, i, j, k).createRange());
					getCplex().addRange(new Triangle_Inequality(this, j, i, k).createRange());

					IloLinearNumExpr expr3 = getCplex().linearNumExpr();
					expr3.addTerm(1.0, v_edge[k][i]);
					expr3.addTerm(1.0, v_edge[k][j]);

					expr3.addTerm(-1.0, v_edge[j][i]);

					//						if(useTriangleReinforcement)
					expr3.addTerm(+1.0, v_rep[k]);

					getCplex().addLe(expr3, 1.0);
					//						cplex.addLazyConstraint(cplex.range(-1.0, expr3, 1.0));
				}
	}

	/**
	 * Add the upper representative constraints (i.e. no more than 1
	 * representative by cluster) : xj + xi,j <= 1 (i < j)
	 * 
	 * - if j is representative, its cluster doesn't contain any node lower than
	 * j
	 * 
	 * - if j is in the same cluster than i with i<j then j is not a
	 * representative
	 * @param solution 
	 */
	void createUpperRepresentativeConstraints() throws IloException {

		for (int j = 1; j < n; ++j)
			for (int i = 0; i < j; ++i) {
				getCplex().addRange(new UpperRepInequality(this, i, j).createRange());
			}

	}

	/**
	 * Add the lower representative constraints (i.e. at least 1 representative
	 * by cluster) : xj + sum(i = 0:j-1) xi,j >= 1
	 * 
	 * - if j in with any lower node him, it is a representative;
	 * 
	 * - if j is not a representative, it is at least linked with one lower
	 * node.
	 * @param solution 
	 */
	void createLowerRepresentativeConstraints() throws IloException {

		for (int j = 0; j < n; ++j)
			getCplex().addRange(new LowerRepInequality(this, j).createRange());

	}

	public void displayAllCoefficientSolution() {

		try {

			double obj = 0.0;
			/*
			 * Display the representative variables different from 0 (<l> by
			 * line)
			 */
			System.out.println(" ");
			System.out.println("Representative variables");

			for (int m = 0; m < n; ++m){
				double value = cvg.getValue(v_rep[m]);
				System.out.println(m + " : " + value);
			}

			/* Display the edge variables different from 0 (<l> by line) */
			System.out.println(" ");
			System.out.println("Edges variables");

			/* While all the edges variables have not been displayed */
			for(int i = 1 ; i < n ; ++i)
				for(int j = 0 ; j < i ; ++j){

					double value = cvg.getValue(v_edge[i][j]);

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

	@Override
	public IloNumVar nodeVar(int i) throws IloException {
		return v_rep[i];
	}
}
