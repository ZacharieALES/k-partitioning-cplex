package cutting_plane;

import java.util.ArrayList;
import java.util.HashMap;

import cut_callback.Fast_cut_callback;
import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.Partition_with_tildes;
import formulation.RepParam;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import inequality_family.Abstract_Inequality;
import mipstart.Closest_rep;
import mipstart.MIPStart;
import separation.Separation_DependentSet_KL;
import separation.Separation_KP1_Dense_Heuristic_Diversification;
import separation.Separation_Linear;
import separation.Separation_Paw_Inequalities_exhaustive;
import separation.Separation_Paw_Inequalities_heuristic;
import separation.Separation_ST_Grotschell;
import separation.Separation_ST_KL;
import separation.Separation_SubRepresentative_sans_doublon;
import separation.Separation_TCC_KL_Fixed_size;
import separation.Separation_Triangle;
import separation.Separation_UpperRep;

public class CP_Rep extends Abstract_Cutting_Plane{

	int MAX_CUT;
	
	public static boolean useTildeInBC = true;
	
	/**
	 * 
	 * @param p
	 * @param MAX_CUT
	 * @param i
	 * @param mod
	 * @param reordering
	 * @param tilim -1 if there is no limit on the time ; a time in seconds otherwise
	 */
	public CP_Rep(RepParam p, int MAX_CUT, int i, int modRemove, int modFindIntSolution, boolean reordering, double tilim) {
		super(p, i, modRemove, modFindIntSolution, reordering, tilim);
		this.MAX_CUT = MAX_CUT;
	}

	@Override
	public void createSeparationAlgorithm() {
		
		sep.add(new CP_Separation(new Separation_Paw_Inequalities_exhaustive(rep), true, true));

		/* Grotschell ST */
		sep.add(new CP_Separation(new Separation_ST_Grotschell(rep, MAX_CUT), true, true));
//		
//		/* Labbe ST */
//		sep.add(new CP_Separation(new Separation_ST_Labbe(rep), true, true));
//		
//		/* Dependent set heuristic */
		sep.add(new CP_Separation(new Separation_KP1_Dense_Heuristic_Diversification(rep), true, true));
//
		/* Paw inequalities */
		sep.add(new CP_Separation(new Separation_Paw_Inequalities_heuristic(rep), true, true));
		
//		/* Sub representative inequalities */
//		sep.add(new CP_Separation(new Separation_SubRepresentative_exhaustive(rep), true, true));
		sep.add(new CP_Separation(new Separation_SubRepresentative_sans_doublon(rep), true, true));

		/* Triangle inequalities */
		RepParam rp = (RepParam)this.rep.p;
		
		/* If the triangle inequalities are:
		 * - not in cutting plane model 
		 * - not generated lazily in the cutting plane step 
		 * - not contained in the branch and cut model */ 
		if(rp.triangle == Triangle.USE_LAZY_IN_BC_ONLY)
			sep.add(new CP_Separation(new Separation_Triangle(rep, MAX_CUT), true, true));
		
		/* If the triangle inequalities are:
		 * - not in cutting plane model 
		 * - not generated lazily in the cutting plane step 
		 * - contained in the branch and cut model */ 
		else if(rp.triangle == Triangle.USE_IN_BC_ONLY)
			sep.add(new CP_Separation(new Separation_Triangle(rep, MAX_CUT), false, true));
		
//		if(!this.rep.rp.useLower)
//			sep.add(new CP_Separation(new Separation_LowerRep(rep, MAX_CUT), false, true));
				
		/* Upper inequalities (formulation) */
		if(!rp.useUpper)
			sep.add(new CP_Separation(new Separation_UpperRep(rep, MAX_CUT), false, true));
		
		/* Linear inequalities (formulation tildes) */
		if(this.rep instanceof Partition_with_tildes)
			if(!((TildeParam)rep.p).useLinear)
				sep.add(new CP_Separation(new Separation_Linear(rep, MAX_CUT), false, true));

		/* Kernighan-Lin ST and Dependent set inequalities */
		sep.add(new CP_Separation(new Separation_ST_KL(rep, 5, true), true, false));
		sep.add(new CP_Separation(new Separation_DependentSet_KL(rep, 5, true), true, false));
		sep.add(new CP_Separation(new Separation_TCC_KL_Fixed_size(rep, 2, null, true), true, false));
				
	}

