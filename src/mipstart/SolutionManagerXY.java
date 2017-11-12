package mipstart;

import java.util.HashMap;

import formulation.PartitionXY;
import formulation.PartitionXY2;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

public class SolutionManagerXY {

	public IloNumVar[] var;
	public double[] val;

	private double evaluation = -1.0;

	/**
	 * Link between and edge and its index in <val> and <var>
	 */
	HashMap<Edge, Integer> idEdge = new HashMap<SolutionManagerXY.Edge, Integer>();
	HashMap<Edge, Integer> idNC = new HashMap<SolutionManagerXY.Edge, Integer>();
	PartitionXY formulation;

	public SolutionManagerXY(PartitionXY formulation) throws IloException{

		this.formulation = formulation;
		var = new IloNumVar[arraySize()];
		val = new double[arraySize()];

		setValToZeroAndCreateID(formulation.n());
		setVar();
	}
	
	

	public void setValToZeroAndCreateID(int n){


		/* Set the edge variables */
		int v = 0;

		for(int i = 0 ; i < n ; ++i)
			for(int j = 0 ; j < i ; ++j){
				val[v] = 0;
				idEdge.put(new Edge(i,j), v);
				//System.out.println(i + " - "+ j);				
				v++;
			}
		
		/* Set the node cluster variables */
		for(int i = 0 ; i < n ; ++i)
			for(int j = 0 ; j < formulation.maxClusterId ; ++j){
				val[v] = 0;
				idNC.put(new Edge(i,j), v);
				//System.out.println(i + " - "+ j);				
				v++;
			}
		
	}

	private int arraySize() {
		return formulation.n() * (formulation.n() - 1) / 2 + formulation.n() * formulation.maxClusterId;
	}
	
	public void setVar() throws IloException{

		var = new IloNumVar[arraySize()];

		int v = 0;

		for(int i = 0 ; i < formulation.n() ; ++i){
			for(int j = 0 ; j < i ; ++j){
				var[v] = formulation.edgeVar(i,j);
				v++;
			}
		}

		
		/* Set the node cluster variables */
		for(int i = 0 ; i < formulation.n ; ++i)
			for(int j = 0 ; j < formulation.maxClusterId ; ++j){
				var[v] = formulation.nodeInClusterVar(i, j);		
				v++;
			}

	}

	public void setEdge(int i, int j, double value){
		this.val[idEdge.get(new Edge(i,j))] = value;
	}

	public void setNC(int i, int j, double value){
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

	/**
	 * Update the formulation and the variables but keep the values. The number of nodes must be the same in both formulations
	 * @param formulation2 The new formulation
	 * @throws IloException
	 */
	public void updateFormulationAnVariables(PartitionXY2 formulation2) throws IloException {

		if(formulation.n() == formulation2.n()) {
			this.formulation = formulation2;
			var = new IloNumVar[arraySize()];
			setVar();
		}
	}

}
