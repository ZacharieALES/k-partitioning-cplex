package main;

import formulation.RepParam.Triangle;
import formulation.TildeParam;
import ilog.concert.IloException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Execution_nn_1 extends Execution{

	public Execution_nn_1(int nm, int nM2, int km, int kM2, int im,
			int iM2) {
		super(nm, nM2, km, kM2, im, iM2);

		results = new double[nM2+1][kM2+1][iM2+1][2];
		
	}

	double[][][][] results;

	@Override
	public void execution() throws IloException {
			
			results[c_n][c_k][c_i][0] = this.getRootRelaxation(new TildeParam(null, -1, true, Triangle.USE_LAZY_IN_BC_ONLY, true, true, true));
			results[c_n][c_k][c_i][1] = this.getRootRelaxation(new TildeParam(null, -1, false, Triangle.USE_LAZY_IN_BC_ONLY, true, true, true));
			
			System.out.println(results[c_n][c_k][c_i][0] + " " + results[c_n][c_k][c_i][1]);
			printResult();

//			ObjectOutputStream oos = null;
//			try {
//			      final FileOutputStream fichier = new FileOutputStream("nn_1.ser");
//			      oos = new ObjectOutputStream(fichier);
//			      oos.writeObject(results);
//			      oos.flush();
//			    } catch (final java.io.IOException e) {
//			      e.printStackTrace();
//			    } finally {
//			      try {
//			        if (oos != null) {
//			          oos.flush();
//			          oos.close();
//			        }
//			      } catch (final IOException ex) {
//			        ex.printStackTrace();
//			      }
//			    }
	}
	
	public void results(){
			double[][][][] results = null;
			
			try {
				FileInputStream fis = new FileInputStream("nn_1.ser");
				ObjectInputStream ois= new ObjectInputStream(fis);
				try {	
					results = (double[][][][]) ois.readObject();
				} finally {
					try {
						ois.close();
					} finally {
						fis.close();
					}
				}
			} catch(IOException ioe) {
				ioe.printStackTrace();
			} catch(ClassNotFoundException cnfe) {
				cnfe.printStackTrace();
			}
			
			for(int n = 20 ; n <= 30 ; ++n){
				System.out.print("\\textbf{" + n + "} & \t");
				for(int k = 2 ; k <= 10 ; ++k){
					double res = 0.0;
					
					for(int i = 0 ; i <= 4 ; ++i){
//						System.out.println(results1[n][k][i] > results2[n][k][i]);		
//						System.out.println(results1[n][k][i] + " : " +  results2[n][k][i]);						
						res += (results[n][k][i][0]-results[n][k][i][1])/results[n][k][i][1];
					}
					res*=100;
					
					System.out.print((Math.round(res/5*10)/10.0) + " & \t");
//					System.out.print((res/5) + "(" + (res2/5) + "\t");
				}
				System.out.println("\\\\");
			}
			
			
	}
	
	public void printResult(){
		
		for(int n = 20 ; n <= 30 ; ++n){
			System.out.print("n : " + n + "\t");
			for(int k = 2 ; k <= 10 ; ++k){
				double res = 0.0;
				
				for(int i = 0 ; i < 5 ; ++i)
					res += Math.abs(results[n][k][i][0]- results[n][k][i][1]) / results[n][k][i][1] * 100;
				
				res /= 5.0;
				System.out.print(res + "\t");
			}
			System.out.println();
		}
			
		
	}

}
