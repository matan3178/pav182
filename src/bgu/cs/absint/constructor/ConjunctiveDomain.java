package bgu.cs.absint.constructor;

import java.util.Collection;

import bgu.cs.absint.AbstractDomain;

/**
 * An abstract domain where states are conjunctions of constraints
 * (varToFactoid).
 * 
 * @author romanm
 * 
 * @param <F>
 *            The type of varToFactoid.
 */
public abstract class ConjunctiveDomain<VarType, F extends Factoid<VarType>, StateType extends ConjunctiveState<VarType, F>, ActionType>
		extends AbstractDomain<StateType, ActionType> {
	/**
	 * Returns a mutable version of the top element (an empty set of
	 * varToFactoid).
	 * 
	 * @return A state with an empty set of varToFactoid.
	 */
	public abstract StateType createEmpty();

	@Override
	public StateType ub(StateType elem1, StateType elem2) {
		if (elem1.equals(getBottom())) {
			return elem2;
		} else if (elem2.equals(getBottom())) {
			return elem1;
		} else {
			// Return the set of varToFactoid in the union of the two sets of
			// varToFactoid that are implied by both abstract states.
			StateType result = createEmpty();
			for (F f1 : elem1) {
				if (leq(elem2, f1))
					result.add(f1);
			}
			for (F f2 : elem2) {
				if (leq(elem1, f2))
					result.add(f2);
			}
			return result;
		}
	}

	@Override
	public StateType lb(StateType elem1, StateType elem2) {
		if (elem1.equals(getBottom()) || elem2.equals(getBottom()))
			return getBottom();
		else if (elem1.equals(getTop()))
			return elem2;
		else if (elem2.equals(getTop()))
			return elem1;
		else {
			// Take the union of the varToFactoid in both abstract states and
			// apply
			// reduction.
			StateType result = createEmpty();
			for (F f1 : elem1) {
				result.add(f1);
			}

			for (F f2 : elem2) {
				result.add(f2);
			}
			result = reduce(result);
			return result;
		}
	}

	@Override
	public boolean leq(StateType elem1, StateType elem2) {
		if (elem1.equals(getBottom())) {
			return true;
		} else if (elem2.equals(getBottom())) {
			return false;
		} else {
			// Define the order relation via join: x <= y iff x join y = y.
			StateType ubElem1Elem2 = ub(elem1, elem2);

			// Check that ubElem1Elem2 is leq elem2 by checking that
			// Check that ubElem1Elem2 contains all of the factoids of elem2.
			Collection<F> ubElem1Elem2Factoids = ubElem1Elem2.getFactoids();
			for (F factoid : elem2.getFactoids()) {
				if (!ubElem1Elem2Factoids.contains(factoid))
					return false;
			}
			return true;
		}
	}

	/**
	 * Determines whether a state implies a given factoid. Override this method
	 * to supply a more precise test.
	 * 
	 * @param s
	 *            A conjunction of varToFactoid.
	 * @param f
	 *            A factoid.
	 * @return The current implementation checks whether there exists a factoid
	 *         in the state that implies the given factoid by checking whether
	 *         it is less than or equal to it.
	 */
	public boolean leq(StateType s, F f) {
		for (F factoidInState : s) {
			if (factoidInState.leq(f))
				return true;
		}
		return false;
	}
}