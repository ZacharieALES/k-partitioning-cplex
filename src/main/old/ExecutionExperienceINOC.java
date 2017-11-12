package main.old;

import java.util.ArrayList;

import callback.cut_callback.FastCutCallback;
import cplex.Cplex;
import cutting_plane.CP_Rep;
import formulation.Partition;
import formulation.PartitionWithTildes;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import ilog.concert.IloException;
import main.Execution;
import results.ComputeResults;
import results.ResultOld;

public class ExecutionExperienceINOC extends Execution{

	public ExecutionExperienceINOC(Cplex cplex, int nm, int nM2, int km, int kM2, int im,
			int iM2) {
		super(cplex, nm, nM2, km, kM2, im, iM2);
	}

	@Override
	public void execution() throws IloException {
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
//		gapValues.add(-250.0);
//		gapValues.add(-500.0);
		
		for(int i = 0 ; i < gapValues.size() ; ++i){
			
	//	    if(c_n % 5== 0 && c_k % 2 == 0 && !(2 * c_i + 2 == c_k))
			if(c_n % 5== 0 && c_k % 2 == 0)
			{
				TildeParam ti = new TildeParam(c_input_file, cplex, c_k);
//			    System.out.println("--\nGap: " + gapValues.get(i) + "\n--");
			    
			    /* Cplex Fast Callback*/
				{
			    ResultOld r2 = new ResultOld();
			    ti.gapDiss = gapValues.get(i);
			    ti.tilim = 3600;
			    
			    PartitionWithTildes p = (PartitionWithTildes)createPartition(ti);
			    p.getCplex().use(new FastCutCallback(p, 500));
			    
			    r2.solveAndGetResults(c_i, p, false);
	
			    r2.firstRelaxation = this.getRootRelaxation(ti);
//			    r2.log();
			    System.out.println("cplex ac cb: " + Math.round(r2.bestInt)  + " " + Math.round(r2.time));
//				r2.serialize("./results/isco2/n_" + c_n + "_k_" + c_k + "_i_" + c_i + "_cplex");
				}
				Partition p;
				
				/* Cplex Sans Callback*/
				{
			    ResultOld r2 = new ResultOld();
			    ti.gapDiss = gapValues.get(i);
			    ti.tilim = 3600;
			    
			    p = createPartition(ti);
			    			    
			    r2.solveAndGetResults(c_i, p, false);
	
			    r2.firstRelaxation = this.getRootRelaxation(new TildeParam(c_input_file, p.getCplex(), c_k));
//			    r2.log();
			    System.out.println("cplex ss cb: " + Math.round(r2.bestInt)  + " " + Math.round(r2.time));
//				r2.serialize("./results/isco2/n_" + c_n + "_k_" + c_k + "_i_" + c_i + "_cplex");
				}
				
				
				// Plans coupants
				{
	 				
				ti = new TildeParam(c_input_file, p.getCplex(), c_k, true, Triangle.USE_LAZY_IN_BC_ONLY, true, false, false);
				ti.gapDiss = gapValues.get(i);
				
				/* Cutting Plane */
				CP_Rep cprep = new CP_Rep(ti, 500, c_i, 10, 10, true, 3600);
				
//				rep.setParam(IloCplex.IntParam.AdvInd, 1);
				
				cprep.solve();
				System.out.println("cp: " + Math.round(cprep.cpresult.bestInt) + " " + Math.round(cprep.cpresult.time)); 
//				cprep.cpresult.serialize("./results/isco3/n_" + c_n + "_k_" + c_k + "_i_" + c_i + "_gap_" + gapValues.get(i));
				}
	
			}
		}
	}
	
