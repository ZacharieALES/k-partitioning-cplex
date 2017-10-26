package inequality_family;

import formulation.interfaces.IFEdgeVNodeClusterV;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

public class SubRepresentativeInequality extends AbstractInequality<IFEdgeVNodeClusterV>{

	private static final long serialVersionUID = -1837044190682764419L;
	public int i;
	public int j;

	public SubRepresentativeInequality(IFEdgeVNodeClusterV formulation, int i, int j){
		super(formulation, IFEdgeVNodeClusterV.class);

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

			expr.addTerm(1.0, formulation.edgeVar(i, j));

			for(int k = 0 ; k <= i ; ++k)
				expr.addTerm(-1.0, formulation.nodeInClusterVar(k, j));

			return new Range(expr, 0.0);

		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public AbstractInequality<IFEdgeVNodeClusterV> clone() {
		return new SubRepresentativeInequality(formulation, i, j);
	}

	@Override
	public double evaluate(VariableGetter vg) throws IloException  {

		double result = 0.0;

		result += vg.getValue(formulation.edgeVar(i,j));

		for(int k = 0 ; k <= i ; ++k)
			result -= vg.getValue(formulation.nodeInClusterVar(k, j));

		return result;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException   {
		return 0.0 - this.evaluate(vg);
	}

}
