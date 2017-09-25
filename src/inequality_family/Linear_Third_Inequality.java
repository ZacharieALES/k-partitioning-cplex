package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

/**
 * Class of inequality which represents for a given couple of nodes i and j (i<j) the first linear constraints xt_i,j - x_i <= 0
 * @author zach
 *
 */
public class Linear_Third_Inequality extends Abstract_Inequality{

	public int i,j;
	
	public Linear_Third_Inequality(Solution_Representative s, int i, int j) {
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
		Range result = null;
	
		try {
			
			if(i > 2){

				expr.addTerm(1.0, s.x_var(i));
				expr.addTerm(1.0, s.x_var(i,j));
				expr.addTerm(-1.0, s.xt_var(i, j));
				
				result = new Range(expr, 1.0);
				
			}
			else if(i == 2){
				
				expr.addTerm(1.0, s.x_var(1,0));

				for (int m = 3; m < s.n(); ++m)
					expr.addTerm(-1.0, s.x_var(m));
				
				expr.addTerm(1.0, s.x_var(i, j));
				expr.addTerm(-1.0, s.xt_var(i, j));
				
				result = new Range(expr, 3 - s.K());
				
			}
			else if(i == 1){

				expr.addTerm(-1.0, s.x_var(0,1));
				expr.addTerm( 1.0, s.x_var(i,j));
				expr.addTerm(-1.0, s.xt_var(i,j));
				
				result = new Range(expr, 0.0);
			}
			
			
		} catch (IloException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public Abstract_Inequality clone() {
		return new Linear_Third_Inequality(s, i, j);
	}

	@Override
	public double evaluate() {
		
		double result = 0.0;
		

		if(i > 2){

			result += s.x(i);
			result += s.x(i,j);
			result -= s.xt(i, j);
			
		}
		else if(i == 2){
			
			result += s.x(1,0);

			for (int m = 3; m < s.n(); ++m)
				result -= s.x(m);
			
			result += s.x(i, j);
			result -= s.xt(i, j);
			
		}
		else if(i == 1){

			result -= s.x(0,1);
			result += s.x(i,j);
			result -= s.xt(i,j);
		}
		
		return result;
	}

	@Override
	public double getSlack() {
		
		double bound = 1.0;
		
		if(i == 2)
			bound = 3.0 - s.K();
		else if(i == 1)
			bound = 0.0;
		
		return bound - this.evaluate();
	}



	@Override
	public boolean useTilde(){return true;}
}
