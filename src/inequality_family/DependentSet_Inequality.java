package inequality_family;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;

import java.util.ArrayList;

import solution.Solution_Representative;

public class DependentSet_Inequality extends Abstract_Inequality{
	
	public final ArrayList<Integer> Z;
	public boolean[] inZ;
	public Double lowerBound = null; 

	public DependentSet_Inequality(Solution_Representative s) {
		super(s);
		
		Z = new ArrayList<Integer>();
		inZ = new boolean[s.n()];
	}
	
	public DependentSet_Inequality(Solution_Representative s, ArrayList<Integer> Z, double lowerBound){
		super(s); 
		this.Z = Z;
		inZ = new boolean[s.n()];
		this.lowerBound = lowerBound;
	}
	
	public DependentSet_Inequality(Solution_Representative s, ArrayList<Integer> Z){
		super(s); 
		this.Z = Z;
		inZ = new boolean[s.n()];
		computeLowerBound();
	}
	
	public double getLowerBound(){
//		if(lowerBound == null)
			computeLowerBound();
		
		return lowerBound;
	}

	@Override
	public Range createRange() {
		
		try {
			
			IloLinearNumExpr expr = s.linearNumExpr();
		
			for(int s = 0 ; s < Z.size() ; ++s)
				for(int s2 = s+1 ; s2 < Z.size() ; ++s2)
					expr.addTerm(+1.0, this.s.x_var(Z.get(s),Z.get(s2)));
			
			if(lowerBound == null)
				computeLowerBound();

			return new Range(lowerBound, expr);
			
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public void computeLowerBound(){

		int div = Z.size() / s.K();
		int mod = Z.size() % s.K();

//		lowerBound = ((div+1) * div * mod + 
//				       div * (div-1) * (s.K() - mod)
//				     ) / 2.0;

		lowerBound = div/2.0 * (mod + Z.size() - s.K());
	}

	@Override
	public Abstract_Inequality clone() {

		DependentSet_Inequality clone = new DependentSet_Inequality(s);
		
		for(int i = 0 ; i < inZ.length ; ++i)
			clone.inZ[i] = inZ[i];
					
		for(int i = 0 ; i < Z.size() ; ++i)
			clone.Z.add(new Integer(Z.get(i)));
		
		return clone;
	}

	@Override
	public double evaluate() throws IloException {
		
		double result = 0.0;
		
		for(int z = 0 ; z < Z.size() ; ++z)
			for(int z2 = z+1 ; z2 < Z.size() ; ++z2)
				result += this.s.x(Z.get(z), Z.get(z2));
		
		return result;
	}

	@Override
	public double getSlack() throws IloException {
		
		if(lowerBound == null)
			this.computeLowerBound();

		return this.evaluate() - lowerBound;
	}

	@Override
	public boolean useTilde(){return false;}
}
