package formulation;

public class XYParam extends Param{
	
	public boolean isSecondXYFormulation;
	
	public XYParam(boolean isInt){
		super(isInt);
	}
	
	public XYParam(boolean isInt, boolean isSecondXYFormulation){
		this(isInt);
		this.isSecondXYFormulation = isSecondXYFormulation;
	}

	public XYParam(boolean isInt, boolean isSecondXYFormulation, boolean useNN_1){
		this(isInt);
		this.isSecondXYFormulation = isSecondXYFormulation;
		this.useNN_1 = useNN_1;
	}
	
}
