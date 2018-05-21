package bgu.cs.absint;

import java.util.List;

/**
 * The identity operation.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public final class IdOperation<StateType> extends UnaryOperation<StateType> {
	/**
	 * The one and only instance of this class.
	 */
	private static IdOperation<?> v = new IdOperation<>();

	/**
	 * Returns an instance of {@link IdOperation}. (Singleton pattern.)
	 */
	@SuppressWarnings("unchecked")
	public static <StateType> IdOperation<StateType> v() {
		return (IdOperation<StateType>) v;
	}

	/**
	 * Use the static method {@link v} to obtain an instance of
	 * {@link IdOperation}.
	 */
	private IdOperation() {
	}

	@Override
	public StateType apply(StateType input) {
		assert input != null;
		return input;
	}

	@Override
	public String toString() {
		return "Id";
	}

	@Override
	public String toString(List<AnalysisVar<StateType>> args) {
		return args.get(0).toString();
	}
}