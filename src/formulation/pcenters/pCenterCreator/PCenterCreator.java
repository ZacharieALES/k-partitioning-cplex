package formulation.pcenters.pCenterCreator;

import java.io.IOException;

import formulation.pcenters.InvalidPCenterInputFile;
import formulation.pcenters.PCenter;
import formulation.pcenters.PCenterIndexedDistancesParam;

public abstract class PCenterCreator {
	public abstract PCenter<?> createFormulationObject(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile;
	public abstract String getMethodName();
}
