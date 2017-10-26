package inequality_family;

import formulation.interfaces.IFEdgeVNodeClusterVNodeVConstrainedClusterNb;
import formulation.interfaces.IFEdgeVNodeVClusterNb;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

@SuppressWarnings("serial")
public class LowerRepInequality extends AbstractInequality<IFEdgeVNodeVClusterNb>{

	public int l;
	
	public LowerRepInequality(IFEdgeVNodeVClusterNb formulation, int l){
		super(formulation, IFEdgeVNodeVClusterNb.class);
		
		this.l = l;
	}

	@Override
	public Range createRange() {

		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();

		try {
			
			if(l > 2){
		
				expr.addTerm(1.0, formulation.nodeVar(l));
				
				for(int i = 0 ; i < l ; ++i)
					if(containsTildeVariables())
						expr.addTerm(1.0, tildeFormulation().nodeInClusterVar(l,i));
					else
						expr.addTerm(1.0, formulation.edgeVar(l,i));
				
				if(containsTildeVariables())
					return new Range(1.0, expr, 1.0);
				else
					return new Range(1.0, expr);
				
			}
			else{
				
				expr.addTerm(1.0, formulation.edgeVar(1,0));
				
				if(containsTildeVariables()){
					expr.addTerm(1.0, tildeFormulation().nodeInClusterVar(2,0));
					expr.addTerm(1.0, tildeFormulation().nodeInClusterVar(2,1));
				}
				else{
					expr.addTerm(1.0, formulation.edgeVar(2,0));
					expr.addTerm(1.0, formulation.edgeVar(2,1));
				}

				for (int k = 3; k < formulation.n(); ++k)
					expr.addTerm(-1.0, formulation.nodeVar(k));

				if(containsTildeVariables())
					return new Range(3.0 - formulation.maximalNumberOfClusters(), expr, 3.0 - formulation.maximalNumberOfClusters());
				else
					return new Range(3.0 - formulation.maximalNumberOfClusters(), expr);
				
			}
			
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
		
	}

	@Override
	public AbstractInequality<IFEdgeVNodeVClusterNb> clone() {
		return new LowerRepInequality(formulation, l);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException  {
		
		double result = 0.0;
		
		if(l != 2){
			
			result += vg.getValue(formulation.nodeVar(l));
			
			for(int i = 0 ; i < l ; ++i)
				if(containsTildeVariables())
					result += vg.getValue(tildeFormulation().nodeInClusterVar(l,i));
				else
					result += vg.getValue(formulation.edgeVar(l,i));
			
			//TODO on veut une egalite pour les tildes pas juste une range !
		}
		else{
			
			result += vg.getValue(formulation.edgeVar(1,0));
			
			if(containsTildeVariables()){
				result += vg.getValue(tildeFormulation().nodeInClusterVar(2,0));
				result += vg.getValue(tildeFormulation().nodeInClusterVar(2,1));
			}
			else{
				result += vg.getValue(formulation.edgeVar(2,0));
				result += vg.getValue(formulation.edgeVar(2,1));
			}

			for (int k = 3; k < formulation.n(); ++k)
				result -= vg.getValue(formulation.nodeVar(k));
			
		}
		
		return result;
	}


	@Override
	public double getSlack(VariableGetter vg) throws IloException   {
		
		double bound = 1.0;
		
		if(l == 2)
			bound = 3.0 - formulation.maximalNumberOfClusters();
		
		if(containsTildeVariables())
			return -Math.abs(this.evaluate(vg) - bound);
		else
			return this.evaluate(vg) - bound;
	}

	public boolean containsTildeVariables() {
		return formulation instanceof IFEdgeVNodeClusterVNodeVConstrainedClusterNb;
	}
	
	public IFEdgeVNodeClusterVNodeVConstrainedClusterNb tildeFormulation() {
		return (IFEdgeVNodeClusterVNodeVConstrainedClusterNb)formulation;
	}
}
