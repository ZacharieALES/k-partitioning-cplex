package separation;

import formulation.PartitionWithRepresentative;
import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.DependentSet_Inequality;
import inequality_family.ST_Inequality;

import java.util.ArrayList;

import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;

/** Abstract class for Kernighan-Lin separation algorithms. 
 * After each transformation the whole slack are not computed from scratch. The subpart of the slack which is recomputed is called the subslacks.
 * 
 * A type of cut is caracterized by:
 *		- the sets involved (e.g.: S and T in a 2-partition inequality)
 *		- the possible transformations (caracterized by their slack and their subslacks) 
 *
 * In Kernighan-Lin a given number of iterations are performed. In each iteration, several phases are carried out until the best transformation found in a phase is not better than the one found in the previous phase.
 **/
public abstract class Abstract_KL_Separation extends Abstract_Separation{
	
	/** At a given point of the algorithm, contains the transformation which would improve the most the cut slack if applied */
	protected Transformation bestNextTransformation;
	
	/** Represent the best cut found so far in an iteration */
	protected Cut bestCut;
	
	/** Slack of the currently considered cut */
	protected double currentCutSlack;
	
	/** Sets involved in the currently considered cut */
	protected Abstract_Inequality currentSets;
	
	/** Contain for each iteration the most violated cut found (if any) */
	private ArrayList<Cut> violatedCut;
		
	/** Total number of iterations */
	int iterations_nb;
	
	/** Current iteration */
	public int it;
	
	public double worstValue;
	boolean stopIteratingWhenCutFound;

	public Abstract_KL_Separation(String name, Solution_Representative ucc, int iterations, boolean stopIteratingWhenCutFound){
		super(name, ucc);
		this.iterations_nb = iterations;
		this.stopIteratingWhenCutFound = stopIteratingWhenCutFound;
		worstValue = Double.MAX_VALUE;
		
	}
		
	@Override
	public ArrayList<Abstract_Inequality> separate() throws IloException {

		violatedCut = new ArrayList<Cut>();
		
		for(it = 0 ; it < iterations_nb ; ++it){			

			Cut bestInCurrentPhase = new Cut();
			Cut bestInPreviousPhase = new Cut();
			
			/* Randomly create the first sets */
			initializeSets();
			
			boolean isOver = false;
			
int phaseNb = 0;			

			/* While a better transformation has been found in the previous phase */
			while(!isOver){

				phaseNb++;
				/* Initialize the subscores according to the current Sets and set the current cut to the currently best cut */
				initializeTransformationArrays();
				initializeSubSlacks();
				currentCutSlack = currentSets.getSlack();
				bestCut = new Cut(currentSets, currentCutSlack);
				
				/* Compute the slacks and get the best transformation */
				computeTransformationSlacks();

				/* While there is still some possible transformations */
				while(bestNextTransformation.slack != worstValue){
					
					/* Apply the transformation on the sets */
					updateSets(bestNextTransformation);

					/* Update the current slack */
					currentCutSlack += bestNextTransformation.slack;				

					/* If the current slack is lower than the best already found */
					if(currentCutSlack < bestCut.slack){
						bestCut.set(currentSets, currentCutSlack);
					}
	
////Use this condition to check that the slacks are updated properly
//if(Math.abs(currentCutSlack-currentSets.getSlack()) > eps){
//	System.out.println("Error: currentslack: " + currentCutSlack + " but currentSet real slack: " + currentSets.getSlack());
//	System.exit(0);
//}

//
//if(currentSets instanceof ST_Inequality)
//	System.out.println("abs: " + Math.round(((ST_Inequality)currentSets).evaluate()) + "\n");

					
					/* Update the subslacks */
					updateSubSlacks(bestNextTransformation);
					
					/* Compute the scores and get the best transformation */
					computeTransformationSlacks();
	
				} // End: while(bestTransformation.score != worstValue){

				bestInPreviousPhase = new Cut(bestInCurrentPhase);
				bestInCurrentPhase = new Cut(bestCut);
				
				/* If the best cut of this iteration is not better than the previous one, the iteration end */
				if(bestInCurrentPhase.slack >= bestInPreviousPhase.slack - eps)
					isOver = true;
				/* Else start a new iteration with the best sets of this iteration */
				else{
					setSets(bestInCurrentPhase.sets);
				}
				
			} // End: while(!isOver){
			
			System.out.println("\nNbOfPhases: " + phaseNb);
			
			/* If a violated inequality is found */
			if(isViolated(bestInPreviousPhase)){
								
				Cut new_vcut = new Cut(bestInPreviousPhase);
								
				boolean already_found = false;
				int j = 0;
				
				while(j < violatedCut.size() && !already_found){
					
					if(new_vcut.equals(violatedCut.get(j)))
						already_found = true;
					
					++j;					
				}
				
				if(!already_found)
					violatedCut.add(new_vcut);
				if(this.stopIteratingWhenCutFound)
					it = iterations_nb;
			}
			
		} /* End: for(it = 0 ; it < iterations_nb ; ++it){	*/
		
		ArrayList<Abstract_Inequality> ineq = new ArrayList<Abstract_Inequality>();
		
		for(int i = 0 ; i < violatedCut.size() ; ++i){
			Abstract_Inequality i_c = violatedCut.get(i).sets;
			
			ineq.add(i_c);		
		}

		return ineq;
	}
	
