package main;

import cplex.Cplex;
import formulation.Param;
import formulation.Partition;
import formulation.PartitionParam;
import formulation.PartitionWithRepresentative;
import formulation.RepParam;
import ilog.concert.IloException;
import ilog.cplex.IloCplex.UnknownObjectException;

public abstract class Execution {

	public int nm, nM;
	public int km, kM;
	public int im, iM;
	public int nbGraph;
	
	public Cplex cplex;
	
	public Execution(Cplex cplex, int nm, int nM, int km, int kM, int im, int iM){
		this.nm = nm;
		this.nM = nM;
		this.km = km;
		this.kM = kM;
		this.im = im;
		this.iM = iM;
		this.cplex = cplex;
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
	
	public Partition createPartition(PartitionParam param) throws IloException{
		param.maxNumberOfNodes = c_n;
		param.inputFile = getInputFile(c_n, c_i);
		param.KMax = c_k;
		return Partition.createPartition(param);
	}
	
	public void updateParam(PartitionParam p){
		p.maxNumberOfNodes = c_n;
		p.KMax = c_k;
	}
	
	public double getRootRelaxation(RepParam rp) throws IloException{
		
		Partition p = null;
		rp.maxNumberOfNodes = c_n;
		
		boolean temp = rp.isInt;
		rp.isInt = false;
		rp.inputFile = c_input_file;
		
		p = createPartition(rp);
		
		p.getCplex().turnOffCPOutput();
		p.getCplex().removeAutomaticCuts();
		p.getCplex().turnOffPrimalDualReduction();
		
		p.getCplex().solve();
		
		rp.isInt = temp;
		
		return p.getCplex().getObjValue();
		
	}
	
	public boolean isRelaxationInteger(RepParam rp) throws UnknownObjectException, IloException{
		
		rp.maxNumberOfNodes = c_n;
		rp.inputFile = c_input_file;
		
		PartitionWithRepresentative p = (PartitionWithRepresentative)createPartition(rp);
		
		p.getCplex().turnOffCPOutput();
		p.getCplex().removeAutomaticCuts();
		p.getCplex().turnOffPrimalDualReduction();
		
		p.getCplex().solve();
		
		boolean result = true;
		int i = 3;
		
		while(result && i < c_n){
			double v = p.variableGetter().getValue(p.nodeVar(i));
//			if(Math.round(v) - v > 1E-4)
			if(Math.abs(Math.round(v) - v) > 1E-4)
				result = false;
			
			i++;
		}
			
		i = 0;
		while(result && i < c_n){
			
			int j = i+1;
			
			while(result && j < c_n){
				double v = p.variableGetter().getValue(p.edgeVar(i,j));
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
