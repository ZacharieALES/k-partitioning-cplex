package formulation.pcenters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import cplex.Cplex;
import formulation.interfaces.IFNodeV;
import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;
import inequality_family.Range;
import variable.CplexVariableGetter;

public abstract class PCenter<CurrentParam extends PCenterParam> implements IFNodeV{
	
	/** Number of clients */
	public int N;
	
	/** Number of factories */
	public int M;
	
	/** Maximal number of built centers */
	public int p;
	
	/** Distance between the clients and the factories */
	public double[][] d;
	
	public IloNumVar[] y;
	
	protected CplexVariableGetter cvg;
	protected CurrentParam param;
	
	/**
	 * Create a p-center formulation from an input file.
	 * The input file format is:
	 * - The first line contains 3 integers separated by a space, they respectively correspond to:
	 * 		- The number of client N
	 * 		- The number of factories M
	 * 		- The value of p (I guess...)
	 * - The N next lines contain M values such that the value on line i and column j is the distance between the client number i and the factory number j.
	 * These values must be separated by spaces
	 * @param inputFile
	 * @throws IOException 
	 * @throws InvalidPCenterInputFile 
	 */
	public PCenter(CurrentParam param) throws IOException, InvalidPCenterInputFile {

		InputStream ips=new FileInputStream(param.inputFile); 
		InputStreamReader ipsr=new InputStreamReader(ips);
		BufferedReader br=new BufferedReader(ipsr);

		/* Read the first line */
		String ligne = br.readLine();
		String[] sTemp = ligne.split(" ");

		if(sTemp.length < 3){
			br.close();
			ips.close();
			throw new InvalidPCenterInputFile(param.inputFile, "The first line contains less than three values.");
		}

		N = Integer.parseInt(sTemp[0]);
		M = Integer.parseInt(sTemp[1]);
		p = Integer.parseInt(sTemp[2]);

		d = new double[N][M];
		int clientNb = 1;

		/* Read the next lines */
		while ((ligne=br.readLine())!=null && clientNb <= M){
			sTemp = ligne.split(" ");

			if(sTemp.length < M){
				br.close();
				ips.close();
				throw new InvalidPCenterInputFile(param.inputFile, "Line n°" + clientNb + " contains less than " + M + " values separated by spaces.");
			}

			for(int j = 0 ; j < M ; j++){
				double cDouble = Double.parseDouble(sTemp[j]);
				d[clientNb - 1][j] = cDouble;
			}

			clientNb++;			  

		}

		br.close();
		ips.close();

		if(clientNb - 1 < M)
			throw new InvalidPCenterInputFile(param.inputFile, "The file only contains " + (clientNb-1) + " distances lines instead of " + M);
		
		this.param = param;
		this.cvg = new CplexVariableGetter(getCplex());
	}
	
	public void createFormulation() throws IloException {

		if(!param.cplexOutput)
			getCplex().turnOffCPOutput();
		
		if(param.cplexAutoCuts)
			getCplex().removeAutomaticCuts();
		
		if(param.cplexPrimalDual)
			getCplex().turnOffPrimalDualReduction();
		
		/* Create the model */
		getCplex().iloCplex.clearModel();
		getCplex().iloCplex.clearCallbacks();
		
		/* Reinitialize the parameters to their default value */
		getCplex().setDefaults();

		if(param.tilim != -1)
			getCplex().setParam(IloCplex.DoubleParam.TiLim, Math.max(10,param.tilim));
		
		
		createClientVariables();
		createNoneClientVariables();
		createConstraints();
		createObjective();
	}

	private void createClientVariables() throws IloException {
		
		if(param.isInt)
			y = new IloIntVar[M];
		else
			y = new IloNumVar[M];
		
		for(int i = 0 ; i < M ; i++) {
			if(param.isInt)
				y[i] = getCplex().iloCplex.intVar(0, 1);
			else
				y[i] = getCplex().iloCplex.numVar(0, 1);
			
			y[i].setName("y" + i);
		}
		
	}

	protected abstract void createConstraints() throws IloException;

	protected abstract void createNoneClientVariables() throws IloException;

	protected abstract void createObjective() throws IloException;

	@Override
	public int n() {
		return N;
	}

	@Override
	public IloNumVar nodeVar(int i) throws IloException {
		return y[i];
	}

