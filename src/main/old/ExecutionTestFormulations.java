package main.old;

import java.util.ArrayList;

import cplex.Cplex;
import formulation.PartitionWithRepresentative;
import formulation.PartitionXY;
import formulation.PartitionXY2;
import formulation.RepParam;
import formulation.TildeParam;
import formulation.XYParam;
import ilog.concert.IloException;
import main.Execution;

public class ExecutionTestFormulations extends Execution{

	public ExecutionTestFormulations(Cplex cplex, int nm, int nM2, int km, int kM2, int im,
			int iM2) {
		super(cplex, nm, nM2, km, kM2, im, iM2);
		
	}


	@Override
	public void execution() throws IloException {
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
		gapValues.add(-250.0);
		gapValues.add(-500.0);
		
		for(int i = 0 ; i < gapValues.size() ; ++i){

			RepParam rep_p = new RepParam(null, cplex, -1, false);
			rep_p.gapDiss = gapValues.get(i);

			TildeParam tp = new TildeParam(null, cplex, -1, false, false);
			tp.gapDiss = gapValues.get(i);
			
			XYParam rp = new XYParam(null, cplex, -1);
			rp.gapDiss = gapValues.get(i);
			


			PartitionWithRepresentative rep0 = ((PartitionWithRepresentative)createPartition(rep_p));
			rep0.getCplex().solve();
			double v0 = rep0.getCplex().getObjValue();

			PartitionWithRepresentative rep = ((PartitionWithRepresentative)createPartition(tp));
			rep.getCplex().solve();
			double v1 = rep.getCplex().getObjValue();
			
			tp.useUpper = true;
			PartitionWithRepresentative rep_tilde_with_upper = ((PartitionWithRepresentative)createPartition(tp));
			rep_tilde_with_upper.getCplex().solve();
			double v2 = rep_tilde_with_upper.getCplex().getObjValue();
			
			
			
//			System.out.println("Tilde sans upper");
//			rep_tilde_with_upper.displayEdgeVariables(5);
//			rep_tilde_with_upper.displayRepresentativeVariables(5);
			
			PartitionXY rep_x_y = ((PartitionXY)createPartition(rp));
			rep_x_y.getCplex().solve();
			double v3 = rep_x_y.getCplex().getObjValue();

//			System.out.println("XY");
//			rep_x_y.displayEdgeVariables(5);
//			System.out.println("-");
//			rep_x_y.displayNodeClusterVariables(5);
//			
			
			rp.isSecondXYFormulation = true;
			PartitionXY2 rep_x_y2 = ((PartitionXY2)createPartition(rp));
			rep_x_y2.getCplex().solve();

//			System.out.println("--");
//			rep_x_y2.displayEdgeVariables(5);
//			System.out.println("-");
//			rep_x_y2.displayNodeClusterVariables(5);
//			System.out.println("-----------");
			
			double v4 = rep_x_y2.getCplex().getObjValue();
			
			
			
			if(Math.abs(v1-v0) > 1E-3 || Math.abs(v1-v2) > 1E-3 || Math.abs(v1-v3) > 1E-3 || Math.abs(v1-v4) > 1E-3)
			
				System.out.println("rep " + Math.abs(v0) + " : tilde " + Math.abs(v1) + " : tilde upper " + Math.abs(v2) + " : xy1 " + Math.abs(v3) + " : xy2 " + Math.abs(v4));
			else
				System.out.println(".");
			
		}

	}
	
}
