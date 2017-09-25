package lazy_callback;
import formulation.Partition;
import formulation.Partition_with_tildes;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.LazyConstraintCallback;
import solution.Solution_Representative;


public abstract class Abstract_LazyCallback extends LazyConstraintCallback implements Solution_Representative{

	public Partition rep = null;
	public int iterations = 0;
	
	public double eps = 0.0000001;
	
	public Abstract_LazyCallback(Partition p){
		rep = p;
	}
	
	/** Time spent in the callback */
	public double time = 0.0;

	public IloNumVar x_var(int i){
		return rep.v_rep[i-3];
	}

	public IloNumVar x_var(int i, int j){
		return rep.v_edge[i][j];
	}
	
	public double x(int i){
		
		try {
			return this.getValue(rep.v_rep[i-3]);
		} catch (IloException e) {
			e.printStackTrace();
			return -Double.MAX_VALUE;
		}
	}

	public double x(int i, int j){
		try {		
			return this.getValue(rep.v_edge[i][j]);
		} catch (IloException e) {
			e.printStackTrace();
			return -Double.MAX_VALUE;
		}
	}
	
	protected void main() throws IloException {
		
		iterations++;
		time -= rep.getCplexTime();
		separates();
		time += rep.getCplexTime();
		
	}

	public void addRange(IloRange range){
		try {
			this.add(range, IloCplex.CutManagement.UseCutFilter);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void addLe(IloLinearNumExpr expr, double ubound){
		try {
//			this.add(rep.le(expr, ubound));
			this.add(rep.le(expr, ubound), IloCplex.CutManagement.UseCutFilter);
		} catch (IloException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void addGe(IloLinearNumExpr expr, double lbound){
		try {
			this.add(rep.ge(expr, lbound), IloCplex.CutManagement.UseCutFilter);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void separates() throws IloException;
	
	public void abortVisible(){
		this.abort();
	}
	
	@Override
	public double xt(int i, int j) {
		return ((Partition_with_tildes)rep).xt(i, j);
	}

	@Override
	public IloLinearNumExpr linearNumExpr() {
		return rep.linearNumExpr();
	}

	@Override
	public IloNumVar xt_var(int i, int j) {
		return ((Partition_with_tildes)rep).xt_var(i, j);
	}

	@Override
	public double getBestObjValue2() {
		return rep.getBestObjValue2();
	}

	@Override
	public double getObjValue2() {
		return rep.getObjValue2();
	}

	@Override
	public boolean isTilde() {
		return rep instanceof Partition_with_tildes;
	}

	@Override
	public double d(int i, int j) {
		return rep.d[i][j];
	}

	@Override
	public int n() {
		return rep.n;
	}

	@Override
	public int K() {
		return rep.K;
	}
}