	@Override
	public Cplex getCplex() {
		return param.cplex;
	}

	@Override
	public CplexVariableGetter variableGetter() {
		return cvg;
	}

	@Override
	public void displaySolution() throws UnknownObjectException, IloException {
		displayYVariables(5);
	}
	
	protected void createAtMostPCenter() throws IloException {

		IloLinearNumExpr expr = getCplex().linearNumExpr();

		for(int m = 0 ; m < M ; m++)
			expr.addTerm(1.0, y[m]);

		getCplex().addRange(new Range(1.0, expr));
	}

	protected  void createAtLeastOneCenter() throws IloException {

		IloLinearNumExpr expr = getCplex().linearNumExpr();

		for(int m = 0 ; m < M ; m++)
			expr.addTerm(1.0, y[m]);

		getCplex().addRange(new Range(expr, p));
	}

	/**
	 * Display the value of the y variables
	 * @param numberByLine Number of variable displayed on each line
	 * @throws IloException 
	 * @throws UnknownObjectException 
	 */
	protected void displayYVariables(int numberByLine) throws UnknownObjectException, IloException {
		
		for(int i = 0 ; i < M ; i++) {
			System.out.print("y" + (i+1) + "=" + cvg.getValue(y[i]) + "\t");
			if(i % numberByLine == numberByLine - 1)
				System.out.println();
		}
	}
	
	/**
	 * Generate an instance in which:
	 * - each client and each factory has (x,y) coordinates randomly generated between 0 and <maxCoordinateValue> 
	 * @param outputFile The path at which the output file will be created 
	 * @param seed The random seed
	 * @param n The number of clients
	 * @param m The number of factories
	 * @param maxCoordinateValue Maximal value of a coordinate of a factory
	 * @param factoriesEqualToClients True if the factories potential sites are the clients sites
	 */
	public static void generateInstance(String outputFile, int seed, int p, int n, int m, int maxCoordinateValue, boolean factoriesEqualToClients) {
	
		Random r = new Random(seed);
		
		/* Generate the client coordinates */
		int[][] clientCoordinates = new int[n][2];
		
		for(int i = 0 ; i < n ; i++) {
			clientCoordinates[i][0] = r.nextInt(maxCoordinateValue);
			clientCoordinates[i][1] = r.nextInt(maxCoordinateValue);
		}
		
		/* Generate the factories coordinates */
		int[][] factoriesCoordinates;
		
		if(factoriesEqualToClients) {
			factoriesCoordinates = clientCoordinates;
			m = n;
		}
		else {
			factoriesCoordinates = new int[m][2];
			for(int i = 0 ; i < m ; i++) {
				factoriesCoordinates[i][0] = r.nextInt(maxCoordinateValue);
				factoriesCoordinates[i][1] = r.nextInt(maxCoordinateValue);
			}
		}
		
		int[][] distances = new int[n][m];
		
		for(int i = 0 ; i < n ; i++)
			for(int j = 0 ; j < n ; j++)
				distances[i][j] = (int)Math.sqrt(Math.pow(clientCoordinates[i][0]-factoriesCoordinates[j][0], 2) + Math.pow(clientCoordinates[i][1]-factoriesCoordinates[j][1], 2));
		
		 try{
		     FileWriter fw = new FileWriter(outputFile, false); // True if the text is appened at the end of the file, false if the content of the file is removed prior to write in it
		     BufferedWriter output = new BufferedWriter(fw);

		     
		     output.write(n + " " + m + " " + p + "\n");
		     
		     for(int i = 0 ; i < n ; i++) {
		    	 for(int j = 0 ; j < m ; j++)
		    		 output.write(distances[i][j] + " ");
		    	 output.write("\n");
		    	 output.flush();
		     }

		     output.close();
		 }
		 catch(IOException ioe){
		     System.out.print("Erreur : ");
		     ioe.printStackTrace();
		 }

		
		
	}
	
//	public static void main(String[] args) {
//		
//		for(int i = 5 ; i < 60 ; i+= 5)
//			for(int p = 2 ; p < 10 ; p++)
//				PCenter.generateInstance("data/pcenters/random/pc_n"+i+"_p"+p+"_i"+"_1.dat", p, p, i, i, 1000, true);
//		
//	}
}
