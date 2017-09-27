package cut_callback;

import java.util.ArrayList;

import formulation.Partition;
import formulation.Partition_with_tildes;
import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import separation.Abstract_Separation;
import separation.Separation_KP1_Dense_Heuristic_Diversification;
import separation.Separation_Paw_Inequalities_heuristic;
import separation.Separation_ST_Labbe;
import separation.Separation_SubRepresentative_sans_doublon;

public class Fast_cut_callback extends Abstract_CutCallback{
	
	public int MAX_CUT;
	public ArrayList<Abstract_Separation> sep_algo = new ArrayList<Abstract_Separation>();
	
	public Fast_cut_callback(Partition p, int MAX_CUT) {
		super(p);
		this.MAX_CUT = MAX_CUT;
		
		/* Grotschell ST */
//		sep_algo.add(new Separation_ST_Grotschell(this, MAX_CUT));
		
//		/* Labbe ST */
//		sep_algo.add(new Separation_ST_Labbe(this));
		
		/* Dependent set heuristic */
		sep.add(new Separation_KP1_Dense_Heuristic_Diversification(this));

		/* Paw inequalities */
		sep.add(new Separation_Paw_Inequalities_heuristic(this));
		
		/* Sub representative inequalities */
		if(p instanceof Partition_with_tildes)
			sep.add(new Separation_SubRepresentative_sans_doublon(this));
		
	}

	@Override
	public void separates() throws IloException {
		
		

		for(Abstract_Separation algo : sep_algo){
			
			ArrayList<Abstract_Inequality> ineq = algo.separate();

			for(Abstract_Inequality in : ineq)
				this.addRange(in.getRange(), 0);
			
		}
		
	}

}
