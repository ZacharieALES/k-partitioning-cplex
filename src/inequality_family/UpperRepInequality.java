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

			if(j != 2){
				expr.addTerm(1.0, formulation.nodeVar(j));
				expr.addTerm(1.0, formulation.edgeVar(j,i));

				result = new Range(expr, 1.0);
			}
			else{

				/*
				 * Constraints for j=2 : x0,1 + xi,2 - sum(i=3:n-1) <= 3 - k (same
				 * expression by replacing x3 by its substitution value, x0,1 -
				 * sum(i=3:n-1) xi + k - 2)
				 */
				expr.addTerm(1.0, formulation.edgeVar(1,0));
				expr.addTerm(1.0, formulation.edgeVar(2,i));

				for (int k = 3; k < formulation.n(); ++k)
					expr.addTerm(-1.0, formulation.nodeVar(k));

				result =  new Range(expr, 3.0 - formulation.maximalNumberOfClusters());
			}

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

		double result = 0.0;

		if(j != 2){
			result += vg.getValue(formulation.nodeVar(j));
			result += vg.getValue(formulation.edgeVar(j,i));
		}
		else{

			result += vg.getValue(formulation.edgeVar(1,0));
			result += vg.getValue(formulation.edgeVar(2,i));

			for (int k = 3; k < formulation.n(); ++k)
				result -= vg.getValue(formulation.nodeVar(k));
		}

		return result;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException   {

		double bound = 1.0;

		if(j == 2)
			bound = 3.0 - formulation.maximalNumberOfClusters();

		return bound - this.evaluate(vg);
	}

}
