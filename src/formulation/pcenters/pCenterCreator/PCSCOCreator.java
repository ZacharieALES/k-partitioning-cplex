package formulation.pcenters.pCenterCreator;

import java.io.IOException;

import formulation.pcenters.InvalidPCenterInputFile;
import formulation.pcenters.PCSCOrdered;
import formulation.pcenters.PCenter;
import formulation.pcenters.PCenterIndexedDistancesParam;

public class PCSCOCreator extends PCenterCreator{

	@Override
	public PCenter<?> createFormulationObject(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile {
		return new PCSCOrdered(param);
	}

	@Override
	public String getMethodName() {
		return "PCSCO";
	}

}
