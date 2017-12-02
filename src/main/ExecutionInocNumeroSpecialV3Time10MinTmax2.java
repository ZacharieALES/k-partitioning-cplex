package main;


import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import callback.cut_callback.FastCutCallback;
import callback.heuristic_callback.KClosestRepresentatives;
import callback.heuristic_callback.KClosestRepresentativesTildes;
import callback.heuristic_callback.KClosestRepresentativesXY;
import callback.heuristic_callback.KClosestRepresentativesXY2;
import cplex.Cplex;
import cutting_plane.CP_Rep;
import formulation.Partition;
import formulation.PartitionParam;
import formulation.PartitionWithRepresentative;
import formulation.PartitionXY;
import formulation.PartitionXY2;
import formulation.RepParam;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import formulation.XYParam;
import ilog.concert.IloException;
import results.ComputeResults;
import results.StandardExperimentResults;
import results.StandardResult;
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
import results.StandardResult.FormulationType;

public class ExecutionInocNumeroSpecialV3Time10MinTmax2 extends Execution{

	public StandardExperimentResults<StandardResult> ser;

	//	TildeParam tp  = new TildeParam(null, cplex, -1, true, Triangle.USE_LAZY_IN_BC_ONLY, true, true, true);
	TildeParam tpBc;
	TildeParam tp;
	RepParam rp;
	XYParam xy1p;
	XYParam xy2p;

	String saveFilePath= "./results/expe_num_special_inoc_v3/resultats_dell_ensta.ser";
	int tilim;
	ArrayList<Double> gapValues;

	public ExecutionInocNumeroSpecialV3Time10MinTmax2(Cplex cplex, int nm, int nM2, int km, int kM2,
			int im, int iM2, int tilim) {
		super(cplex, nm, nM2, km, kM2, im, iM2);

		tpBc  = new TildeParam(null, cplex, c_k, true, Triangle.USE_IN_BC_ONLY, true, false, false);
		tp  = new TildeParam(null, cplex, c_k, true, Triangle.USE, true, true, true);
		rp = new RepParam(null, cplex, c_k, Triangle.USE, true, true, true);
		xy1p = new XYParam(null, cplex, c_k, false, false);
		xy2p = new XYParam(null, cplex, c_k, true, false);

		this.tilim = tilim;
		File saveFile = new File(saveFilePath);

		if(saveFile.exists())
			ser = new StandardExperimentResults<StandardResult>(saveFilePath);
		else
			ser = new StandardExperimentResults<StandardResult>();

		tp.tilim = tilim;
		rp.tilim = tilim;
		xy1p.tilim = tilim;
		xy2p.tilim = tilim;
		tpBc.tilim = tilim;

		gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
		gapValues.add(-250.0);
		gapValues.add(-500.0);

	}

	@Override
	public void execution() throws IloException {

		updateParam(tp);
		updateParam(tpBc);
		updateParam(rp);
		updateParam(xy1p);
		updateParam(xy2p);

		if(c_n % 10 == 0 && c_k % 2 == 0)
		{

			//			int id_n = c_n / 5 - 2;
			//			int id_k = c_k / 2 - 1;

			ComputeResults.log("(n,K): (" + c_n + "," + c_k + "," + c_i + ")");

			for(int gap = 0 ; gap < gapValues.size() ; ++gap){

				ComputeResults.log("gap: " + gap);

				tpBc.gapDiss = gapValues.get(gap);
				tp.gapDiss = gapValues.get(gap);
				rp.gapDiss = gapValues.get(gap);
				xy1p.gapDiss = gapValues.get(gap);
				xy2p.gapDiss = gapValues.get(gap);

					/* Representative formulation */
					performFormulation(FormulationType.REPRESENTATIVE, "rep", rp);
//
//					/* XY1 formulation */
//					if(doXY1)
//						performFormulation(FormulationType.XY1, "xy1", xy1p);
//
//					/* XY2 formulation */
//					performFormulation(FormulationType.XY2, "xy2", xy2p);
//
//					/* Tilde formulation with cp */
//					performFormulation(FormulationType.BC, "cp", tpBc);

//					/* Tilde formulation */
//					performFormulation(FormulationType.TILDE, "tilde", tp);

			}
		}

	}

