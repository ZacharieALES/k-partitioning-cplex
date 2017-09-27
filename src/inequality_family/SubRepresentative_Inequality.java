package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

public class SubRepresentative_Inequality extends Abstract_Inequality{

	private static final long serialVersionUID = -1837044190682764419L;
	public int i;
	public int j;
	
	public SubRepresentative_Inequality(Solution_Representative s, int i, int j){
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
			
			expr.addTerm(1.0, s.x_var(i, j));
			
			for(int k = 0 ; k <= i ; ++k)
				expr.addTerm(-1.0, s.xt_var(k, j));
			
			return new Range(expr, 0.0);
			
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public Abstract_Inequality clone() {
		return new SubRepresentative_Inequality(s, i, j);
	}

	@Override
	public double evaluate() throws IloException  {
		
		double result = 0.0;
		
		result += s.x(i,j);
		
		for(int k = 0 ; k <= i ; ++k)
			result -= s.xt(k, j);
		
		return result;
	}

	@Override
	public double getSlack() throws IloException  {
		return 0.0 - this.evaluate();
	}

	@Override
	public boolean useTilde(){return true;}
}
