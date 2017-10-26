package mipstart;

import ilog.concert.IloException;

public interface AbstractMIPStartGetter {
	
	public abstract SolutionManager getMIPStart() throws IloException;

}
