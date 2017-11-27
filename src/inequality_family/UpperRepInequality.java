package inequality_family;

import formulation.interfaces.IFEdgeVNodeVClusterNb;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

/**
 * Represent the inequalities xj + x_i,j <= 1 (with i < j)
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class UpperRepInequality extends AbstractInequality<IFEdgeVNodeVClusterNb>{

	public int i;
	public int j;

	public UpperRepInequality(IFEdgeVNodeVClusterNb formulation, int i, int j) {
		super(formulation, IFEdgeVNodeVClusterNb.class);

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

		Range result = null;
		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();

		try {

			expr.addTerm(1.0, formulation.nodeVar(j));
			expr.addTerm(1.0, formulation.edgeVar(j,i));

			result = new Range(expr, 1.0);

		} catch (IloException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public AbstractInequality<IFEdgeVNodeVClusterNb> clone() {
		return new UpperRepInequality(formulation, i, j);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException  {

		// TODO check this expression
		double result = 0.0;

		result += vg.getValue(formulation.nodeVar(j));
		result += vg.getValue(formulation.edgeVar(j,i));
		return result;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException   {
		return 1.0 - this.evaluate(vg);
	}

}
