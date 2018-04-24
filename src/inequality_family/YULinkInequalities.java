package inequality_family;

import java.util.ArrayList;
import java.util.List;

import formulation.interfaces.IFNodeVNodeBV;
import formulation.pcenters.PCRadiusCalik;
import formulation.pcenters.PCRadiusIndex;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import variable.VariableGetter;

@SuppressWarnings("serial")
public class YULinkInequalities extends AbstractInequality<PCRadiusCalik>{

	/** Id of the client */
	public int i;
	
	/** Id of the distance */
	public int k;
	
	/* List of the factories which appear in the inequality */
	public List<Integer> factories;
	
	
	public YULinkInequalities(PCRadiusCalik formulation, int i, int k, double dk, double[][] d) {
		
		super(formulation, PCRadiusCalik.class);
		
		this.i = i;
		this.k = k;
		
		factories = new ArrayList<>();
		
		/* For each factory */
		for(int m = 0 ; m < d[0].length; m++) 
			if(!formulation.isFactoryDominated(m) && d[i][m] <= dk)
				factories.add(m);
		
	}

	public YULinkInequalities(PCRadiusCalik formulation, int i2, int k2, List<Integer> factories2) {
		super(formulation, PCRadiusCalik.class);
		
		this.i = i2;
		this.k = k2;
		this.factories = factories2;
	}

	@Override
	public Range createRange() {
		
		IloLinearNumExpr expr = formulation.getCplex().linearNumExpr();
		
		try {
			
			for(int q = 1; q <= k; q++)
//			for(int q = k; q <= k; q++)
				expr.addTerm(1.0, formulation.u(q));
		
		for(Integer m: factories)
			expr.addTerm(-1.0, formulation.nodeVar(m));
		
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		return new Range(expr, 0.0);
	}

	@Override
	public AbstractInequality<PCRadiusCalik> clone() {
		return new YULinkInequalities(formulation, i, k, factories);
	}

	@Override
	protected double evaluate(VariableGetter vg) throws IloException {
		System.out.println("YULinkInequalities: evaluate(): TODO");
		return 0;
	}

	@Override
	public double getSlack(VariableGetter vg) throws IloException {
		System.out.println("YULinkInequalities: getSlack(): TODO");
		return 0;
	}

}
