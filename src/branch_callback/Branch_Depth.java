package branch_callback;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.BranchCallback;
import ilog.cplex.IloCplex.BranchDirection;


public class Branch_Depth extends BranchCallback{

	@Override
	protected void main() throws IloException {
		// TODO Auto-generated method stub
		
		int nb = this.getNbranches(); 
		IloNumVar[][] vars = new IloNumVar[nb][];
		double[][] bounds = new double[nb][];
		BranchDirection[][] dirs = new BranchDirection[nb][];
		
//		this.getBranches(vars, bounds, dirs);
		
		Depth d;
		if(this.getNodeData() == null || !(this.getNodeData() instanceof Depth))
			d = new Depth(0);
		else{
			Depth d_parent = (Depth) this.getNodeData();
			d = new Depth(d_parent.depth + 1);
		}

		for(int i = 0 ; i < nb ; i++)	
			makeBranch(vars[i], bounds[i], dirs[i], this.getObjValue(), d);
		
	}
	
	public class Depth{
		public int depth = 0;
		
		public Depth(int depth){
			this.depth = depth;
		}
	}

}
