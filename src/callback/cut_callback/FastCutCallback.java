package callback.cut_callback;

import java.util.ArrayList;

import formulation.interfaces.IFEdgeVNodeClusterVNodeVConstrainedClusterNb;
import formulation.interfaces.IFEdgeVNodeVClusterNb;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import separation.AbstractSeparation;
import separation.SeparationKP1DenseHeuristicDiversification;
import separation.SeparationPawInequalitiesHeuristic;
import separation.SeparationSubRepresentativeSansDoublon;

public class FastCutCallback extends AbstractCutCallback{
	
	public int MAX_CUT;
	public ArrayList<AbstractSeparation<?>> sep_algo = new ArrayList<>();
	
	public FastCutCallback(IFEdgeVNodeVClusterNb formulation, int MAX_CUT) {
		super(formulation);
		this.MAX_CUT = MAX_CUT;
		
		/* Grotschell ST */
//		sep_algo.add(new Separation_ST_Grotschell(this, MAX_CUT));
		
//		/* Labbe ST */
//		sep_algo.add(new Separation_ST_Labbe(this));
		
		/* Dependent set heuristic */
		sep.add(new SeparationKP1DenseHeuristicDiversification(formulation, this.variableGetter()));

		/* Paw inequalities */
		sep.add(new SeparationPawInequalitiesHeuristic(formulation, this.variableGetter()));
		
		/* Sub representative inequalities */
		if(formulation instanceof IFEdgeVNodeClusterVNodeVConstrainedClusterNb)
			sep.add(new SeparationSubRepresentativeSansDoublon((IFEdgeVNodeClusterVNodeVConstrainedClusterNb)formulation, this.variableGetter()));
		
	}

	@Override
	public void separates() throws IloException {
		
		

		for(AbstractSeparation<?> algo : sep_algo){
			
			ArrayList<AbstractInequality<?>> ineq = algo.separate();

			for(AbstractInequality<?> in : ineq)
				this.addRange(in.getRange(), 0);
			
		}
		
	}

}
