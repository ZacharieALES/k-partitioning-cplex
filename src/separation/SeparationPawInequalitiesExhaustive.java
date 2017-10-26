package separation;

import java.util.ArrayList;

import formulation.interfaces.IFEdgeVNodeV;
import formulation.interfaces.IFormulation;
import ilog.concert.IloException;
import inequality_family.AbstractInequality;
import inequality_family.PawInequality;
import variable.VariableGetter;



public class SeparationPawInequalitiesExhaustive extends AbstractSeparation<IFEdgeVNodeV>{

	public SeparationPawInequalitiesExhaustive(IFEdgeVNodeV formulation, VariableGetter vg) {
		super("Paw", formulation, vg);
		
	}

	@Override
	public ArrayList<AbstractInequality<? extends IFormulation>> separate(){
		// TODO Auto-generated method stub

		ArrayList<AbstractInequality<? extends IFormulation>> result = new ArrayList<>();
		
			for(int b = 3 ;  b < formulation.n(); ++b)
				for(int c = b+1 ; c < formulation.n(); ++c){
					
					double v;
					try {
						v = vg.getValue(formulation.edgeVar(b,c)) + vg.getValue(formulation.nodeVar(b)) + vg.getValue(formulation.nodeVar(c));

					/* The paw inequality can only be violated if x_b,c + x_b + x_c is greater than zero */
					if(v >  0 + eps){
					
						for(int a = 0 ; a < b ; ++a){
							
							double v1;
							try {
								v1 = v + vg.getValue(formulation.edgeVar(a,b)) - vg.getValue(formulation.edgeVar(a,c));

								/* The paw inequality can only be violated if x_a,b + x_b,c - x_a,c + x_b + x_c is greater than one */
								if(v1 >  1 + eps){
									for(int d = 0 ; d < a ; ++d){
										double v2 = v1 + vg.getValue(formulation.edgeVar(c,d));
										if(v2 > 2 + eps){
											result.add(new PawInequality(formulation, a, b, c, d));
										}
									}
									for(int d = a+1 ; d < b ; ++d){
										double v2 = v1 + vg.getValue(formulation.edgeVar(c,d));
										if(v2 > 2 + eps){
											result.add(new PawInequality(formulation, a, b, c, d));
										}
									}
								}
							} catch (IloException e) {
								e.printStackTrace();
							}
						}
					}
					} catch (IloException e1) {
						e1.printStackTrace();
					}
				}
				
		
		return result;
	}

}
