
//Protocole :
//
//On utilise les formulations :
//- rep
//- xy1
//- xy2 renforcé
//- tildes renforcés
//
//on test pour k : 2 4 6 8
//On test les 6 instances de tsp suivantes :
// - burma14
// - bayg29
// - berlin52
// - bier127
// - gr202
// - a280
//
// On utilise cplex par défaut pendant 1h.
// Pour chaque couple (k, instance), on relève :
//- la meilleure relaxation obtenue : x1, x2, x3, x4
//- le nombre de sommets de l'arborescence
//- la meilleure solution entière obtenue
//
// Les résultats seront stockés dans une table de dimension 4, qui correspondent à :
//- d1 : K de 0 à 3 (0 <-> K = 2, ..., 3 <-> K = 8)
//- d2 : formulation de 0 à 3 (0 <-> rep, 1 <-> xy1, 2 <-> xy2, 3 <-> tildes)
//- d3 : instance de 0 à 9 (0 <-> 14, 1 <-> 29, 2 <-> 52, 3 <-> 42, 4 <-> 76)
//- d4 : résultat de 0 à 3 (0 <-> relaxation, 1 <-> temps, 2 <-> taille de l'arbre, 3 <-> solution entière)
package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cplex.Cplex;
import cutting_plane.CP_Rep;
import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.PartitionWithTildes;
import formulation.Partition_x_y;
import formulation.Partition_x_y_2;
import formulation.RepParam;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import formulation.XYParam;
import ilog.concert.IloException;
import results.ComputeResults;

public class ExecutionInocNumeroSpecialNewGraphs extends Execution{

	/**
	 * Store the results of this experiment :
	 * - dimension 1 : K from 0 to 3 (0 : 2, 1 : 4, 2 : 6, 3 : 8)
	 * - dimension 2 : formulation from 0 to 4 (0 : er, 1 : nc1, 2 : nc2, 3 : ext, 4 : cp)
	 * - dimension 3 : instance from 0 to 3 (0 : burma14, 1 : bayg2, 2 : berlin52, 3 : bier127)
	 * - dimension 4 : results from 0 to 3 (0 : relaxation, 1 : time, 2 : tree size, 3 : integer solution)
	 */
	double[][][][] resultats;
	
	double[][][] resultats_cp_sans_tildes_dans_bc;
	

	File[] fileName;
	int tilim = 3600;
	String saveFolder = "./results/expe_num_special_inoc_new/";
	String saveFile = "results4D-K-config-instance-result--tilim_" + tilim + ".ser";
	String saveCpSansTildeInBCFile = "results_cp_sans_tilde_dans_bc_3D-K-instance-result--tilim_" + tilim + ".ser";
	String savePath = saveFolder + saveFile;
	String bis = "_bis.ser";
	boolean saveInBisFile;


	String data_folder = "./data/mac-frauke_liers/";
	
	public int nbOfK = 4;
	public int nbOfConfigurations = 5;
	public int nbOfParametersSaved = 4;
	
