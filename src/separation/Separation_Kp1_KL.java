package separation;
import inequality_family.Abstract_Inequality;
import inequality_family.DependentSet_Inequality;

import java.util.Random;

import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;

public class Separation_Kp1_KL extends Abstract_KL_Separation{

	/** Array which contain the cost to exchange two nodes
	 * exchange[i][j] is equal worstValue if the transformation have already been done or if the nodes i and j have been in the same set
	 */
	double[][] exchange;
	
	public int size;
	Double lowerBound = null;
	
	/* Contain for each vertex i, the sum_z_in_S x(i,z) */
	private double[] xiZ;
	
	public Separation_Kp1_KL(Solution_Representative ucc, int iterations, int size, boolean stopIteratingAfterCutFound) {
		super("Kp1_KL " + size,ucc, iterations, stopIteratingAfterCutFound);
		this.size = size;
	}
	
	
	@Override
	public void setSets(Abstract_Inequality sets) {
		currentSets = ((DependentSet_Inequality)sets).clone();
	}

	@Override
	public void updateSets(Transformation t) {
		Exchange e = (Exchange)t;
		
		addToZ(e.id[0]);
		removeFromZ(e.id[1]);

	}
	
	/**
	 * Add an element in Z (the element is identified by its id in [0,n[)
	 * @param z Element to add in Z
	 */
	public void addToZ(int z){
		currentSet().Z.add(z);
		currentSet().inZ[z] = true;
	}

	/**
	 * Remove an element of Z (the element is identified by its id in [0,n[)
	 * @param z Element of Z to remove
	 */
	public void removeFromZ(int z){
		currentSet().Z.remove(new Integer(z));
		currentSet().inZ[z] = false;
	}

	@Override
	public void initializeSets() {

		currentSets = new DependentSet_Inequality(s);
		
		Random random = new Random();
		
		/* Create Z randomly */
		for(int i = 0 ; i < s.n() ; ++i)
			currentSet().inZ[i] = false;
		
		for(int i = 0 ; i < size ; ++i){
			
			/* Add one node in Z */
			int z = random.nextInt(s.n());
			while(currentSet().inZ[z])
				z=(z+1)%s.n();
			
			addToZ(z);
		}
		
		if(lowerBound == null)
			lowerBound = ((DependentSet_Inequality)currentSets).getLowerBound();
		else
			((DependentSet_Inequality)currentSets).lowerBound = lowerBound;
		
	}

	@Override
	public void initializeSubSlacks() {

		/* Initialize the transformations 
		 * Let y,z be any vertices respectively in Y and Z.
		 * Let Z2=Z-z.
		 * 
		 *  	Cost of Y <-> Z
		 * 		If y and z are exchanged, the cost of the Kp1-inequality x(Z,Z) is modified by x(y,Z2) - x(z,Z2)
		 * 		since x(Z2+y, Z2+y) = x(Z,Z) - x(z, Z2) + x(y, Z2)
		 */
		
		/* To avoid repeating loops, we keep in memory for each 
		 * 		- y : x(y,Z)
		 * 		- z : x(z,Z2)
		 */
		xiZ = new double[s.n()];

		/* Compute xiZ */
		for(int i = 0 ; i < s.n() ; i++){

			xiZ[i] = 0;
			
			/* Id of <i> in <Z> */
			int z = -1;
			
			if(currentSet().inZ[i])
				z = currentSet().Z.indexOf(i);
			
			for(int z2 = 0 ; z2 < currentSet().Z.size() ; ++z2)
				if(z != z2)
					xiZ[i] += s.x(i,currentSet().Z.get(z2));
			
		}

	}

	@Override
	public void computeTransformationSlacks() {

		bestNextTransformation = new Exchange();
		/* For each z in Z */
		for(int z = 0 ; z < currentSet().Z.size() ; z++){

			int z_id = currentSet().Z.get(z);

			/* For each y in Y such that transformation y <-> z is still possible */
			for(int y = 0 ; y < s.n() ; ++y){
			
				if(!currentSet().inZ[y] && exchange[y][z_id] != worstValue){
						
					double x_yz = s.x(y,z_id);
					
					exchange[y][z_id] = xiZ[y] - xiZ[z_id] - x_yz;
					exchange[z_id][y] = exchange[y][z_id];
					
					if(exchange[y][z_id] < this.bestNextTransformation.slack){
						((Exchange)bestNextTransformation).set(exchange[y][z_id], y, z_id);

					}
				}		
			}
		}		
	}

	@Override
	public void updateSubSlacks(Transformation t) {
		
		Exchange e = (Exchange)t;
		
		/* Ensure that this transformation will not be performed a second time */
		exchange[e.id[1]][e.id[0]] = worstValue;
		exchange[e.id[0]][e.id[1]] = worstValue;
		
		for(int i = 0 ; i < s.n() ; ++i)
			if(i != e.id[1])
				if(i != e.id[0])
					xiZ[i] = xiZ[i] - s.x(i, e.id[1]) + s.x(i, e.id[0]);
				else
					xiZ[e.id[0]] = xiZ[e.id[0]] - s.x(i,  e.id[1]);
			else			
				xiZ[e.id[1]] = xiZ[e.id[1]] + s.x(i, e.id[0]);
	}
	
	protected class Exchange extends Transformation{

		/** Id of the nodes concerned by the transformation:
		 * 	The ids are in [0;n[
		 * 	id[0] contains the id of the node in Y
		 * 	id[1] contains the id of the node in Z
		 */
		int[] id = new int[2];
		
		public Exchange(){
			super();
			id[0] = -1;
			id[1] = -1;		
		}
		
		/**
		 * Set the best transformation
		 * @param Score of this transformation
		 * @param idY Id of the node in Y [0,n[
		 * @param idZ Id of the node in Z [0,n[
		 */
		public void set(double slack, int idY, int idZ){
			this.slack = slack;
			id[0] = idY;
			id[1] = idZ;
		}
		
		@Override
		public Exchange clone() {
			Exchange theClone = new Exchange();
			
			theClone.id = id.clone();
			
			return theClone;
		}
		
	}
	
	public DependentSet_Inequality currentSet(){
		return (DependentSet_Inequality)currentSets;
	}

	@Override
	public void initializeTransformationArrays() {
		exchange = new double[s.n()][s.n()];
	}

}
