package results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardExperimentResults<Result extends StandardResult>{

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 7521688851800622580L;

	public StandardExperimentResults() {
		results = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public StandardExperimentResults(String inputFile) {

		File f = new File(inputFile);

		if(f.exists()){

			//create file input stream
			InputStream is;
			try {
				is = new FileInputStream(f.getPath());

				//XML and Java binding
				JAXBContext jaxbContext = JAXBContext.newInstance(StandardResult.class,StandardExperimentResults.class, StandardResultCallbacks.class);

				//class responsible for the process of deserializing
				//XML data into Java object
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

				Object o = jaxbUnmarshaller.unmarshal(is);

				results = ((StandardExperimentResults<Result>)o).results;
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JAXBException e) {
				e.printStackTrace();
			} catch (ClassCastException e) { e.printStackTrace();}

		}

	}

	public StandardExperimentResults(String saveFilePath,
			StandardExperimentResults<StandardResult> standardExperimentResults) {
		// TODO Auto-generated constructor stub
	}

	@XmlElementWrapper(name = "map")
	public HashMap<Integer, SRListWrapper<Result>> results;


	public void saveResults(String outputFile){

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(StandardExperimentResults.class, StandardResult.class, StandardResultCallbacks.class); 

			//class responsible for the process of 
			//serializing Java object into XML data
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			//marshalled XML data is formatted with linefeeds and indentation
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			OutputStream os = new FileOutputStream(outputFile);
			jaxbMarshaller.marshal(this, os);

		} catch (JAXBException e) {
			e.printStackTrace();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Check if the hashmap is correctly constructed
	 * (i.e., if all list contain results which are valid)
	 * @return
	 */
	public boolean check(){

		boolean isValid = true;

		/* For each entry */
		for(Map.Entry<Integer, SRListWrapper<Result>> entry : results.entrySet()) {

			List<Result> value = entry.getValue().list;

			/* If the there are more than 1 results */
			if(value != null && value.size() > 1){

				Result firstResult = value.get(0);
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
	public Result get(Result result){

		Result foundResult = null;
		SRListWrapper<Result> srList = results.get(result.hashCode());

		if(srList != null){
			ArrayList<Result> list = srList.list;

			int i = 0;
			while(foundResult == null && i < list.size()){
				if(list.get(i).i == result.i)
					foundResult = list.get(i);
				i++;
			}
		}

		return foundResult;

	}

	public boolean contain(Result result){

		return get(result) != null;

	}

	public void add(Result result) {

		int hash = result.hashCode();
		SRListWrapper<Result> srList = results.get(hash);

		if(srList == null){
			srList = new SRListWrapper<Result>();
			results.put(hash, srList);
		}

		srList.add(result);

	}


	/**
	 * Get the list of all the valid instances. 
	 * An instance is valid if it has been solved for each possible combination of each parameter in the HashMap <result>
	 * 
	 * @return
	 */
	public List<Integer> getValidInstancesNumber(){

		List<Integer> exhaustiveInstancesNumber = new ArrayList<>();

		/* Get the list of all the instances solved */
		for(Map.Entry<Integer, SRListWrapper<Result>> entry: results.entrySet()) {

			SRListWrapper<Result> value = entry.getValue();

			if(value != null && value.list != null)
				for(Result res: value.list)
					if(!exhaustiveInstancesNumber.contains(res.i))
						exhaustiveInstancesNumber.add(res.i);

		}

		List<Integer> validInstancesNumber = new ArrayList<>();
		Collections.sort(exhaustiveInstancesNumber);

		if(exhaustiveInstancesNumber.size() > 0) {
			System.out.println(exhaustiveInstancesNumber.size() + " instances solved at least once (from n°" + exhaustiveInstancesNumber.get(0) + " to n°" + exhaustiveInstancesNumber.get(exhaustiveInstancesNumber.size()-1) + ")");

			validInstancesNumber = new ArrayList<>(exhaustiveInstancesNumber);

			for(Map.Entry<Integer, SRListWrapper<Result>> entry: results.entrySet()) {

				/* If the entry is valid */
				if(entry.getValue() != null && entry.getValue().list != null && entry.getValue().list.size() != 0) {

					//				System.out.println("Entry considered: "+ entry.getValue().list.get(0));
					/* For each existing instance */
					for(Integer i:exhaustiveInstancesNumber)

						/* If the instance is not solved for the current configuration */
						if(!entry.getValue().containsInstance(i) ) { 
							System.out.println("Instance not solved: " + entry.getValue().list.get(0));
							validInstancesNumber.remove(Integer.valueOf(i));
						}
				}
				else
					System.out.println("Entry " + entry.getKey() + " is invalid.");

			}
		}

		return validInstancesNumber;

	}




}
