package formulation.pcenters;

/**
 * Parameters of the batch resolution
 * @author zach
 *
 */
public class BatchParam {
	
	String filePath;
	
	public boolean doPCSCO = true;
	public boolean doPCSC = false;
	public boolean doPCRad = true;
	public boolean doPCSCIt = true;
	public boolean doPCSCOIt = true;
	public boolean doPCRadIt = true;
	public boolean doPCRadLBInt = true;
	
	public BatchParam(String filePath) {
		this.filePath = filePath;
	}

}
