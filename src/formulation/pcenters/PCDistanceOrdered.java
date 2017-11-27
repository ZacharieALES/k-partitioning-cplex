package formulation.pcenters;

import java.io.IOException;
import java.util.TreeSet;

public abstract class PCDistanceOrdered<CurrentParam extends PCenterParam> extends PCenter<CurrentParam>{

	protected int K;

	/** Ordered value of the existing distances in <d> */
	protected Double[] D;

	public PCDistanceOrdered(CurrentParam param) throws IOException, InvalidPCenterInputFile {

		super(param);

		TreeSet<Double> D = new TreeSet<>();

		for(int i = 0 ; i < N ; i++)
			if(!isClientDominated(i))
				for(int j = 0 ; j < M; j++)
					if(!isFactoryDominated(j))
						D.add(d[i][j]);

		this.K = D.size() - 1;

		this.D = new Double[D.size()];
		D.toArray(this.D);

//				System.out.println("\n-- K = " + this.K);
//				for(int i = 0; i < K; ++i)
//					System.out.println("D[" + i + "] = " + this.D[i]);


		//		for(int i = 0 ; i < N ; i++) {
		//			for(int j = 0 ; j < M; j++)
		//				System.out.print(indexOfDistanceInD(d[i][j]) + "," + d[i][j] +"\t");
		//			System.out.println();
		//		}
		//		System.out.println("--");
	}

	public int K() {
		return K;
	}


	protected boolean clientHasFactoryAtDk(int i, int k) {

		boolean found = false;
		int j = 0;

		while(j < M && !found) {

			if(!isFactoryDominated(j) && D[k].equals(d[i][j]))
				found = true;

			j++;

		}

		return found;

	}


	/**
	 * Get the index of a given distance in the array <D>
	 * @param dist The distance for which we want the index
	 * @return The index of <dist> in <D>; or -1 if dist is not int <D>
	 */
	protected int indexOfDistanceInD(double dist) {

		int result = -1;
		int i = 0;

		while(result == -1 && i < D.length) {

			if(D[i] == dist)
				result = i;
			else
				i++;
		}

		return result;

	}

	/**
	 * Find the smallest distance greater than or equal to <dist>
	 * @param dist The lower bound on the sought distance
	 * @return The smallest distance in <D> greater than or equal to <dist>; or -Double.MAX_VALUE if no distance is greater than <dist>
	 */
	protected double lowestDistanceGreaterThan(double dist) {

		double result = -Double.MAX_VALUE;
		int i = 0;

		while(result == -Double.MAX_VALUE && i < D.length)
			if(D[i] >= dist)
				result = D[i];
			else
				++i;

		return result;

	}

}
