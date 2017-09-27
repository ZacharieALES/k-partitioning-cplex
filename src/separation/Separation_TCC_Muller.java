package separation;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import inequality_family.Abstract_Inequality;
import inequality_family.Range;
import inequality_family.TCC_Inequality;

import java.util.ArrayList;
import java.util.HashMap;

import solution.Solution_Representative;

import cut_callback.Abstract_CutCallback;
import formulation.PartitionWithRepresentative;


/**
 * Find an unsatisfied inequality in the shape of: x(C) - x(Cb) <= p
 * 	- C : a cycle
 * 	- Cb : two-chorded cycle
 * 	- |C| = 2p + 1
 * 
 * !WARNING!: The separation algorithm in fact separate a much larger family of inequalities than the two-chorded cycle inequalities
 * 
 * Algorithm from [Muller95] (On the partial order polytope of a digraph)
 * 
 * It separates any odd sequence of adjacent edges) 
 * From the initial graph G(V,E), we create a directed graph H(Vh, Eh)
 * 
 * For each edge (ij) of E we create in H 
 * 	- 4 nodes : u1_ij, u2_ij, v1_ij and v_2ij
 * 	- 2 arcs : (u1_ij, u2_ij) and (v1_ij, v2_ij) of weight -v_edge[i][j]
 * 
 * For each pair of edge which have a common endnode (ij) (ik), we create
 * 	- 4 arcs (u2_ij, v1_ik), (v2_ij, u1_ik), (u2_ik, v1_ij) and (v2_ik, u1_ij) of weight v_edge[j][k] + 0.5
 * 
 * The separation algorithm consists in finding the shortest path in H between u1_ij and v1_ij, for all (ij) in E.

 * The shortest path in H for each pair of nodes in Vh is obtained thanks to Floyd–Warshall algorithm:
			let dist be a |Vh| × |Vh| array of minimum distances initialized to infinity
			let next be a |Vh| × |Vh| array of vertex indices initialized to null
			
			procedure FloydWarshallWithPathReconstruction ()
				for each vertex v in Vh
				   dist[v][v] = 0
				for each arc (u,v) in Eh
				   dist[u][v] = w(u,v)  // the weight of the arc (u,v)
				for k from 1 to |Vh|
				   for i from 1 to |Vh|
				      for j from 1 to |Vh|
				         if dist[i][k] + dist[k][j] < dist[i][j] then
				            dist[i][j] = dist[i][k] + dist[k][j]
				            next[i][j] = k
            
 * The shortest path between two nodes i and j is then obtained via:
 		   function Path (i, j)
			   if dist[i][j] == infinity then
			     return "no path"
			   var intermediate = next[i][j]
			   if intermediate == null then
			     return " "   // the direct arc from i to j gives the shortest path
			   else
	     		 return Path(i, intermediate) + intermediate + Path(intermediate, j)
 * @author zach
 *
 */
public class Separation_TCC_Muller extends Abstract_Separation{

	public Separation_TCC_Muller(Solution_Representative ucc) {
		super("TCC_Muller", ucc);
	}

	/* Hashmap which associates for each vertex id u1_ij in H its two corresponding nodes i and j in G */
	HashMap<Integer, Integer[]> idToNodes = new HashMap<Integer, Integer[]>();
	
	private double[][] dist;
	private int[][] next;
	
