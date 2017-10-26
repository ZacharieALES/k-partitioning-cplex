package formulation;

import formulation.interfaces.IFEdgeVNodeClusterVNodeVConstrainedClusterNb;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.LinearFirstInequality;
import inequality_family.LinearSecondInequality;
import inequality_family.LinearThirdInequality;

public class PartitionWithTildes extends PartitionWithRepresentative implements IFEdgeVNodeClusterVNodeVConstrainedClusterNb{
	
	/**
	 * Tilde variables */
	public IloNumVar[][] v_tilde;

	public PartitionWithTildes(TildeParam tp) throws IloException {
		this(readDissimilarityInputFile(tp), tp);
	}
	
	public PartitionWithTildes(double[][] objective, TildeParam tp) throws IloException{
		super(objective, tp);
		createLinearConstraints(tp.useLinear);
	}
	
	public void displaySolution(){

		super.displaySolution();
		
		try {
			int l = 6;

			/* Display the edge variables different from 0 (<l> by line) */
			System.out.println("\nTilde variables");
			displayTildeVariables(l);
			
		} catch (UnknownObjectException e) {
			e.printStackTrace();
		} catch (IloException e) {
			e.printStackTrace();
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
	public void displayIntegerTildeVariables(int numberOfElementsByLine)
			throws UnknownObjectException, IloException {

		int i = 2; // Index of the edge first node
		int j = 1; // Index of the edge second node

		/* While all the edges variables have not been displayed */
		while (i != n) {

			int k = 0; // Id of the last element displayed on the line

			/*
			 * While the line is not over (i.e. while it does not contain l
			 * elements or reach the last edge variable)
			 */
			while (k < numberOfElementsByLine && i != n) {

				double value = cvg.getValue(v_tilde[i][j]);

				/* If the value is not zero (or very close) */
				if ( !(value < 0.0+getCplex().PRECISION)) {
					/* If the value is one (or very close) */
					if (value > 1-getCplex().PRECISION) {

						System.out.print(j + "-" + i + "\t\t");
						++k;

					} else
						System.err.println("Error the tilde variable " + j + "-"
								+ i + " is not equal to 0 or 1 (value " + value
								+ ")");

				}

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
	
	/**
	 * Display the value of the edge variables which are equal to 1
	 * 
	 * @param numberOfElementsByLine
	 *            Number of variables displayed by line
	 * @throws UnknownObjectException
	 * @throws IloException
	 */
	public void displayTildeVariables(int numberOfElementsByLine)
			throws UnknownObjectException, IloException {

		int i = 2; // Index of the edge first node
		int j = 1; // Index of the edge second node

		/* While all the edges variables have not been displayed */
		while (i != n) {

				double value = cvg.getValue(v_tilde[i][j]);
				System.out.println("xt_"  +j + "-" + i + " = " + value);
				if (j != i - 1)
					++j;
				else {
					++i;
					j = 0;
				}
		}

	}

	/**
	 * Add : n-3 variables xi which represent the fact that i is representative
	 * of its cluster (i in [3,n-1]) n * n-1 / 2 variables xi,j (i in [0,n-2], j
	 * in [i+1,n-1])
	 */
	@Override
	void createVariables() throws IloException {

		
		super.createVariables();
		
		v_tilde = new IloNumVar[n][];
				
		/* Create the tilde variables */
		/* The first line is equal to the first line of v_edge since xt_0i = x_0i */
		v_tilde[0] = v_edge[0]; 
				
		/* For the next lines */
		for (int i = 1 ; i < n; ++i){
			v_tilde[i] = new IloNumVar[n];

			getCplex().iloCplex.conversion(v_tilde[i], IloNumVarType.Float);

			/* Fill the i first elements */
			v_tilde[i][0] = v_tilde[0][i];
			
			for(int j = 1 ; j < i ; ++j){
				v_tilde[i][j] = getCplex().iloCplex.numVar(0, 1);
				v_tilde[i][j].setName("xt_" + i + "_" + j);
			}
			
		}
		
		/* Link the symetric variables to their equivalent in the lower triangle part of v_tilde
		 * Ex : v[1][0] = v[0][1]
		 */
		for (int i = 1 ; i < n; ++i){
			
			for(int j = 1 ; j < i ; ++j)
				v_tilde[j][i] = v_tilde[i][j];
		}

	}

	/**
	 * Create the linear constraints 1, 2 and 3
	 * 		xt_ij <= x_ij
	 * 		xt_ij <= xi
	 * 		xi + x_ij - xt_ij <= 1
	 * @throws IloException 
	 * **/
	private void createLinearConstraints(boolean add2and3) throws IloException{
				
		for(int i = 1 ; i < n ; ++i)
			for(int j = i+1 ; j < n ; ++j){

				
				/* Constraints 1 (xt_ij - x_ij <= 0) */
				getCplex().addRange(new LinearFirstInequality(this, i, j).createRange());

				if(add2and3){
					/* Constraints 2 (xt_ij - x_i <= 0) */
					getCplex().addRange(new LinearSecondInequality(this, i, j).createRange());
	
					/* Constraints 3 (xi + x_ij - xt_ij <= 1) */
					getCplex().addRange(new LinearThirdInequality(this, i, j).createRange());
				}
				
			}		

	}

	@Override
	public IloNumVar nodeInClusterVar(int i, int k) throws IloException {
		return v_tilde[i][k];
	}
	
	
}
