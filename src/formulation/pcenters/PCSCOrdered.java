package formulation.pcenters;

import java.io.IOException;
import java.util.ArrayList;
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

	@Override
	protected void createYZLinkConstraints() throws IloException{

		/* For each client */
		for(int i = 0 ; i < N ; i++)

			/* For each possible distance */
			for(int k = 1 ; k <= K ; ++k)

				/* If the inequality is not dominated by the next one */
				if(clientHasFactoryAtDk(i, k))
					getCplex().addRange(new YZLinkInequality(this, i, k, D[k], d).createRange());

	}

	@Override
	protected void createConstraints() throws IloException {
		createAtLeastOneCenter();
		createAtMostPCenter();
		createOrderedZ();
		createYZLinkConstraints();

		//		createReinforcementConstraints(); // Do not seem to reinforce
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

			double dMax = d[0][m];

			for(int i = 1 ; i < N ; i++)
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

		int kp1 = indexOfDistanceInD(minColMaxDist) + 1; 

		for(Integer i: colId) {

			IloLinearNumExpr expr = getCplex().linearNumExpr();

			expr.addTerm(1.0, y[i]);
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

			double dMax = d[0][m];

			for(int i = 1 ; i < N ; i++)
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

	private void createOrderedZ() throws IloException {

		for(int k = 1 ; k < K ; k++) {
			IloLinearNumExpr expr = getCplex().linearNumExpr();
			expr.addTerm(1.0, z[k]);
			expr.addTerm(-1.0, z[k+1]);
			getCplex().addRange(new Range(0.0, expr));
		}
	}

	public static void main(String[] args) {

		Cplex cplex = new Cplex();
		try {


			for(int i = 10 ; i < 60 ; i+= 5) {

				System.out.print("\ni = "+ i);

				for(int p = 2 ; p < 10 ; p++) {
					PCenterIndexedDistancesParam param = new PCenterIndexedDistancesParam("data/pcenters/random/pc_n" + i + "_p" + p + "_i_1.dat", cplex);
					param.isInt = false;

					PCSC pcsc = new PCSC(param);
					pcsc.createFormulation();
					cplex.solve();
					double pcscRelax = cplex.getObjValue(); 
					

					param.isInt = true;
					PCSC pcscInt = new PCSC(param);
					pcscInt.createFormulation();
					cplex.solve();
					double pcscOpt = cplex.getObjValue();

					pcscInt.displayZVariables(5);

					param.isInt = false;
					PCSCOrdered pcsco = new PCSCOrdered(param);
					pcsco.createFormulation();
					cplex.solve();
					double pcscoRelax =  cplex.getObjValue();

					param.isInt = true;
					PCSCOrdered pcscoInt = new PCSCOrdered(param);
					pcscoInt.createFormulation();
					cplex.solve();
					double pcscoOpt = cplex.getObjValue();

					pcscoInt.displayZVariables(5);
					System.exit(0);
					if(Math.abs(pcscOpt-pcscoOpt) < 1E-4)
						if(Math.abs(pcscRelax-pcscoRelax) > 1E-4)
							System.out.print("*");
						else
							System.out.print(".");
					else
						System.out.print("!");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
