package callback.cut_callback;

import formulation.pcenters.PCRadiusIndex;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import inequality_family.Range;

public class PCRadCutCallback extends AbstractCutCallback{

	PCRadiusIndex pcri;
	
	public PCRadCutCallback(PCRadiusIndex formulation) {
		super(formulation);
		this.pcri = formulation;
		
	}

	@Override
	public void separates() throws IloException {
		
		double value = this.getValue(pcri.kStarVar());
		double ceil = Math.ceil(value);
		
//		System.out.println("Called: " + (int)ceil);
		if(ceil - value > 1E-4) {
			
			IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();
			
			expr.addTerm(1.0, pcri.kStarVar());
//			System.out.println("\n Add: " + (int)ceil );
			this.addLocalRange(new Range(ceil, expr), -1);
			
		}
		
	}

}
