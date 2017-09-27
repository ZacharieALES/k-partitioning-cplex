package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

public class Triangle_Inequality extends Abstract_Inequality {

	public int s1;
	public int t1, t2;
	
	public Triangle_Inequality(Solution_Representative s, int s1, int t1, int t2) {
		super(s);
		this.s1 = s1;
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = s.linearNumExpr();

		try {
			expr.addTerm(+1.0, s.x_var(s1, t1));
			expr.addTerm(+1.0, s.x_var(s1, t2));
			expr.addTerm(-1.0, s.x_var(t1, t2));

		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 1.0);
	}

	@Override
	public Abstract_Inequality clone() {
		
		return new Triangle_Inequality(s, s1, t1, t2);
	}

	@Override
	public double evaluate() throws IloException  {

		double result = this.s.x(s1, t1);
		result += s.x(s1, t2);
		result -= s.x(t1, t2);
		
		return result;
	}

	@Override
	public double getSlack() throws IloException  {
		return 1.0 - this.evaluate();
	}

	@Override
	public boolean useTilde(){return false;}

}