	public void performFormulation(FormulationType formulation, String formulation_name, PartitionParam param){

		StandardResult result = new StandardResult(c_n, c_i, formulation, param);
		//		int code = result.hashCode();
		StandardResult previousResult = ser.get(result);

		try {
			if(previousResult == null){

				if(formulation != FormulationType.BC){
					Partition p = null;

					switch(formulation){
					case REPRESENTATIVE:
						p = ((PartitionWithRepresentative)createPartition(rp));
						KClosestRepresentatives.onlyRoot = false;
						p.getCplex().use(new KClosestRepresentatives((PartitionWithRepresentative)p));
						p.getCplex().use(new FastCutCallback((PartitionWithRepresentative)p, 100));
						break;
					case XY1:
						p = ((PartitionXY)createPartition(xy1p));
						p.getCplex().use(new KClosestRepresentativesXY((PartitionXY)p));
						break;
					case XY2:
						p = ((PartitionXY2)createPartition(xy2p));
						p.getCplex().use(new KClosestRepresentativesXY2((PartitionXY2)p));
						break;
					default:
						p = ((PartitionWithRepresentative)createPartition(tp));
						KClosestRepresentativesTildes.onlyRoot = false;
//						p.getCplex().use(new KClosestRepresentativesTildes((PartitionWithTildes)p));
//						p.getCplex().use(new FastCutCallback((PartitionWithTildes)p, 100));
						break;
					}

					result.resolutionTime = p.getCplex().solve();
					result.nodes = (int) p.getCplex().getNnodes();
					result.bestRelaxation = p.getCplex().getBestObjValue();
					result.bestInteger = p.getCplex().getObjValue();
				}
				else{
					tpBc.inputFile = c_input_file;

					CP_Rep cprep = new CP_Rep(tpBc, 500, -1,  1, 5, true, tilim);

					result.resolutionTime = cprep.solve();
					result.nodes = (int) cprep.cpresult.node;
					result.bestRelaxation = cprep.cpresult.bestRelaxation;
					result.bestInteger = cprep.cpresult.bestInt;
				}

				ser.add(result);
				ser.saveResults(saveFilePath);
				ser.check();

			}
			else{
				System.out.print("!");
				result = previousResult;
			}

			ComputeResults.log("\t" + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(result.bestRelaxation)+ ", " + Math.round(result.bestInteger) + "] (" + Math.round(result.nodes) + " nodes, " + Math.round(result.resolutionTime) + "s)");
		}catch(IloException e) {e.printStackTrace();}
	}


	String color1 = "blue";
	String color2 = "red";
	String color3 = "green";
	String color4 = "gray";

	double legende_x = 2;
	double legende_y = 5.4;
	double size_legende_x = 3;
	double size_legende_y = 2.6;



	/**
	 * 
	 * @param listN
	 * @param listK
	 * @param base
	 * @param height
	 * @param time Time in seconds
	 */
	public void printSchema(String outputTexFile, List<Integer> listN, List<Integer> listK, double base, double height, int time) {

		Collections.sort(listN);
		int maxN = listN.get(listN.size() - 1);
		int minN = listN.get(0);

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


		content += displayTableTimeGapNodes(listN, listK, 0, time);
		content += displayTableTimeGapNodes(listN, listK, -250, time);
		content += displayTableTimeGapNodes(listN, listK, -500, time);

		content += "\\end{document}\n";


		ComputeResults.writeInFile(outputTexFile, content, false);

		System.out.println("done");

		String s;
		Process p;
		try {
			p = Runtime.getRuntime().exec("pdflatex " + outputTexFile);
			p.waitFor();
			p.destroy();
		} catch (Exception e) {}

	}


