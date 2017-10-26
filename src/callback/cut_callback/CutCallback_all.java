package callback.cut_callback;

import java.util.ArrayList;

import formulation.interfaces.IFEdgeVClusterNb;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import separation.AbstractSeparation;
import separation.SeparationKP1DenseHeuristicDiversification;
import separation.SeparationSTGrotschell;
import separation.SeparationSTLabbe;

public class CutCallback_all extends AbstractCutCallback{

	ArrayList<AbstractSeparation> al = new ArrayList<AbstractSeparation>();
	

	public CutCallback_all(IFEdgeVClusterNb formulation) {
		super(formulation);

		sep.add(new SeparationSTGrotschell(formulation, this.variableGetter(), 500));
		sep.add(new SeparationKP1DenseHeuristicDiversification(formulation, this.variableGetter()));
		sep.add(new SeparationSTLabbe(formulation, this.variableGetter()));
//		sep.add(new Separation_ST_KL(this, 2, true));
//		sep.add(new Separation_DependentSet_KL(this, 2, true));
//		sep.add(new Separation_TCC_KL_Fixed_size(this, 2, null, true));
	}

	@Override
	public void separates() throws IloException {

		int i = 0;
		boolean found = false;
		
		while(!found && i < al.size()){
			
			AbstractSeparation sep = al.get(i);
			ArrayList<AbstractInequality> ineq = sep.separate();
			
			if(ineq.size() > 0){
				found = true;
				System.out.println("-- oh yeah ! " + sep.name + " (" + ineq.size() + ") --");
				
				for(AbstractInequality in : ineq)
					this.addRange(in.getRange(), i);
			}
			
			++i;
			
		}
		
		if(!found)
			System.out.println("-- not found --");
		
	}

}
