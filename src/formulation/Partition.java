package formulation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import generate_input_file.InvalidInputFileException;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.BooleanParam;
import ilog.cplex.IloCplex.DoubleParam;
import ilog.cplex.IloCplex.IntParam;
import ilog.cplex.IloCplex.LazyConstraintCallback;
import ilog.cplex.IloCplex.ParameterSet;
import ilog.cplex.IloCplex.UnknownObjectException;
import ilog.cplex.IloCplex.UserCutCallback;
import inequality_family.Range;
import presolve_callback.CallBack_PresolveInfo;
import cut_callback.Callback_RootRelaxation;


public abstract class Partition {
	
	double epsilon = 0.0000001;

	/* Number of points to cluster */
	public int n;
	
	public Param p;

	public double[][] d;

	public static IloCplex cplex;

	/**
	 * Representative variables Array of n-3 elements. v_rep[i] contains the
	 * value of xi+3 (1=0..n-4)
	 */
	public IloNumVar[] v_rep;
	
	public double rootRelaxation = -1.0;

	/**
	 * Edges variables Array of n elements. v_edge[0] is empty v_edge[i]
	 * contains an array of i variables (which corresponds to x0i, ..., xi-1,i)
	 * for i=1..n-1
	 */
	public IloNumVar[][] v_edge;
	
