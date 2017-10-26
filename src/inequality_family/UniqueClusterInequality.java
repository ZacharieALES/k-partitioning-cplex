package inequality_family;

import formulation.interfaces.IFNodeClusterVClusterNb;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

/**
 * sum_j y_i_j = 1
 * @author zach
 *
 */
@SuppressWarnings("serial")
public class UniqueClusterInequality extends AbstractInequality<IFNodeClusterVClusterNb> {

	int i;

	public UniqueClusterInequality(IFNodeClusterVClusterNb formulation, int i) {
		super(formulation, IFNodeClusterVClusterNb.class);
		
		this.i = i;

	}
	
	@Override
	public Range createRange() {

		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();

		try {
			for(int j = 0 ; j < formulation.maximalNumberOfClusters() ; ++j)
				expr.addTerm(+1.0, formulation.nodeInClusterVar(i, j));

		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(1.0, expr, 1.0);

	}

	@Override
	public AbstractInequality<IFNodeClusterVClusterNb> clone() {
		return new UniqueClusterInequality(formulation, i);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException  {
		double result = 0.0;

		for(int j = 0 ; j < formulation.maximalNumberOfClusters() ; ++j)
			result += vg.getValue(formulation.nodeInClusterVar(i, j));

		return result;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException   {
		return 1.0 - this.evaluate(vg);
	}

}
