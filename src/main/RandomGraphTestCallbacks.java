package main;


import java.io.File;
import java.util.ArrayList;

import callback.cut_callback.EmptyCutCallback;
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
import formulation.PartitionWithTildes;
import formulation.PartitionXY;
import formulation.PartitionXY2;
import formulation.RepParam;
import formulation.RepParam.Triangle;
import formulation.TildeParam;
import formulation.XYParam;
import ilog.concert.IloException;
import results.ComputeResults;
import results.StandardExperimentResults;
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
import results.StandardResultCallbacks;

public class RandomGraphTestCallbacks extends Execution{

	public StandardExperimentResults<StandardResultCallbacks> serCB;

	//	TildeParam tp  = new TildeParam(null, cplex, -1, true, Triangle.USE_LAZY_IN_BC_ONLY, true, true, true);
	TildeParam tpBc;
	TildeParam tp;
	RepParam rp;
	XYParam xy1p;
	XYParam xy2p;


	public static double LAST_SAVE_TIME;
	public static double DELAY_TO_SAVE_IN_MIN = 120;

//	String saveFilePath= "./results/expe_num_special_inoc_v3/resultats_cb_dell_ensta.ser";
	String saveFilePath= "/home/uma/ales/res/2017-11-29_CB_Partitioning/resultats_margaux.ser";
	
	int tilim;
	ArrayList<Double> gapValues;

	public RandomGraphTestCallbacks(Cplex cplex, int nm, int nM2, int km, int kM2,
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
			serCB = new StandardExperimentResults<StandardResultCallbacks>(saveFilePath);
		else
			serCB = new StandardExperimentResults<StandardResultCallbacks>();

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

				/* XY1 formulation */
				performFormulation(FormulationType.XY1, "xy1", xy1p);

				/* XY2 formulation */
				performFormulation(FormulationType.XY2, "xy2", xy2p);

				/* Tilde formulation with cp */
				performFormulation(FormulationType.BC, "cp", tpBc);

				/* Tilde formulation */
				performFormulation(FormulationType.TILDE, "tilde", tp);

			}
		}

	}


	public void performFormulation(FormulationType formulation, String formulation_name, PartitionParam param){

		performFormulation(formulation, formulation_name, param, false, false, false, false);
		performFormulation(formulation, formulation_name, param, false, true, false, false);
		performFormulation(formulation, formulation_name, param, false, false, true, false);
		performFormulation(formulation, formulation_name, param, true, false, false, false);
		performFormulation(formulation, formulation_name, param, true, true, false, false);
		performFormulation(formulation, formulation_name, param, true, false, true, false);
		performFormulation(formulation, formulation_name, param, false, false, false, true);
	}


	public void performFormulation(FormulationType formulation, String formulation_name, PartitionParam param, boolean useFastCB, boolean useHCB, boolean useHRCB, boolean useEmptyCB){

		StandardResultCallbacks result = new StandardResultCallbacks(c_n, c_i, formulation, param, useFastCB, useHCB, useHRCB, useEmptyCB);
		//		int code = result.hashCode();
		StandardResultCallbacks previousResult = serCB.get(result);

		boolean compute = true;

		//		if(formulation == FormulationType.BC
		//				&& (useFastCB || useHCB || useHRCB))
		//			compute = false;

		if(compute) {
			try {

				/* If the result has not already been computed */
				if(previousResult == null){

					if(formulation != FormulationType.BC){
						Partition p = null;

						if(useHCB) {							
							KClosestRepresentativesTildes.onlyRoot = false;
							KClosestRepresentativesXY.onlyRoot = false;
							KClosestRepresentativesXY2.onlyRoot = false;
							KClosestRepresentatives.onlyRoot = false;
						}
						else {
							KClosestRepresentativesTildes.onlyRoot = true;
							KClosestRepresentativesXY.onlyRoot = true;
							KClosestRepresentativesXY2.onlyRoot = true;
							KClosestRepresentatives.onlyRoot = true;
						}

						switch(formulation){
						case REPRESENTATIVE:
							p = ((PartitionWithRepresentative)createPartition(rp));

							if(useHCB || useHRCB)
								p.getCplex().use(new KClosestRepresentatives((PartitionWithRepresentative)p));

							if(useFastCB)
								p.getCplex().use(new FastCutCallback((PartitionWithRepresentative)p, 100));
							break;
						case XY1:
							p = ((PartitionXY)createPartition(xy1p));

							if(useHCB || useHRCB) {
								p.getCplex().use(new KClosestRepresentativesXY((PartitionXY)p));				
							}

							if(useFastCB)
								p.getCplex().use(new FastCutCallback((PartitionXY)p, 100));
							break;
						case XY2:
							p = ((PartitionXY2)createPartition(xy2p));

							if(useHCB || useHRCB) {
								p.getCplex().use(new KClosestRepresentativesXY2((PartitionXY2)p));				
							}

							if(useFastCB)
								p.getCplex().use(new FastCutCallback((PartitionXY)p, 100));
							break;
						default:
							p = ((PartitionWithRepresentative)createPartition(tp));

							if(useHCB || useHRCB)
								p.getCplex().use(new KClosestRepresentativesTildes((PartitionWithTildes)p));

							if(useFastCB)
								p.getCplex().use(new FastCutCallback((PartitionWithTildes)p, 100));
							break;
						}

						if(useEmptyCB)
							p.getCplex().use(new EmptyCutCallback(p));

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

					serCB.add(result);

					if((System.currentTimeMillis() - LAST_SAVE_TIME) > DELAY_TO_SAVE_IN_MIN * 60000) {
						System.out.println("Save...");
						serCB.saveResults(saveFilePath);
						LAST_SAVE_TIME = System.currentTimeMillis();
					}
					serCB.check();

				}
				else{
					System.out.print("!");
					result = previousResult;
				}

				String log = "";

				if(useFastCB)
					log += "fast, ";

				if(useHCB)
					log += "hcb, ";

				if(useHRCB)
					log += "hrcb";

				if(useEmptyCB)
					log += "empty";

				ComputeResults.log("\t" + ComputeResults.getDate() + " : " + formulation_name + ": \t[relaxation, int] : [" +  Math.round(result.bestRelaxation)+ ", " + Math.round(result.bestInteger) + "] (" + Math.round(result.nodes) + " nodes, " + Math.round(result.resolutionTime) + "s), " + log);

			}catch(IloException e) {e.printStackTrace();}
		}
	}


	public static void main(String[] args){

		Cplex cplex = new Cplex();
		LAST_SAVE_TIME = System.currentTimeMillis();

		//				new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, 0, 40, 4, 4, 0, 6, 600).execute();
		//				new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, 30, 40, 4, 4, 0, 6, 3600).execute();

		//				new ExecutionInocNumeroSpecialV3Time10MinTmax2(cplex, 30, 40, 4, 4, 0, 6, 600).execute();
		new RandomGraphTestCallbacks(cplex, 10, 30, 2, 6, 0, 6, 3600).execute();

		//		int i = 4;
		//		int n = 20;
		//		int K = 6;
		//		new RandomGraphTestCallbacks(cplex, n, n, K, K, i, i, 3600).execute();

		cplex.end();
	}


}