	/**
	 * 
	 * @param listN
	 * @param listK
	 * @param base
	 * @param height
	 * @param time Time in seconds
	 */
	public void printComparisonTimesOnRandomInstances(String outputTexFile, List<Integer> listN, List<Integer> listK) {

		Collections.sort(listN);
		int maxN = listN.get(listN.size() - 1);
		int minN = listN.get(0);

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


		content += displayTableTimeGapNodes(listN, listK, 0, 600);
		content += displayTableTimeGapNodes(listN, listK, 0, 3600);
		content += "\\newpage"; 
		content += displayTableTimeGapNodes(listN, listK, -250, 600);
		content += displayTableTimeGapNodes(listN, listK, -250, 3600);
		content += "\\newpage";
		content += displayTableTimeGapNodes(listN, listK, -500, 600);
		content += displayTableTimeGapNodes(listN, listK, -500, 3600);

		content += "\\end{document}\n";


		ComputeResults.writeInFile(outputTexFile, content, false);

		System.out.println("done");

		String s;
		Process p;
		try {
			p = Runtime.getRuntime().exec("pdflatex " + outputTexFile);
			p.waitFor();
			p.destroy();
		} catch (Exception e) {}

	}

	/**
	 * 
	 * @param listN
	 * @param listK
	 * @param time Time in seconds
	 */
	public void printTablesTimeGapNodes(List<Integer> listN, List<Integer> listK, int time) {

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

		content += displayTableTimeGapNodes(listN, listK, 0, time);
		content += displayTableTimeGapNodes(listN, listK, -250, time);
		content += displayTableTimeGapNodes(listN, listK, -500, time);

		content += "\\end{document}\n";

		String saveFile = "./tables_time_gap_nodes.tex";
		File f = new File(saveFile);

		if(f.exists())
			f.delete();

		ComputeResults.writeInFile(saveFile, content, false);

		System.out.println("done");

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


	/**
	 * 
	 * @param gap Gap on the dissimilarity
	 * @param time Maximal time in seconds
	 * @return
	 */
	public String displayTableTimeGapNodes(List<Integer> listN, List<Integer> listK, int gap, int time){

		StandardResult res = new StandardResult();
		res.dissimilarityGap = gap;
		res.tilim = time;

		String content = "\\begin{center}\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{*{2}{M{0.5cm}}*{5}{r@{\\hspace{0.4cm}}r}@{\\hspace{0.5cm}}*{5}{r@{\\hspace{0.5cm}}}}\\toprule\\multirow{2}{*}{\\textbf{n}} & \\multirow{2}{*}{\\textbf{K}}&\\multicolumn{10}{c}{\\textbf{Time (s) and Gap (\\%)}} & \\multicolumn{5}{c}{\\textbf{Nodes}} \\\\& & \\multicolumn{2}{c}{$(F_{nc1})$} & \\multicolumn{2}{c}{$(F_{nc2})$} & \\multicolumn{2}{c}{$(F_{er})$} & \\multicolumn{2}{c}{$(F_{ext})$} & \\multicolumn{2}{c}{$(BC)$} & $(F_{nc1})$ & $(F_{nc2})$ & $(F_{er})$ & $(F_{ext})$ & $(BC)$\\tabularnewline\\hline";
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}\\end{center}";

		Collections.sort(listN);
		Collections.sort(listK);

		int minN = listN.get(0);
		int maxN = listN.get(listN.size() - 1);

		List<Integer> instances = ser.getValidInstancesNumber();

		for(Integer n: listN){

			//			int id_n = Math.round((n - minN)/10*2)+2;

			content += "\\multirow{" + listK.size() + "}{*}{\\textbf{" + n + "}} \t";

			for(Integer k: listK){
				content += "& \\textbf{" + k + "} \t";

				/* Add the time/gap for the 5 configurations */
				for(int formulation = 0 ; formulation < 5 ; ++formulation){

					FormulationType ft;

					switch(formulation) {
					case 0: ft = FormulationType.XY1;break;
					case 1: ft = FormulationType.XY2;break;
					case 2: ft = FormulationType.REPRESENTATIVE;break;
					case 3: ft = FormulationType.TILDE;break;
					default: ft = FormulationType.BC;break;
					}

					double meanTime = 0.0;
					double meanGap = 0.0;

					for(Integer i: instances){


						System.out.println(i);
						res.n = n;
						res.K = k;
						res.i = i;
						res.type = ft;

						System.out.println("Formulation type: "+ ft);
						StandardResult resTemp = ser.get(res);

						System.out.println("(n,k,i,f,h): (" + n + "," + k + ","+ i + "," + formulation + "," + resTemp.hashCode() + ")" );

						if(resTemp == null) {
							System.err.println("Error: unable to find result which corresponds to " + res);
							return null;
						}

						System.out.println("res time: " + resTemp.resolutionTime);
						meanTime += resTemp.resolutionTime;

						/* If there is a gap */
						if(resTemp.resolutionTime >= resTemp.tilim) {
							meanGap += ComputeResults.improvement(resTemp.bestInteger, resTemp.bestRelaxation);
							System.out.println("Gap: " + ComputeResults.improvement(resTemp.bestInteger, resTemp.bestRelaxation));
						}
					}

					meanTime /= (double)(instances.size());
					meanGap /= (double)(instances.size());
					meanGap *= 100.0;

					content += "& " + Math.round(meanTime) + "s & " + Math.round(meanGap) + "\\%";
				}

				/* Add the nodes for the 5 configurations */
				for(int formulation = 0 ; formulation < 5 ; ++formulation){

					FormulationType ft;

					switch(formulation) {
					case 0: ft = FormulationType.REPRESENTATIVE;break;
					case 1: ft = FormulationType.TILDE;break;
					case 2: ft = FormulationType.XY1;break;
					case 3: ft = FormulationType.XY2;break;
					default: ft = FormulationType.BC;break;
					}

					res.type = ft;


					double meanNodes = 0;

					for(Integer i: instances) {
						res.i = i;
						meanNodes += ser.get(res).nodes;
					}

					meanNodes /= instances.size();
					content += "& " + Math.round(meanNodes);
				}

				content += "\\\\\n";
			}

			if(n == maxN)
				content += "\n";
			else
				content += "\\hline";
		}

		content += suf_table1;
		content += "Mean results (in terms of time, gap and number of nodes in the branch-and-cut tree) obtained for each of the five configurations over $10$ instances of $D_" + (res.gapId() + 1) + "$. Configuration $(BC)$ corresponds to the branch-and-cut algorithm presented section~\\ref{sec:branch}.\n";
		content += suf_table2;

		return content;

	}


	public static void main(String[] args){

		Cplex cplex = new Cplex();
			
//				new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, 0, 40, 4, 4, 0, 6, 600).execute();
//				new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, 30, 40, 4, 4, 0, 6, 3600).execute();

//				new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, 30, 40, 4, 4, 0, 6, 600).execute();
//				new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, 30, 40, 4, 4, 0, 6, 3600).execute();

			int i = 4;
			int n = 20;
			int K = 6;
				new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, n, n, K, K, i, i, 3600).execute();

//		ExecutionInocNumeroSpecialV3Time10MinTmax2 expe = new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, 30, 40, 4, 4, 0, 6, 3600);
//
//		List<Integer> listN = new ArrayList<>();
//		List<Integer> listK = new ArrayList<>();
//
//		listN.add(30);
//		listN.add(40);
//
//		listK.add(4);
//
//		expe.printComparisonTimesOnRandomInstances("output.tex", listN, listK);

		cplex.end();
	}


}
