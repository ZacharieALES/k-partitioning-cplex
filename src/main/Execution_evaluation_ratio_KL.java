package main;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import ilog.concert.IloException;

/**
 * Evaluate the criterion used to find the best violated inequality in ST_KL separation.
 * If an inequality ax <= b is considered, the ratio can be used to evaluate the inequality (ax/b) or the difference (ax-b)
 * @author zach
 *
 */
public class Execution_evaluation_ratio_KL extends Execution{

	double[][][] results;
	
	public Execution_evaluation_ratio_KL(int nm, int nM2, int km, int kM2, int im,
			int iM2) {
		super(nm, nM2, km, kM2, im, iM2);
		
		results = new double[nM2+1][kM2+1][iM2+1];
		
	}

	@Override
	public void execution() throws IloException {
		
//			/* Cutting Plane */
//			PartitionWithRepresentative rep = createPartition(new CplexParam(false, true, false, -1), new TildeParam(false, false, false, true, true, false, false));
//			CP_Rep cprep = new CP_Rep(rep, 500, c_i, 300, 750, true, 3600);
//			
//			rep.setParam(IloCplex.IntParam.AdvInd, 1);
//			
//			cprep.solve();
//			
//			results[c_n][c_k][c_i] = Math.abs(cprep.cpresult.cp_first_relaxation-cprep.cpresult.firstRelaxation) / cprep.cpresult.cp_first_relaxation;
//			
//			System.out.println("Res (n,k,i): (" + c_n + "," + c_k + "," + c_i + ") : "+ results[c_n][c_k][c_i]); 
//			System.out.println(cprep.cpresult.cp_first_relaxation + " " + cprep.cpresult.firstRelaxation);
//			printResult2(c_n, c_k, c_i);
//
//
//			ObjectOutputStream oos = null;
//			try {
//			      final FileOutputStream fichier = new FileOutputStream("ratio_false_100_ss_ineq.ser");
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

			double[][][] results1 = null;
			double[][][] results2 = null;
			
			try {
				FileInputStream fis = new FileInputStream("ratio_false_100_ss_ineq.ser");
				ObjectInputStream ois= new ObjectInputStream(fis);
				FileInputStream fis2 = new FileInputStream("ratio_true_100_ss_ineq.ser");
				ObjectInputStream ois2= new ObjectInputStream(fis2);
				try {	
					results1 = (double[][][]) ois.readObject();
					results2 = (double[][][]) ois2.readObject();
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
					double res2 = 0.0;
					
					for(int i = 0 ; i <= 4 ; ++i){
//						System.out.println(results1[n][k][i] > results2[n][k][i]);		
//						System.out.println(results1[n][k][i] + " : " +  results2[n][k][i]);						
						res += results1[n][k][i];
						res2 += results2[n][k][i] - results1[n][k][i];
					}
					res*=100;
					res2*=100;
					
					System.out.print((Math.round(res/5*10)/10.0) + " (+" + (Math.round((res2/5*10))/10.0) + ") & \t");
//					System.out.print((res/5) + "(" + (res2/5) + "\t");
				}
				System.out.println("\\\\");
			}
	}
	
	
	public void printResult2(int cn, int ck, int ci){
		
		for(int n = 20 ; n <= cn ; ++n){
			System.out.print("n : " + n + "\t");
			for(int k = 2 ; k <= ck ; ++k){
				double res = 0.0;
				
				for(int i = 0 ; i <= ci ; ++i)
					res += results[n][k][i];
				
				System.out.print((res/(ci+1)) + "\t");
			}
			System.out.println();
		}
		
	}

}
