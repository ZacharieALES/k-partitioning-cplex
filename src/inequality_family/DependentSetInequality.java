package inequality_family;

import java.util.ArrayList;

import formulation.interfaces.IFEdgeVClusterNb;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

public class DependentSetInequality extends AbstractInequality<IFEdgeVClusterNb>{
	
	private static final long serialVersionUID = 3110641593173641796L;
	public final ArrayList<Integer> Z;
	public boolean[] inZ;
	public Double lowerBound = null; 

	public DependentSetInequality(IFEdgeVClusterNb formulation) {
		super(formulation, IFEdgeVClusterNb.class);
		
		Z = new ArrayList<Integer>();
		inZ = new boolean[formulation.n()];
	}
	
	public DependentSetInequality(IFEdgeVClusterNb formulation, ArrayList<Integer> Z, double lowerBound){
		super(formulation, IFEdgeVClusterNb.class);
		
		
		this.Z = Z;
		inZ = new boolean[formulation.n()];
		this.lowerBound = lowerBound;
	}
	
	public DependentSetInequality(IFEdgeVClusterNb formulation, ArrayList<Integer> Z){
		super(formulation, IFEdgeVClusterNb.class);
		
		
		this.Z = Z;
		inZ = new boolean[formulation.n()];
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
			IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();
		
			for(int s = 0 ; s < Z.size() ; ++s)
				for(int s2 = s+1 ; s2 < Z.size() ; ++s2)
					expr.addTerm(+1.0, this.formulation.edgeVar(Z.get(s),Z.get(s2)));
			
			if(lowerBound == null)
				computeLowerBound();

			return new Range(lowerBound, expr);
			
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public void computeLowerBound(){

		int div = Z.size() / formulation.maximalNumberOfClusters();
		int mod = Z.size() % formulation.maximalNumberOfClusters();

//		lowerBound = ((div+1) * div * mod + 
//				       div * (div-1) * (s.K() - mod)
//				     ) / 2.0;

		lowerBound = div/2.0 * (mod + Z.size() - formulation.maximalNumberOfClusters());
	}

	@Override
	public AbstractInequality<IFEdgeVClusterNb> clone() {

		DependentSetInequality clone = new DependentSetInequality(formulation);
		
		for(int i = 0 ; i < inZ.length ; ++i)
			clone.inZ[i] = inZ[i];
					
		for(int i = 0 ; i < Z.size() ; ++i)
			clone.Z.add(new Integer(Z.get(i)));
		
		return clone;
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException {

		double result = 0.0;
		
		for(int z = 0 ; z < Z.size() ; ++z)
			for(int z2 = z+1 ; z2 < Z.size() ; ++z2)
				result += vg.getValue(formulation.edgeVar(Z.get(z), Z.get(z2)));
		
		return result;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException {
		
		if(lowerBound == null)
			this.computeLowerBound();

		return this.evaluate(vg) - lowerBound;
	}
}
