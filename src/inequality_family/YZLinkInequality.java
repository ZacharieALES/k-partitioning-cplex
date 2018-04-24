package inequality_family;

import java.util.ArrayList;
import java.util.List;

import formulation.interfaces.IFNodeVNodeBV;
import formulation.pcenters.PCSC;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

@SuppressWarnings("serial")
public class YZLinkInequality extends AbstractInequality<PCSC>{

	/** Id of the client */
	public int i;
	
	/** Id of the distance */
	public int k;
	
	/* List of the factories which appear in the inequality */
	public List<Integer> factories;
	
	
	public YZLinkInequality(PCSC formulation, int i, int k, double dk, double[][] d) {
		
		super(formulation, PCSC.class);
		
		this.i = i;
		this.k = k;
		
		factories = new ArrayList<>();
		
		/* For each factory */
		for(int m = 0 ; m < d[0].length; m++) 
			if(!formulation.isFactoryDominated(m) && d[i][m] < dk)
				factories.add(m);
		
//		System.out.println("z" + k + " +  y " +  factories + " >= 1");
		
	}

	public YZLinkInequality(PCSC formulation, int i2, int k2, List<Integer> factories2) {
		super(formulation, PCSC.class);
		
		this.i = i2;
		this.k = k2;
		this.factories = new ArrayList<>(factories2);
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();
		
		try {
			expr.addTerm(1.0, formulation.nodeBVar(k));
		
		for(Integer m: factories)
			expr.addTerm(1.0, formulation.nodeVar(m));
		
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		return new Range(1.0, expr);
	}

	@Override
	public AbstractInequality<PCSC> clone() {
		return new YZLinkInequality(formulation, i, k, factories);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException {
		System.out.println("YZLinkInequality: evaluate(): TODO");
		return 0;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException {
		System.out.println("YZLinkInequality: getSlack(): TODO");
		return 0;
	}

	public boolean dominates(YZLinkInequality ineq) {
		return k >= ineq.k && ineq.factories.containsAll(factories);
	}

}
