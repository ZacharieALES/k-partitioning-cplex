package results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardExperimentResults{

	private static final long serialVersionUID = 7521688851800622580L;

	public StandardExperimentResults() {
		results = new HashMap<>();
	}

	@XmlElementWrapper(name = "map")
	public HashMap<Integer, SRListWrapper> results;

	public static StandardExperimentResults getResults(String inputFile){
		File f = new File(inputFile);

		if(f.exists()){

			//create file input stream
			InputStream is;
			try {
				is = new FileInputStream(f.getPath());

				//XML and Java binding
				JAXBContext jaxbContext = JAXBContext.newInstance(StandardResult.class,StandardExperimentResults.class);

				//class responsible for the process of deserializing
				//XML data into Java object
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				return (StandardExperimentResults)jaxbUnmarshaller.unmarshal(is);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}

		return null;

	}

	public static void saveResults(StandardExperimentResults ser, String outputFile){

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(StandardExperimentResults.class, StandardResult.class); 

			//class responsible for the process of 
			//serializing Java object into XML data
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			//marshalled XML data is formatted with linefeeds and indentation
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			OutputStream os = new FileOutputStream(outputFile);
			jaxbMarshaller.marshal(ser, os);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if the hashmap is correctly constructed
	 * (i.e., if all list contain results which have the same:
	 * - number of nodes
	 * - number of clusters
	 * - gap
	 * - time limit
	 * - formulation
	 * @return
	 */
	public boolean check(){

		boolean isValid = true;

		for(Map.Entry<Integer, SRListWrapper> entry : results.entrySet()) {
			Integer id = entry.getKey();
			List<StandardResult> value = entry.getValue().list;

			if(value != null && value.size() > 1){
				StandardResult firstResult = value.get(0);
				int hash = firstResult.hashCode();
				
				if(isValid && !firstResult.isValid()){
					isValid = false;
					System.err.println("Error invalid result: " + firstResult);
				}
				
				int i = 1;

				while(isValid && i < value.size())
					if(value.get(i).hashCode() != hash || !value.get(i).isValid()){
						isValid = false;
						System.err.println("Error found: \n\tfirst element:" + firstResult + "\n\t" + i + "th element: " + value.get(i));
					}
					else
						i++;
			}
		}
		
		return isValid;

	}
	
	/**
	 * Get the result which corresponds to the characteristics in <result>
	 * @param result The characteristics sought
	 * @return The corresponding result if it exists; null otherwise
	 */
	public StandardResult get(StandardResult result){
		
		StandardResult foundResult = null;
		SRListWrapper srList = results.get(result.hashCode());
		
		if(srList != null){
			ArrayList<StandardResult> list = srList.list;
			
			int i = 0;
			while(foundResult == null && i < list.size()){
				if(list.get(i).i == result.i)
					foundResult = list.get(i);
			}
		}
		
		return foundResult;
		
	}
	
	public boolean contain(StandardResult result){
		
		return get(result) != null;
			
	}

	public void add(StandardResult result) {
		
		int hash = result.hashCode();
		SRListWrapper srList = results.get(hash);
		
		if(srList == null){
			srList = new SRListWrapper();
			results.put(hash, srList);
		}
		
		srList.add(result);
		
	}
	

}
