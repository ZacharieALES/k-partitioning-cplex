package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

/**
 * sum_j y_i_j = 1
 * @author zach
 *
 */
public class UniqueCluster_Inequality extends Abstract_Inequality {

	int i;
	
	public UniqueCluster_Inequality(Solution_Representative s, int i) {
		super(s);
		this.i = i;

	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = s.linearNumExpr();

		try {
			for(int j = 0 ; j < s.K() ; ++j)
				expr.addTerm(+1.0, s.y_var(i, j));
			
		} catch (IloException e) {
			e.printStackTrace();
		}

		return new Range(1.0, expr, 1.0);
		
	}

	@Override
	public Abstract_Inequality clone() {
		return new UniqueCluster_Inequality(s, i);
	}

	@Override
	public double evaluate() {
		double result = 0.0;
		
		for(int j = 0 ; j < s.K() ; ++j)
			result += s.y(i, j);
		
		return result;
	}

	@Override
	public double getSlack() {
		return 1.0 - this.evaluate();
	}

	@Override
	public boolean useTilde(){return false;}
}
