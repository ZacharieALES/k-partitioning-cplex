
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

import ilog.concert.IloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import results.ComputeResults;
import cutting_plane.CP_Rep;
import formulation.CplexParam;
import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.Partition_with_tildes;
import formulation.Partition_x_y;
import formulation.Partition_x_y_2;
import formulation.RepParam;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import formulation.XYParam;

public class Execution_inoc_numero_special_tsp extends Execution{

	/**
	 * Store the results of this experiment :
	 * - dimension 1 : K from 0 to 3 (0 : 2, 1 : 4, 2 : 6, 3 : 8)
	 * - dimension 2 : formulation from 0 to 4 (0 : er, 1 : nc1, 2 : nc2, 3 : ext, 4 : cp)
	 * - dimension 3 : instance from 0 to 4 (0 : burma14, 1 : bayg2, 2 : berlin52, 3 : bier127, 4 : pr76)
	 * - dimension 4 : results from 0 to 3 (0 : relaxation, 1 : time, 2 : tree size, 3 : integer solution)
	 */
	double[][][][] resultats;
	

	double tilim = 3600;

	String bis = "_bis.ser";
	boolean saveInBisFile;

	File[] fileName;
	
	String data_folder = "./data/tsp";
	
	String saveFolder = "./results/expe_num_special_inoc_tsp/";
	String saveFile = "resultats_with_1h_and_fast_heuristics_in_bc.ser";
	String previousSaveFile = "./results/expe_num_special_inoc_tsp/resultats_node_cluster_with_general_clique.ser";

	public int nbOfK = 4;
	public int nbOfConfigurations = 5;
	public int nbOfParametersSaved = 4;
	
	public Execution_inoc_numero_special_tsp(){

		super(0, 0, 0, 0, 0, 0);

		
		fileName = new File(data_folder).listFiles();
		
		Arrays.sort(fileName);
		
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

		resultats = unserialize4DTable(saveFolder + saveFile);
				
	}
	
	public void convertInputs(String pathFolder){
		File f = new File(pathFolder);
		
		if(f.exists() && f.isDirectory()){
			File [] files = f.listFiles();
			
			for(File path: files){
				ComputeResults.convert2DCoordinatesIntoDistanceInputFile(path.getAbsolutePath(), path.getAbsolutePath() + ".converted.txt", 1, 2);
			}
		}
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
				
			}
		
	}
	

	public void performFormulation(int id_k, int formulation, int instance, String formulation_name){
		
		XYParam xy1p = new XYParam(true, false, true);
		XYParam xy2p = new XYParam(true, true, true);
		CplexParam cp = new CplexParam(false, true, true, tilim);
		TildeParam tp  = new TildeParam(true, true, Triangle.USE, true, true, true);
		RepParam rp = new RepParam(true, Triangle.USE, true, true, true);
		TildeParam tp_for_cutting_plane  = new TildeParam(false, true, Triangle.USE_IN_BC_ONLY, true, false, false);
		
		if(resultats[id_k][formulation][instance][0] == -Double.MAX_VALUE
				){
			
			int k = (id_k + 1) *2;
			Partition p = null;
			
			/* If it is not a cutting plane */
			if( formulation < 4 ){
				switch(formulation){
				case 0 :
					p = new PartitionWithRepresentative(k, fileName[instance].getPath(), cp, rp);
					break;
				case 1 : 
					p = new Partition_x_y(k, fileName[instance].getPath(), cp, xy1p);
					break;
				case 2 : 
					p = new Partition_x_y_2(k, fileName[instance].getPath(), cp, xy2p);
					break;
				case 3 : 
					p = new Partition_with_tildes(k, fileName[instance].getPath(), cp, tp);
					break;
				}
				
	//			System.out.println(fileName.get(instance) + " : " + p.d.length);
				
				resultats[id_k][formulation][instance][1] = p.solve();
				resultats[id_k][formulation][instance][0] = p.getNnodes();
				resultats[id_k][formulation][instance][2] = p.getBestObjValue2();
				resultats[id_k][formulation][instance][3] = p.getObjValue2();
			}
			/* If it is a cutting plane */
			else{
				p = new Partition_with_tildes(k, fileName[instance].getPath(), cp, tp_for_cutting_plane);
				CP_Rep cprep = new CP_Rep((Partition_with_tildes)p, 500, -1,  1, 10, true, tilim);

				resultats[id_k][formulation][instance][1] = cprep.solve();
				resultats[id_k][formulation][instance][0] = cprep.cpresult.node;
				resultats[id_k][formulation][instance][2] = cprep.cpresult.bestRelaxation;
				resultats[id_k][formulation][instance][3] = cprep.cpresult.bestInt;
				
			}

			if(!saveInBisFile 
					|| (id_k == nbOfK-1 && formulation == nbOfConfigurations && instance == fileName.length - 1)){
				ComputeResults.serialize(resultats, saveFolder + saveFile);
				saveInBisFile = true;
			}
			else{
				ComputeResults.serialize(resultats, saveFolder + saveFile + bis);
				saveInBisFile = false;
			}
			
			ComputeResults.log(ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_k][formulation][instance][2])+ ", " + Math.round(resultats[id_k][formulation][instance][3]) + "] (" + Math.round(resultats[id_k][formulation][instance][0]) + " nodes, " + Math.round(resultats[id_k][formulation][instance][1]) + "s)");
		}
		else{			
			ComputeResults.log("." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_k][formulation][instance][2])+ ", " + Math.round(resultats[id_k][formulation][instance][3]) + "] (" + Math.round(resultats[id_k][formulation][instance][0]) + " nodes, " + Math.round(resultats[id_k][formulation][instance][1]) + "s)");
		}
	}

	@Override
	public void execution() throws IloException {
		
	}
	

