package formulation.interfaces;

/**
 * Interface which represents a partitioning formulation in which the number of clusters is constrained
 * @author zach
 *
 */
public interface IFConstrainedNbOfClusters {

	public int maximalNumberOfClusters();
	public int minimalNumberOfClusters();

}
