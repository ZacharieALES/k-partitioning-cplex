package formulation.pcenters.pCenterCreator;

import java.io.IOException;

import formulation.pcenters.InvalidPCenterInputFile;
import formulation.pcenters.PCSC;
import formulation.pcenters.PCenter;
import formulation.pcenters.PCenterIndexedDistancesParam;

public class PCSCCreator extends PCenterCreator{

	@Override
	public PCenter<?> createFormulationObject(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {
		return new PCSC(param);
	}

	@Override
	public String getMethodName() {
		return "PCSC";
	}

	@Override
	public PCenter<?> createFormulationObject(double[][] currentD, PCenterIndexedDistancesParam param, int p)
			throws Exception {
		return new PCSC(currentD, param, p);
	}

}
