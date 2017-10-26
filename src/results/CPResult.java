package results;

import java.text.NumberFormat;
import java.util.ArrayList;

public class CPResult extends Result{
	
	private static final long serialVersionUID = -656518835105631616L;
	public double cp_first_relaxation;
	public double cp_time;
	public ArrayList<Cut> cpCutNb = new ArrayList<Result.Cut>();
	public int cp_iteration;
	
	/* Integer.MAX_VALUE if inequalities are never removed during the cutting_plane step.
	 * Otherwise it is equal to the number of iterations between the removal of inequalities
	 */
	public int mod_remove;

	@Override
	public void log(){

		NumberFormat nf0 = NumberFormat.getInstance() ;
		nf0.setMinimumFractionDigits(0);
		nf0.setMaximumFractionDigits(0);

		NumberFormat nf1 = NumberFormat.getInstance() ;
		nf1.setMinimumFractionDigits(1);
		nf1.setMaximumFractionDigits(1);

		NumberFormat nf2 = NumberFormat.getInstance() ;
		nf2.setMinimumFractionDigits(2);
		nf2.setMaximumFractionDigits(2);
		
		String log = "----\n";
		log += "date: " + ComputeResults.getDate();
		
		log += "(n,k,i): (";

		log += n + "," + K + "," + i + ")\n";

		log += "\tcp time:\t" + nf0.format(cp_time) + "s"  + "\n";

		if(time != -1.0)
			log += "\ttime BB:\t\t" + nf0.format(time) + "s\n\tnode:\t\t" + nf0.format(node) + "n\n";

		log += "\tfirst relax.:\t" + nf1.format(cp_first_relaxation) + "\n"; 

		if(bestRelaxation != -1.0 && Math.abs(firstRelaxation - bestInt) > 1E-6)
			log += "\tlast CP relax.:\t" + nf1.format(firstRelaxation) + "\n"; 

		if(bestRelaxation != -1.0 && Math.abs(bestRelaxation - bestInt) > 1E-6){
			log += "\tbest relax.:\t" + nf1.format(bestRelaxation) + "\n"; 
			log += "\tgap:\t\t" + nf2.format(ComputeResults.improvement(bestRelaxation, bestInt)) + "%\n";
		}
		
		log += "\tbest int.:\t" + nf0.format(bestInt) + "\n"; 
				
		log += "\tcp it.:\t\t" + nf0.format(cp_iteration) + "\n"; 

		if(separationTime != -1.0)
			log += "\tsep. time:\t" + nf1.format(separationTime) + "s\n";
		
		if(iterationNb != -1)
			log += "\tsep. it. nb.:\t" + iterationNb + "\n"; 

		if(cpCutNb.size() > 0){
			log += "\tcp cuts:\n";
			
			for(int j = 0 ; j < this.cpCutNb.size() ; ++j)
				log += "\t\t" + cpCutNb.get(j).cutName + " cut:\t" + cpCutNb.get(j).cutNb + "\n";
		}
		
		if(userCutNb.size() > 0 || cplexCutNb.size() > 0){
			log += "\tcuts:\n";
			
			for(int j = 0 ; j < this.userCutNb.size() ; ++j)
				log += "\t\t" + userCutNb.get(j).cutName + " cut:\t" + userCutNb.get(j).cutNb + "\n";
			
			for(int j = 0 ; j < this.cplexCutNb.size() ; ++j)
				log += "\t\t" + cplexCutNb.get(j).cutName + " cut:\t" + cplexCutNb.get(j).cutNb + "\n";
		}
		
		ComputeResults.writeInFile("log.txt", log, true);
		System.out.println(log);
	}
}