	public void removeAutomaticCuts(){
		try {
			cplex.setParam(IloCplex.DoubleParam.CutsFactor, 0.0);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSolved = false;

	public void turnOffCPOutput(){
		/* Turn off cplex output */
		cplex.setOut(null);
		cplex.setWarning(null);
		
	}
	
	public abstract void displaySolution() throws UnknownObjectException, IloException;
	
	public void onlyDisplayRootRelaxation() {
		
	//	cplex.setParam(IloCplex.IntParam.AggCutLim, -1);
		Callback_RootRelaxation rootCB = new Callback_RootRelaxation(true);

		removeAutomaticCuts();
		turnOffCPOutput();
		
		try {
			cplex.use(rootCB);
		} catch (IloException e) {
			e.printStackTrace();
		}
		
	}

	
	public void displayPresolveInfo() {
	
		//cplex.setParam(IloCplex.IntParam.AggCutLim, -1);
		CallBack_PresolveInfo presolveCB = new CallBack_PresolveInfo();
		try {
			cplex.use(presolveCB);
		} catch (IloException e) {
			e.printStackTrace();
		}
	
	}
	
	public void displayObjectiveFunction(){
		try {
			if(isSolved)
				System.out.println("Cplex found a result of: " + cplex.getObjValue());
		} catch (IloException e) {
			e.printStackTrace();
		}	
	}

	public void nodeLimitTo0() {
		try {
			cplex.setParam(IloCplex.IntParam.NodeLim, 0);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public static void end(){
		cplex.end();
	}

	public void turnOffPrimalDualReduction() {
		try {
			cplex.setParam(IloCplex.IntParam.Reduce, 0);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public IloLinearNumExpr linearNumExpr(){
		try {
			return cplex.linearNumExpr();
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public IloRange addRange(Range r){
		try {
			return cplex.addRange(r.lbound, r.expr, r.ubound);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}
	
//	public IloRange addLazyRange(Range r){
//		try {
//			return cplex.addLazyConstraint(cplex.range(r.lbound, r.expr, r.ubound));
//		} catch (IloException e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	public IloRange addRange(double lbound, IloLinearNumExpr expr, double ubound){
		try {
			
			return cplex.addRange(lbound, expr, ubound);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public IloRange addLe(IloLinearNumExpr expr, double bound){
		try {
			
			return cplex.addLe(expr, bound);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public IloRange addGe(IloLinearNumExpr expr, double bound){
		try {
			
			return cplex.addGe(expr, bound);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public double solve(){
		try {
				
			double time = -cplex.getCplexTime();		
			cplex.solve();
			return time + cplex.getCplexTime();
			
			
		} catch (IloException e) {
			e.printStackTrace();
			return -1.0;
		}
	}
	
	public void clearCallback(){
		try {
			cplex.clearCallbacks();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void use(UserCutCallback ucc){
		try {
			cplex.use(ucc);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void use(LazyConstraintCallback lcc){
		try {
			cplex.use(lcc);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public static void start(){
		try {
			cplex = new IloCplex();
			cplex.setParam(IntParam.ClockType, 2);
			
			/* Min gap under which cplex consider that the optimal solution is found */
//			cplex.setParam(DoubleParam.EpGap, 1E-13);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public double getNnodes() {
		return cplex.getNnodes();
	}

	public double getObjValue2() {
		try {
			return cplex.getObjValue();
		} catch (IloException e) {
			e.printStackTrace();
			return -1.0;
		}
	}

	public double getBestObjValue2() {
		try {
			return cplex.getBestObjValue();
		} catch (IloException e) {
			e.printStackTrace();
			return -1.0;
		}
	}

	public int getNcuts(int cuttype) {
		try {
			return cplex.getNcuts(cuttype);
		} catch (IloException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public double getCplexTime() {
		try {
			return cplex.getCplexTime();
		} catch (IloException e) {
			e.printStackTrace();
			return -1.0;
		}
	}

	public IloRange le(IloLinearNumExpr expr, double ubound) {
		try {
			return cplex.le(expr, ubound);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

	public IloRange ge(IloLinearNumExpr expr, double lbound) {
		try {
			return cplex.ge(expr, lbound);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

	public IloRange range(double bound, IloNumExpr expr, double bound2) {
		try {
			return cplex.range(bound, expr, bound2);
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

	public IloRange eq(double e, IloNumVar iloNumVar) {
		try {
			return cplex.eq(e,  iloNumVar);
		} catch (IloException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	public IloRange eq(double e, IloLinearNumExpr iloNumExpr) {
		try {
			return cplex.eq(e,  iloNumExpr);
		} catch (IloException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	public static void setParam(DoubleParam p, double value) {
		try {
			cplex.setParam(p, value);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public static void setParam(BooleanParam p, boolean value) {
		try {
			cplex.setParam(p, value);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public static void setParam(IntParam p, int value) {
		try {
			cplex.setParam(p, value);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public static double getParam(DoubleParam p) {
		try {
			return cplex.getParam(p);
		} catch (IloException e) {
			e.printStackTrace();
			return -1.0;
		}
	}

	public static ParameterSet getParameterSet() {
		try {
			return cplex.getParameterSet();
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setParameterSet(ParameterSet s) {
		try {
			cplex.setParameterSet(s);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public static void setDefaults() {
		try {
			cplex.setDefaults();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	


	public double getSlack(IloRange r) {
		try {
			return cplex.getSlack(r);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return -1.0;
		}
	}

	public void remove(IloRange r) {
		try {
		cplex.remove(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addMIPStart(IloNumVar[] var, double[] val) {
		try {
			cplex.addMIPStart(var, val);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	


	/**
	 * Read a txt file which contains a low triangular matrix. This matrix
	 * represent the dissimilarity between the elements to partition:
	 * 
	 * - the line j must contains j double (the first one is the dissimilarity
	 * between the nodes 0 and j-1, the last one is the dissimilarity between
	 * j-1 and itself (should be zero))
	 * 
	 * @param dissimilarity_file
	 * @param max_number_of_nodes Maximum number of line read in the file (i.e. maximum number of nodes considered in the problem) ; -1 if there is no limit
	 * @throws InvalidInputFileException
	 */
	static double[][] readDissimilarityInputFile(Param param)
			{

		double[][] d = null;
		
		ArrayList<String[]> al_dissimilarity = new ArrayList<String[]>();
		int j = 1;

		File f = new File(param.inputFile);
		if (!f.exists()) {
			System.err.println("The input file '" + param.inputFile
					+ "' does not exist.");
		}

		InputStream ips;
		try {
			ips = new FileInputStream(param.inputFile);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;
			
			if(param.maxNumberOfNodes == -1)
				param.maxNumberOfNodes = Integer.MAX_VALUE;

			/* For each line */
//			while ((line = br.readLine()) != null && j <= max_number_of_nodes) {
			while ((line = br.readLine()) != null && j < param.maxNumberOfNodes) {
//System.out.println(j + " : " + line);
				String[] temp = line.split(" ");

				/* If the number of elements is valid */
				if (temp.length >= j)
					al_dissimilarity.add(j - 1, temp);
				else{
					System.err
					.println("Partition.java: Invalid dissimilarity input file. Error the line \""
							+ line
							+ "\" should contain at least "
							+ j
							+ " double separated by spaces.");
					System.exit(0);
				}

				++j;
			}

			br.close();

			//V1 (erreur)
//			int n = j - 1;
			
			//V2 (sans erreur)
			int n = j;
	
			if (n == 0){
				System.err.println("The input file is empty.");
				System.exit(0);
			}
			else {
//System.out.println("n = " + n);
				d = new double[n][];

				d[0] = new double[n];
				/*
				 * for each line with non diagonal elements (i.e. line 1 to n-1
				 * of al_dissimilarity <-> line 2 to n of the file)
				 */
				// V1
//				for (j = 1; j < n; ++j) {
					
				// V2
				for (j = 0; j < n-1; ++j) {

					String[] currentLine = al_dissimilarity.get(j);
					
					// V1
//					d[j] = new double[j];
					
					// V2
					d[j+1] = new double[n];

					//V1
//					for (int i = 0; i < j; ++i){
					
					//V2
					for (int i = 0; i <= j; ++i){
						
						// V1
//						d[j][i] = Double.parseDouble(currentLine[i]);
						
						//V2
						d[j+1][i] = Double.parseDouble(currentLine[i])+ param.gapDiss;
						//V2
						d[i][j+1] = d[j+1][i];
//						System.out.println("(" + i + "," + (j+1) + "): " + d[j+1][i]);
					}
				}
				
			}
			
//			for(int i = 0 ; i < n ; ++i){
//				if(i < 10 || i > 150){
//					for(j = 0 ; j < i ; ++j)
//						System.out.print(d[i][j] + " ");
//					System.out.println("\n");
//				}
//			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return d;
	}
	

	public double getValue(int i, int j){
		try {
			return cplex.getValue(v_edge[i][j]);
		} catch (UnknownObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
		}
		return Double.MAX_VALUE;
	}
	
	public double getValue(int i){
		try {
			return cplex.getValue(v_rep[i]);
		} catch (UnknownObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
		}
		return Double.MAX_VALUE;
	}

	/**
	 * Display the value of the edge variables which are equal to 1
	 * 
	 * @param numberOfElementsByLine
	 *            Number of variables displayed by line
	 * @throws UnknownObjectException
	 * @throws IloException
	 */
	public void displayEdgeVariables(int numberOfElementsByLine)
			throws UnknownObjectException, IloException {

		int i = 1; // Index of the edge first node
		int j = 0; // Index of the edge second node

		/* While all the edges variables have not been displayed */
		while (i != n) {

			int k = 0; // Id of the last element displayed on the line

			/*
			 * While the line is not over (i.e. while it does not contain l
			 * elements or reach the last edge variable)
			 */
			while (k < numberOfElementsByLine && i != n) {

				double value = cplex.getValue(v_edge[i][j]);

if(value > 1E-4){
System.out.print("x" + i + "-" + j + "(" + value + ")\t\t");
++k;
}
				
////System.out.println(value);
//				/* If the value is not zero (or very close) */
//				if ( !(value < 0.0+epsilon)) {
//					/* If the value is one (or very close) */
//					if (value > 1-epsilon) {
//
//						System.out.print(j + "-" + i + "\t\t");
//						++k;
// 
//					} else
//						System.err.println("Error the edge variable " + j + "-"
//								+ i + " is not equal to 0 or 1 (value " + value
//								+ ")");
//
//				}

				if (j != i - 1)
					++j;
				else {
					++i;
					j = 0;
				}

			}

			System.out.println(" ");
		}

	}
	

	public int n() {
		return n;
	}


	public int K() {
		if(p != null)
			return p.K;
		else
			return -1;
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
		double value = 1 - cplex.getValue(v_edge[1][0]);

		/* If the value is not zero (or very close) */
		if ( !(value < 0.0+epsilon)) {

			/* If the value is one (or very close) */
			if (value > 1.0-epsilon)
				System.out.print("1\t");
			else
				System.err
						.println("Error the representative variable number 1 is not equal to 0 or 1 (value "
								+ value + ")");

		}

		/* Third node: x2 = K - 2 + x01 - sum xi */
		value = p.K - 2 + cplex.getValue(v_edge[1][0]);

		for (int m = 0; m < n - 3; ++m)
			value -= cplex.getValue(v_rep[m]);

		/* If the value is not zero (or very close) */
		if ( !(value < 0.0+epsilon)) {
			/* If the value is one (or very close) */
			if (value > 1.0-epsilon)
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
			while (k < numberOfElementsByLine && i < n - 3) {

				value = cplex.getValue(v_rep[i]);

				/* If the value is not zero (or very close) */
				if ( !(value < 0.0+epsilon)) {
					/* If the value is one (or very close) */
					if (value > 1.0-epsilon){
						System.out.print(i + 3 + "\t");
						++k;

					} else
						System.err
								.println("Error the representative variable number "
										+ i
										+ " is not equal to 0 or 1 (value "
										+ value + ")");

				}

				++i;

			}

			System.out.println(" ");
		}

	}


	
	public static Partition createPartition(Param param){

		Partition p = null;

		if(param instanceof TildeParam){
			TildeParam tp = (TildeParam) param;
			p = new Partition_with_tildes(tp);
		}
		else if(param instanceof RepParam){
			RepParam rp = (RepParam) param;
			p = new PartitionWithRepresentative(rp);
		}
		else{
			XYParam xyp = (XYParam) param;
			if(xyp.isSecondXYFormulation){
				p = new Partition_x_y_2(xyp);
			}
			else
				p = new Partition_x_y(xyp);
		}
		
		return p;
	}
	
}
