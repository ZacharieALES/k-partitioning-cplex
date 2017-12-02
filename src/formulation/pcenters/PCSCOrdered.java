package formulation.pcenters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cplex.Cplex;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import inequality_family.Range;
import inequality_family.YZLinkInequality;

public class PCSCOrdered extends PCSC{

	public PCSCOrdered(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {
		super(param);
	}

	public PCSCOrdered(double[][] currentD, PCenterIndexedDistancesParam param, int p) throws Exception {
		super(currentD, param, p);
	}

	@Override
	protected void createYZLinkConstraints() throws IloException{

		/* Undominated inequalites */
//		List<YZLinkInequality> yzIneq = new ArrayList<>();
		//		int counter = 0; 

		/* For each client */
		for(int i = 0 ; i < N ; i++)
			if(!isClientDominated(i))
				/* For each possible distance */
				for(int k = 1 ; k <= K ; ++k)

					/* If the inequality is not dominated by the next one */
					if(clientHasFactoryAtDk(i, k)) {
						getCplex().addRange(new YZLinkInequality(this, i, k, D[k], d).createRange());
						//					counter++;
					}

		//		System.out.println("\nPCSCO: added " + counter + " yz inequalities");

		// Increase the computation time
		//					addIfNotDominated(new YZLinkInequality(this, i, k, D[k], d), yzIneq);
		//		
		//		for(YZLinkInequality ineq: yzIneq)
		//			getCplex().addRange(ineq.createRange());

	}

	/**
	 * Add and inequality if it is not dominated by an inequality from the list. The inequalities from the list which are dominated by it are removed from the list.
	 * @param ineq The inequality to test.
	 * @param list The list of inequalities.
	 */
	protected void addIfNotDominated(YZLinkInequality ineq, List<YZLinkInequality> list) {

		Iterator<YZLinkInequality> it = list.iterator();
		boolean isDominated = false;

		while(it.hasNext() && !isDominated) {

			YZLinkInequality cIneq = it.next();

			if(cIneq.dominates(ineq))
				isDominated = true;
			else if(ineq.dominates(cIneq))
				it.remove();
		}

		if(!isDominated)
			list.add(ineq);

	}

	@Override
	protected void createConstraints() throws IloException {
		createAtLeastOneCenter();
		createAtMostPCenter();
		createOrderedZ();
		createYZLinkConstraints();

				createReinforcementConstraints(); // Do not seem to reinforce
		//		createReinforcementConstraints2(); // Invalid for p > 1
	}

	/**
	 * For each factory m find dm = Dk = max_i d[i][m]
	 * Let m* = argmin_m dm
	 * We can add: ym* + z(k+1) >= 1
	 * 
	 * Note: invalid for p>1
	 * @throws IloException 
	 */
	protected void createReinforcementConstraints2() throws IloException {

		double minColMaxDist = Integer.MAX_VALUE;
		List<Integer> colId = new ArrayList<>();

		for(int m = 0 ; m < M ; ++m) {

			if(isFactoryDominated(m)) {
				double dMax = d[0][m];

				for(int i = 1 ; i < N ; i++)
					if(!isClientDominated(i))
						if(d[i][m] > dMax)
							dMax = d[i][m];

				if(dMax <= minColMaxDist) {

					if(dMax < minColMaxDist) {
						colId.clear();
						minColMaxDist = dMax;
					}

					colId.add(m);

				}
			}
		}

		int kp1 = indexOfDistanceInD(minColMaxDist) + 1; 

		for(Integer j: colId) {

			IloLinearNumExpr expr = getCplex().linearNumExpr();

			expr.addTerm(1.0, y[j]);
			expr.addTerm(1.0, z[kp1]);

			getCplex().addRange(new Range(1.0, expr));

		}

	}

	/**
	 * For each factory m find Dk = max_i d[i][m]
	 * Add constraint ym + z(k+1) <= 1
	 * 
	 * Note: does not seem to improve the relaxation
	 * @throws IloException
	 */
	protected void createReinforcementConstraints() throws IloException {

		for(int m = 0 ; m < M ; ++m) {

			if(!isFactoryDominated(m)) {

				double dMax = d[0][m];

				for(int i = 1 ; i < N ; i++)
					if(!isClientDominated(i))
						if(d[i][m] > dMax)
							dMax = d[i][m];

				int id = indexOfDistanceInD(dMax);

				if(id != -1 && id < z.length - 1) {

					IloLinearNumExpr expr = getCplex().linearNumExpr();

					expr.addTerm(1.0, y[m]);
					expr.addTerm(1.0, z[id+1]);

					getCplex().addRange(new Range(expr, 1.0));
				}
			}
		}
	}

	private void createOrderedZ() throws IloException {

		for(int k = 1 ; k < K ; k++) {
			IloLinearNumExpr expr = getCplex().linearNumExpr();
			expr.addTerm(1.0, z[k]);
			expr.addTerm(-1.0, z[k+1]);
			getCplex().addRange(new Range(0.0, expr));
		}
	}

	@Override
	public String getMethodName() { return "pcsco";}
}
