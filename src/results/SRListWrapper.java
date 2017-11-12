package results;

import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class SRListWrapper<Result extends StandardResult> {

	@XmlElementWrapper
	@XmlElement (name = "StandardResult")
	ArrayList<Result> list = new ArrayList<>();	
	
	public SRListWrapper(){}
	
	public void add(Result sr){
		list.add(sr);
	}
	
	public boolean containsInstance(int i) {
		
		Iterator<Result> it = list.iterator();
		boolean isContained = false;
		
		while(!isContained && it.hasNext()) 
			if(it.next().i == i) isContained = true;
		
		return isContained;
		
	}

}
