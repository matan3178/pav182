package bgu.cs.absint.constructor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Local;
import bgu.cs.util.StringUtils;

/**
 * A base class for abstract states that have the form of a conjunction of basic
 * facts (subclasses of {@link Factoid}) over program variables.<br>
 * NOTE: as a convention getFactoids() should return null to indicate the bottom
 * state.
 * 
 * @author romanm
 * 
 * @param <VarType>
 *            The implementation type of program variables.
 * @param <F>
 *            The implementation type of factoids.
 */
public abstract class ConjunctiveState<VarType, F extends Factoid<VarType>>
		implements Iterable<F> {
	/**
	 * An element is a set of factoids.
	 */
	public final HashSet<F> factoids;

	/**
	 * Constructs a state with an empty set of factoids.
	 */
	public ConjunctiveState() {
		factoids = new HashSet<>();
	}

	/**
	 * Returns a state with the same set of varToFactoid or the unique bottom
	 * element.
	 */
	public abstract ConjunctiveState<VarType, F> copy();

	/**
	 * Returns a state with an empty set of factoids.
	 */
	@SuppressWarnings({ "unchecked" })
	public ConjunctiveState<VarType, F> empty() {
		try {
			return getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new Error(e);
		}
	}

	/**
	 * Adds a factoid to the set of varToFactoid already in this state.<br>
	 * Note that if the given factoid contradicts any varToFactoid already
	 * contained in this state it is better to switch to bottom instead of
	 * modifying this state.
	 * 
	 * @param factoid
	 *            The varToFactoid to be added.
	 * @return true if the element has changed due to the addition.
	 */
	public boolean add(F factoid) {
		return factoids.add(factoid);
	}

	/**
	 * Removes any factoid containing the given variable.
	 * 
	 * @param lhs
	 *            The variable to be removed from the state.
	 */
	public boolean removeVar(VarType lhs) {
		boolean result = false;
		for (Iterator<F> iter = factoids.iterator(); iter.hasNext();) {
			F factoid = iter.next();
			if (factoid.hasVar(lhs)) {
				iter.remove();
				result = true;
			}
		}
		return result;
	}

	/**
	 * Returns the set of factoids in this state and null if this is the bottom
	 * state.
	 */
	public Collection<F> getFactoids() {
		return factoids;
	}

	public Set<Local> getVars() {
		HashSet<Local> vars = new HashSet<>();
		for (F factoid : getFactoids()) {
			factoid.addVarsTo(vars);
		}
		return vars;
	}

	/**
	 * Returns an iterator over the set of varToFactoid in this set.
	 */
	@Override
	public final Iterator<F> iterator() {
		Collection<F> factoids = getFactoids();
		if (factoids == null)
			return Collections.emptyIterator();
		else
			return getFactoids().iterator();
	}

	@Override
	public String toString() {
		if (getFactoids() == null) {
			return "false";
		} else if (getFactoids().isEmpty()) {
			return "true";
		} else if (getFactoids().size() == 1) {
			return getFactoids().iterator().next().toString();
		} else {
			StringBuilder result = new StringBuilder("and(");
			result.append(StringUtils.toString(getFactoids()));
			result.append(")");
			return result.toString();
		}
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((getFactoids() == null) ? 0 : getFactoids().hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		ConjunctiveState<?, ?> other = (ConjunctiveState<?, ?>) obj;
		if (getFactoids() == null || other.getFactoids() == null) {
			return getFactoids() == null && other.getFactoids() == null;
		} else {
			return bgu.cs.util.Collections.equalSets(getFactoids(),
					other.getFactoids());
		}
	}

	/**
	 * Constructs a state with the given collection of factoids.
	 * 
	 * @param factoids
	 */
	protected ConjunctiveState(Collection<F> factoids) {
		this.factoids = new HashSet<F>(factoids);
	}

	/**
	 * A constructor just for initializing factoids to null, used to represent
	 * the bottom state.
	 */
	protected ConjunctiveState(boolean dummy) {
		this.factoids = null;
	}
}