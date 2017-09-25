package formulation;

import generate_input_file.InvalidInputFileException;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Program which use cplex to solve a K-partition problem of n nodes (0, 1, ...,
 * n-1)
 * 
 * The K clusters are represented by their index (from 0 to K-1)
 * s
 * The 0-1 variables are:
 * 
 * - zk,i,j = 1 if the edge (i,j) is in the cluster k; 0 otherwise - xk,i = 1 if
 * i is in the cluster k ; 0 otherwise
 * 
 * 
 * Four families of constraints are considered:
 * 
 * - zToX : if (ij) in k then i in k and j in k (zkij < xki, zkij < xkj)
 * 
 * - xToZ : if i in k and j in k then (ij) in k (zkij > xki + xkj -1)
 * 
 * - nonEmptyCluster : cluster k contains at least one node (sum(i) xki >= 1)
 * 
 * - nodeCoherenceCluster : node i is exactly in one cluster (sum(k) xki = 1)
 * 
 * @author zach
 * 
 */
public class PartitionWitZijk {

	/**
	 * Node variables Array of K elements. x[k] contains an array of n
	 * values x[k][i] : node variable which correspond to cluster k and node
	 * i (xk,i)
	 */
	private static IloNumVar[][] x;

	/**
	 * Edge variables Array of K elements. Each array z[k] contains an
	 * array of n elements. Each array z[k][i] contains an array of i
	 * variables. z[k][i][j] corresponds to the variable zk,i,j
	 */
	private static IloNumVar[][][] z;
	private static IloCplex cplex;

	/* Number of points to cluster */
	private static int n;

	/* Number of clusters */
	private static int K = 3;

	private static double[][] d;

	public static void main(String[] args, IloCplex cple) {

		try {

			/*
			 * File which contains the dissimilarity between the nodes to
			 * cluster
			 */
			String dissimilarity_file = "data/test_input.txt";

			/* Create the model */
			cplex = cple;

			readDissimilarityInputFile(dissimilarity_file);

			/* Create the variables */
			x = new IloNumVar[K][n];
			z = new IloNumVar[K][n][];

			createVariables();
			createObjectiveFunction();

			createXToZConstraints();
			createZToXConstraints();
			createNonEmptyClustersConstraints();
			//createNodeCoherenceConstraints();

			/* If cplex solved the problem */
			if (cplex.solve()) {

				cplex.output().println(
						"Cplex found a result of: " + cplex.getObjValue());

				displayCluster();
				//displayVariables();

			} else
				cplex.output().println("Cplex did not solve the problem");

			cplex.end();

		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		} catch (InvalidInputFileException e2) {
			System.err
					.println("Invalid dissimilarity input file. Error the line "
							+ e2.getLineNumber()
							+ "should contains "
							+ e2.getLineNumber()
							+ "double seperated by spaces.\nContent of the line: "
							+ e2.getLine());
		}
	}