	public void printResult(){
		
		int nM = 5;
		int kM = 4;
//		int sM = 4;
		int sM = 3;
		
		ResultOld[][][] result = new ResultOld[nM][kM][sM];
		int[] nValue = new int[nM];
//		nValue[0] = 20;
//		nValue[1] = 25;
//		nValue[2] = 30;
//		nValue[3] = 35;
//		nValue[4] = 40;

		nValue[0] = 30;
		nValue[1] = 35;
		nValue[2] = 40;
		nValue[3] = 45;
		nValue[4] = 50;
		
		int[] kValue = new int[kM];
		kValue[0] = 4;
		kValue[1] = 6;
		kValue[2] = 8;
		kValue[3] = 10;
		
		String[] solutionHeader = new String[sM];
//		solutionHeader[0] = "Cplex rep";
		solutionHeader[0] = "Branch and cut";
		solutionHeader[1] = "Cutting plane 1";
		solutionHeader[2] = "Cutting plane 2";
		
		for(int n = 0 ; n < nM ; ++n)
			for(int k = 0 ; k < kM ; ++k){
				
				int i = kValue[k] / 2 - 1;
//				result[n][k][0] = Result.unserialize("./results/isco/n_" + nValue[n] + "_k_" + kValue[k] + "_i_" + i + "_cplex_rep");
				result[n][k][0] = ResultOld.unserialize("./results/isco/n_" + nValue[n] + "_k_" + kValue[k] + "_i_" + i + "_cplex_tilde");
				result[n][k][1] = ResultOld.unserialize("./results/isco/n_" + nValue[n] + "_k_" + kValue[k] + "_i_" + i + "_cp_no_reord");
				result[n][k][2] = ResultOld.unserialize("./results/isco/n_" + nValue[n] + "_k_" + kValue[k] + "_i_" + i + "_cp_reord");
				
			}
		
		ComputeResults c = new ComputeResults();
		System.out.println(ComputeResults.get3DTables(c.new CP_ResultParamToDisplay(), result, nValue, kValue, solutionHeader));
//		System.out.println(ComputeResults.get3DHistogram(result, nValue, kValue, solutionHeader));
		
		
		
		
	}
	
	public void printResult2(){
		
		int nM = 5;
		int kM = 4;
		
		ResultOld[][][] resultCplex = new ResultOld[nM][kM][3];
		ResultOld[][][] resultCP = new ResultOld[nM][kM][3];
		int[] nValue = new int[nM];

		nValue[0] = 30;
		nValue[1] = 35;
		nValue[2] = 40;
		nValue[3] = 45;
		nValue[4] = 50;
		
		int[] kValue = new int[kM];
		kValue[0] = 4;
		kValue[1] = 6;
		kValue[2] = 8;
		kValue[3] = 10;
		
		for(int n = 0 ; n < nM ; ++n)
			for(int k = 0 ; k < kM ; ++k){
				
				double min_cplex = Double.MAX_VALUE;
				double max_cplex = Double.MIN_VALUE;
				double mean_cplex = 0.0;
				
				double min_cp = Double.MAX_VALUE;
				double max_cp = Double.MIN_VALUE;
				double mean_cp = 0.0;
				 
				for(int i = 0 ; i < 20 ; ++i){
					
					ResultOld resCplex = ResultOld.unserialize("./results/isco2/n_" + nValue[n] + "_k_" + kValue[k] + "_i_" + i + "_cplex");
					double gap_cplex = ComputeResults.improvement(resCplex.bestRelaxation, resCplex.bestInt);
					mean_cplex += gap_cplex;
					if(gap_cplex < min_cplex)
						min_cplex = gap_cplex;
					if(gap_cplex > max_cplex)
						max_cplex = gap_cplex;
					
					mean_cplex /= 20.0;
					
					ResultOld resCp = ResultOld.unserialize("./results/isco2/n_" + nValue[n] + "_k_" + kValue[k] + "_i_" + i + "_cplex");
					double gap_cp = ComputeResults.improvement(resCplex.bestRelaxation, resCplex.bestInt);
					mean_cp += gap_cp;
					if(gap_cp < min_cp)
						min_cp = gap_cp;
					if(gap_cp > max_cp)
						max_cp = gap_cp;
					
					mean_cp /= 20.0;
					
					
					
					resultCP[n][k][i] = ResultOld.unserialize("./results/isco2/n_" + nValue[n] + "_k_" + kValue[k] + "_i_" + i + "_cp");
				}
				
			}
		
		ComputeResults c = new ComputeResults();
//		System.out.println(ComputeResults.get3DTables(c.new CP_ResultParamToDisplay(), result, nValue, kValue, solutionHeader));
//		System.out.println(ComputeResults.get3DHistogram(result, nValue, kValue, solutionHeader));
		
		
		
		
	}

}
