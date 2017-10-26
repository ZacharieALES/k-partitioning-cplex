package formulation;

import cplex.Cplex;

public class XYParam extends Param{
	
	public boolean isSecondXYFormulation;
	
	public XYParam(String inputFile, Cplex cplex, int K){
		super(inputFile, cplex, K);
	}
	
	public XYParam(String inputFile, Cplex cplex, int K, boolean isSecondXYFormulation){
		this(inputFile, cplex, K);
		this.isSecondXYFormulation = isSecondXYFormulation;
	}

	public XYParam(String inputFile, Cplex cplex, int K, boolean isSecondXYFormulation, boolean useNN_1){
		this(inputFile, cplex, K);
		this.isSecondXYFormulation = isSecondXYFormulation;
		this.useNN_1 = useNN_1;
	}
	
	public XYParam(XYParam xyp){
		super(xyp);
		isSecondXYFormulation = xyp.isSecondXYFormulation;
	}
	
}
