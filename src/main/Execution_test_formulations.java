package main;

import java.util.ArrayList;

import formulation.PartitionWithRepresentative;
import formulation.Partition_x_y;
import formulation.Partition_x_y_2;
import formulation.RepParam;
import formulation.TildeParam;
import formulation.XYParam;
import ilog.concert.IloException;

public class Execution_test_formulations extends Execution{

	public Execution_test_formulations(int nm, int nM2, int km, int kM2, int im,
			int iM2) {
		super(nm, nM2, km, kM2, im, iM2);
		
	}


	@Override
	public void execution() throws IloException {
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
		gapValues.add(-250.0);
		gapValues.add(-500.0);
		
		for(int i = 0 ; i < gapValues.size() ; ++i){

			RepParam rep_p = new RepParam(null, -1, false);
			rep_p.gapDiss = gapValues.get(i);

			TildeParam tp = new TildeParam(null, -1, false, false);
			tp.gapDiss = gapValues.get(i);
			
			XYParam rp = new XYParam(null, -1);
			rp.gapDiss = gapValues.get(i);
			


			PartitionWithRepresentative rep0 = ((PartitionWithRepresentative)createPartition(rep_p));
			rep0.solve();
			double v0 = rep0.getObjValue2();

			PartitionWithRepresentative rep = ((PartitionWithRepresentative)createPartition(tp));
			rep.solve();
			double v1 = rep.getObjValue2();
			
			tp.useUpper = true;
			PartitionWithRepresentative rep_tilde_with_upper = ((PartitionWithRepresentative)createPartition(tp));
			rep_tilde_with_upper.solve();
			double v2 = rep_tilde_with_upper.getObjValue2();
			
			
			
//			System.out.println("Tilde sans upper");
//			rep_tilde_with_upper.displayEdgeVariables(5);
//			rep_tilde_with_upper.displayRepresentativeVariables(5);
			
			Partition_x_y rep_x_y = ((Partition_x_y)createPartition(rp));
			rep_x_y.solve();
			double v3 = rep_x_y.getObjValue2();

//			System.out.println("XY");
//			rep_x_y.displayEdgeVariables(5);
//			System.out.println("-");
//			rep_x_y.displayNodeClusterVariables(5);
//			
			
			rp.isSecondXYFormulation = true;
			Partition_x_y_2 rep_x_y2 = ((Partition_x_y_2)createPartition(rp));
			rep_x_y2.solve();

//			System.out.println("--");
//			rep_x_y2.displayEdgeVariables(5);
//			System.out.println("-");
//			rep_x_y2.displayNodeClusterVariables(5);
//			System.out.println("-----------");
			
			double v4 = rep_x_y2.getObjValue2();
			
			
			
			if(Math.abs(v1-v0) > 1E-3 || Math.abs(v1-v2) > 1E-3 || Math.abs(v1-v3) > 1E-3 || Math.abs(v1-v4) > 1E-3)
			
				System.out.println("rep " + Math.abs(v0) + " : tilde " + Math.abs(v1) + " : tilde upper " + Math.abs(v2) + " : xy1 " + Math.abs(v3) + " : xy2 " + Math.abs(v4));
			else
				System.out.println(".");
			
		}

	}
	
}
