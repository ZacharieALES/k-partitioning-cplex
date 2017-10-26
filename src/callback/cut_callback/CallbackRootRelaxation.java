package callback.cut_callback;
import formulation.PartitionWithRepresentative;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.UnknownObjectException;
import ilog.cplex.IloCplex.UserCutCallback;


/**
 * Display the root relaxation of the problem and stop cplex execution
 * @author zach
 *
 */
public class CallbackRootRelaxation extends UserCutCallback{
	
	public double rootRelaxation = -1.0;
	public int n ;
	public boolean abort = false;
	
	public PartitionWithRepresentative rep;
	
	public CallbackRootRelaxation(boolean abortAfterGetRoot){
		abort = abortAfterGetRoot;
	}
	
	@Override
	protected void main() throws IloException {

		System.out.println("relax: " + this.getBestObjValue());
		
		if(rootRelaxation == -1.0)
			rootRelaxation = this.getBestObjValue();
		
//		if(abort)
//			this.abort();
		
//		System.out.println("Root relaxation: " + Math.round(this.getBestObjValue()));
		
	}
	
	public void setPartition(PartitionWithRepresentative rep2){
		this.rep = rep2;
		n = rep.v_edge.length;
		System.out.println("n: " + n);
	}
	
	public void displaySolution()
			throws UnknownObjectException, IloException {

		for(int i = 0 ; i < n ; ++i)
			for(int j = i+1 ; j < n ; ++j){
				
				System.out.println("(" + i + ","+j+"): " + this.getValue(rep.v_edge[i][j]));
				
			}
		System.out.println(" ");
		
		for(int i = 3 ; i < n ; ++i)
			System.out.println(i+ ": " + this.getValue(rep.v_rep[i-3]));
		

	}


}
