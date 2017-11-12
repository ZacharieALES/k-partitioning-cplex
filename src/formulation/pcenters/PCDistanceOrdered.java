package formulation.pcenters;

import java.io.IOException;
import java.util.TreeSet;

import formulation.pcenters.PCenterIndexedDistancesParam.PCenterReturnType;
import ilog.concert.IloException;

public abstract class PCDistanceOrdered extends PCenter<PCenterIndexedDistancesParam>{

	protected int K;

	/** Ordered value of the existing distances in <d> */
	protected Double[] D;

	public PCDistanceOrdered(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {

		super(param);

		TreeSet<Double> D = new TreeSet<>();

		for(int i = 0 ; i < N ; i++)
			for(int j = 0 ; j < M; j++)
				D.add(d[i][j]);

		this.K = D.size() - 1;

		this.D = new Double[D.size()];
		D.toArray(this.D);

	}

	public int K() {
		return K;
	}
	

	protected boolean clientHasFactoryAtDk(int i, int k) {

		boolean found = false;
		int j = 0;

		while(j < M && !found) {

			if(D[k].equals(d[i][j]))
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

}
