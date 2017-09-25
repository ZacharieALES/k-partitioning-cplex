package lazy_callback;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import inequality_family.Abstract_Inequality;
import inequality_family.Range;

import java.util.ArrayList;

import separation.Separation_Triangle;
import formulation.PartitionWithRepresentative;

public class Lazy_CB_Triangle extends Abstract_LazyCallback{

	Separation_Triangle sep;
	
	public Lazy_CB_Triangle(PartitionWithRepresentative p, int MAX_CUT) {
		super(p);
		sep = new Separation_Triangle(this,MAX_CUT);		
	}


	@Override
	public void separates() throws IloException {

		ArrayList<Abstract_Inequality> al = sep.separate();
		
		for(Abstract_Inequality i : al){
			Range r = i.getRange();
			this.add(rep.range(r.lbound, r.expr, r.ubound), IloCplex.CutManagement.UseCutPurge);
		}
		
System.out.println(al.size() + " lazy triangle");
	}


	@Override
	public IloNumVar y_var(int i, int j) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public double y(int i, int j) {
		// TODO Auto-generated method stub
		return 0;
	}
}
