package cutting_plane;

import ilog.concert.IloException;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import inequality_family.Abstract_Inequality;

import java.util.ArrayList;
import java.util.Iterator;

import mipstart.Closest_rep;
import mipstart.MIPStart;
import results.CP_Result;
import results.ComputeResults;
import separation.Abstract_Separation;
import formulation.Partition;
import formulation.PartitionWithRepresentative;

public abstract class Abstract_Cutting_Plane {

	public ArrayList<CP_Separation> sep = new ArrayList<CP_Separation>();

	public CP_Result cpresult;
	
	Double minimalTimeBeforeRemovingUntightCut = Double.MAX_VALUE;
	Integer modFindIntSolution = Integer.MAX_VALUE;
	
	public PartitionWithRepresentative rep;
	boolean reordering;
	double tilim;
		
	public Abstract_Cutting_Plane(PartitionWithRepresentative p, int i, double minimalTimeBeforeRemovingUntightCut, int modFindIntSolution, boolean reordering, double tilim){
		rep = p;
		cpresult = new CP_Result();
		cpresult.i = i;
		this.minimalTimeBeforeRemovingUntightCut = minimalTimeBeforeRemovingUntightCut;
		this.reordering = reordering;
		this.tilim = tilim;
		this.modFindIntSolution = modFindIntSolution;
	}
	
	public double eps = 1E-6;
	double oneMeps = 1.0 - eps;
	
