package heuristic_callback;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.UserCutCallback;
import formulation.CplexParam;
import formulation.PartitionWithRepresentative;
import formulation.RepParam;

/**
 * From the root relaxation of cplex, set several variables
 * Output : An instance of PartitionWithRepresentative in which several variables are set to 
 * @author zach
 *
 */
public class CreateHeuristicSolution{
	
	PartitionWithRepresentative rep;
	
	/**
	 * Threshold (between 0 and 1) above which a variable will be set to 1 
	 */
	double threshold;
	
	/**
	 * Array of size rep.n
	 * repEqual0[i] is true if the variable i is not a representative in the heuristic solution (false if the variable is a representative or if we don't know yet)
	 * 
	 */
	boolean [] repEqual0;
	
	int nbRepEqualTo0 = 0;
	
	public CreateHeuristicSolution(int k, String input, int n, CplexParam cp, RepParam rp, double th){
		
		
		rep = new PartitionWithRepresentative(k, input, n, cp, rp);

		rep.turnOffCPOutput();
//		rep.removeAutomaticCuts();
		
		this.threshold = th;
		repEqual0 = new boolean[rep.n];
		
		for(int i = 0 ; i < rep.n ; ++i)
			repEqual0[i] = false;
	}
	
//	/**
//	 * Solve the problem by setting all the edge variables >= to <threshold> to 1. 
//	 * @return A PartitionWithRepresentative in which a MIPStart which corresponds to the optimal solution of the previous problem
//	 */
//	public PartitionWithRepresentative process(){
//				
//		PartitionWithRepresentative output = new PartitionWithRepresentative(rep.K, rep.dissimilarity_file, rep.n, rep.useReinforcement, rep.useNN_1, rep.isInt);
//		
//		CutCB_GetRootRelaxation grr = new CutCB_GetRootRelaxation();
//		
//		try {
//
//			rep.cplex.use(grr);
//			if(rep.solve()){
//
////				IloNumVar[] mipStart = new IloNumVar[rep.n - 3 + rep.n * (rep.n - 1) / 2];
////				double[] value = new double[rep.n - 3 + rep.n * (rep.n - 1) / 2];
////				for(int i = 0 ; i < rep.n-3 ; ++i){
////					mipStart[i] = output.v_rep[i];
////					value[i] = rep.cplex.getValue(rep.v_rep[i]);  
////				}
////				
////				int v = rep.n-3;
////				
////				for(int i = 0 ; i < rep.n ; ++i){
////					for(int j = 0 ; j < i ; ++j){
////						mipStart[v] = output.v_edge[i][j];
////						value[v] = rep.cplex.getValue(rep.v_edge[i][j]);  
////						v++;
////					}
////				}
////				
////				output.cplex.addMIPStart(mipStart, value);
//				
//			}
//			
//		} catch (IloException e) {
//			e.printStackTrace();
//		}
//		
//		return output;
//		
//	}
	
	
	public class CutCB_GetRootRelaxation extends UserCutCallback{

		int nbOfVarSet = 0;
		boolean isRoot = true;
		
		@Override
		protected void main() throws IloException {
			
			if(isRoot){
//			if(true){
				for(int i = 0 ; i < rep.n ; ++i)
					for(int j = 0 ; j < i ; ++j)
						
						if(this.getValue(rep.v_edge[i][j]) >= threshold){
							
							/* If we already know that i is not a representative set x_ij to 1 */
							if(repEqual0[i]){
								this.add(rep.eq(1.0, rep.v_edge[i][j]));
								nbOfVarSet++;
					
							}
							
							/* Else if i can be set to a non representative node */
							else if(nbRepEqualTo0 < rep.n - rep.K){
	
								this.add(rep.eq(1.0, rep.v_edge[i][j]));
								
								if(i > 2)
									this.add(rep.eq(0.0, rep.v_rep[i-3]));
								
								
								repEqual0[i] = true;
	
								
								nbRepEqualTo0++;
								nbOfVarSet++;
								
							}
						}
//System.out.println("Number of var set to 1: " + nbOfVarSet);				
				isRoot = false;
			}
			
		} // End : main class CutCB_GetRootRelaxation
		
	}// End : Class CutCB_GetRootRelaxation

}

