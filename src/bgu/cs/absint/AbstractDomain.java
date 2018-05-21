package bgu.cs.absint;

import java.util.List;

/**
 * Defines a set of operations on the elements of an abstract domain.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 * @param <ActionType>
 *            The implementation type of program statements.
 */
public abstract class AbstractDomain<StateType, ActionType> {
	// //////////////////////////////////////////////////////////////////
	// Cached objects for various abstract operations: each operation is
	// initialized upon the first use and the same object is returned
	// on subsequent uses.

	private ConstantOperation<StateType> cachedBottomOperation = null;
	private ConstantOperation<StateType> cachedTopOperation = null;
	private BinaryOperation<StateType> cachedUBOperation = null;
	private BinaryOperation<StateType> cachedUBLoopOperation = null;
	private BinaryOperation<StateType> cachedWideningOperation = null;
	private BinaryOperation<StateType> cachedNarrowingOperation = null;
	private UnaryOperation<StateType> cachedReductionOperation = null;

	/**
	 * Returns the least element in this abstract domain.
	 * 
	 * @return An abstract state.
	 */
	public abstract StateType getBottom();

	/**
	 * Returns the greatest element in this abstract domain.
	 * 
	 * @return An abstract state.
	 */
	public abstract StateType getTop();

	/**
	 * Returns an upper bound of the argument states, ideally the least upper bound
	 * (join).
	 * 
	 * @param elem11
	 *            An abstract domain element.
	 * @param elem12
	 *            An abstract domain element.
	 * @return An upper bound of {@link elem1} and {@link elem2}.
	 */
	public abstract StateType ub(StateType elem1, StateType elem2);

	/**
	 * Returns an upper bound that is more aggressive upper bound than the one used
	 * inside the loop.
	 * 
	 * @param elem1
	 *            An abstract domain element.
	 * @param elem2
	 *            An abstract domain element.
	 * @return An upper bound of {@link elem1} and {@link elem2}.
	 */
	public StateType ubLoop(StateType elem1, StateType elem2) {
		return ub(elem1, elem2);
	}

	/**
	 * Returns a lower bound of the argument states, ideally the greatest lower
	 * bound (meet).
	 * 
	 * @param elem11
	 *            An abstract domain element.
	 * @param elem12
	 *            An abstract domain element.
	 * @return A lower bound of {@link elem1} and {@link elem2}.
	 */
	public StateType lb(StateType first, StateType second) {
		throw new UnsupportedOperationException("Implement meet for " + getClass() + "!");
	}

	/**
	 * Compares two elements for the order relation defined for this domain.
	 * 
	 * @param first
	 *            An abstract state.
	 * @param second
	 *            An abstract state.
	 * @return true if {@link first} is lower than or equal to {@link second}
	 *         relative to the order relation defined for the corresponding semantic
	 *         domain.
	 */
	public abstract boolean leq(StateType first, StateType second);

	/**
	 * Determines whether one abstract state is strictly lower than another with
	 * respect to the order relation defined for this domain.
	 * 
	 * @param first
	 *            An abstract state.
	 * @param second
	 *            An abstract state.
	 * @return true if {@link first} is strictly lower than {@link second} relative
	 *         to the order relation defined for the corresponding semantic domain.
	 */
	public boolean lt(StateType first, StateType second) {
		boolean leq = leq(first, second);
		boolean geq = leq(second, first);
		return leq && !geq;
	}

	/**
	 * Determines whether one abstract state is greater or equal to another with
	 * respect to the order relation defined for this domain.
	 * 
	 * @param first
	 *            An abstract state.
	 * @param second
	 *            An abstract state.
	 * @return true if {@link first} is greater or equal to {@link second} relative
	 *         to the order relation defined for the corresponding semantic domain.
	 */
	public boolean geq(StateType first, StateType second) {
		return leq(second, first);
	}

