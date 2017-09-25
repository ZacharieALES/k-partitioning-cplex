package main;

import formulation.CplexParam;
import formulation.Param;
import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.Partition_with_tildes;
import formulation.Partition_x_y;
import formulation.Partition_x_y_2;
import formulation.RepParam;
import formulation.TildeParam;
import formulation.XYParam;
import ilog.concert.IloException;

public abstract class Execution {

	public int nm, nM;
	public int km, kM;
	public int im, iM;
	public int nbGraph;
	
	public Execution(int nm, int nM, int km, int kM, int im, int iM){
		this.nm = nm;
		this.nM = nM;
		this.km = km;
		this.kM = kM;
		this.im = im;
		this.iM = iM;
	}
	
	public int c_n;
	public int c_k;
	public int c_i;
	public String c_input_file;
	
	public void execute(){
		
		try {
		
			for(c_i = im ; c_i <= iM ; ++c_i)
			for(c_n = nm ; c_n <= nM ; ++c_n)
				for(c_k = km ; c_k <= kM ; ++c_k)
					if(c_k < c_n)
						{
							System.out.println("(n,k,i): (" + c_n + "," + c_k + "," +c_i + ")"); 
							c_input_file = "data/input_root_relaxation_100/n_" + c_n + "_id_" + c_i + ".txt";
							execution();
							
						}

		} catch (IloException e1) {
			e1.printStackTrace();
		}
	}
	
	
	public Partition getRootRelaxationThenCreatePartition(CplexParam rc, RepParam rp){

		double relaxation = getRootRelaxation(rp);
		
		Partition p = createPartition(rc, rp);
		p.rootRelaxation = relaxation;
		
		return p;
	
	}
	
	public double getRootRelaxation(RepParam rp){
		
		Partition p = null;
		
		boolean temp = rp.isInt;
		rp.isInt = false;

		if(rp instanceof TildeParam){
			TildeParam tp = (TildeParam) rp;
			p = new Partition_with_tildes(c_k, c_input_file, c_n, new CplexParam(false), tp);
		}
		else
			p = new PartitionWithRepresentative(c_k, c_input_file, c_n, new CplexParam(false), rp);
		
		p.turnOffCPOutput();
		p.removeAutomaticCuts();
		p.turnOffPrimalDualReduction();
		
		p.solve();
		
		rp.isInt = temp;
		
		return p.getObjValue2();
		
	}
	
	public boolean isRelaxationInteger(RepParam rp){
		
		Partition p = null;


		if(rp instanceof TildeParam){
			TildeParam tp = (TildeParam) rp;
			p = new Partition_with_tildes(c_k, c_input_file, c_n, new CplexParam(false), tp);
		}
		else
			p = new PartitionWithRepresentative(c_k, c_input_file, c_n, new CplexParam(false), rp);
		
		p.turnOffCPOutput();
		p.removeAutomaticCuts();
		p.turnOffPrimalDualReduction();
		
		p.solve();
		
		boolean result = true;
		int i = 3;
		
		while(result && i < c_n){
			double v = p.getValue(i-3);
//			if(Math.round(v) - v > 1E-4)
			if(Math.abs(Math.round(v) - v) > 1E-4)
				result = false;
			
			i++;
		}
			
		i = 0;
		while(result && i < c_n){
			
			int j = i+1;
			
			while(result && j < c_n){
				double v = p.getValue(i,j);
//				if(Math.round(v) - v > 1E-4)
				if(Math.abs(Math.round(v) - v) > 1E-4)
					result = false;
				
				++j;
			}
				
			++i;
		}
		
		return result;
		
	}

	public Partition createPartition(boolean isTilde, boolean isInt){
	
		CplexParam cp = new CplexParam(false, true, true, -1);
		
		if(isTilde){
			TildeParam tp = new TildeParam(isInt);
			return createPartition(cp, tp);
		}
		else{
			RepParam rp = new RepParam(isInt);
			return createPartition(cp, rp);
		}
		
	}
	
	public Partition createPartition(CplexParam rc, Param param){

		Partition p = null;

		if(param instanceof TildeParam){
			TildeParam tp = (TildeParam) param;
			p = new Partition_with_tildes(c_k, c_input_file, c_n, rc, tp);
		}
		else if(param instanceof RepParam){
			RepParam rp = (RepParam) param;
			p = new PartitionWithRepresentative(c_k, c_input_file, c_n, rc, rp);
		}
		else{
			XYParam xyp = (XYParam) param;
			if(xyp.isSecondXYFormulation){
				p = new Partition_x_y_2(c_k, c_input_file, c_n, rc, xyp);
			}
			else
				p = new Partition_x_y(c_k, c_input_file, c_n, rc, xyp);
		}
		
		return p;
	}
	
	public abstract void execution() throws IloException;

	
}
