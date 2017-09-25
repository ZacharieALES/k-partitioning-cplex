package formulation;

public class TildeParam extends RepParam{

	public boolean useLinear = true;
	
	public TildeParam(boolean isInt){
		super(isInt);
		this.useLower = true;
		this.useUpper = false;
	}
	
	public TildeParam(boolean isInt, boolean useUpper, boolean useNN_1){
		super(isInt, useNN_1);
		this.useUpper = useUpper;
		this.useLower = true;
	}
	
	public TildeParam(boolean isInt, boolean useNN_1, Triangle triangle, boolean useLower, boolean useUpper, boolean useLinear){
		super(isInt, triangle, useNN_1, useLower, useUpper);
		this.useLinear = useLinear;
	}
}
