package main;

import java.util.ArrayList;

import cplex.Cplex;
import formulation.RepParam;
import ilog.concert.IloException;

public class ExecutionIsRelaxationOptimal extends Execution{

	public ExecutionIsRelaxationOptimal(Cplex cplex, int nm, int nM2, int km, int kM2,
			int im, int iM2) {
		super(cplex, nm, nM2, km, kM2, im, iM2);


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
		

		RepParam param = new RepParam(null, cplex, -1, true);
		
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