	public ExecutionInocNumeroSpecialNewGraphs(Cplex cplex){

		super(cplex, 0, 0, 0, 0, 0, 0);
		
		fileName = new File(data_folder).listFiles();
		
		/* Save the list of the computed files in a text file */
		 try{
		     FileWriter fw = new FileWriter(saveFolder + "instances_name_" + saveFile, false); 
		     BufferedWriter output = new BufferedWriter(fw);

		     output.write("---\n" + ComputeResults.getDate() + "\n");
		     output.write("number of different values of K: " + nbOfK + "\n");
		     output.write("number of configurations: " + nbOfConfigurations + "\n");
		     output.write("number of parameters saved: " + nbOfParametersSaved + "\n");
		     for(File f : fileName)
		    	 output.write(f.getName() + "\n");
		     
		     output.flush();
		     output.close();
		 }
		 catch(IOException ioe){
		     System.out.print("Erreur : ");
		     ioe.printStackTrace();
		 }


		resultats = this.unserialize4DTable(savePath);
		resultats_cp_sans_tildes_dans_bc = this.unserialize3DTable(saveCpSansTildeInBCFile);
		
		resultats[0][3][0][0] = 0; // node
		resultats[0][3][0][1] = 3723; // time
		resultats[0][3][0][2] = -2608561; // relax
		resultats[0][3][0][3] = -1; // int

		resultats[1][3][0][0] = 0;
		resultats[1][3][0][1] = 3600;
		resultats[1][3][0][2] = -2716399;
		resultats[1][3][0][3] =  -1;

		resultats[2][3][0][0] = 0;
		resultats[2][3][0][1] = 1736;
		resultats[2][3][0][2] = -2716466;
		resultats[2][3][0][3] =  -2716466;

		resultats[3][3][0][0] = 0;
		resultats[3][3][0][1] = 1455;
		resultats[3][3][0][2] = -2716421;
		resultats[3][3][0][3] =  -2716390;

		resultats[0][3][1][0] = 0;
		resultats[0][3][1][1] = 3651;
		resultats[0][3][1][2] = -2819227;
		resultats[0][3][1][3] =  -1;

		resultats[1][3][1][0] = 0;
		resultats[1][3][1][1] = 3600;
		resultats[1][3][1][2] = -2928500;
		resultats[1][3][1][3] =  -1;

		resultats[2][3][1][0] = 0;
		resultats[2][3][1][1] = 1440;
		resultats[2][3][1][2] = -2929727;
		resultats[2][3][1][3] =  -2929727;

		resultats[3][3][1][0] = 0;
		resultats[3][3][1][1] = 1614;
		resultats[3][3][1][2] = -2929616;
		resultats[3][3][1][3] =  -2929547;

		resultats[0][3][2][0] = 0;
		resultats[0][3][2][1] = 3600;
		resultats[0][3][2][2] = -3850814;
		resultats[0][3][2][3] =  -1;

		resultats[1][3][2][0] = 3;
		resultats[1][3][2][1] = 3600;
		resultats[1][3][2][2] = -3906637;
		resultats[1][3][2][3] =  -1;

		resultats[2][3][2][0] = 0;
		resultats[2][3][2][1] = 740;
		resultats[2][3][2][2] = -3906785;
		resultats[2][3][2][3] =  -3906758;

		resultats[3][3][2][0] = 0;
		resultats[3][3][2][1] = 763;
		resultats[3][3][2][2] = -3906779;
		resultats[3][3][2][3] =  -3906770;

		resultats[0][3][3][0] = 0;
		resultats[0][3][3][1] = 3600;
		resultats[0][3][3][2] = -3930641;
		resultats[0][3][3][3] =  -1;

		resultats[1][3][3][0] = 0;
		resultats[1][3][3][1] = 3600;
		resultats[1][3][3][2] = -4010136;
		resultats[1][3][3][3] =  -1;

		resultats[2][3][3][0] = 0;
		
		System.out.println("new graph: Check this result");
		resultats[2][3][3][1] = 3721;
		resultats[2][3][3][2] = -4010521;
//		resultats[2][3][3][2] = -2608758;
		resultats[2][3][3][3] =  -1;
		
		resultats[2][4][5][1] = 3600;
		
// Last result		
//		.12 mai 2016 19:07:47 : bc:     [relaxation, int] : [-2811389, -2810686] (0 nodes, 3600s)
//		12 mai 2016 19:48:00 : bc sans tilde dans bc:   [relaxation, int] : [-2811248, -2811248] (0 nodes, 2410s)
	}

	public void solve(){
		
		for(int file = 0 ; file < fileName.length ; ++file)
			for(int k = 0 ; k < nbOfK ; k++){
				
				ComputeResults.log(file + "/" + fileName.length + ": " + fileName[file] + " K = " + ((k+1)*2));
				
				/* Representative formulation */
				performFormulation(k, 0, file, "rep");
				
				/* XY1 formulation */	
				performFormulation(k, 1, file, "xy1");
				
				/* XY2 formulation */
				performFormulation(k, 2, file, "xy2");
				
				/* Tilde formulation */
				performFormulation(k, 3, file, "tilde");
				
				/* Tilde formulation */
				performFormulation(k, 4, file, "bc");

				/* Tilde formulation */
				performFormulation(k, 5, file, "bc sans tilde dans bc");
				
			}
		
	}
	