	public double solve(){
		
		createSeparationAlgorithm();
		
		cpresult.cp_time = -rep.getCplexTime();
		
		Partition.setParam(IloCplex.IntParam.RootAlg, 2);
		
		boolean cutFound = true;		
		rep.solve();
				
		cpresult.cp_first_relaxation = rep.getObjValue2();
		cpresult.cp_iteration = 0;
		
		/* Maximal time in seconds given to the cutting plane step to improve the relaxation by at least 0.01%
		 * After that the cutting plane step is stopped (and the branch and cut takes place)
		 */
		double max_time_for_relaxation_improvement = tilim/20.0;
		
		System.out.println(max_time_for_relaxation_improvement + "s");
		
		int max_iteration_for_relaxation_improvement = 5;
		double last_relaxation_improvement_time = -rep.getCplexTime();
		double last_removing_cuts_time = -rep.getCplexTime();
		int last_improvement_iteration = 0;
		
		boolean max_time_for_relaxation_improvement_reached = false;
		boolean max_time_for_cut_removing_reached = false;
		
		double gap = Double.MAX_VALUE;
		double last_improved_relaxation = -Double.MAX_VALUE;
		
		double cplex_min_time = tilim/10.0;
		double cptilim = tilim - cplex_min_time;
		
		double bestInt = Double.MAX_VALUE;
		MIPStart bestMIP = null;
		
		boolean optimumFound = false;
		ArrayList<Abstract_Inequality> toAdd = new ArrayList<Abstract_Inequality>();
		ArrayList<IloRange> toRemove = null;
		
		double last_cp_relaxation = -Double.MAX_VALUE;
		
		boolean isInteger = false;

//TODO Ajouter des bool��ens pour eviter d'executer plusieurs fois les facettes de la formulations ? (et mettre un indice min, indice max �� ex��cuter qui evolue)
		while(cutFound && !optimumFound && !max_time_for_relaxation_improvement_reached){ 
			
			/* Compute the relaxation */
			rep.solve();
			toAdd = new ArrayList<Abstract_Inequality>();
			cutFound = false;

			/* No separation algorithm has yet found any cut at this iteration */
			Iterator<CP_Separation> algo = sep.iterator();
			while(algo.hasNext())
				algo.next().usedAtThisIteration = false;


			
			// Remove variables (part 1/2)
			if(rep.getCplexTime()+last_removing_cuts_time > minimalTimeBeforeRemovingUntightCut){
//			if(cpresult.cp_iteration % minimalTimeBeforeRemovingUntightCut == 0){	
				
				max_time_for_cut_removing_reached = true;
				toRemove= new ArrayList<IloRange>();
				
				for(CP_Separation si : sep)
					for(int i = si.addedIneq.size()-1 ; i >= 0 ; --i){
						Abstract_Inequality ai = si.addedIneq.get(i);
						if(!ai.isTight()){
							toRemove.add(ai.ilorange);
							si.remove(i);
						}
					}				
			}

			/* Remaining cutting plane time */
			double remainingTime = cptilim - (rep.getCplexTime() + cpresult.cp_time);
			
			/* Is the relaxation an integer solution? */ 
			isInteger = isInteger();
			
			last_cp_relaxation = rep.getObjValue2();
			gap = ComputeResults.improvement(last_cp_relaxation,  bestInt);
			System.out.print(Math.round(rep.getCplexTime() + cpresult.cp_time) + "s : [" + Math.round(last_cp_relaxation) + ", " + Math.round(bestInt) + "] " + (Math.round(100*gap)/100.0) + "%");

			/* If the solution is Integer, first test if a separation method from the formulation find a violated inequality (if the optimum is not found) */
			if(isInteger){
				
				Iterator<CP_Separation> it = sep.iterator();
				
				int methode = 0;
				optimumFound = true;
				
				/* For each separation method while no cut is found */
				while(optimumFound && it.hasNext()){
					
					//TODO Warning: to check the integrity, the methods must be in the cutting plane !
					CP_Separation cp = it.next();
					
					/* If the separation method corresponds to inequalities from the formulation */
					if(!cp.toAddInBB){
						
						ArrayList<Abstract_Inequality> r;
						
						try {
							r = cp.se.separate();
						
							if(r.size() > 0){
								tagInequality(r, methode);
								toAdd.addAll(r);
								
								cutFound = true;
																				
								System.out.print(" : " + sep.get(methode).se.name);	
								System.out.print(" (" + r.size() + ")");		
			
//								/* Swap the method to the third place */
//								if(reordering && methode > 1)
//									Collections.rotate(sep.subList(2, methode+1), +1);
							
								optimumFound = false;
							}
							
							cp.usedAtThisIteration = true;
							
						} catch (IloException e) {
							e.printStackTrace();
						}
					}
					
					methode++;
				}
				
			}
			
			/* If the solution is not integer */
			if(!isInteger || !optimumFound){
				
				/* Try to find an integer solution (only if the remaining time is low or each 300 iterations) */
				if(remainingTime < 200 || cpresult.cp_iteration % modFindIntSolution == 0)
				{

					Closest_rep mip_getter = new Closest_rep(rep);
					MIPStart new_mip = mip_getter.getMIPStart();
					double newInt = new_mip.evaluation;
					if(newInt < bestInt){
						bestInt = newInt;
						bestMIP = new_mip;
					}
					
					System.out.print(" : int "  + Math.round(newInt));
	
				}
			}

			double gapFromLastImprovedRelaxation = ComputeResults.improvement(last_improved_relaxation, last_cp_relaxation);
			double time_since_last_improvement = last_relaxation_improvement_time + rep.getCplexTime();
			System.out.println("\n time/gap since last relaxation improvement: " + Math.round(time_since_last_improvement) + "s " + ComputeResults.doubleToString(gapFromLastImprovedRelaxation, 4) + "%");
			if(gapFromLastImprovedRelaxation > 1E-2){
				last_improved_relaxation = last_cp_relaxation;
				last_relaxation_improvement_time = -rep.getCplexTime();
			}
			else if(time_since_last_improvement > max_time_for_relaxation_improvement){
				max_time_for_relaxation_improvement_reached = true;
				System.out.println("\n Max time for relaxation improvement reached (" + Math.round(max_time_for_relaxation_improvement) + "s)");
			}
			
//			System.out.println("\ntime: " + Math.round(rep.getCplexTime()+last_improvement_time) + " iterations: " + last_improvement_iteration + ":" + cpresult.cp_iteration);
			
//			/* If the gap has been improved by more than 1E-2 since the last gap improvement */
//			if(Math.abs(gap-last_improved_gap) > 1E-2){
//				last_improvement_time = -rep.getCplexTime();
//				last_improvement_iteration = cpresult.cp_iteration;
//				last_improved_gap = gap;
//			}
//			
//			/* If the maximum time and if the maximum number of iteration before gap improvement are reached */
//			else if(rep.getCplexTime()+last_improvement_time > max_time_for_gap_improvement
//					&& cpresult.cp_iteration - last_improvement_iteration > max_iteration_for_gap_improvement){
//				max_time_for_gap_improvement_reached = true;
////				System.out.println("\nMax time before improvement of 1E-2 of the gap reached");
//			}
			
//			if(!optimumFound && !cutFound){
			if(!optimumFound && !max_time_for_relaxation_improvement_reached){
				

				int methode = 0;
				
				/* While no cut is found and all the separation methods have not been tested and if there is still time */
//				while(!cutFound && methode < sep.size() && (remainingTime > 0 || tilim == -1.0)){
				while(methode < sep.size() && (remainingTime > 0 || tilim == -1.0)){
					
					try {
						
						CP_Separation sep_i = sep.get(methode);
						
						/* If the separation method has not yet been used in this iteration */
//						if(!sep_i.usedAtThisIteration){
						if(!sep_i.usedAtThisIteration && (!cutFound || sep_i.isQuick)){

							ArrayList<Abstract_Inequality> r = sep_i.se.separate();
							
							if(r.size() > 0){
								tagInequality(r, methode);
								toAdd.addAll(r);
								cutFound = true;
																				
			System.out.print(" : " + sep.get(methode).se.name);
////			
////		//	if(sep.get(methode).se instanceof Separation_Kp1_KL)
////		//		System.out.print( " : " + ((Separation_Kp1_KL)(sep.get(methode).se)).size);
////			
			System.out.print(" (" + r.size() + ")");			
////			
//////			/* Swap the method to the third place */
//////			if(reordering && methode > 1)
//////				Collections.rotate(sep.subList(2, methode+1), +1);
//////		//	Collections.rotate(sep.subList(0, methode+1), +1);
//////		//}
							}
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					methode++;
					remainingTime = cptilim - (rep.getCplexTime() + cpresult.cp_time);
					
				}
				
//				System.out.print("lim: " + cptilim + " actuel: " + (rep.getCplexTime() + cpresult.cp_time));

				if(!cutFound)
					System.out.println("\n--- No cut found ---");	


			}
			
			addInequality(toAdd);
			
			// Remove variables (part 2/2)
			if(max_time_for_cut_removing_reached && cutFound)		{	
//			if(cpresult.cp_iteration % minimalTimeBeforeRemovingUntightCut == 0 && cutFound)		{	
				for(IloRange i: toRemove)
					rep.remove(i);
//				ComputeResults.log(toRemove.size() + " ineq removed");

				System.out.print( " : " + toRemove.size() + " ineq removed");
				max_time_for_cut_removing_reached = false;
				last_removing_cuts_time = -rep.getCplexTime();
			}
			
			System.out.println();

			cpresult.cp_iteration++;


		}
		
//		System.out.print("Optimum found: ");
//		System.out.println(optimumFound);
//		System.out.print("Slow gap improvement: ");
//		System.out.println(max_time_for_gap_improvement_reached);
			
		
		
//Rep_then_relaxations mip_getter2 = new Rep_then_relaxations(rep, this.sep, rep.rp, rep.d);
//MIPStart new_mip2 = mip_getter2.getMIPStart();

//System.out.println(s);
//System.out.println("Getter2 :  " + new_mip2.evaluation);
		
		cpresult.cp_time += rep.getCplexTime();
		
//		rep.displayAllCoefficientSolution();
		
		/* Set the remaining time for cplex in the second step (in the case cplex is used in the second phase) */

		//TODO verifier qu'apres la phase de CP si la solution est entiere elle verifie bien toutes les inegalite de la formulation (peut arriver si on ne laisse pas assez de temps �� la phase de plans coupants
		
		for(CP_Separation s : sep)
			if(s.addedIneq.size() > 0)
				cpresult.cpCutNb.add(cpresult.new Cut(s.se.name, s.addedIneq.size()));

		System.out.println("\t" + Math.round(cpresult.cp_time) + "s " + Math.round(cpresult.firstRelaxation));
		
		if(isInteger){
			System.out.println("\nSolution is integer after cp");
			cpresult.bestRelaxation = -1.0;
			cpresult.time = 0.0;
			cpresult.n = rep.n;
			cpresult.K = rep.K;
			cpresult.node = 0;
			cpresult.separationTime = -1.0;
			cpresult.iterationNb = -1;
			cpresult.bestInt = rep.getObjValue2();
		}
		else{
			System.out.println("\nSolution is not integer after cp");
			findIntSolutionAfterCP(tilim == -1 ? -1 : tilim - cpresult.cp_time, bestMIP);
			
			System.out.println("CP relaxation: " + last_cp_relaxation + " BC relaxation: " + cpresult.bestRelaxation);
			if(cpresult.bestRelaxation < last_cp_relaxation || cpresult.bestRelaxation > 1E15)
				cpresult.bestRelaxation = last_cp_relaxation;
		}
		

		cpresult.firstRelaxation = last_cp_relaxation;
//		cpresult.log();
//		
//		for(CP_Separation se : this.sep){
//			System.out.println(se.se.name + " : " + (se.addedIneq.size() + se.removedIneq));
//		}
		
//		try {
//			rep.displayEdgeVariables(10);
//		} catch (UnknownObjectException e) {
//			e.printStackTrace();
//		} catch (IloException e) {	
//			e.printStackTrace();
//		}
		
		System.out.println("\tcptime: " + Math.round(cpresult.cp_time) + " : b&c time: " + Math.round(cpresult.time) + "s " + Math.round(cpresult.bestRelaxation));
		return cpresult.cp_time + cpresult.time;
			
	}

	
	public void tagInequality(ArrayList<Abstract_Inequality> r, int idSep){
		
		CP_Separation c = sep.get(idSep);
		
		for(Abstract_Inequality ri : r){
			c.addedIneq.add(ri);
		}
		
		c.se.added_cuts += r.size();
	}
	
	public void addInequality(ArrayList<Abstract_Inequality> r){

		for(Abstract_Inequality ri : r)
			ri.ilorange = rep.addRange(ri.getRange());
		
	}
	

	
	public abstract void createSeparationAlgorithm();
	
	/**
	 * Find an integer solution thanks to the best relaxation found by the cutting plane step.
	 * Warning : This method must update the secondStep_time attribute
	 */
	public abstract void findIntSolutionAfterCP(double remaining_time, MIPStart mipStart);
	
	public boolean isInteger(){
		
		boolean result = true;
		
		int i = 0;
		while(i < rep.n && result){
			
			int j = i+1;
			while(result && j < rep.n){
				
				if(!isInteger(rep.x(i,j))){
					result = false;
				}			
				
				++j;
			}
			
			++i;
		}
		
		return result;
	}
	
	public boolean isInteger(double d){
		return d < eps || d > oneMeps; 
	}
	
	
	
	public ArrayList<Abstract_Inequality> getTightConstraints(){
		
		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
		for(CP_Separation si : sep)
			if(si.toAddInBB){		
				for(Abstract_Inequality i : si.addedIneq){					
					if(i.isTight()){		
						result.add(i);
					}
				}
			}
				
		return result;
		
	}	
	public ArrayList<Abstract_Inequality> getAllConstraints(){
		
		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
		for(CP_Separation si : sep)
			if(si.toAddInBB){		
				for(Abstract_Inequality i : si.addedIneq){				
					result.add(i);
				}
			}
				
		return result;
		
	}

	public class CP_Separation{

		public Abstract_Separation se;
		public ArrayList<Abstract_Inequality> addedIneq = new ArrayList<Abstract_Inequality>();
		public boolean isQuick;
		public int removedIneq = 0;
		
		/**
		 * True if the inequalities separated by this family must be added in the branch and cut if the cutting plane does not return an integer solution
		 */
		public boolean toAddInBB;
		public boolean usedAtThisIteration = false;
		
		public CP_Separation(Abstract_Separation se, boolean toAdd, boolean isQuick){
			this.se = se;
			toAddInBB = toAdd;
			this.isQuick = isQuick;
		}

		public void remove(int i) {
			addedIneq.remove(i);
			removedIneq++;
		}
	}
	
}


