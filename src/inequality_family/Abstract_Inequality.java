package inequality_family;

import ilog.concert.IloRange;

import java.io.Serializable;

import solution.Solution_Representative;



public abstract class Abstract_Inequality implements Serializable{
	
	private static final long serialVersionUID = -6530941531155997162L;
	public IloRange ilorange = null;
	public Range range = null;
	public double eps = 1E-6;
	public transient Solution_Representative s;
	
	public Abstract_Inequality(Solution_Representative s){
		this.s = s;
	}
	
	public Range getRange(){
		range = createRange();
		return range;
	}
	
	public abstract Range createRange();
	public abstract Abstract_Inequality clone();
	public abstract double evaluate();
	
	/**
	 * Return a value which represent the difference between the value of the inequality expression and it's bound.
	 *  
	 * @return A positive value if the inequality is not violated; a negative value otherwise.
	 */
	public abstract double getSlack();
	
	public boolean isTight(){
		return Math.abs(getSlack()) < eps;
	}
	
	/**
	 * Enable to know if this family of inequalities can be used in a PartitionWithRepresentative formulation
	 * @return True if this family of inequalities necessarily use tilde variables; false otherwise
	 */
	public abstract boolean useTilde();
	
}
