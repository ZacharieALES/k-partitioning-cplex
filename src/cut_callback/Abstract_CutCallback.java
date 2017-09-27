package cut_callback;
import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.Partition_with_tildes;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex.UserCutCallback;
import inequality_family.Range;

import java.util.ArrayList;

import separation.Abstract_Separation;
import solution.Solution_Representative;


public abstract class Abstract_CutCallback extends UserCutCallback implements Solution_Representative{

	public Partition rep = null;
	public double root_relaxation = -1.0;
	public int iterations = 0;
	public ArrayList<Abstract_Separation> sep = new ArrayList<Abstract_Separation>();
	
	public double eps = 0.0000001;
	
	public Abstract_CutCallback(Partition p){
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
	
	public IloNumVar xt_var(int i, int j) {
		if(this.rep instanceof Partition_with_tildes)
			return ((Partition_with_tildes)this.rep).xt_var(i, j);
		else
			return null;
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
	
	public double xt(int i, int j) {
		try {
			if(this.rep instanceof Partition_with_tildes)
					return this.getValue(((Partition_with_tildes)this.rep).xt_var(i, j));
			else
				return -Double.MAX_VALUE;
		
		} catch (IloException e) {
			e.printStackTrace();
			return -Double.MAX_VALUE;
		}
	}

	
	protected void main() throws IloException {
		
		if(root_relaxation == -1.0)
			root_relaxation = this.getBestObjValue2();
		
		if(!this.isAfterCutLoop()){
			iterations++;
			time -= rep.getCplexTime();
			separates();
			time += rep.getCplexTime();
		}
		
	}

	public void addRange(IloRange range, int idSep){
		try {
			this.add(range, false);
			sep.get(idSep).added_cuts++;
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public void addLocalRange(Range ri, int idSep) {
		try {
			this.addLocal(rep.range(ri.lbound, ri.expr, ri.ubound));
			if(idSep >= 0)
				sep.get(idSep).added_cuts++;
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public void addRange(Range ri, int idSep) {
		try {
			this.add(rep.range(ri.lbound, ri.expr, ri.ubound), false);
			if(idSep >= 0)
				sep.get(idSep).added_cuts++;
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void addLe(IloLinearNumExpr expr, double ubound, int idSep){
		try {
			this.add(rep.le(expr, ubound), false);
			sep.get(idSep).added_cuts++;
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void addGe(IloLinearNumExpr expr, double lbound, int idSep){
		try {
			this.add(rep.ge(expr, lbound), false);
			sep.get(idSep).added_cuts++;
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public int n(){
		return this.rep.n();
	}
	
	public int K(){
		return rep.K();
	}
	
	
	public abstract void separates() throws IloException;
	
	public void abortVisible(){
		this.abort();
	}
	
	public double getBestObjValue2() {
		try {
			return this.getBestObjValue();
		} catch (IloException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public double getObjValue2() {
		try {
			return this.getObjValue();
		} catch (IloException e) {
			e.printStackTrace();
			return -1;
		}
	}
	@Override
	public IloLinearNumExpr linearNumExpr() {
		return rep.linearNumExpr();
	}

	@Override
	public boolean isTilde() {
		return rep instanceof Partition_with_tildes;
	}

	/**
	 * i must be greater than j
	 */
	@Override
	public double d(int i, int j) {
		return rep.d[i][j];
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
