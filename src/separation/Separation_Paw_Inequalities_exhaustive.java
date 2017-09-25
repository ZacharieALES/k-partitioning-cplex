package separation;

import ilog.concert.IloException;
import inequality_family.Abstract_Inequality;
import inequality_family.Paw_Inequality;

import java.util.ArrayList;

import solution.Solution_Representative;

public class Separation_Paw_Inequalities_exhaustive extends Abstract_Separation{


	public Separation_Paw_Inequalities_exhaustive(Solution_Representative ucc) {
		super("Paw", ucc);
	}

	@Override
	public ArrayList<Abstract_Inequality> separate() throws IloException {
		// TODO Auto-generated method stub

		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
			for(int b = 3 ;  b < s.n(); ++b)
				for(int c = b+1 ; c < s.n(); ++c){
					
					double v = s.x(b,c) + s.x(b) + s.x(c);

					/* The paw inequality can only be violated if x_b,c + x_b + x_c is greater than zero */
					if(v >  0 + eps){
					
						for(int a = 0 ; a < b ; ++a){
							
							double v1 = v + s.x(a,b) - s.x(a,c);
					
							/* The paw inequality can only be violated if x_a,b + x_b,c - x_a,c + x_b + x_c is greater than one */
							if(v1 >  1 + eps){
								for(int d = 0 ; d < a ; ++d){
									double v2 = v1 + s.x(c,d);
									if(v2 > 2 + eps){
										result.add(new Paw_Inequality(s, a, b, c, d));
									}
								}
								for(int d = a+1 ; d < b ; ++d){
									double v2 = v1 + s.x(c,d);
									if(v2 > 2 + eps){
										result.add(new Paw_Inequality(s, a, b, c, d));
									}
								}
							}
						}
					}
				}
				
		
		return result;
	}

}
