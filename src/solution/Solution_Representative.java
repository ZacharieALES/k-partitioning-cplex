package solution;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;

public interface Solution_Representative {

	public abstract int n();
	public abstract int K();

	public abstract double xt(int i, int j);
	public abstract double x(int i, int j);
	public abstract double x(int i);
	public abstract IloLinearNumExpr linearNumExpr();
	public abstract IloNumVar xt_var(int i, int j);
	public abstract IloNumVar x_var(int i, int j);
	public abstract IloNumVar x_var(int i);
	public abstract IloNumVar y_var(int i, int j);
	public abstract double getBestObjValue2();
	public abstract double getObjValue2();
	public abstract boolean isTilde();
	public abstract double d(int i, int j);
	public abstract double y(int i, int j);
	
}
