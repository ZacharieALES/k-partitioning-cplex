package main;


import java.io.File;
import java.util.ArrayList;

import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.Partition_x_y;
import formulation.Partition_x_y_2;
import formulation.RepParam;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import formulation.XYParam;
import ilog.concert.IloException;
import results.ComputeResults;
//Protocole :
//
//On utilise les formulations :
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
//On utilise cplex par défaut pendant 10 minutes
//Pour chaque couple (n,k, type de graphe), on relève :
//- la meilleure relaxation obtenue : x1, x2, x3, x4
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
//- d4 : formulation de 0 à 3 (0 <-> rep, 1 <-> xy1, 2 <-> xy2, 3 <-> tildes)
//- d5 : résultat de 0 à 3 (0 <-> relaxation, 1 <-> temps, 2 <-> taille de l'arbre, 3 <-> solution entiere)
//- d6 : instance de 0 à 10
//
//Les meilleures solutions entières seront stockées dans une table de dimension 3 (n, K, gap).

public class Execution_inoc_numero_special_v2_10_min_tmax extends Execution{

	double[][][][][][] resultats;
	double[][][][][][] resultats_old;


	//	TildeParam tp  = new TildeParam(null, -1, true, Triangle.USE_LAZY_IN_BC_ONLY, true, true, true);
	TildeParam tp  = new TildeParam(null, -1, true, Triangle.USE, true, true, true);
	RepParam rp = new RepParam(null, -1, Triangle.USE, true, true, true);
	XYParam xy1p = new XYParam(null, -1, false, false);
	XYParam xy2p = new XYParam(null, -1, true, false);

	String saveFile= "./results/expe_num_special_inoc_v2/resultats_general_clique_node_cluster.ser";
	String saveFile_old = "./results/expe_num_special_inoc_v2/resultats.ser";

	public Execution_inoc_numero_special_v2_10_min_tmax(int nm, int nM2, int km, int kM2,
			int im, int iM2) {
		super(nm, nM2, km, kM2, im, iM2);

		resultats_old = ComputeResults.unserialize(saveFile_old, double[][][][][][].class);
		resultats = ComputeResults.unserialize(saveFile, double[][][][][][].class);

		resultats = new double[9][4][3][4][4][10];

		//			Add if not bis
		//			/* Initialize some results that will be tested afterwards to avoid repeating computations */
		//			for(int i = 0 ; i < 9 ; ++i)
		//				for(int j = 0 ; j < 4 ; ++j)
		//					for(int k = 0 ; k < 3 ; ++k)
		//						for(int l = 0 ; l < 4 ; ++l)
		//							for(int m = 0 ; m < 10 ; m++)
		//								resultats[i][j][k][l][0][m] = -Double.MAX_VALUE;
	}

	@Override
	public void execution() throws IloException {

		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
		gapValues.add(-250.0);
		gapValues.add(-500.0);

		tp.tilim = 600;
		rp.tilim = 600;
		xy1p.tilim = 600;
		xy2p.tilim = 600;

		if(c_n % 10 == 0 && c_k % 2 == 0)
		{

			int id_n = c_n / 5 - 2;
			int id_k = c_k / 2 - 1;

			ComputeResults.log("(n,K): (" + c_n + "," + c_k + "," + c_i + ")");

			for(int gap = 0 ; gap < gapValues.size() ; ++gap){

				ComputeResults.log("gap: " + gap);

				tp.gapDiss = gapValues.get(gap);
				rp.gapDiss = gapValues.get(gap);
				xy1p.gapDiss = gapValues.get(gap);
				xy2p.gapDiss = gapValues.get(gap);

				/* Representative formulation */
				performFormulation(id_n, id_k, c_i, gap, 0, "rep");

				/* XY1 formulation */	
				performFormulation(id_n, id_k, c_i, gap, 1, "xy1");

				/* XY2 formulation */
				performFormulation(id_n, id_k, c_i, gap, 2, "xy2");

				/* Tilde formulation */
				performFormulation(id_n, id_k, c_i, gap, 3, "tilde");

			}
		}

	}

