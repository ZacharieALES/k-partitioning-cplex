package mipstart;

import ilog.concert.IloException;

public interface AbstractMIPStartGetter {
	
	public abstract SolutionManagerRepresentative getMIPStart() throws IloException;

}
