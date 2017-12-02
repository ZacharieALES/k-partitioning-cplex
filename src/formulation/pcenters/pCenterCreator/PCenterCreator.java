package formulation.pcenters.pCenterCreator;

import java.io.IOException;

import formulation.pcenters.InvalidPCenterInputFile;
import formulation.pcenters.PCenter;
import formulation.pcenters.PCenterIndexedDistancesParam;

public abstract class PCenterCreator {
	public abstract PCenter<?> createFormulationObject(PCenterIndexedDistancesParam param) throws IOException, InvalidPCenterInputFile;
	public abstract PCenter<?> createFormulationObject(double[][] currentD, PCenterIndexedDistancesParam param, int p) throws Exception;
	public abstract String getMethodName();
}