	@Override
	public void findIntSolutionAfterCP(double remaining_time, MIPStart mipStart) {

		
		System.out.println("Remaining time for b&c: " + remaining_time);
		
		/* Get the tight constraints */
		ArrayList<Abstract_Inequality> ineq =  this.getTightConstraints();
//		ArrayList<Abstract_Inequality> ineq = this.getAllConstraints();

		rep.p.isInt = true;
		((RepParam)rep.p).useLower = true;
		((RepParam)rep.p).useUpper = true;
		
		if(remaining_time != -1)
			rep.p.tilim = remaining_time;
		
		/* Create the partition with integer variables */
//		if(rep instanceof Partition_with_tildes){
		if(CP_Rep.useTildeInBC){
			TildeParam tp = (TildeParam)rep.p;
			tp.useLinear = true;
			rep = new Partition_with_tildes(tp);
		}
		else
			rep = new PartitionWithRepresentative((RepParam)rep.p);
		
	
//		}
//		if(remaining_time != -1)
//			rep.cp.tilim = remaining_time;
		
//		MIPStart mip = getMIPStart();
		
//		rep = new PartitionWithRepresentative(rep.K, rep.dissimilarity_file, rep.n, rep.cp, rep.rp);
//		rep.cp.output = true;
		
//		Partition.setParam(IloCplex.BooleanParam.MemoryEmphasis, true);
//		Partition.setParam(IloCplex.DoubleParam.WorkMem, 1024);
		Partition.setParam(IloCplex.IntParam.Threads, 4);
//		Partition.setParam(IloCplex.IntParam.CutPass, 100);
//		Partition.setParam(IloCplex.IntParam.Cliques, 1);
//		Partition.setParam(IloCplex.IntParam.Covers, 1);
//		Partition.setParam(IloCplex.IntParam.DisjCuts, 1);
//		Partition.setParam(IloCplex.IntParam.FlowCovers, 1);
//		Partition.setParam(IloCplex.IntParam.FlowPaths, 1);
//		Partition.setParam(IloCplex.IntParam.GUBCovers, 1);
//		Partition.setParam(IloCplex.IntParam.ImplBd, 1);
//		Partition.setParam(IloCplex.IntParam.MCFCuts, 1);
//		Partition.setParam(IloCplex.IntParam.MIRCuts, 1);
//		Partition.setParam(IloCplex.IntParam.ZeroHalfCuts, 1);
//		Partition.setParam(IloCplex.IntParam.FracCuts, 2);
//		
//		Partition.setParam(IloCplex.IntParam.MIPEmphasis, 1);
		
		try {
			mipStart.setVar(rep);
			rep.addMIPStart(mipStart.var, mipStart.val);
		} catch (IloException e) {
			e.printStackTrace();
		}		
		
		/* Add the previously tight constraints to the formulation */
		for(Abstract_Inequality i : ineq){
			
			/* If we use the formulation with tildes in the branch and cut
			 * or if the inequality does not use tilde variables
			 */
			if(CP_Rep.useTildeInBC || !i.useTilde()){
				i.s = rep;
				rep.addRange(i.createRange());
			}
		}
		
//		Abstract_CutCallback acc = new Separation_Kp1_Dense_heuristic(rep).createDefaultCallback(rep);
//		acc = null;
//		Separation_ST_Grotschell skp = new Separation_ST_Grotschell(rep, 500);
//		Abstract_CutCallback acc = skp.createDefaultCallback(rep);
//		CutCallback_all acc = new CutCallback_all(rep);

//		/* Limit the number of cutting plane passes performed by cplex at the root node (as we already performed a cutting plane step and our fast algorithms often found new violated inequalities) */
//		Partition.setParam(IloCplex.IntParam.CutPass, 10);
		Fast_cut_callback acc = new Fast_cut_callback(rep, 500);
		
		if(acc != null)
			rep.use(acc);
		
		cpresult.time = - rep.getCplexTime();
		rep.solve();
		cpresult.time += rep.getCplexTime();
		cpresult.getResults(cpresult.i, rep, acc, false);

	}
	
	public MIPStart getMIPStart() throws IloException{

//		N_minus_k_fusion mipGetter = new N_minus_k_fusion(new Solution_Rep_Partition(rep));
		Closest_rep mipGetter = new Closest_rep(rep);
		
		return mipGetter.getMIPStart();
	}
	
	public void addMIPStart(){
		
			int varNb = rep.n - 3 + rep.n * (rep.n - 1) / 2;

			IloNumVar[] mipStart = new IloNumVar[varNb];
			double[] value = new double[varNb];

			HashMap<Integer, Integer> clusters = new HashMap<Integer, Integer>();
			

			/* Set all the variables to 0 */
			for(int i = 0 ; i < varNb ; ++i){
				value[i] = 0;
			}
			
			/* Create for each node its cluster */
			for(int i = 0 ; i < rep.n ; ++i){
				
				/* Node i is in the cluster (i%rep.K) */
				clusters.put(i, i%rep.K());
			}
			
			/* Set the representative variables from 3 to K-1 to 1 (i.e. the nodes are representative) */
			for(int i = 3 ; i < rep.n ; ++i){
				
				mipStart[i-3] = rep.x_var(i);
			
				if(i < rep.K()) 
					value[i-3] = 1;
			}
			
			/* Set the edge variables which are in the same cluster to 1 */
			int v = rep.n-3;
			
			for(int i = 0 ; i < rep.n ; ++i){
				for(int j = 0 ; j < i ; ++j){
					
					mipStart[v] = rep.x_var(i,j);
					
					/* If the nodes are in the same cluster */
					if(clusters.get(i) == clusters.get(j)){
						value[v] = 1;
					}
					
					v++;
				}
			}
			
			rep.addMIPStart(mipStart, value);
			
	}

}
