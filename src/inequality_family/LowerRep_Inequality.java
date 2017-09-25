package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import solution.Solution_Representative;

public class LowerRep_Inequality extends Abstract_Inequality{

	private static final long serialVersionUID = -1837044190682764419L;
	public int l;
	
	public LowerRep_Inequality(Solution_Representative s, int l){
		super(s);
		this.l = l;
	}

	@Override
	public Range createRange() {

		IloLinearNumExpr expr = s.linearNumExpr();

		try {
			
			if(l > 2){
		
				expr.addTerm(1.0, s.x_var(l));
				
				for(int i = 0 ; i < l ; ++i)
					if(s.isTilde())
						expr.addTerm(1.0, s.xt_var(l,i));
					else
						expr.addTerm(1.0, s.x_var(l,i));
				
				if(s.isTilde())
					return new Range(1.0, expr, 1.0);
				else
					return new Range(1.0, expr);
				
			}
			else{
				
				expr.addTerm(1.0, s.x_var(1,0));
				
				if(s.isTilde()){
					expr.addTerm(1.0, s.xt_var(2,0));
					expr.addTerm(1.0, s.xt_var(2,1));
				}
				else{
					expr.addTerm(1.0, s.x_var(2,0));
					expr.addTerm(1.0, s.x_var(2,1));
				}

				for (int k = 3; k < s.n(); ++k)
					expr.addTerm(-1.0, s.x_var(k));

				if(s.isTilde())
					return new Range(3.0 - s.K(), expr, 3.0 - s.K());
				else
					return new Range(3.0 - s.K(), expr);
				
			}
			
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public Abstract_Inequality clone() {
		return new LowerRep_Inequality(s, l);
	}

	@Override
	public double evaluate() {
		
		double result = 0.0;
		
		if(l != 2){
			
			result += s.x(l);
			
			for(int i = 0 ; i < l ; ++i)
				if(s.isTilde())
					result += s.xt(l,i);
				else
					result += s.x(l,i);
			
			//TODO on veut une egalite pour les tildes pas juste une range !
		}
		else{
			
			result += s.x(1,0);
			
			if(s.isTilde()){
				result += s.xt(2,0);
				result += s.xt(2,1);
			}
			else{
				result += s.x(2,0);
				result += s.x(2,1);
			}

			for (int k = 3; k < s.n(); ++k)
				result -= s.x(k);
			
		}
		
		return result;
	}

	@Override
	public double getSlack() {
		
		double bound = 1.0;
		
		if(l == 2)
			bound = 3.0 - s.K();
		
		if(s.isTilde())
			return -Math.abs(this.evaluate() - bound);
		else
			return this.evaluate() - bound;
	}

	@Override
	public boolean useTilde(){return true;}	
}
