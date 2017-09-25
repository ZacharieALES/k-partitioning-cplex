package formulation;

public class XYParam extends Param{
	
	public boolean isSecondXYFormulation;
	
	public XYParam(String inputFile, int K){
		super(inputFile, K);
	}
	
	public XYParam(String inputFile, int K, boolean isSecondXYFormulation){
		this(inputFile, K);
		this.isSecondXYFormulation = isSecondXYFormulation;
	}

	public XYParam(String inputFile, int K, boolean isSecondXYFormulation, boolean useNN_1){
		this(inputFile, K);
		this.isSecondXYFormulation = isSecondXYFormulation;
		this.useNN_1 = useNN_1;
	}
	
	public XYParam(XYParam xyp){
		super(xyp);
		isSecondXYFormulation = xyp.isSecondXYFormulation;
	}
	
}
