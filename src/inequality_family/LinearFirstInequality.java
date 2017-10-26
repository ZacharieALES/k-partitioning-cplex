package inequality_family;

import formulation.interfaces.IFEdgeVNodeClusterVNodeVConstrainedClusterNb;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

/**
 * Class of inequality which represents for a given couple of nodes i and j (i<j) the first linear constraints xt_i,j - x_i,j <= 0
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class LinearFirstInequality extends AbstractInequality<IFEdgeVNodeClusterVNodeVConstrainedClusterNb>{

	public int i,j;
	
	public LinearFirstInequality(IFEdgeVNodeClusterVNodeVConstrainedClusterNb formulation, int i, int j) {
		super(formulation, IFEdgeVNodeClusterVNodeVConstrainedClusterNb.class);
		
		if(i < j){
			this.i = i;
			this.j = j;
		}
		else{
			this.i = j;
			this.j = i;
		}
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();
	
		try {
			expr.addTerm(1.0, formulation.nodeInClusterVar(i, j));
			expr.addTerm(-1.0, formulation.edgeVar(i,j));
		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 0.0);
	}

	@Override
	public AbstractInequality<IFEdgeVNodeClusterVNodeVConstrainedClusterNb> clone() {
		return new LinearFirstInequality(formulation, i, j);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException {
		return vg.getValue(formulation.nodeInClusterVar(i, j)) - vg.getValue(formulation.edgeVar(i,j));
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException  {
		return -this.evaluate(vg);
	}
	
	public abstract class Letter{
	    public void commonMethod(){}
	}
	
	public abstract class UsesLetter<LetterType extends Letter>{
	    protected LetterType l;

	    public UsesLetter(LetterType l){this.l = l;}
	    public void commonMethod(){l.commonMethod();}
	    public abstract void setLetter(LetterType l);
	}

public class A extends Letter{
    public void methodA(){}
}

	public class UsesA extends UsesLetter<A>{

	    public UsesA(A a){super(a);}

	    public void useA(){l.methodA();}

	    @Override
	    public void setLetter(A a){
	        l = a;
	    }

	    // Specific behavior of the class
	    // ...
	}
}
