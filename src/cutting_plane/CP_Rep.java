package cutting_plane;

import java.util.ArrayList;
import java.util.HashMap;

import callback.cut_callback.FastCutCallback;
import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.PartitionWithTildes;
import formulation.RepParam;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import formulation.interfaces.IFEdgeV;
import formulation.interfaces.IFEdgeVClusterNb;
import formulation.interfaces.IFEdgeVNodeClusterV;
import formulation.interfaces.IFEdgeVNodeClusterVNodeVConstrainedClusterNb;
import formulation.interfaces.IFEdgeVNodeV;
import formulation.interfaces.IFEdgeVNodeVClusterNb;
import formulation.interfaces.IFNodeClusterV;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import inequality_family.AbstractInequality;
import mipstart.AbstractMIPStartGetter;
import mipstart.ClosestRep;
import mipstart.SolutionManagerRepresentative;
import separation.SeparationDependentSetKL;
import separation.SeparationKP1DenseHeuristicDiversification;
import separation.SeparationPawInequalitiesExhaustive;
import separation.SeparationPawInequalitiesHeuristic;
import separation.SeparationSTGrotschell;
import separation.SeparationSTKL;
import separation.SeparationSubRepresentativeSansDoublon;
import separation.SeparationTCCKLFixedSize;
import separation.SeparationTriangle;
import separation.SeparationUpperRep;
import separation.Separation_Linear;

public class CP_Rep extends AbstractCuttingPlane<PartitionWithRepresentative>{

	int MAX_CUT;

	public static boolean useTildeInBC = true;

	/**
	 * 
	 * @param p
	 * @param MAX_CUT
	 * @param i
	 * @param mods
	 * @param reordering
	 * @param tilim -1 if there is no limit on the time ; a time in seconds otherwise
	 * @throws IloException 
	 */
	public CP_Rep(RepParam p, int MAX_CUT, int i, int modRemove, int modFindIntSolution, boolean reordering, double tilim) throws IloException {
		super(p, i, modRemove, modFindIntSolution, reordering, tilim);
		formulation = (PartitionWithRepresentative)Partition.createPartition(p);
		
		this.MAX_CUT = MAX_CUT;
	}

	@Override
	public void createSeparationAlgorithm() {

		sep.add(new CP_Separation<IFEdgeVNodeV>(new SeparationPawInequalitiesExhaustive(formulation, formulation.variableGetter()), true, true));

		/* Grotschell ST */
		sep.add(new CP_Separation<IFEdgeV>(new SeparationSTGrotschell(formulation, formulation.variableGetter(), MAX_CUT), true, true));
		//		
		//		/* Labbe ST */
		//		sep.add(new CP_Separation(new Separation_ST_Labbe(rep), true, true));
		//		
		//		/* Dependent set heuristic */
		sep.add(new CP_Separation<IFEdgeVClusterNb>(new SeparationKP1DenseHeuristicDiversification(formulation, formulation.variableGetter()), true, true));
		//
		/* Paw inequalities */
		sep.add(new CP_Separation<IFEdgeVNodeV>(new SeparationPawInequalitiesHeuristic(formulation, formulation.variableGetter()), true, true));

		//		/* Sub representative inequalities */
		//		sep.add(new CP_Separation(new Separation_SubRepresentative_exhaustive(rep), true, true));
		if(formulation instanceof IFEdgeVNodeClusterV)
			sep.add(new CP_Separation<IFEdgeVNodeClusterV>(new SeparationSubRepresentativeSansDoublon((IFEdgeVNodeClusterV)formulation, formulation.variableGetter()), true, true));

		/* Triangle inequalities */
		RepParam rp = (RepParam)this.formulation.p;

		/* If the triangle inequalities are:
		 * - not in cutting plane model 
		 * - not generated lazily in the cutting plane step 
		 * - not contained in the branch and cut model */ 
		if(rp.triangle == Triangle.USE_LAZY_IN_BC_ONLY)
			sep.add(new CP_Separation<IFEdgeV>(new SeparationTriangle(formulation, formulation.variableGetter(), MAX_CUT), true, true));

		/* If the triangle inequalities are:
		 * - not in cutting plane model 
		 * - not generated lazily in the cutting plane step 
		 * - contained in the branch and cut model */ 
		else if(rp.triangle == Triangle.USE_IN_BC_ONLY)
			sep.add(new CP_Separation<IFEdgeV>(new SeparationTriangle(formulation, formulation.variableGetter(), MAX_CUT), false, true));

		//		if(!this.rep.rp.useLower)
		//			sep.add(new CP_Separation(new Separation_LowerRep(rep, MAX_CUT), false, true));

		/* Upper inequalities (formulation) */
		if(!rp.useUpper)
			sep.add(new CP_Separation<IFEdgeVNodeVClusterNb>(new SeparationUpperRep(formulation, formulation.variableGetter(), MAX_CUT), false, true));

		/* Linear inequalities (formulation tildes) */
		if(this.formulation instanceof PartitionWithTildes)
			if(!((TildeParam)formulation.p).useLinear)
				sep.add(new CP_Separation<IFEdgeVNodeClusterVNodeVConstrainedClusterNb>(new Separation_Linear((PartitionWithTildes)formulation, formulation.variableGetter(), MAX_CUT), false, true));

		/* Kernighan-Lin ST and Dependent set inequalities */
		sep.add(new CP_Separation<IFEdgeV>(new SeparationSTKL(formulation, formulation.variableGetter(), 5, true), true, false));
		sep.add(new CP_Separation<IFEdgeVClusterNb>(new SeparationDependentSetKL(formulation, formulation.variableGetter(), 5, true), true, false));
		sep.add(new CP_Separation<IFEdgeV>(new SeparationTCCKLFixedSize(formulation, formulation.variableGetter(), 2, null, true), true, false));

	}

