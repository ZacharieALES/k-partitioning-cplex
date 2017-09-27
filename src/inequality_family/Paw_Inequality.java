package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

public class Paw_Inequality extends Abstract_Inequality{

	private static final long serialVersionUID = 5802423413805332348L;
	public int a,b,c,d;
	
	public Paw_Inequality(Solution_Representative s, int a, int b, int c, int d) {
		super(s);

		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = s.linearNumExpr();

		try {
			expr.addTerm(+1.0, s.x_var(a, b));
			expr.addTerm(+1.0, s.x_var(b, c));
			expr.addTerm(-1.0, s.x_var(a, c));
			expr.addTerm(+1.0, s.x_var(c, d));
			expr.addTerm(+1.0, s.x_var(b));
			expr.addTerm(+1.0, s.x_var(c));

		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 2.0);
	}

	@Override
	public Abstract_Inequality clone() {
		return new Paw_Inequality(s, a, b, c, d);
	}

	@Override
	public double evaluate() throws IloException  {

		double result = this.s.x(a, b);

		result += this.s.x(b, c);
		result -= this.s.x(a, c);
		result += this.s.x(c, d);
		result += this.s.x(b);
		result += this.s.x(c);
		return result;
	}

	@Override
	public double getSlack() throws IloException  {
		return 2.0 - this.evaluate();
	}

	@Override
	public boolean useTilde(){return false;}

}
