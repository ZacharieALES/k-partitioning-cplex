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
	public boolean doPC = false;
	public boolean doPCOR = false;
	public boolean doPCRadU = false;
	
	public boolean doPCSCIt = true;
	public boolean doPCSCOIt = true;
	public boolean doPCRadIt = true;
	public boolean doPCRUIt = false;
	
	public boolean doPCRadLBInt = true;
	public boolean doPCSCORelax = false;
	public boolean doPCSCRelax = false;
	public boolean doPCRadRelax = false;
	public boolean doPCRelax = false;
	public boolean doPCORRelax = false;
	public boolean doPCRadURelax = false;
	
	/* Do multichotomy with PCSCO */
	public boolean doPCSCOM = false;
	
	/** -1 if the time is not limited */
	public int timeMaxInSeconds = -1;

	
	public BatchParam(String filePath) {
		this.filePath = filePath;
	}

}
