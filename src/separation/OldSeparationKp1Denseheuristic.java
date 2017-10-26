package separation;
import java.util.ArrayList;
import java.util.Random;

import formulation.interfaces.IFEdgeVClusterNb;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import inequality_family.DependentSetInequality;
import variable.VariableGetter;

/**
 * Separate inequalities in the shape :
 * 	Let Z be a set. |Z| = div(Z) * K + mod(Z).
 * 	Inequality: sum_i_j_in_Z x_i,j >= (2 parmis div(Z) + 1) mod(Z) + (2 parmis div(Z)) (K - mod(Z))
 * @author zach
 *
 */
public class OldSeparationKp1Denseheuristic extends AbstractSeparation<IFEdgeVClusterNb>{

	double[] density;
	int[] twoChooseN;
	ViolatedSet bestViolatedInequality;
	Random rand = new Random();

	public OldSeparationKp1Denseheuristic(IFEdgeVClusterNb formulation, VariableGetter vg){
		super("Kp1_dense_heuristic", formulation, vg);
		

		int t = (int) Math.ceil(formulation.n()/formulation.maximalNumberOfClusters())+2;

		twoChooseN = new int[t];
		twoChooseN[0] = 0;
		twoChooseN[1] = 0;
		for(int i = 2 ; i < t ; ++i){
			twoChooseN[i] = i * (i-1) / 2; 
		}

	}

	boolean firstTime = true;	
	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate(){

		density = new double[formulation.n()];

		for(int i = 0 ; i < formulation.n() ; ++i)
			density[i] = 0.0;

		double setDensity = 0.0;

		try{
			for(int i = 0 ; i < formulation.n() ; ++i)
				for(int j = i+1 ; j < formulation.n() ; ++j){
					density[i] += vg.getValue(formulation.edgeVar(i,j));
					density[j] += vg.getValue(formulation.edgeVar(i,j));
					setDensity += vg.getValue(formulation.edgeVar(i,j));
					//				System.out.println("(i,j) : (" + i + "," + j + ") = " + x(i,j));
				}

			/* The set is first composed of all the nodes */
			int nodeInTheSet = formulation.n();

			int minDensity = getMinDensity(nodeInTheSet);
			double gap = minDensity - setDensity;

			bestViolatedInequality  = new ViolatedSet(null, 1.0, -1);
			//		bestViolatedInequality  = new ViolatedSet(null, -1.0, -1);
			//		
			int min_size = formulation.maximalNumberOfClusters() + 1;

			/* While all the set size have not been tested */
			while(nodeInTheSet > min_size ){//&& setDensity >= 1){

				int id = this.getDensestNode();
				nodeInTheSet--;
				setDensity -= density[id];
				this.updateDensities(id, vg);

				minDensity = getMinDensity(nodeInTheSet);
				gap = minDensity - setDensity;

				//			System.out.println(minDensity + "/" + setDensity);
				//			System.out.println("nodeInSet/two[div+1]/two[div]/div/mod: " + nodeInTheSet +"/" + twoChooseN[div+1] +"/" + twoChooseN[div] + "/" + div + "/" + mod);
				/* If the inequality is violated */
				if(gap > 0.0 + 1E-6){

					//				double frac = setDensity / minDensity;

					/* If the inequality is better than the best currently found */
					if(gap > bestViolatedInequality.gap)
						//				if(frac < bestViolatedInequality.gap)
					{
						bestViolatedInequality = new ViolatedSet(density, gap, minDensity);
					}

				}
				//			System.out.println("id: " + id);
				//			System.out.println("density: " + setDensity);

			}
		}catch(IloException e){e.printStackTrace();}

		ArrayList<AbstractInequality<? extends IFormulation>> ineq = new ArrayList<>();

		/* If there is a violated inequality */
		if(bestViolatedInequality.density != null){
			ineq.add(getInequality());
			//			System.out.println("size: " + nodeInTheSet);

		}
		//		else
		//			System.out.println("not found");

		return ineq;
	}

	/**
	 * Get the minimal density for a set of a given size
	 * @param setSize Size of the set
	 * @return
	 */
	private int getMinDensity(int setSize) {
		int div = (int) Math.ceil(setSize/formulation.maximalNumberOfClusters());
		int mod = setSize%formulation.maximalNumberOfClusters();

		return twoChooseN[div+1] * mod + twoChooseN[div] * (formulation.maximalNumberOfClusters() - mod);

	}

	private void updateDensities(int id, VariableGetter vg) throws IloException {

		/* Remove id from the set */
		density[id] = -1.0;

		/* For each node which is still in the set */
		for(int i = 0 ; i < formulation.n() ; ++i)
			if(density[i] != -1.0){

				/* Update its density */
				density[i] -= vg.getValue(formulation.edgeVar(i,id));
			}

	}

	public int getDensestNode(){

		int best = -1;
		double max = -1.0;
		int gap = Math.abs(rand.nextInt());

		for(int i = 0 ; i < formulation.n() ; ++i){
			int g_id = (i + gap) % formulation.n();
			if(density[g_id] > max){
				best = g_id;
				max = density[g_id];
			}
		}

		return best;

	}


	public DependentSetInequality getInequality(){

		ArrayList<Integer> Z = new ArrayList<Integer>();

		for(int i = 0 ; i < formulation.n() ; ++i)
			if(bestViolatedInequality.density[i] != -1.0)
				Z.add(i);

		DependentSetInequality d = new DependentSetInequality(formulation, Z, bestViolatedInequality.minDensity);
		//		System.out.println(Z);
		return d;

	}

	private class ViolatedSet{

		double[] density;

		/* Gap between the density of the set and it's expected bound */
		double gap;
		int minDensity;

		public ViolatedSet(double[] d, double g, int m){

			if(d != null)
				density = d.clone();
			gap = g;
			minDensity = m;
		}

	}

}
