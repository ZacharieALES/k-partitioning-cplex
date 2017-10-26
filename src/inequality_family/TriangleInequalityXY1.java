package inequality_family;

import formulation.interfaces.IFEdgeVNodeClusterV;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

/**
 * y_i_j + y_i_k - x_j_k <= 1
 * Ensure that if j and k are together then x_jk = 1
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class TriangleInequalityXY1 extends AbstractInequality<IFEdgeVNodeClusterV>{

	public int s1;
	public int t1, t2;

	public TriangleInequalityXY1(IFEdgeVNodeClusterV formulation, int s1, int t1, int t2) {
		super(formulation, IFEdgeVNodeClusterV.class);
		
		this.s1 = s1;
		this.t1 = t1;
		this.t2 = t2;
	}
	
	@Override
	public Range createRange() {

		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();
		try {
			expr.addTerm(+1.0, formulation.nodeInClusterVar(t1, s1));
			expr.addTerm(+1.0, formulation.nodeInClusterVar(t2, s1));
			expr.addTerm(-1.0, formulation.edgeVar(t1, t2));

		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 1.0);
	}

	@Override
	public AbstractInequality<IFEdgeVNodeClusterV> clone() {

		return new TriangleInequalityXY1(formulation, s1, t1, t2);

	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException  {
		double result =  vg.getValue(formulation.nodeInClusterVar(t1, s1));
		result += vg.getValue(formulation.nodeInClusterVar(t2, s1));
		result -= vg.getValue(formulation.edgeVar(t1, t2));

		return result;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException {
		return 1.0 - this.evaluate(vg);
	}
}
