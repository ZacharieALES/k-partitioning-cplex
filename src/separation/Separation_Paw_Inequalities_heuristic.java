package separation;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.Paw_Inequality;

import java.util.ArrayList;

import solution.Solution_Representative;

/**
 * For each b we try to construct greedily a violated paw inequalities by adding:
 * - the node c which maximizes x_c + x_bc
 * - if (xb + xc + x_bc > 0) the node a which maximizes x_ab - x_ac
 * - if (xb + xc + x_bc  + x_ab - x_ac > 1) the node d which maximizes x_cd   
 * @author zach
 *
 */
public class Separation_Paw_Inequalities_heuristic extends Abstract_Separation{


	public Separation_Paw_Inequalities_heuristic(Solution_Representative ucc) {
		super("Paw", ucc);
	}

	@Override
	public ArrayList<Abstract_Inequality> separate() throws IloException {
		// TODO Auto-generated method stub

		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
			for(int b = 3 ;  b < s.n(); ++b){
				
				double v = s.x(b);
				
				int bestC = -1;
				double bestCValue = -Double.MAX_VALUE;
			
				for(int c = b+1 ; c < s.n(); ++c){
					
					double currentCValue = s.x(b,c) + s.x(c);
					
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
						
						double currentAValue = s.x(a,b) - s.x(a,c);
						
						if(currentAValue > bestAValue){
							bestAValue = currentAValue;
							bestA = a;
						}
					}
					
					v += bestAValue;
					int a = bestA;
					
					if(bestA != -1 && v >  1 + eps){
						
						double bestDValue = -Double.MAX_VALUE;
						int bestD = -1;
						
						for(int d = 0 ; d < a ; ++d){
							
							if(s.x(c,d) > bestDValue){
								bestDValue = s.x(c,d);
								bestD = d;
							}
						}
						
						for(int d = a+1 ; d < b ; ++d){
							
							if(s.x(c,d) > bestDValue){
								bestDValue = s.x(c,d);
								bestD = d;
								
							}
						}
						
						if(bestD != -1 && v + bestDValue > 2 + eps){
//							System.out.print("\tPaw (a,b,c,d): (" + a + "," + b + "," + c + "," + bestD + "): "+ s.x(b) + " + " + s.x(b,c)  + " + " + s.x(c)  + " + " + s.x(a,b)  + " - " + s.x(a,c) + " + "  + s.x(c,bestD) + " = " + (s.x(b) + s.x(b,c) + s.x(c) + s.x(a,b) - s.x(a,c) + s.x(c,bestD)));
							result.add(new Paw_Inequality(s, a, b, c, bestD));
						}
					}
				}
			}
				
		
		return result;
	}

}
