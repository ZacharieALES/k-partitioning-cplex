package formulation.interfaces;

import cplex.Cplex;
import variable.CplexVariableGetter;
import variable.VariableValueProvider;

public interface IFormulation extends VariableValueProvider{
	
	public Cplex getCplex();
	public CplexVariableGetter variableGetter();

}