	public void performFormulation(int id_k, int formulation, int instance, String formulation_name){

		try {
		TildeParam tp = new TildeParam(fileName[instance].getPath(), cplex, -1, true, Triangle.USE, true, true, true);
		TildeParam tp_for_cutting_plane = new TildeParam(fileName[instance].getPath(), cplex, -1, true, Triangle.USE_IN_BC_ONLY, true, false, false);
		
		RepParam rp = new RepParam(fileName[instance].getPath(), cplex, -1, Triangle.USE, true, true, true);
		XYParam xy1p = new XYParam(fileName[instance].getPath(), cplex, -1, false, true);
		XYParam xy2p = new XYParam(fileName[instance].getPath(), cplex, -1, true, true);
		rp.tilim = tilim;
		tp.tilim = tilim;
		xy1p.tilim = tilim;
		xy2p.tilim = tilim;
		
		if( (formulation <= 4 && resultats[id_k][formulation][instance][0] == -Double.MAX_VALUE
			|| formulation == 5 && resultats_cp_sans_tildes_dans_bc[id_k][instance][0] == -Double.MAX_VALUE)
			//|| formulation == 3//|| formulation == 3 ||  id_k == 1
				)
		{
			int k = (id_k + 1) *2;
			Partition p = null;
			
			/* If it is not a cutting plane */
			if( formulation < 4 ){
				switch(formulation){
				case 0 :
					rp.KMax = k;
					p = new PartitionWithRepresentative(rp);
					break;
				case 1 : 
					xy1p.KMax = k;
					p = new Partition_x_y(xy1p);
					break;
				case 2 : 
					xy2p.KMax = k;
					p = new Partition_x_y_2(xy2p);
					break;
				case 3 : 
					tp.KMax = k;
					p = new PartitionWithTildes(tp);
					break;
				}
				
	//			System.out.println(fileName.get(instance) + " : " + p.d.length);
				
				resultats[id_k][formulation][instance][1] = p.getCplex().solve();
				resultats[id_k][formulation][instance][0] = p.getCplex().getNnodes();
				resultats[id_k][formulation][instance][2] = p.getCplex().getBestObjValue();
				resultats[id_k][formulation][instance][3] = p.getCplex().getObjValue();
			}
			/* If it is a cutting plane */
			else{
				
				if(formulation == 4){
					CP_Rep.useTildeInBC = true;
				}
				else{
					CP_Rep.useTildeInBC = false;
				}
				
				CP_Rep cprep = new CP_Rep(tp_for_cutting_plane, 500, -1,  1, 4, true, tilim);

				if(formulation == 4){
					resultats[id_k][formulation][instance][1] = cprep.solve();
					resultats[id_k][formulation][instance][0] = cprep.cpresult.node;
					resultats[id_k][formulation][instance][2] = cprep.cpresult.bestRelaxation;
					resultats[id_k][formulation][instance][3] = cprep.cpresult.bestInt;
				}
				else{
					resultats_cp_sans_tildes_dans_bc[id_k][instance][1] = cprep.solve();
					resultats_cp_sans_tildes_dans_bc[id_k][instance][0] = cprep.cpresult.node;
					resultats_cp_sans_tildes_dans_bc[id_k][instance][2] = cprep.cpresult.bestRelaxation;
					resultats_cp_sans_tildes_dans_bc[id_k][instance][3] = cprep.cpresult.bestInt;		
				}
				
			}
			
			if(!saveInBisFile 
					|| (id_k == nbOfK-1 && formulation == nbOfConfigurations)){
				if(formulation <= 4)
					ComputeResults.serialize(resultats, savePath);
				else
					ComputeResults.serialize(resultats_cp_sans_tildes_dans_bc, saveCpSansTildeInBCFile);
				saveInBisFile = true;
			}
			else{
				if(formulation <= 4)
					ComputeResults.serialize(resultats, savePath + bis);
				else
					ComputeResults.serialize(resultats_cp_sans_tildes_dans_bc, saveCpSansTildeInBCFile + bis);
				saveInBisFile = false;
			}			
			if(formulation <= 4)
				ComputeResults.log(ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_k][formulation][instance][2])+ ", " + Math.round(resultats[id_k][formulation][instance][3]) + "] (" + Math.round(resultats[id_k][formulation][instance][0]) + " nodes, " + Math.round(resultats[id_k][formulation][instance][1]) + "s)");
			else
				ComputeResults.log(ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats_cp_sans_tildes_dans_bc[id_k][instance][2])+ ", " + Math.round(resultats_cp_sans_tildes_dans_bc[id_k][instance][3]) + "] (" + Math.round(resultats_cp_sans_tildes_dans_bc[id_k][instance][0]) + " nodes, " + Math.round(resultats_cp_sans_tildes_dans_bc[id_k][instance][1]) + "s)");
		}
		else{
			if(formulation <= 4)
				ComputeResults.log("." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_k][formulation][instance][2])+ ", " + Math.round(resultats[id_k][formulation][instance][3]) + "] (" + Math.round(resultats[id_k][formulation][instance][0]) + " nodes, " + Math.round(resultats[id_k][formulation][instance][1]) + "s)");			
			else
				ComputeResults.log("." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats_cp_sans_tildes_dans_bc[id_k][instance][2])+ ", " + Math.round(resultats_cp_sans_tildes_dans_bc[id_k][instance][3]) + "] (" + Math.round(resultats_cp_sans_tildes_dans_bc[id_k][instance][0]) + " nodes, " + Math.round(resultats_cp_sans_tildes_dans_bc[id_k][instance][1]) + "s)");

		}
		}catch(IloException e) {e.printStackTrace();}
	}

	@Override
	public void execution() throws IloException {
	}
	

	public String displayTableTimeGapNodes(){
		
		
		
		double [][] bestInt = new double[nbOfK][fileName.length];
			
		for(int i = 0 ; i < fileName.length ; ++i)
			try {
				fileName[i] = new File(fileName[i].getCanonicalPath().replace("_", "."));
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		int nbFormulations = 5;
		int []order_formulations = new int[nbFormulations];
		order_formulations[0] = 1;
		order_formulations[1] = 2;
		order_formulations[2] = 0;
		order_formulations[3] = 3;
		order_formulations[4] = 4;
//		order_formulations[5] = 5;
				
		/* Find for each of the 5 configurations (i.e., the 4 formulations and the CP) the best feasible solution */
		for(int K = 0 ; K < nbOfK ; ++K)
			for(int i = 0 ; i < fileName.length ; ++i){
				bestInt[K][i] = Double.MAX_VALUE;
					
				for(int f = 0 ; f < order_formulations.length ; ++f){

					double c_int;
					if(f != 5)
						c_int = resultats[K][f][i][3];
					else
						c_int = resultats_cp_sans_tildes_dans_bc[K][i][3];

					if(c_int != -1.0 && c_int < bestInt[K][i])
						bestInt[K][i] = c_int;
				}
				
				if(bestInt[K][i] == Double.MAX_VALUE)
					System.out.println("Error, no integer value for: K = " + K + ", filename = " + fileName[i]);
				
			}
		
		String content = "\\begin{center}\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{"
				+ "M{1.5cm}M{0.5cm}"
				+ "*{5}{r@{\\hspace{0.4cm}}r}@{\\hspace{0.5cm}}"
				+ "*{5}{c@{\\hspace{0.5cm}}}}"
				+ "\\toprule\\multirow{2}{*}{\\textbf{Instance}} "
				+ "& \\multirow{2}{*}{\\textbf{K}}"
				+ "&\\multicolumn{10}{c}{\\textbf{Time (s) and Gap (\\%)}} "
				+ "& \\multicolumn{5}{c}{\\textbf{Nodes}} \\\\& "
				+ "& \\multicolumn{2}{c}{$(F_{nc1})$} "
				+ "& \\multicolumn{2}{c}{$(F_{nc2})$} "
				+ "& \\multicolumn{2}{c}{$(F_{er})$} "
				+ "& \\multicolumn{2}{c}{$(F_{ext})$} "
				+ "& \\multicolumn{2}{c}{$(BC)$} "
//				+ "& \\multicolumn{2}{c}{$(BC_2)$} "
				+ "& $(F_{nc1})$ & $(F_{nc2})$ & $(F_{er})$ & $(F_{ext})$ & $(BC)$\\tabularnewline\\hline";
		
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}\\end{center}";
		
		/* For each instance */
		for(int instance = 0 ; instance < fileName.length ; instance++){
//			try {
//				content += "\\multirow{" + nbFormulations + "}{*}{\\textbf{" + fileName[instance].getCanonicalPath() + "}} \t";
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			content += "\\multirow{" + nbOfK + "}{*}{\\textbf{" + instance + "}} \t";
			for(int k = 0 ; k < nbOfK ; ++k){
				content += "& \\textbf{" + (2*k+2) + "} \t";
				
				/* Add the time/gap for the formulations */
				for(int formulation = 0 ; formulation < order_formulations.length ; ++formulation){
					
					double gap = 0.0;
					
					double time;
					double relax;
					double bestI = bestInt[k][instance];	
					
					if(formulation != 5){
						time = resultats[k][order_formulations[formulation]][instance][1];
						relax = resultats[k][order_formulations[formulation]][instance][2];
					}
					else{
						time = resultats_cp_sans_tildes_dans_bc[k][instance][1];
						relax = resultats_cp_sans_tildes_dans_bc[k][instance][2];
					}
					
									
					/* If there is a gap */
					if(time >= tilim)
						gap = 100.0 * ComputeResults.improvement(bestI, relax);
					
					if(time > tilim + 100){
						System.out.println("high max time: " + time);
						System.out.println(fileName[instance] + " K = " + k + " formulation : " + order_formulations[formulation] + " bestI = " + bestI + " " + relax + " time: " + time + "s");
					}
					
					content += "& " + Math.round(time) + "s & " + Math.round(gap) + "\\%";
				}
				
				/* Add the nodes for the 4 formulations */
				for(int formulation = 0 ; formulation < order_formulations.length ; ++formulation)
					if(formulation != 5)
						content += "& " + Math.round(resultats[k][order_formulations[formulation]][instance][0]);
					else
						content += "& " + Math.round(resultats_cp_sans_tildes_dans_bc[k][instance][0]);
				
				
				content += "\\\\\n";
			}
						
			if(instance == fileName.length-1)
				content += "\n";
			else
				content += "\\hline";
		}
		content += suf_table1;
		content += "Results (in terms of time, gap and number of nodes in the branch-and-cut tree) obtained for each of the configurations over " + fileName.length + " instances of 100 nodes. Configuration $(BC)$ corresponds to the branch-and-cut algorithm presented section~\\ref{sec:branch}.\n";
		content += suf_table2;
		
		return content;
		
	}

	public String displayTableMeanTimeGapNodes(){
		
		
		
		double [][] bestInt = new double[nbOfK][fileName.length];
			
		for(int i = 0 ; i < fileName.length ; ++i)
			try {
				fileName[i] = new File(fileName[i].getCanonicalPath().replace("_", "."));
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		int nbFormulations = 5;
		int []order_formulations = new int[nbFormulations];
		order_formulations[0] = 1;
		order_formulations[1] = 2;
		order_formulations[2] = 0;
		order_formulations[3] = 3;
		order_formulations[4] = 4;
//		order_formulations[5] = 5;
				
		/* Find for each of the 5 configurations (i.e., the 4 formulations and the CP) the best feasible solution */
		for(int K = 0 ; K < nbOfK ; ++K)
			for(int i = 0 ; i < fileName.length ; ++i){
				bestInt[K][i] = Double.MAX_VALUE;
					
				for(int f = 0 ; f < order_formulations.length ; ++f){

					double c_int;
					if(f != 5)
						c_int = resultats[K][f][i][3];
					else
						c_int = resultats_cp_sans_tildes_dans_bc[K][i][3];

					if(c_int != -1.0 && c_int < bestInt[K][i])
						bestInt[K][i] = c_int;
				}
				
				if(bestInt[K][i] == Double.MAX_VALUE)
					System.out.println("Error, no integer value for: K = " + K + ", filename = " + fileName[i]);
				
			}
		
		String content = "\\begin{center}\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{"
				+ "M{1.5cm}M{0.5cm}"
				+ "*{5}{r@{\\hspace{0.4cm}}r}@{\\hspace{0.5cm}}"
				+ "*{5}{c@{\\hspace{0.5cm}}}}"
				+ "\\toprule\\multirow{2}{*}{\\textbf{Instance}} "
				+ "& \\multirow{2}{*}{\\textbf{K}}"
				+ "&\\multicolumn{10}{c}{\\textbf{Time (s) and Gap (\\%)}} "
				+ "& \\multicolumn{5}{c}{\\textbf{Nodes}} \\\\& "
				+ "& \\multicolumn{2}{c}{$(F_{nc1})$} "
				+ "& \\multicolumn{2}{c}{$(F_{nc2})$} "
				+ "& \\multicolumn{2}{c}{$(F_{er})$} "
				+ "& \\multicolumn{2}{c}{$(F_{ext})$} "
				+ "& \\multicolumn{2}{c}{$(BC)$} "
//				+ "& \\multicolumn{2}{c}{$(BC_2)$} "
				+ "& $(F_{nc1})$ & $(F_{nc2})$ & $(F_{er})$ & $(F_{ext})$ & $(BC)$\\tabularnewline\\hline";
		
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}\\end{center}";
		
			
		content += "\\multirow{" + nbOfK + "}{*}{\\textbf{frauk-liers}} \t";
		for(int k = 0 ; k < nbOfK ; ++k){
			content += "& \\textbf{" + (2*k+2) + "} \t";
				
			/* Add the time/gap for the formulations */
			for(int formulation = 0 ; formulation < order_formulations.length ; ++formulation){

				double gap = 0.0;
				double time = 0.0;

				/* For each instance */
				for(int instance = 0 ; instance < fileName.length ; instance++){
					
					double bestI = bestInt[k][instance];	
					double new_time;
					double new_relax;
					
					if(formulation != 5){
						new_time = resultats[k][order_formulations[formulation]][instance][1];
						new_relax = resultats[k][order_formulations[formulation]][instance][2];
					}
					else{
						new_time = resultats_cp_sans_tildes_dans_bc[k][instance][1];
						new_relax = resultats_cp_sans_tildes_dans_bc[k][instance][2];
					}
					time += new_time;			
					
					/* If there is a gap */
					if(time >= tilim)
						gap += 100.0 * ComputeResults.improvement(bestI, new_relax);
					
					if(new_time > tilim + 100){
						System.out.println("high max time: " + time);
						System.out.println(fileName[instance] + " K = " + k + " formulation : " + order_formulations[formulation] + " bestI = " + bestI + " " + new_relax + " time: " + time + "s");
					}
					
//					if(k == 2 && instance == 3)//order_formulations[formulation]== 3)
				
					
				}
				
				time /= fileName.length;
				gap /= fileName.length;

				content += "& " + Math.round(time) + "s & " + Math.round(gap) + "\\%";
				
			}
			
			/* Add the nodes for the 4 formulations */
			for(int formulation = 0 ; formulation < order_formulations.length ; ++formulation){
				
				int nodes = 0;
				
				/* For each instance */
				for(int instance = 0 ; instance < fileName.length ; instance++)
					if(formulation != 5)
						nodes += resultats[k][order_formulations[formulation]][instance][0];
					else
						nodes += resultats_cp_sans_tildes_dans_bc[k][instance][0];
				
				nodes /= fileName.length;
				content += "& " + Math.round(nodes);
			}
			
			
			content += "\\\\\n";
		}
					
//		content += "\\hline";
		
		content += suf_table1;
		content += "Mean results (in terms of time, gap and number of nodes in the branch-and-cut tree) obtained for each of the configurations over " + fileName.length + " instances of 100 nodes. Configuration $(BC)$ corresponds to the branch-and-cut algorithm presented section~\\ref{sec:branch}.\n";
		content += suf_table2;
		
		return content;
		
	}


