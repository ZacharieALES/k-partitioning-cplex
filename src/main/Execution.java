package main;

import formulation.Param;
import formulation.Partition;
import formulation.PartitionWithRepresentative;
import formulation.Partition_with_tildes;
import formulation.RepParam;
import formulation.TildeParam;
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
	
	private String getInputFile(int n, int i){
		return "data/input_root_relaxation_100/n_" + n + "_id_" + i + ".txt";
	}
	
	public void execute(){
		
		try {
		
			for(c_i = im ; c_i <= iM ; ++c_i)
			for(c_n = nm ; c_n <= nM ; ++c_n)
				for(c_k = km ; c_k <= kM ; ++c_k)
					if(c_k < c_n)
						{
							System.out.println("(n,k,i): (" + c_n + "," + c_k + "," +c_i + ")"); 
							c_input_file = getInputFile(c_n, c_i);
							execution();
							
						}

		} catch (IloException e1) {
			e1.printStackTrace();
		}
	}
	
	public Partition createPartition(Param param){
		param.maxNumberOfNodes = c_n;
		param.inputFile = getInputFile(c_n, c_i);
		param.K = c_k;
		return Partition.createPartition(param);
	}
	
	public void updateParam(Param p){
		p.maxNumberOfNodes = c_n;
		p.K = c_k;
	}
	
	public Partition getRootRelaxationThenCreatePartition(RepParam rp){

		double relaxation = getRootRelaxation(rp);
		
		Partition p = createPartition(rp);
		p.rootRelaxation = relaxation;
		
		return p;
	
	}
	
	public double getRootRelaxation(RepParam rp){
		
		Partition p = null;
		rp.maxNumberOfNodes = c_n;
		
		boolean temp = rp.isInt;
		rp.isInt = false;
		rp.inputFile = c_input_file;
		
		p = createPartition(rp);
		
		p.turnOffCPOutput();
		p.removeAutomaticCuts();
		p.turnOffPrimalDualReduction();
		
		p.solve();
		
		rp.isInt = temp;
		
		return p.getObjValue2();
		
	}
	
	public boolean isRelaxationInteger(RepParam rp){
		
		Partition p = null;
		rp.maxNumberOfNodes = c_n;
		rp.inputFile = c_input_file;
		
		p = createPartition(rp);
		
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
	
	public abstract void execution() throws IloException;

	
}