	@Override
	public ArrayList<Abstract_Inequality> separate(){

		ArrayList<Abstract_Inequality> result = new ArrayList<Abstract_Inequality>();
		
		try{
		/* Number of nodes in the graph H */
		int n2 = 4*s.n()*(s.n()-1);
		
		dist = new double[n2][n2];
		next = new int[n2][n2];
		
		/* Get all the shortest path via Floyd Warshall Algorithm */
		
		/* Initialize all the weights to infinity */
		for(int i = 0 ; i < n2 ; ++i)
			for(int j = 0 ; j < n2 ; ++j){
				dist[i][j] = Integer.MAX_VALUE;
				dist[j][i] = Integer.MAX_VALUE;
				next[i][j] = -1;
				next[j][i] = -1; 
			}
		
		for(int i = 0 ; i < n2 ; i++)
			dist[i][i] = 0.0;
		
		/* For each edge (ij) of G */
		for(int i = 0 ; i< s.n()-1 ; i++)
			for(int j = i+1 ; j < s.n() ; j++){
				
				Integer[] nodes = new Integer[2];
				nodes[0] = i;
				nodes[1] = j;
				
				/* Get the id of u1_ij */
				Integer id_ij = id(i,j);
				idToNodes.put(id_ij, nodes);
				
				/* (u1_ij,u2_ij) and (v1_ij,v2_ij) weight is -v_edge[i][j] */
				dist[id_ij  ][id_ij+1] = - s.x(i,j) + eps;
				dist[id_ij+2][id_ij+3] = - s.x(i,j) + eps;
//					dist[id_ij+1][id_ij  ] = dist[id_ij  ][id_ij+1];
//					dist[id_ij+3][id_ij+2] = dist[id_ij+2][id_ij+3];
				
				/* 
				 * For all node different from i and j
				 */
				for(int k = 0 ; k < s.n() ; k++)
					
					if(k!=i && k!=j){

						int id_ik = id(i,k);
						int id_jk = id(j,k);
						
						/* (u2_ij, v1_ik) and (v2_ij,u1_ik) weight is v_edge[i][k]+0.5 */
						dist[id_ij+1][id_ik+2] = s.x(j,k) + 0.5 + eps;
						dist[id_ij+3][id_ik  ] = s.x(j,k) + 0.5 + eps;
//							dist[id_ik+2][id_ij+1] = dist[id_ij+1][id_ik+2];
//							dist[id_ik  ][id_ij+3] = dist[id_ij+3][id_ik  ];
						
						/* (u2_ij, v1_jk) and (v2_ij,u1_jk) weight is v_edge[j][k]+0.5 */
						dist[id_ij+1][id_jk+2] = s.x(i,k) + 0.5 + eps;
						dist[id_ij+3][id_jk  ] = s.x(i,k) + 0.5 + eps;
//							dist[id_jk+2][id_ij+1] = dist[id_ij+1][id_jk+2];
//							dist[id_jk  ][id_ij+3] = dist[id_ij+3][id_jk  ];
					}
			}
		
		for(int k = 0 ; k < n2 ; ++k)
			for(int i = 0 ; i < n2 ; ++i)
				for(int j = 0 ; j < n2 ; ++j){
					
					double value = dist[i][k]+dist[k][j];
					
					if(value < dist[i][j]){
						dist[i][j] = value;
						next[i][j] = k;
					}
						
				}
		
		/* Get the TC inequalities which are violated */
		ArrayList<ArrayList<Integer>> al_cyclesInH = new ArrayList<ArrayList<Integer>>();
		
		for(int i = 0 ; i < s.n() ; ++i)
			for(int j = i+1 ; j < s.n() ; ++j){
				
				int id_ij = id(i,j);
				
				/* If the inequality is violated
				 * i.e. if dist[u1_ij][v1_ij] < 0.5
				 * i.e. if there is a path in H which goes from u1_ij to v1_ij whose size is lower than 0.5 
				 */
				if(dist[id_ij][id_ij+2] < 0.5-eps){
	
					/* Get the path which correspond to it's inequality
					 * The path contains all the intermediate nodes in the path from id_ij to id_ij+2 */
					ArrayList<Integer> pathInH = this.getPath(id_ij, id_ij+2);
				
					/*
					 * <pathInH> is in the shape of: u2_ij ; v1_x ; v2_x ; u1_y ; u2_y
					 * Except the first edge ij, the other appear twice in the path.
					 * So we remove them.
					 */
					ArrayList<Integer> cycleInH = new ArrayList<Integer>();
					for(int k = 0 ; k < pathInH.size() ; k+=2){
						
						int id_u1 = pathInH.get(k);
						
						/* Get the id which corresponds to u1 (if u1 -> id_u1-0, if u2 -> id_u1-1, if v1 -> id_u1-2 and if v2 ->id_u1-3) */
						id_u1 = id_u1 - (id_u1%4);
						cycleInH.add(id_u1);
						
					}
					
					ArrayList<Integer> orderedCycleInH = orderCycle(cycleInH);
					

					if(!al_cyclesInH.contains(orderedCycleInH)){
						al_cyclesInH.add(orderedCycleInH);
					}
				}
											
			}

		/* If at least one violated inequality have been found */
		if(al_cyclesInH.size() > 0){
			
			for(int i = 0 ; i < al_cyclesInH.size() ; ++i){
				
				/* Convert it in a linear expression and add it to cplex */
				ArrayList<Integer> cycle = al_cyclesInH.get(i);
//IloRange range = rep.cplex.le((cycle.size()-1)/2, convertCycleHToTCCInequality(cycle));
				result.add(getInequality(cycle));

//System.out.println(cycle);	
//System.out.println(range + "\n--");

			}
		}
		}catch(IloException e){e.printStackTrace();}

		return result;
	
	}
	
	/**
	 * For a given edge ij in G, return the id of u1_ij in Eh
	 * @param i First node of V
	 * @param j Second node of V
	 * @return Id of u1_ij
	 */
	private int id(int i, int j){
		
		if(j < i){
			
			int temp = i;
			i = j;
			j = temp;
			
		}
		
		/* Note : if i, j and the table were starting at 1 instead of zero, the relation would be :
		 * id = (i-1) (4n-2i) + 4 (j-i-1) + 1 (since it is equal to sum_k=1_to_i-1 4*(n-k) + 4(j-i-1) + 1)
		 * Since i, j and the table starts at 0, we obtain
		 * - i -> i+1
		 * - j -> j+1
		 * - id -> id - 1
		 * -> i * (4n-2i-2) + 4*(j-i-1)
		 */	
		return i*(4*s.n()-2*i-2)+4*(j-i-1);
		
	}
	
