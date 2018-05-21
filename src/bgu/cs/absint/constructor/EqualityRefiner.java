package bgu.cs.absint.constructor;

import java.util.Collection;

import bgu.cs.util.Pair;

/**
 * An interface for abstract domains that support reductions by equalities. This
 * is useful for abstract domains participating in a Cartesian product.<br>
 * NOTE: remember that bottom does not return any equalities and cannot be
 * refined --- return null.
 * 
 * @author romanm
 * 
 */
public interface EqualityRefiner<VarType> {
	/**
	 * Refines the given state in-place with the supplied collection of
	 * equalities.
	 * 
	 * @param state
	 *            An abstract domain element. Cast it to the corresponding
	 *            element type.
	 * @param equalities
	 *            A collection of equalities between local variables.
	 * @return The refined state if it is different than the input state and
	 *         null otherwise (no refinement).
	 */
	public Object refineByEqualities(Object state,
			Collection<Pair<VarType, VarType>> equalities);

	/**
	 * Returns the set of equalities between local variables that can be
	 * inferred from the given state.
	 * 
	 * @param state
	 *            An abstract domain element. Cast it to the corresponding
	 *            element type.
	 * @return A collection of equalities between local variables that are
	 *         implied by the given state.
	 */
	public Collection<Pair<VarType, VarType>> inferEqualities(Object state);
}