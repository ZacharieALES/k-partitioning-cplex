package mipstart;

import java.util.HashMap;

import formulation.PartitionWithRepresentative;
import formulation.interfaces.IFEdgeVNodeVClusterNbEdgeW;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

public class SolutionManagerRepresentative {

	public IloNumVar[] var;
	public double[] val;

	private double evaluation = -1.0;

	/**
	 * Link between and edge and its index in <val> and <var>
	 */
	HashMap<Edge, Integer> id = new HashMap<SolutionManagerRepresentative.Edge, Integer>();
	IFEdgeVNodeVClusterNbEdgeW formulation;

	public SolutionManagerRepresentative(IFEdgeVNodeVClusterNbEdgeW formulation) throws IloException{

		this.formulation = formulation;
		var = new IloNumVar[arraySize()];
		val = new double[arraySize()];

		setValToZeroAndCreateID(formulation.n());
		setVar();
	}
	
	private int arraySize() {
		return formulation.n() - 3 + formulation.n() * (formulation.n() - 1) / 2;
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
				id.put(new Edge(i,j), v);
				//System.out.println(i + " - "+ j);				
				v++;
			}

	}

	public void setVar() throws IloException{

		var = new IloNumVar[arraySize()];

		for(int i = 3 ; i < formulation.n() ; ++i)
			var[i-3] = formulation.nodeVar(i);

		int v = formulation.n()-3;

		for(int i = 0 ; i < formulation.n() ; ++i){
			for(int j = 0 ; j < i ; ++j){
				var[v] = formulation.edgeVar(i,j);
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
		this.val[id.get(new Edge(i,j))] = value;
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

	/**
	 * Update the formulation and the variables but keep the values. The number of nodes must be the same in both formulations
	 * @param formulation2 The new formulation
	 * @throws IloException
	 */
	public void updateFormulationAnVariables(PartitionWithRepresentative formulation2) throws IloException {

		if(formulation.n() == formulation2.n()) {
			this.formulation = formulation2;
			var = new IloNumVar[arraySize()];
			setVar();
		}
	}

}
