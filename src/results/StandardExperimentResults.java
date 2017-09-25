package results;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

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
		results = new ArrayList<>();
	}
	
	@XmlElementWrapper
	@XmlElement (name = "StandardResult")
	public
	ArrayList<StandardResult> results;
	
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

}
