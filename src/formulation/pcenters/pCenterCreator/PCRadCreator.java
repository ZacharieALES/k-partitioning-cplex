package formulation.pcenters.pCenterCreator;

import java.io.IOException;

import formulation.pcenters.InvalidPCenterInputFile;
import formulation.pcenters.PCRadiusIndex;
import formulation.pcenters.PCenter;
import formulation.pcenters.PCenterIndexedDistancesParam;

public class PCRadCreator extends PCenterCreator{

	@Override
	public PCenter<?> createFormulationObject(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {
		return new PCRadiusIndex(param);
	}

	@Override
	public String getMethodName() {
		return "PCRad";
	}

	@Override
	public PCenter<?> createFormulationObject(double[][] d, PCenterIndexedDistancesParam param, int p) throws Exception {
		return new PCRadiusIndex(d, param, p);
	}

}