	private ArrayList<Integer> getPath(int i, int j){

		ArrayList<Integer> cycle = new ArrayList<Integer>();
		
		/* If there is a path from <i> to <j> */
		if(dist[i][j] < Integer.MAX_VALUE){
			
			int intermediate = next[i][j];
			
			/* If the shortest path has intermediate */
			if( intermediate != -1){
				
				cycle = getPath(i, intermediate);
				cycle.add(intermediate);
				cycle.addAll(getPath(intermediate, j));
				
			}
			
		}
		else
			System.out.println("error, no path between " + i + " and " + j);
		
		
		return cycle;
		
	}
	
	private Abstract_Inequality getInequality(ArrayList<Integer> input) {
		
		TCC_Inequality result = null;
		
		try {
//System.out.println("Convert " + input);			
			IloLinearNumExpr expr = s.linearNumExpr();
//String s = "";	

			/* 1 - Get three successive nodes in the cycle: previousNode2 -> previousNode -> currentNode */
			Integer[] t1 = idToNodes.get(input.get(input.size()-1));
			Integer[] t2 = idToNodes.get(input.get(0));
	
			/* The current node corresponds to the in input.get(1) which is not in input.get(0) */
			int currentNode = t2[1];
			int previousNode = t2[0];
			int previousNode2 = t1[0];
			
			if(previousNode != t1[1])
				if(previousNode != t1[0]){
					int t = currentNode;
					currentNode = previousNode;
					previousNode = t;
					if(previousNode == t1[0])
						previousNode2 = t1[1];
					
				}
				else
					previousNode2 = t1[1];
	
			expr.addTerm(1.0, s.x_var(previousNode,currentNode));
			expr.addTerm(-1.0, s.x_var(previousNode2,currentNode));
			
//s += "x_" + previousNode + "," + currentNode + " - x_" + previousNode2 + "," + currentNode;
			
			for(int i = 1 ; i < input.size() ; i++){
	
				t2 = idToNodes.get(input.get(i));

// Use this if it is not necessarily a cycle
				
				/* Among {previousNode, currentNode} find the one which is in t2:
				 * - put it in previousNode
				 * - put the other one in previousNode2 */
				if(previousNode == t2[0] || previousNode == t2[1]){
					previousNode2 = currentNode;
				}
				else{
					previousNode2 = previousNode;
					previousNode = currentNode;
				}
// End: Use				
				/* Among t2 find the node which is not equal to previousNode and put it in currentNode */
				if(t2[0] == previousNode)
					currentNode = t2[1];
				else
					currentNode = t2[0];

				expr.addTerm(1.0, s.x_var(previousNode,currentNode));
				expr.addTerm(-1.0, s.x_var(previousNode2,currentNode));
//s += " + x_" + previousNode + "," + currentNode + " - x_"+ previousNode2 + "," + currentNode;
				
			}
			
//			System.out.println(s);

			result = new TCC_Inequality(s, input.size());
			result.C = new ArrayList<Integer>(input);
			result.range = new Range(expr, (input.size()-1)/2);
		
		} catch (IloException e) {
			e.printStackTrace();
		}
		
		return result;
		
	}

	/**
	 * Order a list of integers such that:
	 * - each element keep the two same neighbors (note: element 0 and size-1 are considered as neighbor)
	 * - the first element e1 is the one with the lowest index
	 * - the second element is the neighbor of e1 with the lowest index.
	 * Ex : {3, 5, 2, 1, 6, 4} become {1, 2, 5, 3, 4, 6}
	 * @param input
	 * @return
	 */
	public ArrayList<Integer> orderCycle(ArrayList<Integer> input){
	
		int size = input.size();
		ArrayList<Integer> output = new ArrayList<Integer>();
		
		/* Find the lowest element and its position*/
		int min = input.get(0);
		int minPosition = 0;
				
		for(int k = 1 ; k < size ; ++k){
			int value = input.get(k);
			if(value < min){
				min = value;
				minPosition = k;
			}
		}
		
		/* Get it's orientation */
		int orientation;
		int next = (minPosition+1)%size;
		int previous = (minPosition-1+size)%size;

		if(input.get(previous) > input.get(next))
			orientation = 1;
		else
			orientation = -1;
		
		/* Order according to the minimum element and the orientation */
		for(int k = 0 ; Math.abs(k) < size ; k+=orientation){
			int id = (minPosition + k+size)%size;
			output.add(input.get(id));		
		}
		
		return output;
			
	}
	
}