public String displayTableTimeGapNodes(){
		
		
		double [][][][] resultats = null;
		
		double [][] bestInt = new double[4][4];
//		try {
//			resultats = ComputeResults.unserialize4D("./results/expe_num_special_inoc_tsp/resultats.ser");
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		int nbFormulations = 5;
		int []order_formulations = new int[nbFormulations];
		order_formulations[0] = 1;
		order_formulations[1] = 2;
		order_formulations[2] = 0;
		order_formulations[3] = 3;
		order_formulations[4] = 4;
		
		String[] instance_name = new String[4];
		instance_name[0] = "burma14";
		instance_name[1] = "bayg29";
		instance_name[2] = "berlin52";
		instance_name[3] = "swiss42";
				
		/* Find for each of the 5 configurations (i.e., the 4 formulations and the CP) the best feasible solution */
		for(int K = 0 ; K < 4 ; ++K)
			for(int i = 0 ; i < 4 ; ++i){
				bestInt[K][i] = Double.MAX_VALUE;
					
				for(int f = 0 ; f <= 4 ; ++f){
					double c_int = resultats[K][f][i][3];
					
					if(c_int != -1.0 && c_int < bestInt[K][i])
						bestInt[K][i] = c_int;
				}
				
			}
		
		String content = "\\begin{center}\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{M{1.5cm}M{0.5cm}*{5}{r@{\\hspace{0.4cm}}r}@{\\hspace{0.5cm}}*{5}{r@{\\hspace{0.5cm}}}}\\toprule\\multirow{2}{*}{\\textbf{Instance}} & \\multirow{2}{*}{\\textbf{K}}&\\multicolumn{10}{c}{\\textbf{Time (s) and Gap (\\%)}} & \\multicolumn{5}{c}{\\textbf{Nodes}} \\\\& & \\multicolumn{2}{c}{$(F_{nc1})$} & \\multicolumn{2}{c}{$(F_{nc2})$} & \\multicolumn{2}{c}{$(F_{er})$} & \\multicolumn{2}{c}{$(F_{ext})$} & \\multicolumn{2}{c}{$(BC)$} & $(F_{nc1})$ & $(F_{nc2})$ & $(F_{er})$ & $(F_{ext})$ & $(BC)$\\tabularnewline\\hline";
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}\\end{center}";
		
		
		
		for(int instance = 0 ; instance < 4 ; instance++){
			content += "\\multirow{" + nbFormulations + "}{*}{\\textbf{" + instance_name[instance] + "}} \t";
			for(int k = 0 ; k <= 3 ; ++k){
				content += "& \\textbf{" + (2*k+2) + "} \t";
				
				/* Add the time/gap for the 4 formulations */
				for(int formulation = 0 ; formulation <= 4 ; ++formulation){
					
					double gap = 0.0;
					
					double time = resultats[k][order_formulations[formulation]][instance][1];
					double relax = resultats[k][order_formulations[formulation]][instance][2];
					double bestI = bestInt[k][instance];	
					
									
					/* If there is a gap */
					if(time >= 600)
						gap = 100.0 * ComputeResults.improvement(bestI, relax);
				System.out.println(instance_name[instance] + " K = " + k + " formulation : " + order_formulations[formulation] + " bestI = " + bestI + " " + relax);	
					content += "& " + Math.round(time) + "s & " + Math.round(gap) + "\\%";
				}
				
				/* Add the nodes for the 4 formulations */
				for(int formulation = 0 ; formulation <= 4 ; ++formulation)
					content += "& " + Math.round(resultats[k][order_formulations[formulation]][instance][0]);
				
				
				content += "\\\\\n";
			}
						
			if(instance == 3)
				content += "\n";
			else
				content += "\\hline";
		}
		content += suf_table1;
		content += "Mean results (in terms of time, gap and number of nodes in the branch-and-cut tree) obtained for each of the five configurations over four instances of the TSP lib. Configuration $(BC)$ corresponds to the branch-and-cut algorithm presented section~\\ref{sec:branch}.\n";
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

		content += displayTableTimeGapNodes();
		
		content += "\\end{document}\n";

		
		ComputeResults.writeInFile("./tables_tsp_time_gap_nodes.tex", content);
		
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
				for(int k = 0 ; k < fileName.length ; ++k)
						array[i][j][k][0] = -Double.MAX_VALUE;
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


}
