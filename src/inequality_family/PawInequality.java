package inequality_family;

import formulation.interfaces.IFEdgeVNodeV;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

public class PawInequality extends AbstractInequality<IFEdgeVNodeV>{

	private static final long serialVersionUID = 5802423413805332348L;
	public int a,b,c,d;
	
	public PawInequality(IFEdgeVNodeV formulation, int a, int b, int c, int d) {
		super(formulation, IFEdgeVNodeV.class);


		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();

		try {
			expr.addTerm(+1.0, formulation.edgeVar(a, b));
			expr.addTerm(+1.0, formulation.edgeVar(b, c));
			expr.addTerm(-1.0, formulation.edgeVar(a, c));
			expr.addTerm(+1.0, formulation.edgeVar(c, d));
			expr.addTerm(+1.0, formulation.nodeVar(b));
			expr.addTerm(+1.0, formulation.nodeVar(c));

		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 2.0);
	}

	@Override
	public AbstractInequality<IFEdgeVNodeV> clone() {
		return new PawInequality(formulation, a, b, c, d);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException  {

		double result = vg.getValue(formulation.edgeVar(a, b));

		result += vg.getValue(formulation.edgeVar(b, c));
		result -= vg.getValue(formulation.edgeVar(a, c));
		result += vg.getValue(formulation.edgeVar(c, d));
		result += vg.getValue(formulation.nodeVar(b));
		result += vg.getValue(formulation.nodeVar(c));
		return result;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException   {
		return 2.0 - this.evaluate(vg);
	}
}
