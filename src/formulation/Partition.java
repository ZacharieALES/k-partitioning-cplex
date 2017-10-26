package formulation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cplex.Cplex;
import formulation.interfaces.IFEdgeVClusterNb;
import formulation.interfaces.IFEdgeW;
import generate_input_file.InvalidInputFileException;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;
import variable.CplexVariableGetter;


public abstract class Partition implements IFEdgeVClusterNb, IFEdgeW{


	public Param p;

	/* Number of points to cluster */
	public int n;

	public double[][] d;

	/**
	 * Edges variables Array of n elements. v_edge[0] is empty v_edge[i]
	 * contains an array of i variables (which corresponds to x0i, ..., xi-1,i)
	 * for i=1..n-1
	 */
	public IloNumVar[][] v_edge;
	
	public abstract void displaySolution() throws UnknownObjectException, IloException;

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
	
	@Override
	public double edgeWeight(int i, int j) {
		return d[i][j];
	}
	
	@Override
	public IloNumVar edgeVar(int i, int j) throws IloException {
		return v_edge[i][j];
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

				double value = cvg.getValue(v_edge[i][j]);

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


	public int KMax() {
		if(p != null)
			return p.KMax;
		else
			return -1;
	}


	public int KMin() {
		if(p != null)
			return p.KMin;
		else
			return -1;
	}
	
	CplexVariableGetter cvg;
	
	public CplexVariableGetter intValueGetter() {
		return cvg;
	}

	public static Partition createPartition(Param param) throws IloException{

		Partition p = null;

		if(param instanceof TildeParam){
			TildeParam tp = (TildeParam) param;
			p = new PartitionWithTildes(tp);
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
		p.cvg = new CplexVariableGetter(p.getCplex());
		
		return p;
	}

	public int maximalNumberOfClusters() {
		return KMax();
	}
	public int minimalNumberOfClusters() {
		return KMin();
	}

	public void createNN_1Constraints(){

		try {
			int d = (int) Math.floor((n-1)/p.KMax);
			int mo = (n-1)%p.KMax;
			int n1 = (d+1) * d / 2;
			int n2 = d * (d-1) / 2;
			int righthand = n1 * mo + n2 * (p.KMax-mo);
			
			for(int j = 0 ; j < n ; ++j){
				IloLinearNumExpr expr;
					expr = getCplex().linearNumExpr();
	
				for(int l = 0 ; l < n ; ++l)
					if(l != j)
						for(int m = l+1 ; m < n ; ++m){
							if(m != j)
								expr.addTerm(+1.0, v_edge[l][m]);
						}
			
				getCplex().addGe(expr, righthand);
	
			}
	
			d = (int) Math.floor((n)/p.KMax);
			mo = (n)%p.KMax;
			n1 = (d+1) * d / 2;
			n2 = d * (d-1) / 2;
			righthand = n1 * mo + n2 * (p.KMax-mo);
			
			IloLinearNumExpr expr = getCplex().linearNumExpr();
			for(int l = 0 ; l < n ; ++l)
				for(int m = l+1 ; m < n ; ++m){
					expr.addTerm(+1.0, v_edge[l][m]);
					}
			
			getCplex().addGe(expr, righthand);

		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Cplex getCplex() {
		return p.cplex;
	}

	@Override
	public CplexVariableGetter variableGetter() {
		return cvg;
	}
	
}
