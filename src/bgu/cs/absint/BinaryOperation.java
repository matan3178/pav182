package bgu.cs.absint;

/**
 * The super-class of all abstract operations that accept a pair of abstract
 * states and return a single abstract state.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public abstract class BinaryOperation<StateType> extends Operation<StateType> {
	@Override
	public final byte arity() {
		return 2;
	}
}