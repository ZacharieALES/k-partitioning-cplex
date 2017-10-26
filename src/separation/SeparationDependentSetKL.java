package separation;
import java.util.Random;

import formulation.interfaces.IFEdgeVClusterNb;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import inequality_family.DependentSetInequality;
import variable.VariableGetter;


public class SeparationDependentSetKL extends AbstractKLSeparation<IFEdgeVClusterNb>{

	/** Array which contain the cost to exchange two nodes
	 * exchange[i][j] is equal worstValue if the transformation have already been done or if the nodes i and j have been in the same set
	 */
	double[][] exchange;
	
	/* 
	 * move[i][] : corresponds to the move of the node i in or out of Z
	 * The first line (move[i][0]) corresponds to the impact of the move on the left-hand side of the equation (the part with the variables)
	 * The second line (move[i][1]) corresponds to the impact of the move on the right-hand side of the equation (the lower bound)
	 */
	double[][] move;
	double setWeight;
	double lower_bound;
	//TODO ajouter possibilite evaluation avec difference et pas ratio
	
//	Double lowerBound = null;
	int q, r;

	//TODO ajouter possibilite evaluation avec difference et pas ratio
	/* Contain for each vertex i, the sum_z_in_S x(i,z) */
	private double[] xiZ;
	
	public SeparationDependentSetKL(IFEdgeVClusterNb formulation, VariableGetter vg, int iterations, boolean stopIteratingAfterCutFound) {
		super("DependentSet_KL ", formulation, vg, iterations, stopIteratingAfterCutFound);
		
	}
	
	@Override
	public void setSets(AbstractInequality<IFEdgeVClusterNb> sets) {
		currentSets = ((DependentSetInequality)sets).clone();
		q = currentSet().Z.size() / formulation.maximalNumberOfClusters();
		r = currentSet().Z.size() % formulation.maximalNumberOfClusters();
	}

	@Override
	public void updateSets(Transformation t) {

		
		if(t instanceof Exchange){
			Exchange e = (Exchange)t;
			
			addToZ(e.id[0]);
			removeFromZ(e.id[1]);
		}
		else{
			
			Move m = (Move)t;
			
			if(m.inZ){
				removeFromZ(m.id);
				
				if(r != 0)
					r--;
				else{
					r = formulation.maximalNumberOfClusters() - 1;
					q--;
				}
			}
			else{
				addToZ(m.id);
				
				if(r != formulation.maximalNumberOfClusters()-1)
					r++;
				else{
					q++;
					r = 0;
				}
			}
			
			currentSet().getLowerBound();
			
		}
	
//System.out.println("Apres");
//this.displaySet();
//System.out.println("Best next transfo slack: " + this.bestNextTransformation.slack);
//System.out.println("---------");

	}
//TODO Verifier qu'on ne descend pas en dessous de K+1 en taille de set
	
	
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

		currentSets = new DependentSetInequality(formulation);
		
		Random random = new Random();
		
		/* Create Z randomly */
		for(int i = 0 ; i < formulation.n() ; ++i)
			currentSet().inZ[i] = false;
		
		/* Create a size between K+1 and n */
		int size = random.nextInt(formulation.n() - formulation.maximalNumberOfClusters()) + 1 + formulation.maximalNumberOfClusters();
		
		for(int i = 0 ; i < size ; ++i){
			
			/* Add one node in Z */
			int z = random.nextInt(formulation.n());
			while(currentSet().inZ[z])
				z=(z+1)%formulation.n();
			
			addToZ(z);
		}

