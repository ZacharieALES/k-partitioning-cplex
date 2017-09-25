package separation;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import inequality_family.Abstract_Inequality;

import java.util.ArrayList;

import solution.Solution_Representative;
import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;

/**
 * Abstract class for all the separation algorithms. It includes a default Abstract_CutCallback class which can be used whenever the user separation step consists only in performing the separation described in the current class.
 * @author zach
 *
 */
public abstract class Abstract_Separation{

	protected double eps = 1.0e-4;
	public Solution_Representative s;
	
	public String name;
	public int added_cuts = 0;
	
	public Abstract_Separation(String name, Solution_Representative s){
		this.s = s;
		this.name = name;
	}
		
	/**
	 * Find cut that separate the relaxation from the integer polyhedron.
	 * @return True if at least one cut is found; false otherwise.
	 * @throws IloException
	 */
	public abstract ArrayList<Abstract_Inequality> separate() throws IloException;
	
	public Abstract_CutCallback createDefaultCallback(PartitionWithRepresentative p){
		DefaultCallback d = new DefaultCallback(p);
		
		return d;
	}

	/**
	 * Abstract_CutCallback which only use the current separation algorithm and add the obtained ranges.
	 * @author zach
	 *
	 */
	 public class DefaultCallback extends Abstract_CutCallback{

		public DefaultCallback(PartitionWithRepresentative p) {
			super(p);
			sep.add(Abstract_Separation.this);
			s = this;
		}

		@Override
		public void separates() throws IloException{
			ArrayList<Abstract_Inequality> ineq = separate();
			
			for(Abstract_Inequality i : ineq)
				this.addRange(i.getRange(), 0);
		}


		
	}

}
