package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;

import java.util.ArrayList;

import solution.Solution_Representative;

public class ST_Inequality extends Abstract_Inequality{
	
	public ArrayList<Integer> S = new ArrayList<Integer>();
	public ArrayList<Integer> T = new ArrayList<Integer>();
	
	public boolean[] inS;
	public boolean[] inT;
	
	public ST_Inequality(Solution_Representative s){
		super(s);

		S = new ArrayList<Integer>();
		T = new ArrayList<Integer>();
		
		inS = new boolean[s.n()];
		inT = new boolean[s.n()];
	}
	
	@Override
	public Range createRange() {

		try {
		
			IloLinearNumExpr expr = s.linearNumExpr();
			
			for(int s = 0 ; s < S.size() ; ++s){
				
				for(int s2 = s+1 ; s2 < S.size() ; ++s2)
					expr.addTerm(-1.0, this.s.x_var(S.get(s),S.get(s2)));
	
				
				for(int t = 0 ; t < T.size() ; ++t)
					expr.addTerm(1.0, this.s.x_var(S.get(s),T.get(t)));
				
			}
			
			for(int t = 0 ; t < T.size() ; ++t)
				for(int t2 = t+1 ; t2 < T.size() ; ++t2)
					expr.addTerm(-1.0, this.s.x_var(T.get(t),T.get(t2)));
	
//			System.out.println("s: " + set.S);
//			System.out.println("t: " + set.T);
//			System.out.println("score: " + evaluateSets(set) + "\n");
			return new Range(expr, (double)S.size());
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	@Override
	public ST_Inequality clone() {

		ST_Inequality clone = new ST_Inequality(s);

		for(int i = 0 ; i < s.n() ; ++i){
			clone.inS[i] = inS[i];
			clone.inT[i] = inT[i];
		}
		
		for(int i = 0 ; i < S.size() ; ++i)
			clone.S.add(new Integer(S.get(i)));
		
		for(int i = 0 ; i < T.size() ; ++i)
			clone.T.add(new Integer(T.get(i)));
		
		return clone;
	}
	
	@Override
	public double evaluate() throws IloException  {
		
		double result = 0.0;
		
		for(int s = 0 ; s < S.size() ; ++s){
			
			for(int t = 0 ; t < T.size() ; ++t)
				result += this.s.x(S.get(s), T.get(t));
			
			for(int s2 = s+1 ; s2 < S.size() ; ++s2)
					result -= this.s.x(S.get(s), S.get(s2));
		}
		
		for(int t = 0 ; t < T.size() ; ++t)
			for(int t2 = t+1 ; t2 < T.size() ; ++t2)
					result -= this.s.x(T.get(t), T.get(t2));
		
		return result;
	}

	@Override
	public double getSlack() throws IloException  {
		return S.size() - this.evaluate();
	}
	

	@Override
	public boolean useTilde(){return false;}
}
