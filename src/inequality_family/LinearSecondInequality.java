package inequality_family;

import formulation.interfaces.IFEdgeVNodeClusterVNodeVConstrainedClusterNb;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

/**
 * Class of inequality which represents for a given couple of nodes i and j (i<j) the first linear constraints xt_i,j - x_i <= 0
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class LinearSecondInequality extends AbstractInequality<IFEdgeVNodeClusterVNodeVConstrainedClusterNb>{

	public int i,j;

	public LinearSecondInequality(IFEdgeVNodeClusterVNodeVConstrainedClusterNb formulation, int i, int j) {
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
		Range result = null;

		try {

			expr.addTerm(1.0, formulation.nodeInClusterVar(i, j));
			expr.addTerm(-1.0, formulation.nodeVar(i));

			result = new Range(expr, 0.0);

		} catch (IloException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public AbstractInequality<IFEdgeVNodeClusterVNodeVConstrainedClusterNb> clone() {
		return new LinearSecondInequality(formulation, i, j);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException {

		double result = 0.0;

		result += vg.getValue(formulation.nodeInClusterVar(i, j));
		result -= vg.getValue(formulation.nodeVar(i));

		return result;
	}


	@Override
	public double getSlack(VariableGetter vg) throws IloException{

		double bound = 0.0;
		return bound - this.evaluate(vg);
	}

}
