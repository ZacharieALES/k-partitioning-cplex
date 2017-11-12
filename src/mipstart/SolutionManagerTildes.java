package mipstart;

import java.util.HashMap;

import formulation.PartitionWithTildes;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

public class SolutionManagerTildes {

	public IloNumVar[] var;
	public double[] val;

	private double evaluation = -1.0;

	/**
	 * Link between an edge and its index in <val> and <var>
	 */
	HashMap<Edge, Integer> idEdge = new HashMap<SolutionManagerTildes.Edge, Integer>();

	/**
	 * Link between a node cluster variable and its index in <val> and <var>
	 */
	HashMap<Edge, Integer> idNC = new HashMap<SolutionManagerTildes.Edge, Integer>();
	PartitionWithTildes formulation;

	public SolutionManagerTildes(PartitionWithTildes formulation) throws IloException{

		this.formulation = formulation;
		var = new IloNumVar[arraySize()];
		val = new double[arraySize()];

		setValToZeroAndCreateID(formulation.n());
		setVar();
		
//		for(int i = 0 ; i < arraySize() ; i++)
//			for(int j = 0 ; j < i ; j++)
//				if(var[i] == var[j] || var[i] == null || var[j] == null || var[i].getName().equals(var[j].getName())) {
//					System.out.println(var[i].getName() + " equals " + var[j].getName());
//					System.exit(0);
//				}
	}

	public void setValToZeroAndCreateID(int n){

		/* Set all the representative variables to zero */
		for(int i = 3 ; i < n ; ++i)
			val[i-3] = 0;

		/* Set the edge variables */
		int v = n-3;

		for(int i = 0 ; i < n ; ++i)
			for(int j = 0 ; j < i ; ++j){
				val[v] = 0;
				idEdge.put(new Edge(i,j), v);		
				v++;
			}

		/* Set the tildes variables */
		for(int i = 0 ; i < n ; ++i)
			for(int j = 1 ; j < i ; ++j){
				val[v] = 0;
				idNC.put(new Edge(i,j), v);			
				v++;
			}

	}

	public void setVar() throws IloException{

		for(int i = 3 ; i < formulation.n() ; ++i)
			var[i-3] = formulation.nodeVar(i);

		int v = formulation.n()-3;

		for(int i = 0 ; i < formulation.n() ; ++i){
			for(int j = 0 ; j < i ; ++j){
				var[v] = formulation.edgeVar(i,j);
				v++;
			}
		}

		for(int i = 0 ; i < formulation.n() ; ++i){
			for(int j = 1 ; j < i ; ++j){
				var[v] = formulation.nodeInClusterVar(i,j);
				v++;
			}
		}

	}

	/**
	 * Set a representative variable to a given value.
	 * If the representative variable corresponds to one of the first 3 nodes nothing is done (since these variables are fixed by fixing all the other variables)
	 * @param rep Id of the node concerned (between 3 and n)
	 * @param value Value of the representative variable (between 0 and 1)
	 */
	public void setRep(int rep, double value){
		if(rep > 3)
			this.val[rep-3] = value;
	}

	public void setEdge(int i, int j, double value){
		this.val[idEdge.get(new Edge(i,j))] = value;
	}

	public void setNC(int i, int j, double value){
		if(i == 0 || j == 0)
			this.val[idEdge.get(new Edge(i,j))] = value;
		else
			this.val[idNC.get(new Edge(i,j))] = value;
	}

	/**
	 * ID of an edge i,j (with i < j)
	 * @author zach
	 *
	 */
	public class Edge{
		int i;
		int j;
		int hashcode;

		public Edge(int i, int j){
			if(i < j){
				this.i = i;
				this.j = j;
			}
			else{
				this.i = j;
				this.j = i;
			}

			hashcode = 1000 * this.i + this.j;
		}

		@Override
		public int hashCode(){	
			return hashcode;
		}

		@Override
		public boolean equals(Object o){

			if (this==o)
				return true;
			if (o instanceof Edge) {
				Edge e = (Edge)o;

				return this.i == e.i 
						&& this.j == e.j;
			}
			return false;
		}
	}

	public double evaluate(){

		if(evaluation == -1.0){
			double result = 0.0;

			int v = formulation.n()-3;

			for(int i = 0 ; i < formulation.n() ; ++i){
				for(int j = 0 ; j < i ; ++j){
					if(val[v] == 1.0)
						result += formulation.edgeWeight(i, j);
					v++;
				}
			}

			return result;
		}
		else
			return evaluation;
	}
	
	public int arraySize() {return formulation.n() - 3 + 2 * formulation.n() * (formulation.n() - 1) / 2 - (formulation.n() - 1);} 

	/**
	 * Update the formulation and the variables but keep the values. The number of nodes must be the same in both formulations
	 * @param formulation2 The new formulation
	 * @throws IloException
	 */
	public void updateFormulationAnVariables(PartitionWithTildes formulation2) throws IloException {

		if(formulation.n() == formulation2.n()) {
			this.formulation = formulation2;
			var = new IloNumVar[arraySize()];
			setVar();
		}
	}

}
