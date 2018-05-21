package bgu.cs.absint;

/**
 * A variable that stores a value from a given semantic domain.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public class AnalysisVar<StateType> {
	/**
	 * An abstract state.
	 */
	public StateType value;

	/**
	 * A name to display.
	 */
	protected final String name;

	public AnalysisVar(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}