		q = size / formulation.maximalNumberOfClusters();
		r = size % formulation.maximalNumberOfClusters();
		
//		if(lowerBound == null)
//			lowerBound = 
//					((DependentSet_Inequality)currentSets).getLowerBound();
//		else
//			((DependentSet_Inequality)currentSets).lowerBound = lowerBound;
	
//this.displaySet();		
		
	}
	
	public void displaySet() throws IloException{
		
//		double d = 0.0;
		
		for(int i = 0 ; i < formulation.n() ; ++i)
			if(currentSet().inZ[i])
				System.out.print(i + " ");
		
//		for(int i = 0 ; i < formulation.n() ; ++i)
//			if(currentSet().inZ[i])
//				for(int j = 0 ; j < i ; ++j)
//					if(currentSet().inZ[j])
//						d += vg.getValue(formulation.edgeVar(i,j));
		
		
		System.out.println();

	}

	@Override
	public void initializeSubSlacks()  throws IloException {

		/* Initialize the transformations 
		 * Let y,z be any vertices respectively in Y and Z.
		 * Let Z2=Z-z.
		 * 
		 *  	Cost of Y <-> Z
		 * 		If y and z are exchanged, the cost of x(Z,Z) - lower_bound is modified by x(y,Z2) - x(z,Z2)
		 * 		since x(Z2+y, Z2+y) = x(Z,Z) - x(z, Z2) + x(y, Z2)
		 * 
		 * 		Cost of Y -> Z
		 * 		If y is moved to Z, the cost of x(Z,Z) - lower_bound is modified by x(y,Z) - q
		 * 
		 * 		Cost of Z -> Y
		 * 		If z is moved to Y, the cost of x(Z,Z) - lower_bound is modified by:
		 * 			* "- x(z,Z\z) + q" if r > 0
		 * 			* "- x(z,Z\z) + (q-1)" if r = 0 
		 */
		
		/* To avoid repeating loops, we keep in memory for each 
		 * 		- y : x(y,Z)
		 * 		- z : x(z,Z2)
		 */
		xiZ = new double[formulation.n()];
		setWeight = 0.0;
		lower_bound = q/2 * (r + currentSet().Z.size() - formulation.maximalNumberOfClusters());
		
		/* Compute xiZ */
		for(int i = 0 ; i < formulation.n() ; i++){

			xiZ[i] = 0;
			
			/* Id of <i> in <Z> */
			int z = -1;
			
			if(currentSet().inZ[i])
				z = currentSet().Z.indexOf(i);
			
			for(int z2 = 0 ; z2 < currentSet().Z.size() ; ++z2)
				if(z != z2)
					xiZ[i] += vg.getValue(formulation.edgeVar(i,currentSet().Z.get(z2)));

			/* Add the value of the edges in Z which are connected to <i> */
			if( z != -1)
				setWeight += xiZ[i];
		}
		
		/* All the edges inside Z have been counted twice */
		setWeight /= 2.0;

	}

	@Override
	public void computeTransformationSlacks() throws IloException {

		bestNextTransformation = new Exchange();
		double lowest_frac = Double.MAX_VALUE;
		
		/* For each z in Z */
		for(int z = 0 ; z < currentSet().Z.size() ; z++){

			int z_id = currentSet().Z.get(z);

			/* For each y in Y such that transformation y <-> z is still possible */
			for(int y = 0 ; y < formulation.n() ; ++y){
			
				if(!currentSet().inZ[y] && exchange[y][z_id] != worstValue){
						
					double x_yz = vg.getValue(formulation.edgeVar(y,z_id));
					
					exchange[y][z_id] = xiZ[y] - xiZ[z_id] - x_yz;
					exchange[z_id][y] = exchange[y][z_id];
					
					//TODO mettre possibilite d'utiliser difference au lieu de ratio
					double frac = (setWeight + exchange[y][z_id] + 1E-6) / lower_bound;
					
//					if(exchange[y][z_id] < this.bestNextTransformation.slack){
					if(frac < lowest_frac){
						((Exchange)bestNextTransformation).set(exchange[y][z_id], y, z_id);
						lowest_frac = frac;
					}
				}		
			}
		}
		
		
		/* For each node */
		for(int i = 0 ; i < formulation.n(); ++i){
			
			if(move[i][0] != worstValue){
			
				/* If the node is in Z */
				if(currentSet().inZ[i]){
					move[i][0] = -xiZ[i];
					move[i][1] = -q;
					
					if(r == 0)
						move[i][1]++;
				}
				/* If the node is not in Z */
				else{ 
					move[i][0] = xiZ[i];
					move[i][1] = q;
				}
				
				double frac = (setWeight + move[i][0] + 1E-6) / (lower_bound + move[i][1]);
				
				if(frac < lowest_frac){					
					bestNextTransformation = new Move();
					((Move)bestNextTransformation).set(move[i][0] - move[i][1], i, currentSet().inZ[i]);
				}
					
			}
				
		}
		
	}

	@Override
	public void updateSubSlacks(Transformation t) throws IloException {
		
		/* If the transformation is an exchange */
		if(t instanceof Exchange){
			
			Exchange e = (Exchange)t;

			setWeight += exchange[e.id[0]][e.id[1]];
			
			/* Ensure that this transformation will not be performed a second time */
			exchange[e.id[1]][e.id[0]] = worstValue;
			exchange[e.id[0]][e.id[1]] = worstValue;
			
			for(int i = 0 ; i < formulation.n() ; ++i)
				if(i != e.id[1])
					if(i != e.id[0])
						xiZ[i] = xiZ[i] - vg.getValue(formulation.edgeVar(i, e.id[1])) + vg.getValue(formulation.edgeVar(i, e.id[0]));
					else
						xiZ[e.id[0]] = xiZ[e.id[0]] - vg.getValue(formulation.edgeVar(i,  e.id[1]));
				else			
					xiZ[e.id[1]] = xiZ[e.id[1]] + vg.getValue(formulation.edgeVar(i, e.id[0]));
		}
	
		/* If the transformation is a move */
		else{
			
			Move m = (Move)t;

			setWeight += move[m.id][0];
			lower_bound += move[m.id][1];
			
			move[m.id][0] = worstValue;
			
			/* If the node left the set Z */
			if(m.inZ){
				for(int i = 0 ; i < formulation.n() ; ++i)
					if(i != m.id)
						xiZ[i] -= vg.getValue(formulation.edgeVar(i, m.id));
			}
			
			/* If the node enter the set Z */
			else{

				for(int i = 0 ; i < formulation.n() ; ++i)
					if(i != m.id)
						xiZ[i] += vg.getValue(formulation.edgeVar(i, m.id));
			}
			
		}
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
	protected class Move extends Transformation{

		/** Id of the nodes concerned by the transformation:
		 * 	The ids are in [0;n[
		 * 	id[0] contains the id of the node in Y
		 * 	id[1] contains the id of the node in Z
		 */
		int id;
		
		boolean inZ;
		
		public Move(){
			super();
			id = -1;
			inZ = false;
		}
		
		/**
		 * Set the best transformation
		 * @param Score of this transformation
		 * @param id Id of the node
		 */
		public void set(double slack, int id, boolean inZ){
			this.slack = slack;
			this.id = id;
			this.inZ = inZ;
		}
		
		@Override
		public Move clone() {
			Move theClone = new Move();
			
			theClone.id = id;
			theClone.inZ = inZ;
			
			return theClone;
		}
		
	}
	
	public DependentSetInequality currentSet(){
		return (DependentSetInequality)currentSets;
	}

	@Override
	public void initializeTransformationArrays() {
		exchange = new double[formulation.n()][formulation.n()];
		move = new double[formulation.n()][2];
	}

}
