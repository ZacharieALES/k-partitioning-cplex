package main;

import results.ComputeResults;
import formulation.CplexParam;
import formulation.PartitionWithRepresentative;
import formulation.RepParam;
import formulation.TildeParam;
import ilog.concert.IloException;

public class Execution_time_ac_ss_tildes extends Execution{

	double[][][] tildes_temps = new double[21][21][5];
	double[][][] bestInt = new double[21][21][5];

	double[][][] norm_temps = new double[21][21][5];
	
	public Execution_time_ac_ss_tildes(int nm, int nM2, int km, int kM2,
			int im, int iM2) {
		super(nm, nM2, km, kM2, im, iM2);
	}

	@Override
	public void execution() throws IloException {

		PartitionWithRepresentative repT = ((PartitionWithRepresentative)createPartition(new CplexParam(false, true, true, -1), new TildeParam(true, false, false)));
		tildes_temps[c_n][c_k][c_i] = repT.solve();
		bestInt[c_n][c_k][c_i] = repT.getObjValue2();
		
		ComputeResults.serialize(tildes_temps, "./results/isco_rr/tildes_temps");
		ComputeResults.serialize(bestInt, "./results/isco_rr/bestInt");
		
		System.out.println("tilde: " + Math.round(tildes_temps[c_n][c_k][c_i]) + "s");
		
		PartitionWithRepresentative repSST = ((PartitionWithRepresentative)createPartition(new CplexParam(false, true, true, -1), new RepParam(true, false)));
		norm_temps[c_n][c_k][c_i] = repSST.solve();

		ComputeResults.serialize(norm_temps, "./results/isco_rr/norm_temps");
		
		System.out.println("ss tildes: " + Math.round(norm_temps[c_n][c_k][c_i]) + "s");
		
	}
	
	public void unserialize(){

		tildes_temps = ComputeResults.unserialize("./results/isco_rr/tildes_temps", double[][][].class);
		bestInt = ComputeResults.unserialize("./results/isco_rr/bestInt", double[][][].class);

		norm_temps = ComputeResults.unserialize("./results/isco_rr/norm_temps", double[][][].class);

	}
	

	
	public void printResult(){
		
		for(int n = 7 ; n < 21 ; ++n){
			System.out.print("n: " + n + " :\t");
			
			for(int k = 2 ; k < n ; ++k){
//				System.out.println("K: " + k);
			    double impr = 0;
				for(int i = 0 ; i < 5 ; ++i){
//					System.out.println(norm_temps[n][k][i] + " " +tildes_temps[n][k][i]);
				    impr += ComputeResults.improvement(tildes_temps[n][k][i], norm_temps[n][k][i]);
					
				}
				
				impr *= 100/20;
				
				System.out.print(Math.round(impr) + "\t");

					
			}
			
			System.out.println();
			
		}
		
		
	}
	
	public void printGapResult(){
		
		

		double[][][] res_tildes = ComputeResults.unserialize("ISCO_res_tildes", double[][][].class);
		double [][][] res_ss_tildes = ComputeResults.unserialize("ISCO_res_ss_tildes", double[][][].class);
		
		for(int n = 7 ; n < 21 ; ++n){
			System.out.print(n);
			
			for(int k = 2 ; k < n ; ++k){
//				System.out.println("K: " + k);
			    double gap = 0.0;
			    double gapTildes = 0.0;
				for(int i = 0 ; i < 5 ; ++i){
//					System.out.println(norm_temps[n][k][i] + " " +tildes_temps[n][k][i]);
//					if(bestInt[n][k][i] != 0.0 && bestInt[n][k][i] != 1.0){
						gap += ComputeResults.improvement(bestInt[n][k][i], res_ss_tildes[n][k][i]);
						gapTildes += ComputeResults.improvement(bestInt[n][k][i], res_tildes[n][k][i]);
//					}
//if(n == 13 && k == 11)					
//System.out.println(bestInt[n][k][i] + "/" +  res_tildes[n][k][i] + "/" + res_ss_tildes[n][k][i]);		
					
				}
				
				gap *= 100/5;
				gapTildes *= 100/5;
				
				System.out.print(" & " + Math.abs(Math.round(gap)) + "/" + Math.abs(Math.round(gapTildes)));
//System.exit(0);
					
			}
			
			for(int k = n ; k <= 19 ; ++k)
				System.out.print("&");
			
			
			System.out.println("\\\\");
			
		}
		
		
	}


}
