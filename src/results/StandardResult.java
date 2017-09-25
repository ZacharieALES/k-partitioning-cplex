package results;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class StandardResult {
	
	public enum FormulationType{REPRESENTATIVE, TILDE, XY1, XY2}
	
	@XmlAttribute
	public int resolutionTime;
	
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
	public int dissimilarityGap;
	
	@XmlAttribute
	public int i;

	@XmlAttribute
	public int tilim;
	
	@XmlElement
	public FormulationType type;
	
	public StandardResult(){}
	

}
