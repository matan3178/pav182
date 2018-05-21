package bgu.cs.absint;

import java.util.List;

/**
 * An operation that takes no values and always returns the same value.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public final class ConstantOperation<StateType> extends Operation<StateType> {
	protected final StateType value;

	/**
	 * Constructs an operation for the given {@link value}.
	 * 
	 * @param value
	 *            The values that {@link apply} should return.
	 */
	public ConstantOperation(StateType value) {
		this.value = value;
	}

	@Override
	public byte arity() {
		return 0;
	}

	@Override
	public StateType apply(List<StateType> inputs) {
		assert inputs.size() == 0;
		return value;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public String toString(List<AnalysisVar<StateType>> args) {
		return value.toString();
	}
}