public void printTablesTimeGapNodes() {
		
		String content = "\\documentclass[landscape]{article}\n\n";

		content += "\\usepackage[french]{babel}\n";
		content += "\\usepackage [utf8] {inputenc} % utf-8 / latin1\n";
		content += "\\usepackage{tikz}\n";
		content += "\\usepackage{amssymb}\n";
		content += "\\setlength{\\hoffset}{-18pt} \n\n";
		
		content += "\\usepackage{array}\n\\usepackage{booktabs}\\usepackage{multirow}\n\\newcolumntype{M}[1]{>{\\centering}m{#1}}\n";
		
		content += "\\setlength{\\oddsidemargin}{0pt} % Marge gauche sur pages impaires \n";
		content += "\\setlength{\\evensidemargin}{0pt} % Marge gauche sur pages paires \n";
		content += "\\setlength{\\marginparwidth}{10pt} % Largeur de note dans la marge \n";
		content += "\\setlength{\\textwidth}{540pt} % Largeur de la zone de texte (17cm) \n";
		content += "\\setlength{\\voffset}{-18pt} % Bon pour DOS \n";
		content += "\\setlength{\\marginparsep}{0pt} % Séparation de la marge \n";
		content += "\\setlength{\\topmargin}{0pt} % Pas de marge en haut \n";
		content += "\\setlength{\\headheight}{0pt} % Haut de page \n";
		content += "\\setlength{\\headsep}{0pt} % Entre le haut de page et le texte \n";
		content += "\\setlength{\\footskip}{0pt} % Bas de page + séparation \n";
		content += "\\setlength{\\textheight}{538pt} % Hauteur de la zone de texte (25cm) \n";
		content += "\\begin{document}\n\n";

		content += displayTableMeanTimeGapNodes();
		content += displayTableTimeGapNodes();
		
		content += "\\end{document}\n";

		String outputTexFile = "./tables_inoc_new_graphs_time_gap_nodes.tex";
		File f = new File(outputTexFile);
		
		if(f.exists())
			f.delete();

		ComputeResults.writeInFile(outputTexFile, content, false);
		
		System.out.println("done");
		
	}

