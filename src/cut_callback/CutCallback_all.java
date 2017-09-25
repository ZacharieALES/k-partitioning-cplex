package cut_callback;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;

import java.util.ArrayList;

import separation.Abstract_Separation;
import separation.Separation_DependentSet_KL;
import separation.Separation_KP1_Dense_Heuristic_Diversification;
import separation.Separation_ST_Grotschell;
import separation.Separation_ST_KL;
import separation.Separation_ST_Labbe;
import separation.Separation_TCC_KL_Fixed_size;
import formulation.PartitionWithRepresentative;

public class CutCallback_all extends Abstract_CutCallback{

	ArrayList<Abstract_Separation> al = new ArrayList<Abstract_Separation>();

	public CutCallback_all(PartitionWithRepresentative p) {
		super(p);

		sep.add(new Separation_ST_Grotschell(this, 500));
		sep.add(new Separation_KP1_Dense_Heuristic_Diversification(this));
		sep.add(new Separation_ST_Labbe(this));
//		sep.add(new Separation_ST_KL(this, 2, true));
//		sep.add(new Separation_DependentSet_KL(this, 2, true));
//		sep.add(new Separation_TCC_KL_Fixed_size(this, 2, null, true));
	}

	@Override
	public void separates() throws IloException {

		int i = 0;
		boolean found = false;
		
		while(!found && i < al.size()){
			
			Abstract_Separation sep = al.get(i);
			ArrayList<Abstract_Inequality> ineq = sep.separate();
			
			if(ineq.size() > 0){
				found = true;
				System.out.println("-- oh yeah ! " + sep.name + " (" + ineq.size() + ") --");
				
				for(Abstract_Inequality in : ineq)
					this.addRange(in.getRange(), i);
			}
			
			++i;
			
		}
		
		if(!found)
			System.out.println("-- not found --");
		
	}

	@Override
	public double y(int i, int j) {
		// TODO Auto-generated method stub
		return 0;
	}

}
