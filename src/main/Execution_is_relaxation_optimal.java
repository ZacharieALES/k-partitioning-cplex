package main;

import formulation.RepParam;
import ilog.concert.IloException;

import java.util.ArrayList;

public class Execution_is_relaxation_optimal extends Execution{

	public Execution_is_relaxation_optimal(int nm, int nM2, int km, int kM2,
			int im, int iM2) {
		super(nm, nM2, km, kM2, im, iM2);


		for(int i = 0 ; i < 21 ; ++i)
			for(int j = 0 ; j < 21 ; ++j){
				results1[i][j] = 0;
				results2[i][j] = 0;
				results3[i][j] = 0;
			}
	}


	int [][] results1 = new int[21][21];
	int [][] results2 = new int[21][21];
	int [][] results3 = new int[21][21];

	@Override
	public void execution() throws IloException {
		

		RepParam param = new RepParam(false, true);
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
		gapValues.add(-250.0);
		gapValues.add(-500.0);

		for(int i = 0 ; i < gapValues.size() ; ++i){
		
			param.gapDiss = gapValues.get(i);

			if(this.isRelaxationInteger(param)){
				if(i==0)
					results1[c_n][c_k]++;
				if(i==1)
					results2[c_n][c_k]++;
				if(i==2)
					results3[c_n][c_k]++;
				
			}
				
			
		}
		
		if(c_n == nM && (c_k == kM || c_k == c_n-1)) {
			for(int n = 7 ; n < 21 ; ++n){
			
			for(int k = 2 ; k < n ; ++k){
				System.out.print((int) results1[n][k] + "\t");
			}
			System.out.println();
			}
		
			for(int n = 7 ; n < 21 ; ++n){
				
				for(int k = 2 ; k < n ; ++k){
					System.out.print((int)results2[n][k] + "\t");
				}
				System.out.println();
			}
			
			for(int n = 7 ; n < 21 ; ++n){
				
				for(int k = 2 ; k < n ; ++k){
					System.out.print((int)results3[n][k] + "\t");
				}
				System.out.println();
			}
		}
	}
	
	

}
