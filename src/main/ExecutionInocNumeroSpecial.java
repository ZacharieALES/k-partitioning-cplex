package main;


import java.util.ArrayList;

import cplex.Cplex;
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

public class ExecutionInocNumeroSpecial extends Execution{

	double[][][][][] resultats;
	double[][][] bestInt;


	TildeParam tp;
	RepParam rp;
	XYParam xy1p;
	XYParam xy2p;

	public ExecutionInocNumeroSpecial(Cplex cplex, int nm, int nM2, int km, int kM2,
			int im, int iM2) {
		super(cplex, nm, nM2, km, kM2, im, iM2);

		tp  = new TildeParam(null, cplex, -1, true, Triangle.USE_LAZY_IN_BC_ONLY, true, true, true);
		rp = new RepParam(null, cplex, -1, Triangle.USE, true, true, true);
		xy1p = new XYParam(null, cplex, -1, false, false);
		xy2p = new XYParam(null, cplex, -1, true, false);


		resultats = ComputeResults.unserialize("./results/expe_num_special_inoc/resultats.ser", double[][][][][].class);

		// Add if bis
		//			resultats = new double[9][4][3][4][3];
		//			
		//			/* Initialize some results that will be tested afterwards to avoid repeating computations */
		//			for(int i = 0 ; i < 9 ; ++i)
		//				for(int j = 0 ; j < 4 ; ++j)
		//					for(int k = 0 ; k < 3 ; ++k)
		//						for(int l = 0 ; l < 4 ; ++l)
		//								resultats[i][j][k][l][0] = -Double.MAX_VALUE;

		//			bestInt = ComputeResults.unserialize("./results/expe_num_special_inoc/bestInt.ser");	 
		//
		//			// If the file does not exist
		//			bestInt = new double[9][4][3];
		//			
		//			/* Initialize some results that will be tested afterwards to avoid repeating computations */
		//			for(int i = 0 ; i < 9 ; ++i)
		//				for(int j = 0 ; j < 4 ; ++j)
		//					for(int k = 0 ; k < 3 ; ++k)
		//								bestInt[i][j][k] = -Double.MAX_VALUE;

		// Valeurs ajoutées manuellement suite à un soucis de sauvegarde (a vérifier si possible)
		//		bestInt[4][0][0] = 42214;
		//		resultats[4][0][0][2][0] = 42212;
		//		resultats[4][0][0][2][1] =2226;
		//		resultats[4][0][0][2][2] =120746;
		//		bestInt[5][0][0] =63942;
		//		resultats[5][0][0][0][0] =63936;
		//		resultats[5][0][0][0][1] =618;
		//		resultats[5][0][0][0][2] =1244;
		//		resultats[5][0][0][1][0] =62316;
		//		resultats[5][0][0][1][1] =3600;
		//		resultats[5][0][0][1][2] =425818;
		//		resultats[5][0][0][2][0] =32082;
		//		resultats[5][0][0][2][1] =3600;
		//		resultats[5][0][0][2][2] =18799;
		//		resultats[5][0][0][3][0] =63942;
		//		resultats[5][0][0][3][1] =384;
		//		resultats[5][0][0][3][2] =322;
		//		bestInt[5][0][1] = -8577;
		//		resultats[5][0][1][0][0] =-8577;
		//		resultats[5][0][1][0][1] =880;
		//		resultats[5][0][1][0][2] =2594;
		//		resultats[5][0][1][1][0] =-8577;
		//		resultats[5][0][1][1][1] =258;
		//		resultats[5][0][1][1][2] =28908;
		//		resultats[5][0][1][2][0] =-15913;
		//		resultats[5][0][1][2][1] =3600;
		//		resultats[5][0][1][2][2] =10943;
		//		resultats[5][0][1][3][0] =-8577;
		//		resultats[5][0][1][3][1] =464;
		//		resultats[5][0][1][3][2] =348;
		//		bestInt[6][0][0] = 81420;
		//		resultats[6][0][0][0][0] = 81415;
		//		resultats[6][0][0][0][1] = 2838;
		//		resultats[6][0][0][0][2] =1171;
		//		resultats[6][0][0][1][0] =68042;
		//		resultats[6][0][0][1][1] =3600;
		//		resultats[6][0][0][1][2] =97123;
		//		resultats[6][0][0][2][0] =34565;
		//		resultats[6][0][0][2][1] =3600;
		//		resultats[6][0][0][2][2] =4488;
		//		resultats[6][0][0][3][0] =81420;
		//		resultats[6][0][0][3][1] =1188;
		//		resultats[6][0][0][3][2] =216;
		//		bestInt[6][0][1] = -13788;
		//		resultats[6][0][1][0][0] =-14542 ;
		//		resultats[6][0][1][0][1] = 3600;
		//		resultats[6][0][1][0][2] = 1580;
		//		resultats[6][0][1][1][0] =-13789;
		//		resultats[6][0][1][1][1] = 2384;
		//		resultats[6][0][1][1][2] =47006;
		//		resultats[6][0][1][2][0] =-26090;
		//		resultats[6][0][1][2][1] =3600;
		//		resultats[6][0][1][2][2] =2639;
		//		resultats[6][0][1][3][0] =-13788;
		//		resultats[6][0][1][3][1] =1384;
		//		resultats[6][0][1][3][2] =365;
		//		bestInt[6][0][2] = -187718;
		//		resultats[6][0][2][0][0] = -187718;
		//		resultats[6][0][2][0][1] = 205;
		//		resultats[6][0][2][0][2] = 49;
		//		resultats[6][0][2][1][0] = -187718;
		//		resultats[6][0][2][1][1] = 1;
		//		resultats[6][0][2][1][2] = 75;
		//		resultats[6][0][2][2][0] = -187718;
		//		resultats[6][0][2][2][1] = 14;
		//		resultats[6][0][2][2][2] = 49;
		//		resultats[6][0][2][3][0] = -187718;
		//		resultats[6][0][2][3][1] = 127;
		//		resultats[6][0][2][3][2] = 51;
		//		bestInt[7][0][0] = 110573;
		//		resultats[7][0][0][0][0] = 104456;
		//		resultats[7][0][0][0][1] = 3600;
		//		resultats[7][0][0][0][2] = 437;
		//		resultats[7][0][0][1][0] = 78155;
		//		resultats[7][0][0][1][1] = 3600;
		//		resultats[7][0][0][1][2] = 25006;



		//		resultats[4][2][0][1][0] = -Double.MAX_VALUE;
		//		resultats[5][2][0][1][0] = -Double.MAX_VALUE;
		//		resultats[7][2][0][1][0] = -Double.MAX_VALUE;
		//		resultats[8][2][0][1][0] = -Double.MAX_VALUE;
		//		resultats[3][3][0][1][0] = -Double.MAX_VALUE;
		//		resultats[4][3][0][1][0] = -Double.MAX_VALUE;
	}