	public void performFormulation(int id_n, int id_k, int id_i, int gap, int formulation, String formulation_name){


		if(resultats[id_n][id_k][gap][formulation][0][id_i] == -Double.MAX_VALUE
				// TO REMOVE
				&& (formulation == 1 || formulation == 2) ){


			Partition p = null;
			switch(formulation){
			case 0 :
				p = ((PartitionWithRepresentative)createPartition(rp));
				break;
			case 1 :
				p = ((Partition_x_y)createPartition(xy1p));
				break;
			case 2 :
				p = ((Partition_x_y_2)createPartition(xy2p));
				break;
			case 3 :
				p = ((PartitionWithRepresentative)createPartition(tp));
				break;
			}

			resultats[id_n][id_k][gap][formulation][1][id_i] = p.solve();
			resultats[id_n][id_k][gap][formulation][2][id_i] = p.getNnodes();
			resultats[id_n][id_k][gap][formulation][0][id_i] = p.getBestObjValue2();
			resultats[id_n][id_k][gap][formulation][3][id_i] = p.getObjValue2();

			ComputeResults.serialize(resultats, saveFile);

			ComputeResults.log("\t" + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_n][id_k][gap][formulation][0][id_i])+ ", " + Math.round(resultats[id_n][id_k][gap][formulation][3][id_i]) + "] (" + Math.round(resultats[id_n][id_k][gap][formulation][2][id_i]) + " nodes, " + Math.round(resultats[id_n][id_k][gap][formulation][1][id_i]) + "s)");
			ComputeResults.log("\t" + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats_old[id_n][id_k][gap][formulation][0][id_i])+ ", " + Math.round(resultats_old[id_n][id_k][gap][formulation][3][id_i]) + "] (" + Math.round(resultats_old[id_n][id_k][gap][formulation][2][id_i]) + " nodes, " + Math.round(resultats_old[id_n][id_k][gap][formulation][1][id_i]) + "s)");

		}
		else{
			if(formulation == 1 || formulation == 2){
				System.out.println("\t." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_n][id_k][gap][formulation][0][id_i])+ ", " + Math.round(resultats[id_n][id_k][gap][formulation][3][id_i]) + "] (" + Math.round(resultats[id_n][id_k][gap][formulation][2][id_i]) + " nodes, " + Math.round(resultats[id_n][id_k][gap][formulation][1][id_i]) + "s)");
				System.out.println("\t." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats_old[id_n][id_k][gap][formulation][0][id_i])+ ", " + Math.round(resultats_old[id_n][id_k][gap][formulation][3][id_i]) + "] (" + Math.round(resultats_old[id_n][id_k][gap][formulation][2][id_i]) + " nodes, " + Math.round(resultats_old[id_n][id_k][gap][formulation][1][id_i]) + "s)");
			}
			else{
				System.out.println("." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats_old[id_n][id_k][gap][formulation][0][id_i])+ ", " + Math.round(resultats_old[id_n][id_k][gap][formulation][3][id_i]) + "] (" + Math.round(resultats_old[id_n][id_k][gap][formulation][2][id_i]) + " nodes, " + Math.round(resultats_old[id_n][id_k][gap][formulation][1][id_i]) + "s)");
			}
		}
	}


	String color1 = "blue";
	String color2 = "red";
	String color3 = "green";
	String color4 = "gray";

	double legende_x = 2;
	double legende_y = 5.4;
	double size_legende_x = 3;
	double size_legende_y = 2.6;


	public void printSchema(int minN, int maxN, double base, double height) {

		String content = "\\documentclass[landscape]{article}\n\n";

		String[] valueOfN = new String[(maxN/5)-(minN/5)+1];
		for(int i = 0 ; i < valueOfN.length  ; i++){
			valueOfN[i] = ((Integer)(i*5+10)).toString();
		}

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


		//		content += printSchemaTimeGap(minN, maxN, base, height, valueOfN, 0);
		//		content += printSchemaTimeGap(minN, maxN, base, height, valueOfN, 1);
		//		content += printSchemaTimeGap(minN, maxN, base, height, valueOfN, 2);


		//		content += displayTableNodes(minN, maxN, valueOfN, 0);
		//		content += displayTableNodes(minN, maxN, valueOfN, 1);
		//		content += displayTableNodes(minN, maxN, valueOfN, 2);


		content += displayTableTimeGapNodes(minN, maxN, 0);
		content += displayTableTimeGapNodesWithDifferentIntegerSolutions(minN, maxN, 0);
		content += displayTableTimeGapNodes(minN, maxN, 1);
		content += displayTableTimeGapNodesWithDifferentIntegerSolutions(minN, maxN, 1);
		content += displayTableTimeGapNodes(minN, maxN, 2);
		content += displayTableTimeGapNodesWithDifferentIntegerSolutions(minN, maxN, 2);

		content += "\\end{document}\n";


		ComputeResults.writeInFile("./schema.tex", content);

		System.out.println("done");

	}

	public void printTablesTimeGapNodes(int minN, int maxN) {

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

		content += displayTableTimeGapNodes(minN, maxN, 0);
		content += displayTableTimeGapNodes(minN, maxN, 1);
		content += displayTableTimeGapNodes(minN, maxN, 2);

		content += "\\end{document}\n";

		String saveFile = "./tables_time_gap_nodes.tex";
		File f = new File(saveFile);

		if(f.exists())
			f.delete();

		ComputeResults.writeInFile(saveFile, content);

		System.out.println("done");

	}


	public String printSchemaTimeGap(int minN, int maxN, double base, double height, String[] valueOfN, int data_set){


		double [][][][][] resultats = null;
		double [][][] bestInt = null;
		resultats = ComputeResults.unserialize("./results/expe_num_special_inoc/resultats.ser", double[][][][][].class);
		bestInt = ComputeResults.unserialize("./results/expe_num_special_inoc/bestInt.ser", double[][][].class);

		/* Get the maximal gap to set the scale */
		double maxGap = 0.0;
		for(int n = 0 ; n < valueOfN.length ; n++){
			for(int formulation = 0 ; formulation < 4 ; ++formulation){
				for(int k = 0 ; k <= 3 ; k++){

					double relax = resultats[n][k][data_set][formulation][0];
					double time = resultats[n][k][data_set][formulation][1];
					double bestI = bestInt[n][k][data_set];	
					if(time >= 3600 && bestI != -Double.MAX_VALUE) {
						double the_gap = ComputeResults.improvement(bestI, relax);

						if(the_gap > maxGap)
							maxGap = the_gap;
					}

				}
			}
		}

		String content = "";

		content += "\\begin{figure}\n";
		content += "\\begin{tikzpicture}\n\n";

		content += "% Axe horizontal\n";
		double sizeH = 2.5 * valueOfN.length + 0.5;
		content += "\\draw (0,0) -- (" + sizeH + ",0); \n";
		content += "\\draw (" + sizeH + ",-0.4) node{$K$}; \n";

		content += "\\node[label={[rotate=-90]$\\blacktriangle$}] at (" + (sizeH - 0.2) + ",-0.15) {};\n\n";

		content += "% Pointille horizontal separation temps/gap\n";
		content += "\\draw[dashed] (-1.7," + (height/2) + ") -- (" + sizeH  + "," + (height/2) + "); \n\n";

		content += "% Axe vertical\n";
		content += "\\draw (-0.4,4.75) node{$3600$};\n";
		content += "\\draw (-0.15,5.2) node{$0$};\n";

		content += "\\draw (0,0) -- (0,"  + height +"); \n";
		content += "\\draw(0.025," + height + ") node{$\\blacktriangle$};\n";

		content += "\\draw (-0.4,"+ (height/6) + ") node{$1200$};\n";
		content += "\\draw[loosely dotted] (0," + (height/6) + ") -- (" + sizeH + "," + (height/6) + "); \n";

		content += "\\draw (-0.4," + (height/3.10)+ ") node{$2400$};\n";
		content += "\\draw[loosely dotted] (0," + (height/3.10)+ ") -- (" + sizeH + "," + (height/3.10)+ "); \n";

		content += "\\draw (-0.4," + (2*height/3)+ ") node{$" + ((Double)(100*maxGap/3)).intValue() +"$};\n";
		content += "\\draw[loosely dotted] (0," + (2*height/3)+ ") -- (" + sizeH + "," + (2*height/3)+ "); \n";

		content += "\\draw (-0.4," + (height/1.21)+ ") node{$" + ((Double)(2*100*maxGap/3)).intValue() +"$};\n";
		content += "\\draw[loosely dotted] (0," + (height/1.21)+ ") -- (23," + (height/1.21)+ "); \n\n";

		content += "% Legende temps / gap\n";
		content += "\\node[label={[rotate=90]temps ($s$)}] at (-1," + (height/4.16)+ ") {};\n";
		content += "\\node[label={[rotate=90]gap (\\%)}] at (-1," + (3*height/4)+ ") {};\n";
		content += "\\draw (-0.25,0) node{$0$};\n\n";

		content += "% Legende formulations\n";
		content += "\\draw[black, fill=white] (" + legende_x + "," + legende_y + ") -- (" + (legende_x + size_legende_x) + "," + legende_y + ") -- (" + (legende_x + size_legende_x) + "," + (legende_y + size_legende_y) + ") -- (" + legende_x + "," + (legende_y + size_legende_y) + ") -- cycle;\n";
		content += "\\node at (" + (legende_x + base/0.625) + ", " + (legende_y + base/0.217) + "){\\textbf{L\\'egende}};\n";

		content += "\\node at (" + (legende_x + 1.9) + ", " + (legende_y + 1.8) + ") {repr\\'esentant};\n";
		content += drawTriangle(legende_x + base/2.5, legende_y + base/0.27, color1, base);
		content += drawTriangle(legende_x + base/0.71, legende_y + base/0.27, color1, base);
		content += "\\draw[color=" + color1 + "] (" + (legende_x + base/2.5) + "," + (legende_y + base/0.27) + ") -- (" + (legende_x + base/0.71) + "," + (legende_y + base/0.27) + ");\n";

		content += "\\node at (" + (legende_x + 1.3) + ", " + (legende_y + 1.3) + ") {tilde};\n";
		content += drawSquare(legende_x + 0.2, legende_y + 1.35, color2, base);
		content += drawSquare(legende_x + 0.7, legende_y + 1.35, color2, base);
		content += "\\draw[color=" + color2 + "] (" + (legende_x + 0.2) + "," + (legende_y + 1.35) + ") -- (" + (legende_x + 0.6) + "," + (legende_y + 1.35) + ");\n";

		content += "\\node at (" + (legende_x + 1.3) + ", " + (legende_y + 0.8) + ") {xy1};\n";
		content += drawCircle(legende_x + 0.2, legende_y + 0.85, color3, base);
		content += drawCircle(legende_x + 0.7, legende_y + 0.85, color3, base);
		content += "\\draw[color=" + color3 + "] (" + (legende_x + 0.2) + "," + (legende_y + 0.85) + ") -- (" + (legende_x + 0.7) + "," + (legende_y + 0.85) + ");\n";

		content += "\\node at (" + (legende_x + 1.3) + ", " + (legende_y + 0.3) + ") {xy2};\n";
		content += drawCross(legende_x + 0.2, legende_y + 0.35, color4, base);
		content += drawCross(legende_x + 0.7, legende_y + 0.35, color4, base);
		content += "\\draw[color=" + color4 + "] (" + (legende_x + 0.2) + "," + (legende_y + 0.35) + ") -- (" + (legende_x + 0.7) + "," + (legende_y + 0.35) + ");\n\n";


		content += "% Horizontal tick\n";
		double gap = 0.5;
		for(int i = 0 ; i < valueOfN.length ; ++i){
			content += "\\draw[loosely dotted] (" + (gap - 0.1) + ",-0.7) -- (" + (gap + 1.7) + ",-0.7); \n";
			content += "\\draw (" + (gap + 0.8) + ",-1) node{$n=" + valueOfN[i] + "$};\n";
			content += "\\draw (" + getXPosition(i, 0, base) + ",0) -- (" + getXPosition(i, 0, base) + ",0.2);\n";
			content += "\\draw (" + getXPosition(i, 0, base)+ ",-0.4) node{$2$};\n";
			content += "\\draw (" + getXPosition(i, 1, base) + ",0) -- (" + getXPosition(i, 1, base) + ",0.2);\n";
			content += "\\draw (" + getXPosition(i, 1, base) + ",-0.4) node{$4$};\n";
			content += "\\draw (" + getXPosition(i, 2, base) + ",0) -- (" + getXPosition(i, 2, base) + ",0.2);\n";
			content += "\\draw (" + getXPosition(i, 2, base) + ",-0.4) node{$6$};\n";
			content += "\\draw (" + getXPosition(i, 3, base) + ",0) -- (" + getXPosition(i, 3, base) + ",0.2);\n";
			content += "\\draw (" + getXPosition(i, 3, base) + ",-0.4) node{$8$};\n\n";
			gap += base * 5;
		}



		for(int n = 0 ; n < valueOfN.length ; n++){
			for(int formulation = 0 ; formulation < 4 ; ++formulation){

				double[] yPositions = new double[4];

				for(int k = 0 ; k <= 3 ; k++){

					double relax = resultats[n][k][data_set][formulation][0];
					double time = resultats[n][k][data_set][formulation][1];
					double bestI = bestInt[n][k][data_set];	

					//					System.out.println("relax / time / bestI : " + relax + " " + time + " " + bestI);

					if(time < 3600)
						if(time != 0.0){
							yPositions[k] = height/2 * time/3600;

							if(yPositions[k] < 1E-3)
								yPositions[k] = 0.01;
						}
						else
							yPositions[k] = height;

					else{
						double the_gap;
						if(bestI == -Double.MAX_VALUE){
							the_gap = height;
						}
						else
							the_gap = ComputeResults.improvement(bestI, relax);


						yPositions[k] = height/2 + height/2 * the_gap/maxGap;
						//						System.out.println("gap : " + the_gap);
					}

					//					System.out.println("pos : " + yPositions[k]);

				}

				String c_color = "";
				switch(formulation){
				case 0 : 
					c_color = color1;
					content += drawTriangle(getXPosition(n, 0, base), yPositions[0], c_color, base);
					content += drawTriangle(getXPosition(n, 1, base), yPositions[1], c_color, base);
					content += drawTriangle(getXPosition(n, 2, base), yPositions[2], c_color, base);
					content += drawTriangle(getXPosition(n, 3, base), yPositions[3], c_color, base);		
					break;

				case 1 : 
					c_color = color3;
					content += drawCircle(getXPosition(n, 0, base), yPositions[0], c_color, base);
					content += drawCircle(getXPosition(n, 1, base), yPositions[1], c_color, base);
					content += drawCircle(getXPosition(n, 2, base), yPositions[2], c_color, base);
					content += drawCircle(getXPosition(n, 3, base), yPositions[3], c_color, base);
					break;
				case 2 : 
					c_color = color4;
					content += drawCross(getXPosition(n, 0, base), yPositions[0], c_color, base);
					content += drawCross(getXPosition(n, 1, base), yPositions[1], c_color, base);
					content += drawCross(getXPosition(n, 2, base), yPositions[2], c_color, base);
					content += drawCross(getXPosition(n, 3, base), yPositions[3], c_color, base);
					break;
				case 3 : 
					c_color = color2;
					content += drawSquare(getXPosition(n, 0, base), yPositions[0], c_color, base);
					content += drawSquare(getXPosition(n, 1, base), yPositions[1], c_color, base);
					content += drawSquare(getXPosition(n, 2, base), yPositions[2], c_color, base);
					content += drawSquare(getXPosition(n, 3, base), yPositions[3], c_color, base);
					break;
				}

				/* Draw the lines between the dots */
				content += "\\draw [color = " + c_color + "] (" + getXPosition(n, 0, base) + "," + yPositions[0] + ") -- (" + getXPosition(n, 1, base) + "," + yPositions[1] + "); \n";
				content += "\\draw [color = " + c_color + "] (" + getXPosition(n, 1, base) + "," + yPositions[1] + ") -- (" + getXPosition(n, 2, base) + "," + yPositions[2] + "); \n";
				content += "\\draw [color = " + c_color + "] (" + getXPosition(n, 2, base) + "," + yPositions[2] + ") -- (" + getXPosition(n, 3, base) + "," + yPositions[3] + "); \n";


			}
		}

		content += "\\end{tikzpicture}\n\n";
		content += "\\caption{Comparaison  des  performances  de quatre  formulations  de $K$-partitionnement sur les instances de  $D_" + (data_set + 1) + "$.}\n";
		content += "\n\\end{figure}";

		content += "\\clearpage";
		return content;

	}



	public String printSchemaNode(int minN, int maxN, double base, double height, String[] valueOfN, int data_set){


		double [][][][][] resultats = null;
		resultats = ComputeResults.unserialize("./results/expe_num_special_inoc/resultats.ser", double[][][][][].class);

		/* Get the maximal number of nodes to set the scale */
		int maxNode = 0;
		for(int n = 0 ; n < valueOfN.length ; n++){
			for(int formulation = 0 ; formulation < 4 ; ++formulation){
				for(int k = 0 ; k <= 3 ; k++){

					double nodeNb = resultats[n][k][data_set][formulation][2];
					if(nodeNb > maxNode)
						maxNode = ((Double)nodeNb).intValue();

				}
			}
		}

		System.out.println(maxNode);

		String content = "";

		content += "\\begin{figure}\n";
		content += "\\begin{tikzpicture}\n\n";

		content += "% Axe horizontal\n";
		double sizeH = 2.5 * valueOfN.length + 0.5;
		content += "\\draw (0,0) -- (" + sizeH + ",0); \n";
		content += "\\draw (" + sizeH + ",-0.4) node{$K$}; \n";

		content += "\\node[label={[rotate=-90]$\\blacktriangle$}] at (" + (sizeH - 0.2) + ",-0.15) {};\n\n";

		for(int i = 0 ; i < 5 ; ++i){

			double mult = (i+1) /6.0;
			double h = mult * height;

			content += "\\draw (-0.4,"+ h + ") node{$" + ((Double)(maxNode* mult)).intValue() + "$};\n";
			content += "\\draw[loosely dotted] (0," + h + ") -- (" + sizeH + "," + h + "); \n";

		}

		content += "% Axe vertical\n";
		content += "\\draw (-0.4,," + height + ") node{$" + maxNode + "$};\n";

		content += "\\draw (0,0) -- (0,"  + height +"); \n";
		content += "\\draw(0.025," + height + ") node{$\\blacktriangle$};\n";


		content += "% Legende temps / gap\n";
		content += "\\node[label={[rotate=90]node}] at (-1," + (height/2)+ ") {};\n";
		content += "\\draw (-0.25,0) node{$0$};\n\n";

		content += "% Legende formulations\n";
		content += "\\draw[black, fill=white] (" + legende_x + "," + legende_y + ") -- (" + (legende_x + size_legende_x) + "," + legende_y + ") -- (" + (legende_x + size_legende_x) + "," + (legende_y + size_legende_y) + ") -- (" + legende_x + "," + (legende_y + size_legende_y) + ") -- cycle;\n";
		content += "\\node at (" + (legende_x + base/0.625) + ", " + (legende_y + base/0.217) + "){\\textbf{L\\'egende}};\n";

		content += "\\node at (" + (legende_x + 1.9) + ", " + (legende_y + 1.8) + ") {repr\\'esentant};\n";
		content += drawTriangle(legende_x + base/2.5, legende_y + base/0.27, color1, base);
		content += drawTriangle(legende_x + base/0.71, legende_y + base/0.27, color1, base);
		content += "\\draw[color=" + color1 + "] (" + (legende_x + base/2.5) + "," + (legende_y + base/0.27) + ") -- (" + (legende_x + base/0.71) + "," + (legende_y + base/0.27) + ");\n";

		content += "\\node at (" + (legende_x + 1.3) + ", " + (legende_y + 1.3) + ") {tilde};\n";
		content += drawSquare(legende_x + 0.2, legende_y + 1.35, color2, base);
		content += drawSquare(legende_x + 0.7, legende_y + 1.35, color2, base);
		content += "\\draw[color=" + color2 + "] (" + (legende_x + 0.2) + "," + (legende_y + 1.35) + ") -- (" + (legende_x + 0.6) + "," + (legende_y + 1.35) + ");\n";

		content += "\\node at (" + (legende_x + 1.3) + ", " + (legende_y + 0.8) + ") {xy1};\n";
		content += drawCircle(legende_x + 0.2, legende_y + 0.85, color3, base);
		content += drawCircle(legende_x + 0.7, legende_y + 0.85, color3, base);
		content += "\\draw[color=" + color3 + "] (" + (legende_x + 0.2) + "," + (legende_y + 0.85) + ") -- (" + (legende_x + 0.7) + "," + (legende_y + 0.85) + ");\n";
		content += "\\node at (" + (legende_x + 1.3) + ", " + (legende_y + 0.3) + ") {xy2};\n";
		content += drawCross(legende_x + 0.2, legende_y + 0.35, color4, base);
		content += drawCross(legende_x + 0.7, legende_y + 0.35, color4, base);
		content += "\\draw[color=" + color4 + "] (" + (legende_x + 0.2) + "," + (legende_y + 0.35) + ") -- (" + (legende_x + 0.7) + "," + (legende_y + 0.35) + ");\n\n";


		content += "% Horizontal tick\n";
		double gap = 0.5;
		for(int i = 0 ; i < valueOfN.length ; ++i){
			content += "\\draw[loosely dotted] (" + (gap - 0.1) + ",-0.7) -- (" + (gap + 1.7) + ",-0.7); \n";
			content += "\\draw (" + (gap + 0.8) + ",-1) node{$n=" + valueOfN[i] + "$};\n";
			content += "\\draw (" + getXPosition(i, 0, base) + ",0) -- (" + getXPosition(i, 0, base) + ",0.2);\n";
			content += "\\draw (" + getXPosition(i, 0, base)+ ",-0.4) node{$2$};\n";
			content += "\\draw (" + getXPosition(i, 1, base) + ",0) -- (" + getXPosition(i, 1, base) + ",0.2);\n";
			content += "\\draw (" + getXPosition(i, 1, base) + ",-0.4) node{$4$};\n";
			content += "\\draw (" + getXPosition(i, 2, base) + ",0) -- (" + getXPosition(i, 2, base) + ",0.2);\n";
			content += "\\draw (" + getXPosition(i, 2, base) + ",-0.4) node{$6$};\n";
			content += "\\draw (" + getXPosition(i, 3, base) + ",0) -- (" + getXPosition(i, 3, base) + ",0.2);\n";
			content += "\\draw (" + getXPosition(i, 3, base) + ",-0.4) node{$8$};\n\n";
			gap += base * 5;
		}



		for(int n = 0 ; n < valueOfN.length ; n++){
			for(int formulation = 0 ; formulation < 4 ; ++formulation){

				double[] yPositions = new double[4];

				for(int k = 0 ; k <= 3 ; k++){
					yPositions[k] = resultats[n][k][data_set][formulation][2] / maxNode * height;

					if(yPositions[k] < 1)
						yPositions[k] = 0;
				}

				//					System.out.println("pos : " + yPositions[k]);

				String c_color = "";
				switch(formulation){
				case 0 : 
					c_color = color1;
					content += drawTriangle(getXPosition(n, 0, base), yPositions[0], c_color, base);
					content += drawTriangle(getXPosition(n, 1, base), yPositions[1], c_color, base);
					content += drawTriangle(getXPosition(n, 2, base), yPositions[2], c_color, base);
					content += drawTriangle(getXPosition(n, 3, base), yPositions[3], c_color, base);		
					break;

				case 1 : 
					c_color = color3;
					content += drawCircle(getXPosition(n, 0, base), yPositions[0], c_color, base);
					content += drawCircle(getXPosition(n, 1, base), yPositions[1], c_color, base);
					content += drawCircle(getXPosition(n, 2, base), yPositions[2], c_color, base);
					content += drawCircle(getXPosition(n, 3, base), yPositions[3], c_color, base);
					break;
				case 2 : 
					c_color = color4;
					content += drawCross(getXPosition(n, 0, base), yPositions[0], c_color, base);
					content += drawCross(getXPosition(n, 1, base), yPositions[1], c_color, base);
					content += drawCross(getXPosition(n, 2, base), yPositions[2], c_color, base);
					content += drawCross(getXPosition(n, 3, base), yPositions[3], c_color, base);
					break;
				case 3 : 
					c_color = color2;
					content += drawSquare(getXPosition(n, 0, base), yPositions[0], c_color, base);
					content += drawSquare(getXPosition(n, 1, base), yPositions[1], c_color, base);
					content += drawSquare(getXPosition(n, 2, base), yPositions[2], c_color, base);
					content += drawSquare(getXPosition(n, 3, base), yPositions[3], c_color, base);
					break;
				}

				/* Draw the lines between the dots */
				content += "\\draw [color = " + c_color + "] (" + getXPosition(n, 0, base) + "," + yPositions[0] + ") -- (" + getXPosition(n, 1, base) + "," + yPositions[1] + "); \n";
				content += "\\draw [color = " + c_color + "] (" + getXPosition(n, 1, base) + "," + yPositions[1] + ") -- (" + getXPosition(n, 2, base) + "," + yPositions[2] + "); \n";
				content += "\\draw [color = " + c_color + "] (" + getXPosition(n, 2, base) + "," + yPositions[2] + ") -- (" + getXPosition(n, 3, base) + "," + yPositions[3] + "); \n";


			}
		}

		content += "\\end{tikzpicture}\n\n";
		content += "\\caption{Comparaison  du nombre de sommets utililsés par quatre  formulations  de $K$-partitionnement sur les instances de  $D_" + (data_set + 1) + "$.}\n";
		content += "\n\\end{figure}";

		content += "\\clearpage";
		return content;

	}


	public String displayTableNodes(int minN, int maxN, String[] valueOfN, int data_set){

		String content = "";

		double [][][][][] resultats = null;
		resultats = ComputeResults.unserialize("./results/expe_num_special_inoc_v2/resultats.ser", double[][][][][].class);

		double [][][][][] resultats_new_xy = null;
		resultats_new_xy = ComputeResults.unserialize("./results/expe_num_special_inoc_v2/resultats_general_clique_node_cluster.ser", double[][][][][].class);


		String pre_table = "\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{M{0.5cm}*{6}{r@{\\hspace{0.5cm}}}r@{}}\\toprule\\multirow{2}{*}{\\textbf{n}} & \\multirow{2}{*}{\\textbf{Formulation}} & \\multicolumn{4}{c}{\\textbf{K}} \\\\& & \\textbf{2} &\\textbf{4} &\\textbf{6} &\\textbf{8} \\tabularnewline\\hline";
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}";

		int nbFormulations = 4;

		content += pre_table;
		for(int n = minN ; n <= maxN ; n+=5){

			content += "\\multirow{" + nbFormulations + "}{*}{\\textbf{" + n + "}} \t";

			content += "& $(F_{xy1})$\t";
			for(int k = 0 ; k <= 3 ; ++k)
				content += "&\t" + Math.round(resultats_new_xy[n/5-2][k][data_set][1][2]);
			content += "\\\\";

			content += "& $(F_{xy2})$\t";
			for(int k = 0 ; k <= 3 ; ++k)
				content += "&\t" + Math.round(resultats_new_xy[n/5-2][k][data_set][2][2]);
			content += "\\\\\n ";

			content += "& $(F_{rep})$\t";
			for(int k = 0 ; k <= 3 ; ++k)
				content += "&\t" + Math.round(resultats[n/5-2][k][data_set][0][2]);
			content += "\\\\";

			content += "& $(F_{tilde})$\t";
			for(int k = 0 ; k <= 3 ; ++k)
				content += "&\t" + Math.round(resultats[n/5-2][k][data_set][3][2]);
			content += "\\\\\n ";

			if(n == maxN)
				content += "\n";
			else
				content += "\\hline";
		}

		content += suf_table1;
		content += "Nombre de sommets utililsés par quatre  formulations  de $K$-partitionnement sur les instances de  $D_" + (data_set + 1) + "$.\n";
		content += suf_table2;

		return content;

	}

	public String displayTableTimeGapNodes(int minN, int maxN, int data_set){


		double [][][][][][] resultats = null;
		double [][][][][][] resultats_xy = null;
		double [][][][][] resultatsCP = null;
		double [][][][][] resultatsCP2 = null;
		double [][][] bestInt = new double[9][4][10];


		String saveFileAllFastSeparationHeuristicFolder = "./results/expe_num_special_inoc_v2/";
		String saveFileAllFastSeparationHeuristicFile = "resultats_several_fast_separation_in_bc.ser";

		resultatsCP = ComputeResults.unserialize("./results/expe_num_special_inoc_v2/resultats_cp.ser", double[][][][][].class);
		resultatsCP2 = ComputeResults.unserialize(saveFileAllFastSeparationHeuristicFolder + saveFileAllFastSeparationHeuristicFile, double[][][][][].class);
		resultats = ComputeResults.unserialize("./results/expe_num_special_inoc_v2/resultats.ser", double[][][][][][].class);
		resultats_xy = ComputeResults.unserialize("./results/expe_num_special_inoc_v2/resultats_general_clique_node_cluster.ser", double[][][][][][].class);


		int instanceMax = 9;

		int nbFormulations = 5;
		int []order_formulations = new int[nbFormulations];
		order_formulations[0] = 1;
		order_formulations[1] = 2;
		order_formulations[2] = 0;
		order_formulations[3] = 3;

		/* Find for each of the 5 configurations (i.e., the 4 formulations and the CP) the best feasible solution */
		for(int n = 0 ; n < 9 ; ++n)
			for(int K = 0 ; K < 4 ; ++K)
				for(int i = 0 ; i < instanceMax ; ++i){
					bestInt[n][K][i] = Double.MAX_VALUE;

					for(int f = 0 ; f < 4 ; ++f){
						double c_int;

						/* If it is a xy formulation */
						if(f == 1 || f == 2)
							c_int = resultats_xy[n][K][data_set][f][3][i];
						else
							c_int = resultats[n][K][data_set][f][3][i];

						if(c_int != -1.0 && c_int < bestInt[n][K][i])
							bestInt[n][K][i] = c_int;
					}

					if(resultatsCP2[n][K][data_set][3][i] != -1.0 && resultatsCP2[n][K][data_set][3][i] < bestInt[n][K][i])
						bestInt[n][K][i] = resultatsCP2[n][K][data_set][3][i];

				}

		String content = "\\begin{center}\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{*{2}{M{0.5cm}}*{5}{r@{\\hspace{0.4cm}}r}@{\\hspace{0.5cm}}*{5}{r@{\\hspace{0.5cm}}}}\\toprule\\multirow{2}{*}{\\textbf{n}} & \\multirow{2}{*}{\\textbf{K}}&\\multicolumn{10}{c}{\\textbf{Time (s) and Gap (\\%)}} & \\multicolumn{5}{c}{\\textbf{Nodes}} \\\\& & \\multicolumn{2}{c}{$(F_{nc1})$} & \\multicolumn{2}{c}{$(F_{nc2})$} & \\multicolumn{2}{c}{$(F_{er})$} & \\multicolumn{2}{c}{$(F_{ext})$} & \\multicolumn{2}{c}{$(BC)$} & $(F_{nc1})$ & $(F_{nc2})$ & $(F_{er})$ & $(F_{ext})$ & $(BC)$\\tabularnewline\\hline";
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}\\end{center}";


		for(int n = minN ; n <= maxN ; n+=10){

			int id_n = Math.round((n - minN)/10*2)+2;

			content += "\\multirow{" + nbFormulations + "}{*}{\\textbf{" + n + "}} \t";

			for(int k = 0 ; k <= 3 ; ++k){
				content += "& \\textbf{" + (2*k+2) + "} \t";

				/* Add the time/gap for the 4 formulations */
				for(int formulation = 0 ; formulation < 4 ; ++formulation){

					double meanTime = 0.0;
					double meanGap = 0.0;

					for(int i = 0 ; i < instanceMax ; ++i){

						double time;
						double relax;
						double bestI;

						if(order_formulations[formulation] == 1 || order_formulations[formulation] == 2){
							time = resultats_xy[id_n][k][data_set][order_formulations[formulation]][1][i];
							relax = resultats_xy[id_n][k][data_set][order_formulations[formulation]][0][i];
						}
						else{
							time = resultats[id_n][k][data_set][order_formulations[formulation]][1][i];
							relax = resultats[id_n][k][data_set][order_formulations[formulation]][0][i];
						}

						bestI = bestInt[id_n][k][i];	
						meanTime += time;

						/* If there is a gap */
						if(time >= 600)
							meanGap += ComputeResults.improvement(bestI, relax);
					}

					meanTime /= ((Integer)instanceMax).doubleValue();
					meanGap /= ((Integer)instanceMax).doubleValue();
					meanGap *= 100.0;

					content += "& " + Math.round(meanTime) + "s & " + Math.round(meanGap) + "\\%";
				}

				/* Add the time/gap for the CP */
				double meanTime = 0.0;
				double meanTime_old = 0.0;
				double meanGap = 0.0;
				double meanGap_old = 0.0;

				for(int i = 0 ; i < instanceMax ; ++i){

					double time = resultatsCP2[id_n][k][data_set][1][i];
					double time_old = resultatsCP[id_n][k][data_set][1][i];
					double relax = resultatsCP2[id_n][k][data_set][0][i];
					double relax_old = resultatsCP[id_n][k][data_set][0][i];
					double bestI = bestInt[id_n][k][i];	

					meanTime += time;
					meanTime_old += time_old;

					/* If there is a gap */
					if(time >= 600)
						meanGap += ComputeResults.improvement(bestI, relax);

					if(time_old >= 600)
						meanGap_old += ComputeResults.improvement(bestI, relax_old);

				}

				meanTime /= ((Integer)instanceMax).doubleValue();
				meanGap /= ((Integer)instanceMax).doubleValue();
				meanTime_old /= ((Integer)instanceMax).doubleValue();
				meanGap_old /= ((Integer)instanceMax).doubleValue();
				meanGap *= 100.0;
				meanGap_old *= 100.0;

				content += "& " + Math.round(meanTime) + "s & " + Math.round(meanGap) + "\\%";

				//				System.out.println("new: " + Math.round(meanTime) + "s " + Math.round(meanGap) + "%");
				//				System.out.println("old: " + Math.round(meanTime_old) + "s " + Math.round(meanGap_old) + "%");

				/* Add the nodes for the 4 formulations */
				for(int formulation = 0 ; formulation < 4 ; ++formulation){

					double meanNodes = 0;

					for(int i = 0 ; i < instanceMax ; ++i)
						meanNodes += resultats[id_n][k][data_set][order_formulations[formulation]][2][i];

					meanNodes /= instanceMax;
					content += "& " + Math.round(meanNodes);
				}

				/* Add the nodes for the CP */
				double meanNodes = 0;

				for(int i = 0 ; i < instanceMax ; ++i)
					meanNodes += resultatsCP[id_n][k][data_set][2][i];	


				meanNodes /= instanceMax;
				content += "& " + Math.round(meanNodes);

				content += "\\\\\n";
			}

			if(n == maxN)
				content += "\n";
			else
				content += "\\hline";
		}

		content += suf_table1;
		content += "Mean results (in terms of time, gap and number of nodes in the branch-and-cut tree) obtained for each of the five configurations over $10$ instances of $D_" + (data_set + 1) + "$. Configuration $(BC)$ corresponds to the branch-and-cut algorithm presented section~\\ref{sec:branch}.\n";
		content += suf_table2;

		return content;

	}


	public String displayTableTimeGapNodesWithDifferentIntegerSolutions(int minN, int maxN, int data_set){


		double [][][][][][] resultats = null;
		double [][][][][][] resultats_xy = null;
		double [][][][][] resultatsCP = null;
		double [][][][][] resultatsCP2 = null;

		double [][][] worstInt = new double[9][4][10];

		String saveFileAllFastSeparationHeuristicFolder = "./results/expe_num_special_inoc_v2/";
		String saveFileAllFastSeparationHeuristicFile = "resultats_several_fast_separation_in_bc.ser";

		resultatsCP = ComputeResults.unserialize("./results/expe_num_special_inoc_v2/resultats_cp.ser", double[][][][][].class);
		resultatsCP2 = ComputeResults.unserialize(saveFileAllFastSeparationHeuristicFolder + saveFileAllFastSeparationHeuristicFile, double[][][][][].class);
		resultats = ComputeResults.unserialize("./results/expe_num_special_inoc_v2/resultats.ser", double[][][][][][].class);
		resultats_xy = ComputeResults.unserialize("./results/expe_num_special_inoc_v2/resultats_general_clique_node_cluster.ser", double[][][][][][].class);


		int instanceMax = 9;

		int nbFormulations = 5;
		int []order_formulations = new int[nbFormulations];
		order_formulations[0] = 1;
		order_formulations[1] = 2;
		order_formulations[2] = 0;
		order_formulations[3] = 3;

		String content = "\\begin{center}\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{*{2}{M{0.5cm}}*{5}{r@{\\hspace{0.4cm}}r}@{\\hspace{0.5cm}}*{5}{r@{\\hspace{0.5cm}}}}\\toprule\\multirow{2}{*}{\\textbf{n}} & \\multirow{2}{*}{\\textbf{K}}&\\multicolumn{10}{c}{\\textbf{Time (s) and Gap (\\%)}} & \\multicolumn{5}{c}{\\textbf{Nodes}} \\\\& & \\multicolumn{2}{c}{$(F_{nc1})$} & \\multicolumn{2}{c}{$(F_{nc2})$} & \\multicolumn{2}{c}{$(F_{er})$} & \\multicolumn{2}{c}{$(F_{ext})$} & \\multicolumn{2}{c}{$(BC)$} & $(F_{nc1})$ & $(F_{nc2})$ & $(F_{er})$ & $(F_{ext})$ & $(BC)$\\tabularnewline\\hline";
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}\\end{center}";




		/* Find for each of the 5 configurations (i.e., the 4 formulations and the CP) the best feasible solution */
		for(int n = 0 ; n < 9 ; ++n)
			for(int K = 0 ; K < 4 ; ++K){
				for(int i = 0 ; i < instanceMax ; ++i){
					worstInt[n][K][i] = -Double.MAX_VALUE;

					for(int f = 0 ; f < 4 ; ++f){
						double c_int;

						/* If it is a xy formulation */
						if(f == 1 || f == 2)
							c_int = resultats_xy[n][K][data_set][f][3][i];
						else
							c_int = resultats[n][K][data_set][f][3][i];

						if(c_int != -1.0 && c_int > worstInt[n][K][i])
							worstInt[n][K][i] = c_int;
					}

					if(resultatsCP2[n][K][data_set][3][i] != -1.0 && resultatsCP2[n][K][data_set][3][i] > worstInt[n][K][i])
						worstInt[n][K][i] = resultatsCP2[n][K][data_set][3][i];

					if(n >= 2 && n%2 == 0){
						if(worstInt[n][K][i] == -Double.MAX_VALUE || worstInt[n][K][i] == -1.0){
							System.err.println("!!!!!!!!!!!!!!!No integer solution found");
							System.out.println("n/k/gap: " + n + "/"  + K + "/" + data_set + " worst: " + worstInt[n][K][i]);
						}

					}
				}
			}

		for(int n = minN ; n <= maxN ; n+=10){

			int id_n = Math.round((n - minN)/10*2)+2;

			content += "\\multirow{" + nbFormulations + "}{*}{\\textbf{" + n + "}} \t";

			for(int k = 0 ; k <= 3 ; ++k){
				content += "& \\textbf{" + (2*k+2) + "} \t";

				/* Add the time/gap for the 4 formulations */
				for(int formulation = 0 ; formulation < 4 ; ++formulation){

					double meanTime = 0.0;
					double meanGap = 0.0;

					for(int i = 0 ; i < instanceMax ; ++i){

						double time;
						double relax;
						double bestI;

						if(order_formulations[formulation] == 1 || order_formulations[formulation] == 2){
							time = resultats_xy[id_n][k][data_set][order_formulations[formulation]][1][i];
							relax = resultats_xy[id_n][k][data_set][order_formulations[formulation]][0][i];
							bestI = resultats_xy[id_n][k][data_set][order_formulations[formulation]][3][i];
						}
						else{
							time = resultats[id_n][k][data_set][order_formulations[formulation]][1][i];
							relax = resultats[id_n][k][data_set][order_formulations[formulation]][0][i];
							bestI = resultats[id_n][k][data_set][order_formulations[formulation]][3][i];
						}

						meanTime += time;

						if(bestI == -1.0 || bestI == -Double.MAX_VALUE){
							bestI = worstInt[id_n][k][i];
							System.err.println("Integer solution invalid, formulation: " + formulation + " n/k/gap:" + n + "/" + k + "/" + data_set);
						}

						/* If there is a gap */
						if(time >= 600)
							meanGap += ComputeResults.improvement(bestI, relax);
					}

					meanTime /= ((Integer)instanceMax).doubleValue();
					meanGap /= ((Integer)instanceMax).doubleValue();
					meanGap *= 100.0;

					content += "& " + Math.round(meanTime) + "s & " + Math.round(meanGap) + "\\%";
				}

				/* Add the time/gap for the CP */
				double meanTime = 0.0;
				double meanTime_old = 0.0;
				double meanGap = 0.0;
				double meanGap_old = 0.0;

				for(int i = 0 ; i < instanceMax ; ++i){

					double time = resultatsCP2[id_n][k][data_set][1][i];
					double time_old = resultatsCP[id_n][k][data_set][1][i];
					double relax = resultatsCP2[id_n][k][data_set][0][i];
					double relax_old = resultatsCP[id_n][k][data_set][0][i];
					double bestI = resultatsCP[id_n][k][data_set][3][i];	

					if(bestI == -1.0 || bestI == -Double.MAX_VALUE){
						bestI = worstInt[id_n][k][i];
						System.err.println("2Integer solution invalid, formulation: cp n/k/gap:" + n + "/" + k + "/" + data_set);
					}

					meanTime += time;
					meanTime_old += time_old;

					/* If there is a gap */
					if(time >= 600)
						meanGap += ComputeResults.improvement(bestI, relax);

					if(time_old >= 600)
						meanGap_old += ComputeResults.improvement(bestI, relax_old);

				}

				meanTime /= ((Integer)instanceMax).doubleValue();
				meanGap /= ((Integer)instanceMax).doubleValue();
				meanTime_old /= ((Integer)instanceMax).doubleValue();
				meanGap_old /= ((Integer)instanceMax).doubleValue();
				meanGap *= 100.0;
				meanGap_old *= 100.0;

				content += "& " + Math.round(meanTime) + "s & " + Math.round(meanGap) + "\\%";

				//				System.out.println("new: " + Math.round(meanTime) + "s " + Math.round(meanGap) + "%");
				//				System.out.println("old: " + Math.round(meanTime_old) + "s " + Math.round(meanGap_old) + "%");

				/* Add the nodes for the 4 formulations */
				for(int formulation = 0 ; formulation < 4 ; ++formulation){

					double meanNodes = 0;

					for(int i = 0 ; i < instanceMax ; ++i)
						meanNodes += resultats[id_n][k][data_set][order_formulations[formulation]][2][i];

					meanNodes /= instanceMax;
					content += "& " + Math.round(meanNodes);
				}

				/* Add the nodes for the CP */
				double meanNodes = 0;

				for(int i = 0 ; i < instanceMax ; ++i)
					meanNodes += resultatsCP[id_n][k][data_set][2][i];	


				meanNodes /= instanceMax;
				content += "& " + Math.round(meanNodes);

				content += "\\\\\n";
			}

			if(n == maxN)
				content += "\n";
			else
				content += "\\hline";
		}

		content += suf_table1;
		content += "Mean results (in terms of time, gap and number of nodes in the branch-and-cut tree) obtained for each of the five configurations over $10$ instances of $D_" + (data_set + 1) + "$. Configuration $(BC)$ corresponds to the branch-and-cut algorithm presented section~\\ref{sec:branch}.\n";
		content += suf_table2;

		return content;

	}


	private double getXPosition(int n, int k, double base) {		
		return (5 * n + k+1) * base; 
	}


	private String drawTriangle(double centerX, double centerY, String color, double base){
		double i = base/5;
		return "\\fill[color=" + color + "] (" +  (centerX-i) + "," + (centerY-i) + ") -- (" + (centerX+i) + "," + (centerY-i) + ") -- (" + centerX + "," + (centerY+i) + ") -- cycle;\n";
	}

	private String drawSquare(double centerX, double centerY, String color, double base){
		double i = base/5;
		return "\\fill[color=" + color + "] (" + (centerX - i) + "," + (centerY-i) + ") -- (" + (centerX + i) + "," + (centerY-i) + ") -- (" + (centerX + i) + "," + (centerY + i) + ") -- (" + (centerX - i) + "," + (centerY + i) + ") -- cycle;\n";
	}

	private String drawCircle(double centerX, double centerY, String color, double base){
		double i = base/4.55;
		return "\\fill[color=" + color + "] (" + centerX + "," + centerY + ") circle(" + i + ");\n";
	}

	private String drawCross(double centerX, double centerY, String color, double base){
		double i = base/5;
		double j = base/0.417;
		String content = "\\draw[line width=" + j + "pt,color=" + color + "] (" + (centerX - i) + "," + (centerY - i) + ") -- (" + (centerX + i) + "," + (centerY + i) + ");\n";
		return content + "\\draw[line width=" + j + "pt,color=" + color + "] (" + (centerX - i) + "," + (centerY + i) + ") -- (" + (centerX + i) + "," + (centerY - i) + ");\n";

	}


	public static void main(String[] args){

		Execution_inoc_numero_special_v2_10_min_tmax expe = new Execution_inoc_numero_special_v2_10_min_tmax(0, 0, 0, 0, 0, 0);
		expe.printSchema(20, 50, 0, 0);
	}
}
