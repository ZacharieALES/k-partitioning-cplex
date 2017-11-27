package callback.cut_callback;

import formulation.pcenters.PCSC;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import inequality_family.Range;

public class PCSCCutCallback extends AbstractCutCallback{

	PCSC pcsc;
	
	public PCSCCutCallback(PCSC formulation) {
		super(formulation);
		this.pcsc = formulation;
		
	}

	@Override
	public void separates() throws IloException {
		
//		double value = this.getValue(pcsc.kStarVar());
//		double ceil = Math.ceil(value);
		
		int k = 0;
		double value = this.getValue(pcsc.nodeBVar(k));
		
		while(k < pcsc.K() && value > 1 - 1E-4) {
			++k;
			value = this.getValue(pcsc.nodeBVar(k));
		}
		
		if(k < pcsc.K() && value > 1E-4) {
			
			IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();
			
			expr.addTerm(1.0, pcsc.nodeBVar(k));
			this.addLocalRange(new Range(1.0, expr), -1);
		}		
	}

}
