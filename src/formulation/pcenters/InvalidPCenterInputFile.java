package formulation.pcenters;

@SuppressWarnings("serial")
public class InvalidPCenterInputFile extends Exception {
	
	public InvalidPCenterInputFile(String inputFile, String reason){
		super("The input file " + inputFile + " is not valid because: " + reason);
	}

}
