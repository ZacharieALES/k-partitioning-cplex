package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

/**
 * Represent the inequalities xj + x_i,j <= 1 (with i < j)
 * @author zach
 *
 */
public class UpperRep_Inequality extends Abstract_Inequality{

	public int i;
	public int j;
	
	public UpperRep_Inequality(Solution_Representative s, int i, int j) {
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

		Range result = null;
		IloLinearNumExpr expr = s.linearNumExpr();

		try {
			
			if(j != 2){
				expr.addTerm(1.0, s.x_var(j));
				expr.addTerm(1.0, s.x_var(j,i));
				
				result = new Range(expr, 1.0);
			}
			else{
				
				/*
				 * Constraints for j=2 : x0,1 + xi,2 - sum(i=3:n-1) <= 3 - k (same
				 * expression by replacing x3 by its substitution value, x0,1 -
				 * sum(i=3:n-1) xi + k - 2)
				 */
				expr.addTerm(1.0, s.x_var(1,0));
				expr.addTerm(1.0, s.x_var(2,i));
				
				for (int k = 3; k < s.n(); ++k)
					expr.addTerm(-1.0, s.x_var(k));
				
				result =  new Range(expr, 3.0 - s.K());
			}
			
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	@Override
	public Abstract_Inequality clone() {
		return new UpperRep_Inequality(s, i, j);
	}

	@Override
	public double evaluate() throws IloException  {
		
		double result = 0.0;
		
		if(j != 2){
			result += s.x(j);
			result += s.x(j,i);
		}
		else{
			
			result += s.x(1,0);
			result += s.x(2,i);
			
			for (int k = 3; k < s.n(); ++k)
				result -= s.x(k);
		}
		
		return result;
	}

	@Override
	public double getSlack() throws IloException  {
		
		double bound = 1.0;
		
		if(j == 2)
			bound = 3.0 - s.K();
		
		return bound - this.evaluate();
	}
	

	@Override
	public boolean useTilde(){return false;}

}
