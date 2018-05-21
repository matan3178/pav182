package bgu.cs.absint;

import java.util.List;

/**
 * A unary operation that operates by composing two given unary operations.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public class ComposedOperation<StateType> extends UnaryOperation<StateType> {
	protected final UnaryOperation<StateType> first;
	protected final UnaryOperation<StateType> second;

	/**
	 * Creates an instance of a {@link ComposedOperation} out of two given
	 * operations.
	 * 
	 * @param first
	 *            The operation applied first.
	 * @param second
	 *            The operation applied second.
	 * @return The composition of {@link first} and {@link second}.
	 */
	public static <StateType> UnaryOperation<StateType> compose(
			UnaryOperation<StateType> first, UnaryOperation<StateType> second) {
		// Optimize away identity operations.
		if (first == IdOperation.v())
			return second;
		if (second == IdOperation.v())
			return first;
		return new ComposedOperation<>(first, second);
	}

	public static <StateType> AssumeTransformer<StateType> compose(
			final AssumeTransformer<StateType> first,
			final UnaryOperation<StateType> second) {
		return new AssumeTransformer<StateType>(first.polarity) {
			protected UnaryOperation<StateType> first;
			protected UnaryOperation<StateType> second;

			@Override
			public StateType apply(StateType input) {
				StateType intermediateState = first.apply(input);
				assert intermediateState != null;
				StateType finalState = second.apply(intermediateState);
				assert finalState != null;
				return finalState;
			}

			@Override
			public String toString() {
				return second.toString() + "(" + first.toString() + ")";
			}
		};
	}

	@Override
	public StateType apply(StateType input) {
		StateType intermediateState = first.apply(input);
		assert intermediateState != null;
		StateType finalState = second.apply(intermediateState);
		assert finalState != null;
		return finalState;
	}

	@Override
	public StateType apply(List<StateType> inputs) {
		assert inputs.size() == 1;
		return apply(inputs.get(0));
	}

	@Override
	public String toString() {
		return second.toString() + "(" + first.toString() + ")";
	}

	@Override
	public String toString(List<AnalysisVar<StateType>> args) {
		return second.toString() + "(" + first.toString(args) + ")";
	}

	/**
	 * Use the static method {@link create} to create a new instance of this
	 * class.
	 * 
	 * @param first
	 *            The operation applied first.
	 * @param second
	 *            The operation applied second.
	 */
	protected ComposedOperation(UnaryOperation<StateType> first,
			UnaryOperation<StateType> second) {
		assert first != null && second != null;
		this.first = first;
		this.second = second;
	}
}