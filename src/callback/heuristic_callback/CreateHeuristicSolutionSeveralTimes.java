package callback.heuristic_callback;
import java.util.ArrayList;

import formulation.PartitionWithRepresentative;
import formulation.RepParam;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.HeuristicCallback;

/**
 * From the root relaxation of cplex, set several variables
 * Output : An instance of PartitionWithRepresentative in which several variables are set to 
 * @author zach
 *
 */
public class CreateHeuristicSolutionSeveralTimes{
	
	PartitionWithRepresentative rep;
	boolean isRoot = true;
	
	double[] value = null;
	IloNumVar[] mipStart = null;
	
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
	
	public CreateHeuristicSolutionSeveralTimes(int n, RepParam rp, double th){
		
		rp.maxNumberOfNodes = n;
		
		rep = new PartitionWithRepresentative(rp);

		rep.getCplex().turnOffCPOutput();
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
//		HeuristicCB hcb = new HeuristicCB();
//		
//		try {
//
//			rep.use(hcb);
//			if(rep.solve()){
//
//				IloNumVar[] mipStart = new IloNumVar[rep.n - 3 + rep.n * (rep.n - 1) / 2];
//				double[] value = new double[rep.n - 3 + rep.n * (rep.n - 1) / 2];
//				for(int i = 0 ; i < rep.n-3 ; ++i){
//					mipStart[i] = output.v_rep[i];
//					value[i] = rep.cplex.getValue(rep.v_rep[i]);  
//				}
//				
//				int v = rep.n-3;
//				
//				for(int i = 0 ; i < rep.n ; ++i){
//					for(int j = 0 ; j < i ; ++j){
//						mipStart[v] = output.v_edge[i][j];
//						value[v] = rep.cplex.getValue(rep.v_edge[i][j]);  
//						v++;
//					}
//				}
//				
//				output.cplex.addMIPStart(mipStart, value);
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
	
	
	public class HeuristicCB extends HeuristicCallback{

		int nbOfVarSet = 0;
		PartitionWithRepresentative output = new PartitionWithRepresentative((RepParam)rep.p);

		@Override
		protected void main() throws IloException {

			if(!isRoot)
				System.exit(0);
			
			if(isRoot)
				isRoot = false;
			
			boolean[][] isSet = new boolean[rep.n][rep.n];
			
			for(int i = 0 ; i < rep.n ; ++i)
				for(int j = 0 ; j < i ; ++j)
					isSet[i][j] = false;
				
			
			boolean modification = true;
			
			while(modification && nbRepEqualTo0 < rep.n - rep.KMax()){
				
				modification = false;
				
				ArrayList<Integer[]> al_modif = new ArrayList<Integer[]>();

				/* For each variable */
				for(int i = 0 ; i < rep.n ; ++i){

					for(int j = 0 ; j < i ; ++j){	
	
						if(!isSet[i][j] && this.getValue(rep.v_edge[i][j]) >= threshold){
							
							/* If we already know that i is not a representative set x_ij to 1 */
							if(repEqual0[i]){
								modification = true;
								nbOfVarSet++;
								
								isSet[i][j] = true;
							}
							
							/* Else if i can be set to a non representative node */
							else if(nbRepEqualTo0 < rep.n - rep.KMax()){

								modification = true;
								
								repEqual0[i] = true;
	
								nbRepEqualTo0++;
								nbOfVarSet++;
								
								isSet[i][j] = true;
								
							}
						}
					}
				}
				
				IloNumVar[] vars = new IloNumVar[al_modif.size()];
				double[] lb = new double[al_modif.size()];
				double[] ub = new double[al_modif.size()];
				
				
				for(int i = 0 ; i < al_modif.size() ; ++i){
					Integer[] t = al_modif.get(i);
					vars[i] = rep.v_edge[t[0]][t[1]];
					lb[i] = 1.0;
					ub[i] = 1.0;
				}
				
				this.solve();
				
			}
			
			/* If the proper number of non representative nodes have been found */
			if(nbRepEqualTo0 == rep.n - rep.KMax()){
				
				mipStart = new IloNumVar[rep.n - 3 + rep.n * (rep.n - 1) / 2];
				value = new double[rep.n - 3 + rep.n * (rep.n - 1) / 2];
				
				for(int i = 0 ; i < rep.n-3 ; ++i){
					mipStart[i] = output.v_rep[i];
					value[i] = this.getValue(rep.v_rep[i]);  
				}
				
				int v = rep.n-3;
				
				for(int i = 0 ; i < rep.n ; ++i){
					for(int j = 0 ; j < i ; ++j){
						mipStart[v] = output.v_edge[i][j];
						value[v] = this.getValue(rep.v_edge[i][j]);  
						v++;
					}
				}
				
//System.out.println("found");				
				
//				ArrayList<Integer> representative = new ArrayList<Integer>();
//				
//				for(int i = 0 ; i < rep.n ; ++i){
//					if(!repEqual0[i])
//						representative.add(i);					
//				}
//				
//if(representative.size() != rep.K)
//	System.out.println("ERROR : wrong number of representative");
//
//				IloNumVar[] vars = new IloNumVar[rep.n - 3 + rep.n * (rep.n - 1) / 2];
//				double[] bounds = new double[rep.n - 3 + rep.n * (rep.n - 1) / 2];
//				int v = rep.n-3;
//				
//				for(int i = 0 ; i < rep.n ; ++i){
//
//					if(i >= 3){
//						vars[i] = rep.v_rep[i-3];
//						
//						if(!repEqual0[i])
//							bounds[i-3] = 1.0;
//						else
//							bounds[i-3] = 0.0;
//					}
//					
//				}
//				
//				for(int i = 0 ; i < rep.n ; ++i)
//					for(int j = i+1 ; j < rep.n ; ++j){
//						
//						boolean found = false;
//						int k = 0;
//
//						vars[i] = rep.v_edge[i][j];
//						
//						while(!found){
//							if(this.getValue(vars[i]) >= 1.0-0.0001){
//								
//							}
//							else
//								k++;
//						}
//						
//					}
			}
			
			/* If some node still need to be set as non representative */
			else{
				//TODO Set them starting by n and decreasing
				System.out.println("not found");
//
//						System.out.println("(n,K,Rep0): (" + rep.n + "," + rep.K +  "," + nbRepEqualTo0 + ")"); 
			}
			
			this.abort();
			
			
		} // End : main class CutCB_GetRootRelaxation
		
	}// End : Class CutCB_GetRootRelaxation

}