	/**
	 * Determines whether one abstract state is strictly greater than another with
	 * respect to the order relation defined for this domain.
	 * 
	 * @param first
	 *            An abstract state.
	 * @param second
	 *            An abstract state.
	 * @return true if {@link first} is strictly greater than {@link second}
	 *         relative to the order relation defined for the corresponding semantic
	 *         domain.
	 */
	public boolean gt(StateType first, StateType second) {
		return lt(second, first);
	}

	/**
	 * Determines whether two elements are equivalent with respect to the order
	 * relation defined for this domain. That is, 'x' is less than or equal to 'y'
	 * and vice verse.
	 * 
	 * @param first
	 *            An abstract state.
	 * @param second
	 *            An abstract state.
	 * @return true if {@link first} is greater or equal to {@link second} and vice
	 *         verse relative to the order relation defined for the corresponding
	 *         semantic domain.
	 */
	public boolean eq(StateType first, StateType second) {
		if (first == second)
			return true;
		boolean leq = leq(first, second);
		boolean geq = leq(second, first);
		return leq && geq;
	}

	/**
	 * Checks whether two elements are comparable with respect to the order relation
	 * defined for this domain.
	 * 
	 * @param first
	 *            An abstract domain element.
	 * @param second
	 *            An abstract domain element.
	 * @return true if one element is less than or equal to the other.
	 */
	public boolean comparable(StateType first, StateType second) {
		return leq(first, second) || leq(second, first);
	}

	/**
	 * Returns an upper bound of two abstract states that ensures a bound on the
	 * size of increasing chains combined with this operator.
	 * 
	 * @param elem1
	 *            An abstract domain element.
	 * @param elem2
	 *            An abstract domain element.
	 * @return The widening of {@link elem1} (first iterate) {@link elem2} (second
	 *         iterate). Notice that this does not ensure termination in general.
	 */
	public StateType widen(StateType elem1, StateType elem2) {
		return ub(elem1, elem2);
	}

	/**
	 * Returns an element between the two given elements that ensures a bound on the
	 * size of decreasing chains combined with this operator.
	 * 
	 * @param elem1
	 *            An abstract domain element.
	 * @param elem2
	 *            An abstract domain element.
	 * @return The default implementation yields the lower bound of the two
	 *         elements. Notice that this does not ensure termination in general.
	 */
	public StateType narrow(StateType elem1, StateType elem2) {
		return lb(elem1, elem2);
	}

	/**
	 * Reduce the input element to a more refined element.
	 * 
	 * @param input
	 *            An abstract element.
	 * @return An abstract element that is less or equal to the input element and
	 *         has the same meaning.
	 */
	public StateType reduce(StateType input) {
		return input;
	}

	/**
	 * Returns the abstract transformer for a given action.
	 * 
	 * @param action
	 *            An action in the language targeted by the intended analysis.
	 * @return The corresponding abstract transformer.
	 */
	public abstract UnaryOperation<StateType> getTransformer(ActionType action);

	/**
	 * Returns a constant operation that always returns the bottom element.
	 */
	public ConstantOperation<StateType> getBottomOperation() {
		if (cachedBottomOperation == null)
			cachedBottomOperation = new ConstantOperation<StateType>(getBottom());
		return cachedBottomOperation;
	}

	/**
	 * Returns a constant operation that always returns the top element.
	 */
	public ConstantOperation<StateType> getTopOperation() {
		if (cachedTopOperation == null)
			cachedTopOperation = new ConstantOperation<StateType>(getTop());
		return cachedTopOperation;
	}

	/**
	 * Returns an upper bound operation appropriate for loop heads. Override this
	 * method to return a more aggressive upper bound than the one used inside the
	 * loop.
	 * 
	 * @return The default upper bound operation.
	 */
	public BinaryOperation<StateType> getUBLoopOperation() {
		if (cachedUBLoopOperation == null)
			cachedUBLoopOperation = new BinaryOperation<StateType>() {
				@Override
				public StateType apply(StateType first, StateType second) {
					assert first != null && second != null;
					return ubLoop(first, second);
				}

				@Override
				public StateType apply(List<StateType> inputs) {
					assert inputs.size() == 2;
					assert inputs.get(0) != null && inputs.get(1) != null;
					return ubLoop(inputs.get(0), inputs.get(1));
				}

				@Override
				public String toString() {
					return "JoinLoop_" + AbstractDomain.this.getClass().getSimpleName();
				}
			};
		return cachedUBLoopOperation;
	}

