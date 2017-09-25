package formulation;

public class RepParam extends Param{
	
	public boolean useLower = true;
	public boolean useUpper = true;
	public Triangle triangle = Triangle.USE;
	
	/**
	 * Specify how the triangle inequalities must be used
	 * USE : the triangle inequalities are always put into the model
	 * USE_LAZY : the triangle inequalities are not into the model and are generated lazily in a callback during the cutting plane step and the branch and cut.
	 * USE_LAZY_IN_BC_ONLY : Same as USE_LAZY but the triangle inequalities are generated like any other inequalities in the cutting plane step (otherwise all the triangle inequalities must be satisfied by the relaxation until the other separation algorithms are called)
	 * USE_IN_BC_ONLY : the triangle inequalities are generated like other families in the cutting plane step; they are added to the model during the branch and cut step 
	 * @author zach
	 *
	 */
	public enum Triangle{
		USE, USE_LAZY, USE_LAZY_IN_BC_ONLY, USE_IN_BC_ONLY
	}
	
	public RepParam(boolean isInt){
		super(isInt);
	}
	
	public RepParam(boolean isInt, boolean useNN_1){
		this(isInt);
		this.useNN_1 = useNN_1;	
	}

	public RepParam(boolean isInt, Triangle triangle, boolean useNN_1, boolean useLower, boolean useUpper){
		this(isInt, useNN_1);

		this.triangle = triangle;
		this.useLower = useLower;
		this.useUpper = useUpper;
	}
	
}