	@Override
	public void execution() throws IloException {

		ArrayList<Double> gapValues = new ArrayList<Double>();
		gapValues.add(0.0);
		gapValues.add(-250.0);
		gapValues.add(-500.0);

		if(c_n % 5 == 0 && (
				c_k == 2 && c_i == 0 ||
				c_k == 4 && c_i == 1 ||
				c_k == 6 && c_i == 2 ||
				c_k == 8 && c_i == 3
				)
				){

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
				performFormulation(id_n, id_k, gap, 0, "rep");

				/* XY1 formulation */	
				performFormulation(id_n, id_k, gap, 1, "xy1");

				/* XY2 formulation */
				performFormulation(id_n, id_k, gap, 2, "xy2");

				/* Tilde formulation */
				performFormulation(id_n, id_k, gap, 3, "tilde");

			}
		}

	}

	public void performFormulation(int id_n, int id_k, int gap, int formulation, String formulation_name){

		rp.tilim = 3600;
		xy1p.tilim = 3600;
		xy2p.tilim = 3600;
		tp.tilim = 3600;

		try {

			// REMOVE THE b CONDITION (after "||"): Added to remove false results where the optimality is not completely proved
			boolean b = (Math.round(resultats[id_n][id_k][gap][formulation][1]) < 3600 && Math.abs(resultats[id_n][id_k][gap][formulation][0] - bestInt[id_n][id_k][gap]) > 0.999); 
			if(resultats[id_n][id_k][gap][formulation][0] == -Double.MAX_VALUE || b ){

				if(b)
					System.out.println(".." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_n][id_k][gap][formulation][0])+ ", " + Math.round(bestInt[id_n][id_k][gap]) + "] (" + Math.round(resultats[id_n][id_k][gap][formulation][2]) + " nodes, " + Math.round(resultats[id_n][id_k][gap][formulation][1]) + "s)");		

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

				resultats[id_n][id_k][gap][formulation][1] = p.getCplex().solve();
				resultats[id_n][id_k][gap][formulation][2] = p.getCplex().getNnodes();
				resultats[id_n][id_k][gap][formulation][0] = p.getCplex().getBestObjValue();

				/* If this is the first obtained integer solution for the 3-uple (n, k, gap) */
				if(bestInt[id_n][id_k][gap] == -Double.MAX_VALUE)
					bestInt[id_n][id_k][gap] = p.getCplex().getObjValue();
				else
					bestInt[id_n][id_k][gap] = Math.min(bestInt[id_n][id_k][gap], p.getCplex().getObjValue());

				ComputeResults.serialize(resultats, "./results/expe_num_special_inoc/resultats.ser");
				ComputeResults.serialize(bestInt, "./results/expe_num_special_inoc/bestInt.ser");

				ComputeResults.log(ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_n][id_k][gap][formulation][0])+ ", " + Math.round(p.getCplex().getObjValue()) + "] (" + Math.round(resultats[id_n][id_k][gap][formulation][2]) + " nodes, " + Math.round(resultats[id_n][id_k][gap][formulation][1]) + "s)");
			}
			else{
				System.out.println("." + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(resultats[id_n][id_k][gap][formulation][0])+ ", " + Math.round(bestInt[id_n][id_k][gap]) + "] (" + Math.round(resultats[id_n][id_k][gap][formulation][2]) + " nodes, " + Math.round(resultats[id_n][id_k][gap][formulation][1]) + "s)");		
			}

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


		content += printSchemaTimeGap(minN, maxN, base, height, valueOfN, 0);
		content += printSchemaTimeGap(minN, maxN, base, height, valueOfN, 1);
		content += printSchemaTimeGap(minN, maxN, base, height, valueOfN, 2);


		content += displayTable(minN, maxN, base, height, valueOfN, 0);
		content += displayTable(minN, maxN, base, height, valueOfN, 1);
		content += displayTable(minN, maxN, base, height, valueOfN, 2);

		content += "\\end{document}\n";


		ComputeResults.writeInFile("./schema.tex", content, false);

		System.out.println("done");

	}


	private String printSchemaTimeGap(int minN, int maxN, double base, double height, String[] valueOfN, int data_set){


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



	private String printSchemaNode(int minN, int maxN, double base, double height, String[] valueOfN, int data_set){


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


	public String displayTable(int minN, int maxN, double base, double height, String[] valueOfN, int data_set){

		String content = "";

		double [][][][][] resultats = null;

		resultats = ComputeResults.unserialize("./results/expe_num_special_inoc/resultats.ser", double[][][][][].class);

		String pre_table = "\\begin{table}\\renewcommand{\\arraystretch}{1.2}\\centering \\begin{tabular}{M{0.5cm}*{6}{r@{\\hspace{0.5cm}}}r@{}}\\toprule\\multirow{2}{*}{\\textbf{n}} & \\multirow{2}{*}{\\textbf{Formulation}} & \\multicolumn{4}{c}{\\textbf{K}} \\\\& & \\textbf{2} &\\textbf{4} &\\textbf{6} &\\textbf{8} \\tabularnewline\\hline";
		String suf_table1 = "\\bottomrule\n\\end{tabular}\n\\caption{";
		String suf_table2 = "}\\end{table}";

		int nbFormulations = 4;

		content += pre_table;
		for(int n = minN ; n <= maxN ; n+=5){

			content += "\\multirow{" + nbFormulations + "}{*}{\\textbf{" + n + "}} \t";

			content += "& $(F_{xy1})$\t";
			for(int k = 0 ; k <= 3 ; ++k)
				content += "&\t" + Math.round(resultats[n/5-2][k][data_set][1][2]);
			content += "\\\\";

			content += "& $(F_{xy2})$\t";
			for(int k = 0 ; k <= 3 ; ++k)
				content += "&\t" + Math.round(resultats[n/5-2][k][data_set][2][2]);
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


}
