package formulation;
import formulation.RepParam.Triangle;
import lazy_callback.Lazy_CB_Triangle;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.IntParam;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.LowerRep_Inequality;
import inequality_family.Triangle_Inequality;
import inequality_family.UpperRep_Inequality;
import solution.Solution_Representative;

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
 * The variables x0, x1 and x2 are not considered since:
 * 
 * - x0 = 1
 * 
 * - x1 = 1 - x0,1
 * 
 * - x2 = x0,1 - sum(i=3:n-1) xi + k - 2
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
public class PartitionWithRepresentative extends Partition implements Solution_Representative{
	
	public PartitionWithRepresentative(RepParam rp){
		this(readDissimilarityInputFile(rp), rp);
	}
	
public static boolean test = true;
	
	public PartitionWithRepresentative(double objectif[][], RepParam rp){
		
		this.d = objectif;
		this.n = d.length;
		
		this.p = new RepParam(rp);

		if(!rp.cplexOutput)
			turnOffCPOutput();
		
		if(rp.cplexAutoCuts)
			removeAutomaticCuts();
		
		if(rp.cplexPrimalDual)
			turnOffPrimalDualReduction();
		
		try {
			
			/* Create the model */
			cplex.clearModel();
			cplex.clearCallbacks();
			
			/* Reinitialize the parameters to their default value */
			cplex.setDefaults();
			
			if(rp.tilim != -1)
			    cplex.setParam(IloCplex.DoubleParam.TiLim, Math.max(10,rp.tilim));
			
//			cplex.setParam(DoubleParam.WorkMem, 2000);
//			cplex.setParam(DoubleParam.TreLim, 500);
			cplex.setParam(IntParam.NodeFileInd, 3);
			
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
				cplex.use(new Lazy_CB_Triangle(this, 500));
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

		/*
		 * Add the upper and lower constraints which correspond to x2 >= 0
		 * and x2 <= 1 with x2 = K - 2 + x01 - sum xi
		 */
		createX2BoundConstraints();
		
		if(p.useNN_1)
			createNN_1Constraints();
		
	}
	
	public void createNN_1Constraints(){

		try {
			int d = (int) Math.floor((n-1)/p.K);
			int mo = (n-1)%p.K;
			int n1 = (d+1) * d / 2;
			int n2 = d * (d-1) / 2;
			int righthand = n1 * mo + n2 * (p.K-mo);
			
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
	
			d = (int) Math.floor((n)/p.K);
			mo = (n)%p.K;
			n1 = (d+1) * d / 2;
			n2 = d * (d-1) / 2;
			righthand = n1 * mo + n2 * (p.K-mo);
			
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

		IloLinearNumExpr obj = cplex.linearNumExpr();

		for (int i = 1; i < n; ++i)
			for (int j = 0; j < i; ++j)
				obj.addTerm(d[i][j], v_edge[i][j]);

		cplex.addMinimize(obj);

	}

	/**
	 * Add : n-3 variables xi which represent the fact that i is representative
	 * of its cluster (i in [3,n-1]) n * n-1 / 2 variables xi,j (i in [0,n-2], j
	 * in [i+1,n-1])
	 */
	void createVariables() throws IloException {

		v_rep = new IloNumVar[n - 3];
		if(p.isInt)
			v_edge = new IloIntVar[n][];
		else
			v_edge = new IloNumVar[n][];

		v_rep = cplex.numVarArray(n - 3, 0.0, 1.0);
		
		for(int i = 0 ; i < n - 3 ; i++)
			v_rep[i].setName("r_" + i);

//		for (int i = 1; i < n; ++i)
//			v_edge[i] = cplex.intVarArray(i, 0, 1);
		
		/* Create the edge variables (lower triangular part of v_edge) */
		for (int i = 0 ; i < n; ++i){
			if(p.isInt)
				v_edge[i] = new IloIntVar[n];
			else
				v_edge[i] = new IloNumVar[n];

			cplex.conversion(v_edge[i], IloNumVarType.Float);
			
			for(int j = 0 ; j < i ; ++j){
				if(p.isInt)
					v_edge[i][j] = cplex.intVar(0, 1);
				else
					v_edge[i][j] = cplex.numVar(0,1);
				
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

					addRange(new Triangle_Inequality(this, i, j, k).createRange());
					addRange(new Triangle_Inequality(this, j, i, k).createRange());

					
					
					IloLinearNumExpr expr3 = cplex.linearNumExpr();
					expr3.addTerm(1.0, v_edge[k][i]);
					expr3.addTerm(1.0, v_edge[k][j]);
				
					if(k != 2){

						expr3.addTerm(-1.0, v_edge[j][i]);
						
//						if(useTriangleReinforcement)
							expr3.addTerm(+1.0, v_rep[k-3]);

						cplex.addLe(expr3, 1.0);
//						cplex.addLazyConstraint(cplex.range(-1.0, expr3, 1.0));
					}
					else{
						
//						if(useTriangleReinforcement){
							for (int l = 3; l < n; ++l)
								expr3.addTerm(-1.0, v_rep[l - 3]);
							
							cplex.addLe(expr3, 3.0-p.K);
//						}
//						else{
//							expr3.addTerm(-1.0, v_edge[j][i]);
//							cplex.addLe(expr3, 1.0);
//						}

					}
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

		for (int j = 2; j < n; ++j)
			for (int i = 0; i < j; ++i) {
				addRange(new UpperRep_Inequality(this, i, j).createRange());
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

		for (int j = 2; j < n; ++j)
			addRange(new LowerRep_Inequality(this, j).createRange());

	}

	/**
	 * Add the upper and lower constraints which correspond to x2 >= 0 and x2 <=
	 * 1 with x2 = K - 2 + x01 + sum xi -> x01 - sum xi <= 3 - K -> x01 - sum xi
	 * >= 2 - K
	 * **/
	private void createX2BoundConstraints() throws IloException {

		IloLinearNumExpr expr = cplex.linearNumExpr();

		// double value = K - 2 + cplex.getValues(v_edge[1])[0];

		expr.addTerm(1.0, v_edge[1][0]);

		for (int m = 0; m < n - 3; ++m)
			expr.addTerm(-1.0, v_rep[m]);

		cplex.addLe(expr, 3.0 - p.K);
		cplex.addGe(expr, 2.0 - p.K);

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
			
			/* First node: x0 = 1 */
			System.out.println("0 : 1.0");

			/* Second node: x1 = 1 - x01 */
			double value = 1 - cplex.getValue(v_edge[1][0]);
			System.out.println("1 : " + value);

			/* Third node: x2 = K - 2 + x01 - sum xi */
			value = p.K - 2 + cplex.getValue(v_edge[1][0]);
			for (int m = 0; m < n - 3; ++m){
				value -= cplex.getValue(v_rep[m]);
			}
			System.out.println("2 : " + value);

			for (int m = 0; m < n - 3; ++m){
				value = cplex.getValue(v_rep[m]);
				System.out.println(m+3 + " : " + value);
			}

			/* Display the edge variables different from 0 (<l> by line) */
			System.out.println(" ");
			System.out.println("Edges variables");

			/* While all the edges variables have not been displayed */
			for(int i = 1 ; i < n ; ++i)
				for(int j = 0 ; j < i ; ++j){

					value = cplex.getValue(v_edge[i][j]);
					
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
	
	/**
	 * Create a range which corresponds to x3=1
	 * @return The range
	 * @throws IloException
	 */
	public IloRange x3Eq1() throws IloException{
		return cplex.range(3.0-p.K, exprX3(), 3.0-p.K);
	}

	
	/**
	 * Create a range which corresponds to x3=0
	 * @return The range
	 * @throws IloException
	 */
	public IloRange x3Eq0() throws IloException{
		return cplex.range(2.0-p.K, exprX3(), 2.0-p.K);
	}
	
	/**
	 * Create a range which corresponds to x2=1
	 * @return The range
	 * @throws IloException
	 */
	public IloRange x2Eq1() throws IloException{
		return cplex.range(0.0, exprX2(), 0.0);
	}

	
	/**
	 * Create a range which corresponds to x2=0
	 * @return The range
	 * @throws IloException
	 */
	public IloRange x2Eq0() throws IloException{
		return cplex.range(1.0, exprX2(), 1.0);
	}
	
	/**
	 * x3 = K - 2 + x12 - sum xi
	 * exprX3() <=> x12 - sum xi
	 * 
	 * x3 >= 1 -> exprX3() >= 3 - K
	 * x3 <= 0 -> exprX3() <= 2 - K 
	 * @return An expression which corresponds to x12 - sum xi
	 * @throws IloException
	 */
	public IloLinearNumExpr exprX3() throws IloException{
		
		IloLinearNumExpr expr = cplex.linearNumExpr();
		
		expr.addTerm(1.0, v_edge[1][0]);

		for (int m = 0; m < n - 3; ++m)
			expr.addTerm(-1.0, v_rep[m]);
		
		return expr;
	}
	
	/**
	 * x2 = 1 - x12
	 * exprX2() <=> x12
	 * 
	 * x2 >= 1 -> exprX2() <= 0
	 * x2 <= 0 -> exprX2() >= 1
	 * @return An expression which corresponds to x12
	 * @throws IloException
	 */
	public IloLinearNumExpr exprX2() throws IloException{
		
		IloLinearNumExpr expr = cplex.linearNumExpr();
		
		expr.addTerm(1.0, v_edge[1][0]);
		return expr;
	
	}
	
	public IloNumVar x_var(int i){
		return v_rep[i-3];
	}
	
	public IloNumVar x_var(int i, int j){
		return v_edge[i][j];
	}
	
	public double x(int i){
		try {
			return cplex.getValue(x_var(i));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("(i): (" + i + ")");
			System.exit(0);
			return -1;
		}
	}
	
	private int counter = 0;
	
	public double x(int i, int j){
		try {
		return cplex.getValue(x_var(i,j));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("(i,j): (" + i + "," + j + ")");
			System.exit(0);
			return 1.0;
		}
	}



	@Override
	public double xt(int i, int j) {
		if(isTilde())
			return ((Partition_with_tildes)this).xt(i, j);
		else
			return -1.0;
	}


	@Override
	public IloNumVar xt_var(int i, int j) {
		if(isTilde())
			return ((Partition_with_tildes)this).xt_var(i, j);
		else
			return null;
	}

	@Override
	public boolean isTilde() {
		return this instanceof Partition_with_tildes;
	}


	/**
	 * i must be greater than j
	 */
	@Override
	public double d(int i, int j) {
		return d[i][j];
	}

	@Override
	public IloNumVar y_var(int i, int j) {
		return null;
	}

	@Override
	public double y(int i, int j) {
		return 0;
	}


}
