package results;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import formulation.Param;
import results.StandardResult.FormulationType;

@XmlAccessorType(XmlAccessType.FIELD)
public class StandardResult {

	public enum FormulationType{REPRESENTATIVE, TILDE, XY1, XY2, BC}

	@XmlAttribute
	public double resolutionTime;

	@XmlAttribute
	public int nodes;

	@XmlAttribute
	public double bestInteger;

	@XmlAttribute
	public double bestRelaxation;

	@XmlAttribute
	public int n;

	@XmlAttribute
	public int K;

	@XmlAttribute
	public double dissimilarityGap;

	@XmlAttribute
	public int i;

	@XmlAttribute
	public double tilim;

	@XmlElement
	public FormulationType type;

	public StandardResult(){}
	
	public StandardResult(int n, int i, FormulationType formulation, Param param) {
		this.n = n;
		this.i = i;
		this.dissimilarityGap = param.gapDiss;
		this.K = param.K;
		this.tilim = param.tilim;
		type = formulation;
	}

	@Override
	public int hashCode(){
		return gapId() +
				10 * formulationId() +
				100 * K +
				10000 * n +
				1000000 * timeId();
	}

	private int timeId(){
		switch((int)tilim){
		case 600: return 0;
		case 3600: return 1;
		default: return -1;
		}
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
	
	private int gapId(){
		switch((int)dissimilarityGap){
		case 0: return 0;
		case -250: return 1;
		case -500: return 2;
		default: return -1;
		}
	}
	
	public boolean isValid(){
		return gapId() != -1 && formulationId() != -1 && timeId() != -1 && n > 0 && K > 0;
	}
	
	@Override
	public String toString(){
		return "n" + n + "-K" + K + "-formul" + formulationId() + "-t" + timeId() + "-gap" + gapId() + "-i" + i;
	}


}
