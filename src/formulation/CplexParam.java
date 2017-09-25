package formulation;

public class CplexParam {
	
	public boolean output = false;
	public boolean primalDual = true;
	public boolean autoCuts = true;
	public double tilim = -1;
	
	public CplexParam(boolean output){
		this.output = output;
	}
	
	public CplexParam(boolean output, boolean primalDual, boolean autoCuts, double tilim){
		this(output);
		this.primalDual = primalDual;
		this.autoCuts = autoCuts;
		this.tilim = tilim;
	}
	
}
