package separation;

import java.util.ArrayList;

import formulation.interfaces.IFEdgeVNodeV;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import inequality_family.PawInequality;
import variable.VariableGetter;



/**
 * For each b we try to construct greedily a violated paw inequalities by adding:
 * - the node c which maximizes x_c + x_bc
 * - if (xb + xc + x_bc > 0) the node a which maximizes x_ab - x_ac
 * - if (xb + xc + x_bc  + x_ab - x_ac > 1) the node d which maximizes x_cd   
 * @author zach
 *
 */
public class SeparationPawInequalitiesHeuristic extends AbstractSeparation<IFEdgeVNodeV>{

	public SeparationPawInequalitiesHeuristic(IFEdgeVNodeV formulation, VariableGetter vg) {
		super("Paw", formulation, vg);
		
	}

	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate(){

		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
			for(int b = 3 ;  b < formulation.n(); ++b){
				
				try{
				double v = vg.getValue(formulation.nodeVar(b));
				
				int bestC = -1;
				double bestCValue = -Double.MAX_VALUE;
			
				for(int c = b+1 ; c < formulation.n(); ++c){
					
					double currentCValue = vg.getValue(formulation.edgeVar(b,c)) + vg.getValue(formulation.nodeVar(c));
					
					if(currentCValue > bestCValue){
						bestCValue = currentCValue;
						bestC = c;
					}
				}
				
				v += bestCValue;
				int c = bestC;
				
				if(bestC != -1 && v > 0 + eps){
				
					int bestA = -1;
					double bestAValue = -Double.MAX_VALUE;
					
					for(int a = 0 ; a < b ; ++a){
						
						double currentAValue;
						try {
							currentAValue = vg.getValue(formulation.edgeVar(a,b)) - vg.getValue(formulation.edgeVar(a,c));

							if(currentAValue > bestAValue){
								bestAValue = currentAValue;
								bestA = a;
							}
						} catch (IloException e) {
							e.printStackTrace();
						}
						
					}
					
					v += bestAValue;
					int a = bestA;
					
					if(bestA != -1 && v >  1 + eps){
						
						double bestDValue = -Double.MAX_VALUE;
						int bestD = -1;
						
						for(int d = 0 ; d < a ; ++d){
							
							try {
								if(vg.getValue(formulation.edgeVar(c,d)) > bestDValue){
									bestDValue = vg.getValue(formulation.edgeVar(c,d));
									bestD = d;
								}
							} catch (IloException e) {
								e.printStackTrace();
							}
						}
						
						for(int d = a+1 ; d < b ; ++d){
							
							try {
								if(vg.getValue(formulation.edgeVar(c,d)) > bestDValue){
									bestDValue = vg.getValue(formulation.edgeVar(c,d));
									bestD = d;
									
								}
							} catch (IloException e) {
								e.printStackTrace();
							}
						}
						
						if(bestD != -1 && v + bestDValue > 2 + eps){
//							System.out.print("\tPaw (a,b,c,d): (" + a + "," + b + "," + c + "," + bestD + "): "+ vg.getValue(formulation.edgeVar(b) + " + " + vg.getValue(formulation.edgeVar(b,c)  + " + " + vg.getValue(formulation.edgeVar(c)  + " + " + vg.getValue(formulation.edgeVar(a,b)  + " - " + vg.getValue(formulation.edgeVar(a,c) + " + "  + vg.getValue(formulation.edgeVar(c,bestD) + " = " + (vg.getValue(formulation.edgeVar(b) + vg.getValue(formulation.edgeVar(b,c) + vg.getValue(formulation.edgeVar(c) + vg.getValue(formulation.edgeVar(a,b) - vg.getValue(formulation.edgeVar(a,c) + vg.getValue(formulation.edgeVar(c,bestD)));
							result.add(new PawInequality(formulation, a, b, c, bestD));
						}
					}
				}
				}catch(IloException e){e.printStackTrace();}
			}
				
		
		return result;
	}

}