	/**
	 * The upper bound operation for this domain.
	 * 
	 * @return A binary operation.
	 */
	public BinaryOperation<StateType> getUBOperation() {
		if (cachedUBOperation == null)
			cachedUBOperation = new BinaryOperation<StateType>() {
				@Override
				public StateType apply(StateType first, StateType second) {
					assert first != null && second != null;
					return ub(first, second);
				}

				@Override
				public StateType apply(List<StateType> inputs) {
					assert inputs.size() == 2;
					assert inputs.get(0) != null && inputs.get(1) != null;
					return ub(inputs.get(0), inputs.get(1));
				}

				@Override
				public String toString() {
					return "Join_" + AbstractDomain.this.getClass().getSimpleName();
				}
			};
		return cachedUBOperation;
	}

	/**
	 * An operation that applies the upper bound operator to a sequence of input
	 * states.
	 * 
	 * @param size
	 *            The number of input states that need to be joined.
	 * @return An upper bound of the given input states, relative to a corresponding
	 *         order relation.
	 */
	public Operation<StateType> getMultiUBOperation(final byte size) {
		assert size >= 2;
		return new Operation<StateType>() {

			@Override
			public StateType apply(List<StateType> inputs) {
				StateType result = inputs.get(0);
				for (int i = 1; i < inputs.size(); ++i) {
					StateType currState = inputs.get(i);
					result = ub(result, currState);
				}
				return result;
			}

			@Override
			public String toString() {
				return "MultiJoin_" + AbstractDomain.this.getClass().getSimpleName();
			}

			@Override
			public byte arity() {
				return size;
			}
		};
	}

	/**
	 * An operation that applies the upper bound operator to a sequence of input
	 * states at a loop head.
	 * 
	 * @param size
	 *            The number of input states that need to be joined.
	 * @return An upper bound of the given input states, relative to a corresponding
	 *         order relation.
	 */
	public Operation<StateType> getMultiUBLoopOperation(final byte size) {
		assert size >= 2;
		return new Operation<StateType>() {

			@Override
			public StateType apply(List<StateType> inputs) {
				StateType result = inputs.get(0);
				for (int i = 1; i < inputs.size(); ++i) {
					StateType currState = inputs.get(i);
					result = ubLoop(result, currState);
				}
				return result;
			}

			@Override
			public String toString() {
				return "MultiJoinLoop_" + AbstractDomain.this.getClass().getSimpleName();
			}

			@Override
			public byte arity() {
				return size;
			}
		};
	}

	/**
	 * A widening operation for this abstract domain.<br>
	 * Override this method to return a widening operation object with a state
	 * (e.g., a counter may be added to change the behavior of the operator after a
	 * given number of iterations).
	 * 
	 * @return A binary operation on two consecutive iterates.
	 */
	public BinaryOperation<StateType> getWideningOperation() {
		if (cachedWideningOperation == null)
			cachedWideningOperation = new BinaryOperation<StateType>() {
				@Override
				public StateType apply(StateType first, StateType second) {
					assert first != null && second != null;
					return widen(first, second);
				}

				@Override
				public StateType apply(List<StateType> inputs) {
					assert inputs.size() == 2;
					assert inputs.get(0) != null && inputs.get(1) != null;
					return widen(inputs.get(0), inputs.get(1));
				}

				@Override
				public String toString() {
					return "Widen_" + AbstractDomain.this.getClass().getSimpleName();
				}
			};
		return cachedWideningOperation;
	}

