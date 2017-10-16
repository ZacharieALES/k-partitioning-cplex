package results;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;

import cut_callback.Abstract_CutCallback;
import formulation.Partition;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import separation.Abstract_Separation;


/**
 * Results of an execution of cplex
 * @author zach
 *
 */
public class Result implements Serializable{

	private static final long serialVersionUID = 6331586870193467691L;
	
	public int n;
	public int K;
	public int i;

	public double bestInt;
	public double bestRelaxation;
	
	public double time;
	public double node;
	public ArrayList<Cut> cplexCutNb = new ArrayList<Cut>();
	public ArrayList<Cut> userCutNb = new ArrayList<Cut>();
	
	/* Number of time our separation algorithm is called */
	public int iterationNb;
	public double separationTime;
	public double firstRelaxation = -1.0;
	
	public void solveAndGetResults(int i, Partition p, boolean log){
		solveAndGetResults(i, p, null, log);
	}
	
	public void solveAndGetResults(int i, Abstract_CutCallback ucc, boolean log){
		solveAndGetResults(i, ucc.rep, ucc, log);
	}
	
	public void solveAndGetResults(int i, Partition p, Abstract_CutCallback ucc, boolean log){

		time = p.solve();
		getResults(i, p, ucc, log);

	}
	
	public void getResults(int i, Partition p,
			Abstract_CutCallback ucc, boolean log) {
		
		try{
			
			n = p.n;
			K = p.K();
			this.i = i;
			
			node = p.getNnodes();
			System.out.println("Result, nb of nodes: " + p.getNnodes());

			if(ucc != null && ucc.sep != null){

				ArrayList<Abstract_Separation> al_as = ucc.sep;				
				separationTime = ucc.time;
				iterationNb = ucc.iterations;
				firstRelaxation = ucc.root_relaxation;

				for(int j = 0 ; j < al_as.size() ; ++j)
					if(al_as.get(j).added_cuts > 0)
						userCutNb.add(new Cut(al_as.get(j).name, al_as.get(j).added_cuts));
				
			}
			else{
				separationTime = -1.0;
				iterationNb = -1;
				firstRelaxation = -1.0;
			}
			
			bestInt = p.getObjValue2();
			bestRelaxation = p.getBestObjValue2();
		
			countCplexCuts(p);
				
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		if(log)
			log();	
		
	}

	public void log(){

		NumberFormat nf0 = NumberFormat.getInstance() ;
		nf0.setMinimumFractionDigits(0);
		nf0.setMaximumFractionDigits(0);

		NumberFormat nf1 = NumberFormat.getInstance() ;
		nf1.setMinimumFractionDigits(1);
		nf1.setMaximumFractionDigits(1);

		NumberFormat nf2 = NumberFormat.getInstance() ;
		nf2.setMinimumFractionDigits(2);
		nf2.setMaximumFractionDigits(2);
		
		String log = "----\n";
		log += "date: " + ComputeResults.getDate();
		
		log += "(n,k,i): (";

		log += n + "," + K + "," + i + ")\n";
		
		log += "\ttime:\t\t" + nf0.format(time) + "s\n\tnode:\t\t" + nf0.format(node) + "n\n";

		log += "\tbest relax.:\t" + nf1.format(bestRelaxation) + "\n"; 
		
		log += "\tbest int.:\t" + nf0.format(bestInt) + "\n"; 

		if(firstRelaxation != -1.0)
			log += "\tfirst relax.:\t" + nf1.format(firstRelaxation) + "\n";
		
		log += "\tgap:\t\t" + nf2.format(ComputeResults.improvement(bestRelaxation, bestInt)) + "%\n"; 

		if(separationTime != -1.0)
			log += "\tsep. time:\t" + nf1.format(separationTime) + "s\n";
		
		if(iterationNb != -1.0)
			log += "\tsep. it. nb.:\t" + iterationNb + "\n"; 
		
		if(this.userCutNb.size() > 0 || this.cplexCutNb.size() > 0)
			log += "\tcuts:\n";
		for(int j = 0 ; j < this.userCutNb.size() ; ++j)
			log += "\t\t" + userCutNb.get(j).cutName + " cut:\t" + userCutNb.get(j).cutNb + "\n";
		
		for(int j = 0 ; j < this.cplexCutNb.size() ; ++j)
			log += "\t\t" + cplexCutNb.get(j).cutName + " cut:\t" + cplexCutNb.get(j).cutNb + "\n";

		ComputeResults.writeInFile("log.txt", log, true);
		System.out.println(log);
	}
	
	public void serialize(String file){
		
	    try {
		      FileOutputStream fichier = new FileOutputStream(file);
		      ObjectOutputStream oos = new ObjectOutputStream(fichier);
		      oos.writeObject(this);
		      oos.flush();
		      oos.close();
		    }
		    catch (java.io.IOException e) {
		      e.printStackTrace();
		    }
		
	}
	
	public static Result unserialize(String file){
		
		Result results = null;
		
		try {
		      FileInputStream fichier = null;
	    	  fichier = new FileInputStream(file);
		      
		      ObjectInputStream ois = new ObjectInputStream(fichier);
		      
		      results = (Result) ois.readObject();
		      
		      ois.close();
		      fichier.close();
		} 
	    catch (java.io.IOException e) {
	      e.printStackTrace();
	    }
	    catch (ClassNotFoundException e) {
	      e.printStackTrace();
	    }
 		
		return results;
	}	
	

	public class Cut implements Serializable{

		private static final long serialVersionUID = -6969643363994348665L;
		
		public String cutName;
		public int cutNb;
		
		public Cut(String name, int nb){
			cutName = name;
			cutNb = nb;
		}
		
	}
		
	public void countCplexCuts(Partition p) throws IloException{
	
		if(p.getNcuts(IloCplex.CutType.CliqueCover) != 0)
			this.cplexCutNb.add(new Cut("CliqueCover", p.getNcuts(IloCplex.CutType.CliqueCover)));
		
		if(p.getNcuts(IloCplex.CutType.Cover) != 0)
			this.cplexCutNb.add(new Cut("Cover", p.getNcuts(IloCplex.CutType.Cover)));
		
		if(p.getNcuts(IloCplex.CutType.Disj) != 0)
			this.cplexCutNb.add(new Cut("Disj", p.getNcuts(IloCplex.CutType.Disj)));
		
		if(p.getNcuts(IloCplex.CutType.FlowCover) != 0)
			this.cplexCutNb.add(new Cut("FlowCover", p.getNcuts(IloCplex.CutType.FlowCover)));
		
		if(p.getNcuts(IloCplex.CutType.FlowPath) != 0)
			this.cplexCutNb.add(new Cut("FlowPath", p.getNcuts(IloCplex.CutType.FlowPath)));
		
		if(p.getNcuts(IloCplex.CutType.Frac) != 0)
			this.cplexCutNb.add(new Cut("Frac", p.getNcuts(IloCplex.CutType.Frac)));
		
		if(p.getNcuts(IloCplex.CutType.GUBCover) != 0)
			this.cplexCutNb.add(new Cut("GUBCover", p.getNcuts(IloCplex.CutType.GUBCover)));
		
		if(p.getNcuts(IloCplex.CutType.ImplBd) != 0)
			this.cplexCutNb.add(new Cut("ImplBd", p.getNcuts(IloCplex.CutType.ImplBd)));
		
		if(p.getNcuts(IloCplex.CutType.LocalCover) != 0)
			this.cplexCutNb.add(new Cut("LocalCover", p.getNcuts(IloCplex.CutType.LocalCover)));
		
		if(p.getNcuts(IloCplex.CutType.MCF) != 0)
			this.cplexCutNb.add(new Cut("MCF", p.getNcuts(IloCplex.CutType.MCF)));
		
		if(p.getNcuts(IloCplex.CutType.MIR) != 0)
			this.cplexCutNb.add(new Cut("MIR", p.getNcuts(IloCplex.CutType.MIR)));
		
		if(p.getNcuts(IloCplex.CutType.ObjDisj) != 0)
			this.cplexCutNb.add(new Cut("ObjDisj", p.getNcuts(IloCplex.CutType.ObjDisj)));
		
		if(p.getNcuts(IloCplex.CutType.SolnPool) != 0)
			this.cplexCutNb.add(new Cut("SolnPool", p.getNcuts(IloCplex.CutType.SolnPool)));
	
		if(p.getNcuts(IloCplex.CutType.Table) != 0)
			this.cplexCutNb.add(new Cut("Table", p.getNcuts(IloCplex.CutType.Table)));
		
		if(p.getNcuts(IloCplex.CutType.Tighten) != 0)
			this.cplexCutNb.add(new Cut("Tighten", p.getNcuts(IloCplex.CutType.Tighten)));
		
		if(p.getNcuts(IloCplex.CutType.ZeroHalf) != 0)
			this.cplexCutNb.add(new Cut("ZeroHalf", p.getNcuts(IloCplex.CutType.ZeroHalf)));
	}
}
