package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;

import java.util.ArrayList;

import solution.Solution_Representative;

public class TCC_Inequality extends Abstract_Inequality{
	
	public ArrayList<Integer> C = new ArrayList<Integer>();
	public int[] inC;
	public int p;

	public TCC_Inequality(Solution_Representative s, int size) {
		super(s);
		inC = new int[s.n()];
		C = new ArrayList<Integer>();
		p = (size-1)/2;
	}

	@Override
	public Range createRange() {

		Range result = null;
		
		
		try {
			IloLinearNumExpr expr = s.linearNumExpr();
//		System.out.println(C.size());
//		System.out.println(C);
			for(int c = 0 ; c < C.size(); ++c){
				expr.addTerm(+1.0, s.x_var(C.get(c),C.get((c+1)%C.size())));
				expr.addTerm(-1.0, s.x_var(C.get(c),C.get((c+2)%C.size())));
			}

			result = new Range(expr, p);

//if(2*p + 1 != C.size()){
//	System.out.println("Taille C : " + C.size());
//	System.out.println(expr.toString() + " <= " + p);			
//	System.exit(0);
//}
		} catch (IloException e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	public Abstract_Inequality clone() {
		
		TCC_Inequality clone = new TCC_Inequality(s, C.size());
		
		for(int i = 0 ; i < inC.length ; ++i)
			clone.inC[i] = inC[i];

		for(int i = 0 ; i < C.size() ; ++i)
			clone.C.add(new Integer(C.get(i)));
		
		return clone;
	}

	@Override
	public double evaluate() {

		double result = 0.0;
		
		for(int i = 0 ; i < C.size() ; ++i){

			result += this.s.x(C.get((i+1)%C.size()),C.get(i));
			result -= this.s.x(C.get((i+2)%C.size()),C.get(i));
			
		}
		
		return result;
	}

	@Override
	public double getSlack() {
		return p - this.evaluate();
	}


	@Override
	public boolean useTilde(){return false;}
}
