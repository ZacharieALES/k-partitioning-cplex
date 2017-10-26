package cutting_plane;

import java.util.ArrayList;

import formulation.interfaces.IFormulation;
import inequality_family.AbstractInequality;
import separation.AbstractSeparation;

public class CP_Separation<Formulation extends IFormulation>{

	public AbstractSeparation<Formulation> se;
	public ArrayList<AbstractInequality<? extends IFormulation>> addedIneq = new ArrayList<>();
	public boolean isQuick;
	public int removedIneq = 0;

	/**
	 * True if the inequalities separated by this family must be added in the branch and cut if the cutting plane does not return an integer solution
	 */
	public boolean toAddInBB;
	public boolean usedAtThisIteration = false;

	public CP_Separation(AbstractSeparation<Formulation> se, boolean toAdd, boolean isQuick){
		this.se = se;
		toAddInBB = toAdd;
		this.isQuick = isQuick;
	}

	public void remove(int i) {
		addedIneq.remove(i);
		removedIneq++;
	}
}