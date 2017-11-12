package results;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import formulation.PartitionParam;

@XmlAccessorType(XmlAccessType.FIELD)
public class StandardResultCallbacks extends StandardResult{

	public StandardResultCallbacks(){}

	@XmlAttribute
	boolean useFastCB;

	@XmlAttribute
	boolean useHCB;
	
	@XmlAttribute
	boolean useHRCB;
	
	public StandardResultCallbacks(int n, int i, FormulationType formulation, PartitionParam param, boolean useFastCB, boolean useHCB, boolean useHRCB) {
		super(n, i, formulation, param);
		this.useFastCB = useFastCB;
		this.useHCB = useHCB;
		this.useHRCB = useHRCB;
	}

	@Override
	public int hashCode(){
		return gapId() +
				10 * formulationId() +
				100 * K +
				10000 * n +
				1000000 * callbackId();
	}
	
	private int callbackId() {
		
		if(useFastCB)
			if(useHCB)
				return 0;
			else if(useHRCB)
				return 1;
			else
				return 2;
		else
			if(useHCB)
				return 3;
			else if(useHRCB)
				return 4;
			else
				return 5;
	}
	
	private int formulationId(){
		switch(type){
		case REPRESENTATIVE: return 0;
		case TILDE: return 1;
		case XY1: return 2;
		case XY2: return 3;
		case BC: return 4;
		default: return -1;
		}
	}
	
	public int gapId(){
		switch((int)dissimilarityGap){
		case 0: return 0;
		case -250: return 1;
		case -500: return 2;
		default: return -1;
		}
	}
	
	public boolean isValid(){
		return gapId() != -1 && formulationId() != -1 && callbackId() != -1 && n > 0 && K > 0;
	}
	
	@Override
	public String toString(){
		return "n" + n + "-K" + K + "-formul" + formulationId() + "-cb" + callbackId() + "-gap" + gapId() + "-i" + i;
	}


}
