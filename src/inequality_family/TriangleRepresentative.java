package inequality_family;

import formulation.interfaces.IFEdgeVNodeV;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

/**
 * Inequality in the shape of x_s1_t1 + x_s1_t2 - x_t1_t2 + x_s1 + x_t2 + x_v_t2 s<= 2
 * with t1 < s1 < t2 and v < s1
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class TriangleRepresentative extends AbstractInequality<IFEdgeVNodeV>{

	public int s1, t1, t2, v;

	public TriangleRepresentative(IFEdgeVNodeV formulation, int t1, int v, int s1, int t2) {
		super(formulation, IFEdgeVNodeV.class);
		
		this.s1 = s1;
		this.t1 = t1;
		this.t2 = t2;
		this.v = v;
	}

	@Override
	public Range createRange() {

		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();

		try {
			expr.addTerm(+1.0, formulation.edgeVar(s1, t1));
			expr.addTerm(+1.0, formulation.edgeVar(s1, t2));
			expr.addTerm(-1.0, formulation.edgeVar(t1, t2));
			expr.addTerm(+1.0, formulation.edgeVar(v, t2));
			expr.addTerm(+1.0, formulation.nodeVar(s1));
			expr.addTerm(+1.0, formulation.nodeVar(t2));

		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 2.0);
	}

	@Override
	public AbstractInequality<IFEdgeVNodeV> clone() {
		return new TriangleRepresentative(formulation, t1, v, s1, t2);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException  {
		double result = vg.getValue(formulation.edgeVar(s1, t1));

		result += vg.getValue(formulation.edgeVar(s1, t2));
		result -= vg.getValue(formulation.edgeVar(t1, t2));
		result += vg.getValue(formulation.edgeVar(v, t2));
		result += vg.getValue(formulation.nodeVar(s1));
		result += vg.getValue(formulation.nodeVar(t2));
		return result;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException   {
		return 2.0 - this.evaluate(vg);
	}

}