	@Override
	public void findIntSolutionAfterCP(double remaining_time, SolutionManagerRepresentative mipStart) {


		System.out.println("Remaining time for b&c: " + remaining_time);

		/* Get the tight constraints */
		ArrayList<AbstractInequality<? extends IFormulation>> ineq =  this.getTightConstraints();
		//		ArrayList<Abstract_Inequality> ineq = this.getAllConstraints();

		formulation.p.isInt = true;
		((RepParam)formulation.p).useLower = true;
		((RepParam)formulation.p).useUpper = true;

		formulation.p.cplexOutput = true;
		if(remaining_time != -1)
			formulation.p.tilim = remaining_time;


		try {
			/* Create the partition with integer variables */
			//		if(rep instanceof Partition_with_tildes){
			if(CP_Rep.useTildeInBC){
				TildeParam tp = (TildeParam)formulation.p;
				tp.useLinear = true;
				formulation = new PartitionWithTildes(tp);
			}
			else
				formulation = new PartitionWithRepresentative((RepParam)formulation.p);


			//		}
			//		if(remaining_time != -1)
			//			rep.cp.tilim = remaining_time;

			//		MIPStart mip = getMIPStart();

			//		rep = new PartitionWithRepresentative(rep.K, rep.dissimilarity_file, rep.n, rep.cp, rep.rp);
			//		rep.cp.output = true;

			//		Partition.setParam(IloCplex.BooleanParam.MemoryEmphasis, true);
			//		Partition.setParam(IloCplex.DoubleParam.WorkMem, 1024);
//			formulation.getCplex().setParam(IloCplex.IntParam.Threads, 4);
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
				mipStart.updateFormulationAnVariables(formulation);
				mipStart.setVar();
				formulation.getCplex().addMIPStart(mipStart.var, mipStart.val);
				
				System.out.println("!!!!!!!!!!MIP START DONE!!!!!!!");
			} catch (IloException e) {
				e.printStackTrace();
			}		

			/* Add the previously tight constraints to the formulation */
			for(AbstractInequality<? extends IFormulation> i : ineq){

				/* If we use the formulation with tildes in the branch and cut
				 * or if the inequality does not use tilde variables
				 */
				if(CP_Rep.useTildeInBC || !(i.formulation instanceof IFNodeClusterV)){
					i.setFormulation(formulation);
					try {
						formulation.getCplex().addRange(i.createRange());
					} catch (IloException e) {
						e.printStackTrace();
					}
				}
				else
					System.out.println("CP_Rep: Inequalitiy not added");
			}

			//		Abstract_CutCallback acc = new Separation_Kp1_Dense_heuristic(rep).createDefaultCallback(rep);
			//		acc = null;
			//		Separation_ST_Grotschell skp = new Separation_ST_Grotschell(rep, 500);
			//		Abstract_CutCallback acc = skp.createDefaultCallback(rep);
			//		CutCallback_all acc = new CutCallback_all(rep);

			//		/* Limit the number of cutting plane passes performed by cplex at the root node (as we already performed a cutting plane step and our fast algorithms often found new violated inequalities) */
			//		Partition.setParam(IloCplex.IntParam.CutPass, 10);
			FastCutCallback acc = new FastCutCallback(formulation, 500);

			if(acc != null)
				formulation.getCplex().use(acc);

			cpresult.time = - formulation.getCplex().getCplexTime();

			formulation.getCplex().solve();
			cpresult.time += formulation.getCplex().getCplexTime();
			cpresult.getResults(cpresult.i, formulation, acc, false);
		} catch (IloException e) {
			e.printStackTrace();
		}

	}

	public SolutionManagerRepresentative getMIPStart() throws IloException{

		//		N_minus_k_fusion mipGetter = new N_minus_k_fusion(new Solution_Rep_Partition(rep));
		ClosestRep mipGetter = new ClosestRep(formulation);

		return mipGetter.getMIPStart();
	}

	public void addMIPStart(){

		int varNb = formulation.n - 3 + formulation.n * (formulation.n - 1) / 2;

		IloNumVar[] mipStart = new IloNumVar[varNb];
		double[] value = new double[varNb];

		HashMap<Integer, Integer> clusters = new HashMap<Integer, Integer>();


		/* Set all the variables to 0 */
		for(int i = 0 ; i < varNb ; ++i){
			value[i] = 0;
		}

		/* Create for each node its cluster */
		for(int i = 0 ; i < formulation.n ; ++i){

			/* Node i is in the cluster (i%rep.K) */
			clusters.put(i, i%formulation.KMax());
		}

		try {
			/* Set the representative variables from 3 to K-1 to 1 (i.e. the nodes are representative) */
			for(int i = 3 ; i < formulation.n ; ++i){

				mipStart[i-3] = formulation.nodeVar(i);

				if(i < formulation.KMax()) 
					value[i-3] = 1;
			}

			/* Set the edge variables which are in the same cluster to 1 */
			int v = formulation.n-3;

			for(int i = 0 ; i < formulation.n ; ++i){
				for(int j = 0 ; j < i ; ++j){

					mipStart[v] = formulation.edgeVar(i,j);

					/* If the nodes are in the same cluster */
					if(clusters.get(i) == clusters.get(j)){
						value[v] = 1;
					}

					v++;
				}
			}

			formulation.getCplex().addMIPStart(mipStart, value);
		}
		catch(IloException e) {e.printStackTrace();}

	}

	public boolean isInteger(){

		boolean result = true;

		try {
			int i = 0;
			while(i < formulation.n && result){

				int j = i+1;
				while(result && j < formulation.n){

					if(!isInteger(formulation.variableGetter().getValue(formulation.edgeVar(i,j)))){
						result = false;
					}	

					++j;
				}

				++i;
			}
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}	

		return result;
	}

	@Override
	public AbstractMIPStartGetter getMIPSolution() {
		
//		try {
//			PartitionWithTildes p = (PartitionWithTildes)formulation;
//			System.out.print(new ClosestRepTilde(p, p).getMIPStart().evaluation);
//		} catch (IloException e) {
//			e.printStackTrace();
//		}
		
		return new ClosestRep(formulation);
	}

	@Override
	public PartitionWithRepresentative getFormulation() {
		return formulation;
	}

}
