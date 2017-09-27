package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import solution.Solution_Representative;

/**
 * x_ij - y_ij + y_jk <= 1
 * Ensure that if i and j are not together then x_ij = 0
 * @author zach
 *
 */
public class Triangle_Inequality_x_y2 extends Abstract_Inequality{

	public int s1;
	public int t1, t2;
	
	public Triangle_Inequality_x_y2(Solution_Representative s, int s1, int t1, int t2) {
		super(s);
		this.s1 = s1;
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = s.linearNumExpr();
		try {
			expr.addTerm(+1.0, s.y_var(t1, s1));
			expr.addTerm(-1.0, s.y_var(t2, s1));
			expr.addTerm(+1.0, s.x_var(t1, t2));

		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 1.0);
	}

	@Override
	public Abstract_Inequality clone() {
		
		return new Triangle_Inequality_x_y2(s, s1, t1, t2);
		
	}

	@Override
	public double evaluate() throws IloException  {
		double result = this.s.y(t1, s1);
		result -= s.y(t2, s1);
		result += s.x(t1, t2);
		
		return result;
	}

	@Override
	public double getSlack() throws IloException  {
		return 1.0 - this.evaluate();
	}
	

	@Override
	public boolean useTilde(){return false;}

}
