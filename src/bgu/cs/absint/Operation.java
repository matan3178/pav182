package bgu.cs.absint;

import java.util.ArrayList;
import java.util.List;

/**
 * The super-class of all abstract operations that may appear on the right-hand
 * side of an {@link Equation}.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public abstract class Operation<StateType> {
	protected ArrayList<StateType> multipleArgs = new ArrayList<>(3);

	public Operation() {
	}

	/**
	 * The number of input arguments.
	 * 
	 * @return A non-negative number.
	 */
	public abstract byte arity();

	/**
	 * Applies the operation to the given input states.
	 * 
	 * @param input
	 *            An array of states.
	 * @return The result of applying the operation to {@link inputs}.
	 */
	public StateType apply(List<StateType> inputs) {
		assert inputs != null;
		switch (inputs.size()) {
		case 0:
			return apply();
		case 1:
			StateType input0 = inputs.get(0);
			assert input0 != null;
			return apply(input0);
		case 2:
			StateType input1 = inputs.get(0);
			StateType input2 = inputs.get(1);
			assert input1 != null && input2 != null;
			return apply(input1, input2);
		default:
			throw new UnsupportedOperationException();
		}
	}

	public StateType apply() {
		assert arity() == 0;
		throw new UnsupportedOperationException();
	}

	public StateType apply(StateType input) {
		assert input != null && arity() == 1;
		throw new UnsupportedOperationException();
	}

	public StateType apply(StateType input1, StateType input2) {
		assert input1 != null && input2 != null && arity() == 2;
		throw new UnsupportedOperationException();
	}

	public StateType apply(StateType[] inputs) {
		assert inputs != null && inputs.length == arity();
		multipleArgs.clear();
		for (int i = 0; i < inputs.length; ++i) {
			StateType state = inputs[i];
			assert state != null;
			multipleArgs.set(i, state);
		}
		return apply(multipleArgs);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * Returns a human-readable representation of the operation for the given
	 * argument variables.
	 * 
	 * @param args
	 *            Variables.
	 * @return a human-readable representation of the operation for the given
	 *         argument variables.
	 */
	public String toString(List<AnalysisVar<StateType>> args) {
		StringBuilder result = new StringBuilder(toString() + "(");
		for (int i = 0; i < args.size(); ++i) {
			result.append(args.get(i));
			if (i < args.size() - 1)
				result.append(", ");
		}
		result.append(")");
		return result.toString();
	}
}