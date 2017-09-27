package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

/**
 * Class of inequality which represents for a given couple of nodes i and j (i<j) the first linear constraints xt_i,j - x_i,j <= 0
 * @author zach
 *
 */
public class Linear_First_Inequality extends Abstract_Inequality{

	public int i,j;
	
	public Linear_First_Inequality(Solution_Representative s, int i, int j) {
		super(s);
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
		
		IloLinearNumExpr expr = s.linearNumExpr();
	
		try {
			expr.addTerm(1.0, s.xt_var(i, j));
			expr.addTerm(-1.0, s.x_var(i,j));
		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(expr, 0.0);
	}

	@Override
	public Abstract_Inequality clone() {
		return new Linear_First_Inequality(s, i, j);
	}

	@Override
	public double evaluate() throws IloException {
		return s.xt(i, j) - s.x(i,j);
	}

	@Override
	public double getSlack() throws IloException {
		return -this.evaluate();
	}


	@Override
	public boolean useTilde(){return true;}
}
