package bgu.cs.absint;

/**
 * The super-class of all abstract operations that accept a single abstract
 * state and return a single abstract state.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public abstract class UnaryOperation<StateType> extends Operation<StateType> {
	@Override
	public final byte arity() {
		return 1;
	}

	@Override
	public StateType apply(StateType input) {
		assert input != null && arity() == 1;
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	public Object unsafeApply(Object object) {
		return apply((StateType) object);
	}
}