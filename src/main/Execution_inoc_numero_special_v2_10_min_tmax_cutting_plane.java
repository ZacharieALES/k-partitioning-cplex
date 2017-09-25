package main;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cutting_plane.CP_Rep;
import formulation.CplexParam;
import formulation.Partition_with_tildes;
import formulation.RepParam.Triangle;
import formulation.TildeParam;

//Protocole :
//
//On utilise la formulation tilde et l'algo de plans coupants pendant 10 minutes
//- rep
//- xy1
//- xy2 renforcé
//- tildes renforcés
//
//on test pour k : 2 4 6 8
//on test pour n : 10 15 20 25 30 35 40 45 50
//
//On considère les 3 gaps/types de graphes possibles (poids +, +/-, -).
//
//Les instances utilisées sont de 1 a 10
//
//Pour chaque couple (n,k, type de graphe), on relève :
//- la meilleure relaxation obtenue
//- le nombre de sommets de l'arborescence
//- la meilleure solution entière obtenue
//
//Pour chaque couple (n,k,formulation, type de graphes), on calcule :
//- le gap
//
//Les résultats seront stockés dans une table de dimension 6, qui correspondent à :
//- d1 : n de 0 à 8 (0 <-> n = 10, ..., 8 <-> n = 50)
//- d2 : K de 0 à 3 (0 <-> K = 2, ..., 3 <-> K = 8)
//- d3 : gap de 0 à 2 (0 <-> poids +, 1 <-> poids +/-, 2 <-> poids -)
//- d5 : résultat de 0 à 3 (0 <-> relaxation, 1 <-> temps, 2 <-> taille de l'arbre, 3 <-> solution entiere)
//- d6 : instance de 0 à 10
//
//Les meilleures solutions entières seront stockées dans une table de dimension 3 (n, K, gap).

import ilog.concert.IloException;
import results.ComputeResults;

public class Execution_inoc_numero_special_v2_10_min_tmax_cutting_plane extends Execution{

	double[][][][][] resultats;

	String saveFile1SeparationHeuristic = "./results/expe_num_special_inoc_v2/resultats_cp.ser";
	String saveFileAllFastSeparationHeuristicFolder = "./results/expe_num_special_inoc_v2/";
	String saveFileAllFastSeparationHeuristicFile = "resultats_several_fast_separation_in_bc.ser";
	String saveFileOtherFormulations = "./results/expe_num_special_inoc_v2/resultats.ser";

int nbOfN = 9;
int nbOfK = 4;
int nbOfGap = 3;
int nbOfResults = 4;
int nbOfInstances = 10;
int tilim = 600;

	String bis = "_bis.ser";
	boolean saveInBisFile;
	
	public Execution_inoc_numero_special_v2_10_min_tmax_cutting_plane(int nm, int nM2, int km, int kM2,
			int im, int iM2) {
		super(nm, nM2, km, kM2, im, iM2);

//			resultats = ComputeResults.unserialize(saveFile1SeparationHeuristic, double[][][][][].class);
		
			File folder = new File(saveFileAllFastSeparationHeuristicFolder);
			if(!folder.exists())
				try {
					folder.createNewFile();
				} catch (IOException e) {e.printStackTrace();}
		
			resultats = unserialize5DTable(saveFileAllFastSeparationHeuristicFolder + saveFileAllFastSeparationHeuristicFile);
		
	}

	@Override
	public void execution() throws IloException {
		
		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
		gapValues.add(-250.0);
		gapValues.add(-500.0);
		
		if(c_n % 10 == 0 && c_k % 2 == 0)
		{
			
			int id_n = c_n / 5 - 2;
			int id_k = c_k / 2 - 1;
			
			ComputeResults.log("(n,K): (" + c_n + "," + c_k + "," + c_i + ")");

			for(int gap = 0 ; gap < gapValues.size() ; ++gap){
								
				
				/* Tilde formulation with cp */
				performFormulation(id_n, id_k, c_i, gap, gapValues.get(gap), "cp");
				
			}
		}
		
	}
	
	public void performFormulation(int id_n, int id_k, int id_i, int gap, double gapValue, String formulation_name){

	
		// TODO créer la partition directement dans CP_Rep pour éviter de lui donner une formulation à resoudre en nombres entiers
		
		CplexParam cp = new CplexParam(false, true, true, tilim);
		TildeParam tp2  = new TildeParam(false, true, Triangle.USE_IN_BC_ONLY, true, false, false);

		tp2.gapDiss = gapValue;

		if(resultats[id_n][id_k][gap][0][id_i] == -Double.MAX_VALUE)
		{
			Partition_with_tildes p  = ((Partition_with_tildes)createPartition(cp, tp2));

			CP_Rep cprep = new CP_Rep(p, 500, -1,  1, 5, true, tilim);

			resultats[id_n][id_k][gap][1][id_i] = cprep.solve();
			resultats[id_n][id_k][gap][2][id_i] = cprep.cpresult.node;
			resultats[id_n][id_k][gap][0][id_i] = cprep.cpresult.bestRelaxation;
			resultats[id_n][id_k][gap][3][id_i] = cprep.cpresult.bestInt;
			
			if(!saveInBisFile 
					|| (id_n == nbOfN - 1 && id_k == nbOfK - 1 && gap == nbOfGap - 1 && id_i == nbOfInstances - 1)){
				ComputeResults.serialize(resultats, saveFileAllFastSeparationHeuristicFolder + saveFileAllFastSeparationHeuristicFile);
				saveInBisFile = true;
			}
			else{
				ComputeResults.serialize(resultats, saveFileAllFastSeparationHeuristicFolder + saveFileAllFastSeparationHeuristicFile + bis);
				saveInBisFile = false;
			}
			
			ComputeResults.log(ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_n][id_k][gap][0][id_i])+ ", " + Math.round(resultats[id_n][id_k][gap][3][id_i]) + "] (" + Math.round(resultats[id_n][id_k][gap][2][id_i]) + " nodes, " + Math.round(resultats[id_n][id_k][gap][1][id_i]) + "s)");
		}
		else
		{
			System.out.println("." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_n][id_k][gap][0][id_i])+ ", " + Math.round(resultats[id_n][id_k][gap][3][id_i]) + "] (" + Math.round(resultats[id_n][id_k][gap][2][id_i]) + " nodes, " + Math.round(resultats[id_n][id_k][gap][1][id_i]) + "s)");		
		}
		
	}
	

public double[][][][][] unserialize5DTable(String path){
	
	double[][][][][] array;
	
	/* If the file does not exist, create an empty array */
	if(!new File(path).exists()){
		array = new double[nbOfN][nbOfK][nbOfGap][nbOfResults][nbOfInstances];
		
		/* Initialize some results that will be tested afterwards to avoid repeating computations */
		for(int i = 0 ; i < nbOfN ; ++i)
			for(int j = 0 ; j < nbOfK ; ++j)
				for(int k = 0 ; k < nbOfGap ; ++k)
					for(int l = 0 ; l < nbOfInstances ; ++l){
						array[i][j][k][0][l] = -Double.MAX_VALUE;
				}
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
			array = ComputeResults.unserialize(f.getAbsolutePath(), double[][][][][].class);
		}catch(Exception e){
			
			/* If it does not work (probably because the program was interrupted while the file was created)
			 * unserialize the bis file */
			array = ComputeResults.unserialize(f_bis.getAbsolutePath(), double[][][][][].class);
		}
	}
		
	return array;
}

}