	/**
	 * Say if a cut is violated according to the considered family of inequalities studied
	 * @param cut The considered cut
	 * @return True if the cut is violated, false otherwise
	 */
	public boolean isViolated(Cut cut) {
		return cut.slack < 0.0 - eps;
	}
	
	/**
	 * Set the value of the considered sets
	 * @param sets
	 */
	public abstract void setSets(Abstract_Inequality sets);

	/**
	 * Update the sets according to the transformation
	 * @param t
	 */
	public abstract void updateSets(Transformation t);

	/**
	 * Initialize the different sets involved in the type of cuts considered.
	 * Note: The set variables must be initialized in this function
	 */
	public abstract void initializeSets();
	
	/** Initialize the sub score 
	 * Note: The subscore variables must be initialized in this function*/
	public abstract void initializeSubSlacks();
	
	public abstract void initializeTransformationArrays();
	
	public class Cut{
		
		public double slack;
		public Abstract_Inequality sets;
		
		public Cut(Abstract_Inequality s, double currentCutSlack){
			this.slack = currentCutSlack;
			sets = s;
		}
		
		public void set(Abstract_Inequality currentSets, double currentCutSlack) {
			this.slack = currentCutSlack;
			this.sets = currentSets.clone();
			
		}

		public Cut(){
			this.slack = worstValue;
			sets = null;
		}

		public Cut(Cut copy){
			
			slack = copy.slack;
			
//			try{
//				System.out.println(copy.sets.toString());
//			}
//			catch(NullPointerException e){
//				e.printStackTrace();
//				System.exit(0);
//			}
			
			if(copy.sets != null)
				sets = copy.sets.clone();
			else
				sets = null;
		}
			
	}
	
	/**
	 * Compute the scores of the transformations thanks to the sub scores.
	 */
	public abstract void computeTransformationSlacks();	
	
	/**
	 * Update the sub scores and set the infeasible transformations after a given transformation
	 * @param t Last transformation. 
	 */
	public abstract void updateSubSlacks(Transformation t);
	
	/**
	 * Represents one transformation
	 * @author zach
	 *
	 */
	public abstract class Transformation implements Cloneable{
		
		/**
		 * Value of the best transformation (correspond to a violated inequality if > 0)
		 */				
		double slack;
		
		public Transformation(){
			slack = worstValue;
		}
		
		public abstract Transformation clone();
		
	}

}