public double[][][][] unserialize4DTable(String path){
	
	double[][][][] array;
	
	/* If the file does not exist, create an empty array */
	if(!new File(path).exists()){
		array = new double[nbOfK][nbOfConfigurations][fileName.length][nbOfParametersSaved];
		
		/* Initialize some results that will be tested afterwards to avoid repeating computations */
		for(int i = 0 ; i < nbOfK ; ++i)
			for(int j = 0 ; j < nbOfConfigurations ; ++j)
				for(int k = 0 ; k < fileName.length ; ++k){
						array[i][j][k][0] = -Double.MAX_VALUE;
				}
		System.out.println("The input file does not exist");
	}
	
	/* If the file exist */
	else{ 

		File f = new File(path);
		File f_bis = new File(path + bis);
		
		/* If the bis file exists and is more recent */
		if(f_bis.exists() && f.lastModified() > f_bis.lastModified()){
			File temp = f;
			f = f_bis;
			f_bis = temp;
		}
		
		/* Try to unserialize it */
		try{
			array = ComputeResults.unserialize(f.getAbsolutePath(), double[][][][].class);
		}catch(Exception e){
			
			/* If it does not work (probably because the program was interrupted while the file was created)
			 * unserialize the bis file */
			array = ComputeResults.unserialize(f_bis.getAbsolutePath(), double[][][][].class);
		}
	}
	
	return array;
}


public double[][][] unserialize3DTable(String path){
	
	double[][][] array;
	
	/* If the file does not exist, create an empty array */
	if(!new File(path).exists()){
		array = new double[nbOfK][fileName.length][nbOfParametersSaved];
		
		/* Initialize some results that will be tested afterwards to avoid repeating computations */
		for(int i = 0 ; i < nbOfK ; ++i)
				for(int k = 0 ; k < fileName.length ; ++k){
						array[i][k][0] = -Double.MAX_VALUE;
				}
		System.out.println("The input file does not exist");
	}
	
	/* If the file exist */
	else {

		File f = new File(path);
		File f_bis = new File(path + bis);
		
		/* If the bis file exists and is more recent */
		if(f_bis.exists() && f.lastModified() > f_bis.lastModified()){
			File temp = f;
			f = f_bis;
			f_bis = temp;
		}
			
		/* Try to unserialize it */
		try{			
			array = ComputeResults.unserialize(f.getAbsolutePath(), double[][][].class);
		}catch(Exception e){
			
			/* If it does not work (probably because the program was interrupted while the file was created)
			 * unserialize the bis file */
			array = ComputeResults.unserialize(f_bis.getAbsolutePath(), double[][][].class);
		}
	}
	
	return array;
}


}
