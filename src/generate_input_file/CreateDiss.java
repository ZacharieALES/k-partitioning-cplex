package generate_input_file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateDiss {
	
	public static void createFile(String nameFile, int n)
	{	

		try
		{

			File myFile = new File(nameFile);
			
			if(myFile.exists())
				myFile.delete(); 
			
			FileWriter fw = new FileWriter(nameFile, true);
			
			
			BufferedWriter output = new BufferedWriter(fw);
			
			for(int i = 0 ; i < n ; ++i){
				for(int j = 0 ; j <= i ; ++j){
					output.write(Math.round(Math.random()*500) + " ");
					output.flush();
				}
				output.write("\n");
			}
			
			output.close();

		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
			}

	}

}
