package cutting_plane;

import formulation.PartitionWithRepresentative;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import ilog.concert.IloException;
import main.Execution;

public class CP_Execution_Rep extends Execution{

	int MAXCUT;
	int mod;
	
	public CP_Execution_Rep(int nm, int nM2, int km, int kM2, int im, int iM2, int MAXCUT, int mod) {
		super(nm, nM2, km, kM2, im, iM2);
		this.MAXCUT = MAXCUT;
		this.mod = mod;
	}

	@Override
	public void execution() throws IloException {
		TildeParam tp = new TildeParam(null, c_k, true, Triangle.USE_LAZY_IN_BC_ONLY, true, false, false);
		tp.cplexOutput = false;
		tp.cplexPrimalDual = true;
		tp.cplexAutoCuts =true;
		tp.tilim = -1;
		
		CP_Rep cprep = new CP_Rep(tp, MAXCUT, c_i, mod, 300, true, -1);
		cprep.solve();
		cprep.cpresult.log();
		
	}

}