	/**
	 * A narrowing operation for this abstract domain.<br>
	 * Override this method to return a narrowing operation object with a state
	 * (e.g., a counter may be added to change the behavior of the operator after a
	 * given number of iterations).
	 * 
	 * 
	 * @return A binary operation on two consecutive iterates.
	 */
	public BinaryOperation<StateType> getNarrowingOperation() {
		if (cachedNarrowingOperation == null)
			cachedNarrowingOperation = new BinaryOperation<StateType>() {
				@Override
				public StateType apply(StateType first, StateType second) {
					assert first != null && second != null;
					return narrow(first, second);
				}

				@Override
				public StateType apply(List<StateType> inputs) {
					assert inputs.size() == 2;
					assert inputs.get(0) != null && inputs.get(1) != null;
					return narrow(inputs.get(0), inputs.get(1));
				}

				@Override
				public String toString() {
					return "Narrow_" + AbstractDomain.this.getClass().getSimpleName();
				}
			};
		return cachedNarrowingOperation;
	}

	/**
	 * An operation that refines an abstract domain element. That is, given an
	 * abstract domain element it returns a lower element with the same meaning.
	 * 
	 * @return The identity operation. Override this method to return a better
	 *         implementation or if the implementation of reduce is identity (for
	 *         better efficiency).
	 */
	public UnaryOperation<StateType> getReductionOperation() {
		if (cachedReductionOperation == null)
			cachedReductionOperation = new UnaryOperation<StateType>() {
				@Override
				public StateType apply(StateType input) {
					assert input != null && arity() == 1;
					return reduce(input);
				}

				@Override
				public String toString() {
					return "Reduce_" + AbstractDomain.this.getClass().getSimpleName();
				}
			};
		return cachedReductionOperation;
	}

	/**
	 * A version of {@link ub} but without the type safety.
	 * 
	 * @param o1
	 *            An object of type StateType.
	 * @param o2
	 *            An object of type StateType.
	 * @return An object of type StateType.
	 */
	@SuppressWarnings("unchecked")
	public final Object unsafeUB(Object o1, Object o2) {
		return ub((StateType) o1, (StateType) o2);
	}

	/**
	 * A version of {@link lb} but without the type safety.
	 * 
	 * @param o1
	 *            An object of type StateType.
	 * @param o2
	 *            An object of type StateType.
	 * @return An object of type StateType.
	 */
	@SuppressWarnings("unchecked")
	public final Object unsafeLB(Object o1, Object o2) {
		return lb((StateType) o1, (StateType) o2);
	}

	/**
	 * A version of {@link widen} but without the type safety.
	 * 
	 * @param o1
	 *            An object of type StateType.
	 * @param o2
	 *            An object of type StateType.
	 * @return An object of type StateType.
	 */
	@SuppressWarnings("unchecked")
	public final Object unsafeWiden(Object o1, Object o2) {
		return widen((StateType) o1, (StateType) o2);
	}

	/**
	 * A version of {@link narrow} but without the type safety.
	 * 
	 * @param o1
	 *            An object of type StateType.
	 * @param o2
	 *            An object of type StateType.
	 * @return An object of type StateType.
	 */
	@SuppressWarnings("unchecked")
	public final Object unsafeNarrow(Object o1, Object o2) {
		return narrow((StateType) o1, (StateType) o2);
	}

	/**
	 * A version of {@link leq} but without the type safety.
	 * 
	 * @param o1
	 *            An object of type StateType.
	 * @param o2
	 *            An object of type StateType.
	 * @return The result of leq.
	 */
	@SuppressWarnings("unchecked")
	public final boolean unsafeLeq(Object o1, Object o2) {
		return leq((StateType) o1, (StateType) o2);
	}

	/**
	 * A version of {@link lt} but without the type safety.
	 * 
	 * @param o1
	 *            An object of type StateType.
	 * @param o2
	 *            An object of type StateType.
	 * @return The result of lt.
	 */
	@SuppressWarnings("unchecked")
	public final boolean unsafeLt(Object o1, Object o2) {
		return lt((StateType) o1, (StateType) o2);
	}

	/**
	 * A version of reduce without the type safet.
	 * 
	 * @param input
	 *            An object of type StateType
	 * @return The refined state.
	 */
	@SuppressWarnings("unchecked")
	public final Object unsafeReduce(Object input) {
		return reduce((StateType) input);
	}
}