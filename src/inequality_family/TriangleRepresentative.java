package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

/**
 * Inequality in the shape of x_s1_t1 + x_s1_t2 - x_t1_t2 + x_s1 + x_t2 + x_v_t2 s<= 2
 * with t1 < s1 < t2 and v < s1
 * @author zach
 *
 */
public class TriangleRepresentative extends Abstract_Inequality{
	
	public int s1, t1, t2, v;

	public TriangleRepresentative(Solution_Representative s, int t1, int v, int s1, int t2) {
		super(s);
		this.s1 = s1;
		this.t1 = t1;
		this.t2 = t2;
		this.v = v;
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = s.linearNumExpr();

		try {
			expr.addTerm(+1.0, s.x_var(s1, t1));
			expr.addTerm(+1.0, s.x_var(s1, t2));
			expr.addTerm(-1.0, s.x_var(t1, t2));
			expr.addTerm(+1.0, s.x_var(v, t2));
			expr.addTerm(+1.0, s.x_var(s1));
			expr.addTerm(+1.0, s.x_var(t2));

		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 2.0);
	}

	@Override
	public Abstract_Inequality clone() {
		return new TriangleRepresentative(s, t1, v, s1, t2);
	}

	@Override
	public double evaluate() throws IloException  {
		double result = this.s.x(s1, t1);

		result += this.s.x(s1, t2);
		result -= this.s.x(t1, t2);
		result += this.s.x(v, t2);
		result += this.s.x(s1);
		result += this.s.x(t2);
		return result;
	}

	@Override
	public double getSlack() throws IloException  {
		return 2.0 - this.evaluate();
	}
	

	@Override
	public boolean useTilde(){return false;}

}
