package bgu.cs.absint;

/**
 * An interface that states can implement to denote that a possible error has
 * been detected by an analysis.
 * 
 * @author romanm
 */
public interface ErrorState {
	/**
	 * Returns the error message associated with the error state.
	 */
	public String getMessages();
}