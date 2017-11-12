package inequality_family;

import java.util.ArrayList;
import java.util.List;

import formulation.PCRadiusIndex;
import formulation.interfaces.IFNodeVNodeBV;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

@SuppressWarnings("serial")
public class YKStarLinkInequalities extends AbstractInequality<PCRadiusIndex>{

	/** Id of the client */
	public int i;
	
	/** Id of the distance */
	public int k;
	
	/* List of the factories which appear in the inequality */
	public List<Integer> factories;
	
	
	public YKStarLinkInequalities(PCRadiusIndex formulation, int i, int k, double dk, double[][] d) {
		
		super(formulation, PCRadiusIndex.class);
		
		this.i = i;
		this.k = k;
		
		factories = new ArrayList<>();
		
		/* For each factory */
		for(int m = 0 ; m < d[0].length; m++) 
			if(d[i][m] < dk)
				factories.add(m);
		
	}

	public YKStarLinkInequalities(PCRadiusIndex formulation, int i2, int k2, List<Integer> factories2) {
		super(formulation, PCRadiusIndex.class);
		
		this.i = i2;
		this.k = k2;
		this.factories = factories2;
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();
		
		try {
			expr.addTerm(1.0, formulation.kStarVar());
		
		for(Integer m: factories)
			expr.addTerm(k, formulation.nodeVar(m));
		
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		return new Range(k, expr);
	}

	@Override
	public AbstractInequality<PCRadiusIndex> clone() {
		return new YKStarLinkInequalities(formulation, i, k, factories);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException {
		System.out.println("YKStarLinkInequalities: evaluate(): TODO");
		return 0;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException {
		System.out.println("YKStarLinkInequalities: getSlack(): TODO");
		return 0;
	}

}
