package formulation;

public class Param {
	
	/**
	 * Value added to all the weight of each edge in the graph (enable to quickly change the value of all the edges of a graph without generating a new input file)
	 */
	public double gapDiss = 0.0;

	public boolean isInt;
	public boolean useNN_1 = false;
	
	public Param(boolean isInt){
		this.isInt = isInt;
	}
}