	public static void displayVariables() throws UnknownObjectException,
			IloException {

		cplex.output().println("Node variables (k-i): ");

		for (int k = 0; k < K; ++k)
			for (int i = 0; i < n; ++i) {

				double value = cplex.getValues(x[k])[i];

				if (value == 1.0)
					cplex.output().println("(" + k + "-" + i + ") - ");
				else if (value > 0.0)
					System.err.println("Node variable for k=" + k + " and i ="
							+ i + "has an invalid value: " + value);

			}

		cplex.output().println(" ");
		cplex.output().println(" ");

		cplex.output().println("Edge variables (k-i,j): ");

		for (int k = 0; k < K; ++k)
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < i; ++j) {

					double value = cplex.getValues(z[k][i])[j];

					if (value == 1.0)
						cplex.output().println(
								"(" + k + "-" + i + "," + j + ") - ");
					else if (value > 0.0)
						System.err.println("Edge variable for k=" + k + ", i ="
								+ i + "and j =  " + j
								+ "has an invalid value: " + value);

				}

	}

	/**
	 * Display the value of the edge variables which are equal to 1
	 * 
	 * @param numberOfElementsByLine
	 *            Number of variables displayed by line
	 * @throws UnknownObjectException
	 * @throws IloException
	 */
	private static void displayCluster() throws UnknownObjectException,
			IloException {

		cplex.output().println("Clusters :");

		for (int k = 0; k < K; ++k) {
cplex.output().println("k : " + k);

			for (int i = 0; i < n; ++i) {
cplex.output().print("i : " + i);
//double test = cplex.getValue(x[0][0]);
cplex.output().println("plop "  );
//System.err.println(" " + cplex.getValues(x[k]));
				if ( (cplex.getValue(x[k][i])) == 1.0)
					cplex.output().print(" - " + i);
			}

			cplex.output().println(" ");
			cplex.output().println(" ");
		}

	}

	/**
	 * Create the objective function
	 * 
	 * @throws IloException
	 */
	static void createObjectiveFunction() throws IloException {

		IloLinearNumExpr obj = cplex.linearNumExpr();

		for (int k = 0; k < K; ++k)
			for (int i = 1; i < n; ++i)
				for (int j = 0; j < i; ++j)
					obj.addTerm(d[i][j], z[k][i][j]);

		cplex.addMinimize(obj);

	}

	static void createVariables() throws IloException {

		for (int k = 0; k < K; ++k){
			x[k] = cplex.numVarArray(n, 0.0, 1.0);
			cplex.output().println("k : " + k);
			cplex.output().println("length : " + x[k].length);
		}

		for (int k = 0; k < K; ++k)
			for (int i = 0; i < n; ++i)
				z[k][i] = cplex.numVarArray(i, 0.0, 1.0);

	}

	/**
	 * Add xToZ constraints : zkij - xik <= 0 and zkij - xjk <= 0
	 * 
	 * @throws IloException
	 */
	static void createXToZConstraints() throws IloException {

		for (int k = 0; k < K; ++k)
			for (int i = 0; i < n - 1; ++i)
				for (int j = 0; j < i; ++j) {

					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					IloLinearNumExpr expr2 = cplex.linearNumExpr();

					/* Add zkij */
					expr1.addTerm(1.0, z[k][i][j]);
					expr2.addTerm(1.0, z[k][i][j]);

					/* -xik and -xjk */
					expr1.addTerm(-1.0, x[k][i]);
					expr2.addTerm(-1.0, x[k][j]);

					cplex.addLe(expr1, 0.0);
					cplex.addLe(expr2, 0.0);

				}

	}

	/**
	 * Add xToZ constraints : xik + xjk - zkij <= 1
	 * 
	 * @throws IloException
	 */
	static void createZToXConstraints() throws IloException {

		for (int k = 0; k < K; ++k)
			for (int i = 0; i < n - 1; ++i)
				for (int j = 0; j < i; ++j) {

					IloLinearNumExpr expr = cplex.linearNumExpr();

					expr.addTerm(1.0, x[k][i]);
					expr.addTerm(1.0, x[k][j]);
					expr.addTerm(-1.0, z[k][i][j]);

					cplex.addLe(expr, 1.0);
				}
	}

	/**
	 * Add the non empty cluster constraints sum(i) xik >= 1
	 */
	static void createNonEmptyClustersConstraints() throws IloException {

		for (int k = 0; k < K; ++k) {

			IloLinearNumExpr expr = cplex.linearNumExpr();

			for (int i = 0; i < n - 1; ++i)
				expr.addTerm(1.0, x[k][i]);

			cplex.addGe(1.0, expr);

		}
	}

	/**
	 * Add the node coherence constraint (i is exactly in one cluster): sum(k)
	 * xik = 1
	 */
	static void createNodeCoherenceConstraints() throws IloException {

		for (int i = 0; i < n; ++i) {

			IloLinearNumExpr expr = cplex.linearNumExpr();

			for (int k = 0; k < K - 1; ++k)
				expr.addTerm(1.0, x[k][i]);

			cplex.addEq(1.0, expr);

		}
	}

	/**
	 * Read a txt file which contains a low triangular matrix. This matrix
	 * represent the dissimilarity between the elements to partition: - the line
	 * j must contains j double (the first one is the dissimilarity between the
	 * nodes 0 and j-1, the last one is the dissimilarity between j-1 and itself
	 * (should be zero))
	 * 
	 * @param dissimilarity_file
	 * @throws InvalidInputFileException
	 */
	static void readDissimilarityInputFile(String dissimilarity_file)
			throws InvalidInputFileException {

		ArrayList<String[]> al_dissimilarity = new ArrayList<String[]>();

		int j = 1;

		File f = new File(dissimilarity_file);
		if (!f.exists()) {
			System.err.println("The input file '" + dissimilarity_file
					+ "' does not exist.");
		}

		InputStream ips;
		try {
			ips = new FileInputStream(dissimilarity_file);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line;

			/* For each line */
			while ((line = br.readLine()) != null) {

				String[] temp = line.split(" ");

				/* If the number of elements is valid */
				if (temp.length == j)
					al_dissimilarity.add(j - 1, temp);
				else
					throw new InvalidInputFileException(line, j);

				++j;
			}

			br.close();

			n = j - 1;

			if (n == 0)
				System.err.println("The input file is empty.");
			else {
				d = new double[n][];

				/*
				 * for each line with non diagonal elements (i.e. line 1 to n-1
				 * of al_dissimilarity <-> line 2 to n of the file)
				 */
				for (j = 1; j < n; ++j) {

					String[] currentLine = al_dissimilarity.get(j);
					d[j] = new double[j];

					for (int i = 0; i < j; ++i)
						d[j][i] = Double.parseDouble(currentLine[i]);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
