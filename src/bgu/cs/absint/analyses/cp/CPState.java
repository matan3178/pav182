package bgu.cs.absint.analyses.cp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Local;
import soot.jimple.Constant;
import bgu.cs.absint.constructor.ConjunctiveState;

/**
 * A set of varToFactoid of the form {@code x=c} for a local variable 'x' and a
 * constant 'c'. There is at most one factoid for any given variable. The
 * (symbolic) meaning of a state is given by the conjunction of the
 * varToFactoid.
 * 
 * @author romanm
 * 
 */
public class CPState extends ConjunctiveState<Local, CPFactoid> {
	/**
	 * An immutable bottom element.
	 */
	public static final CPState bottom = new CPState(false) {
		@Override
		public boolean addFactoid(Local lhs, Constant rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(CPFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}
	};

	/**
	 * An immutable top element.
	 */
	public static final CPState top = new CPState() {
		@Override
		public boolean addFactoid(Local lhs, Constant rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(CPFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}
	};

	/**
	 * An element is a set of varToFactoid.
	 */
	protected final Set<CPFactoid> factoids;

	public CPState() {
		factoids = new HashSet<>();
	}

	@Override
	public CPState copy() {
		if (this == bottom)
			return bottom;
		else
			return new CPState(this);
	}

	@Override
	public Collection<CPFactoid> getFactoids() {
		return factoids;
	}

	public boolean addFactoid(Local lhs, Constant rhs) {
		assert getConstantForVar(lhs) == null;
		return factoids.add(new CPFactoid(lhs, rhs));
	}

	@Override
	public boolean add(CPFactoid factoid) {
		assert getConstantForVar(factoid.lhs) == null;
		return factoids.add(factoid);
	}

	/**
	 * Returns the constant value associated with the given variable, if there
	 * is one, and null otherwise.
	 * 
	 * @param lhs
	 *            A variable.
	 * @return The constant value associated with {@link lhs} or null if there
	 *         is none.
	 */
	public Constant getConstantForVar(Local lhs) {
		for (CPFactoid factoid : factoids) {
			if (factoid.lhs.equals(lhs))
				return factoid.rhs;
		}
		return null;
	}

	/**
	 * Removes any factoid containing the given variable.
	 * 
	 * @param lhs
	 *            The variable to be removed from the state.
	 */
	@Override
	public boolean removeVar(Local lhs) {
		for (Iterator<CPFactoid> iter = factoids.iterator(); iter.hasNext();) {
			CPFactoid factoid = iter.next();
			if (factoid.hasVar(lhs)) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether this state is equivalent to bottom. That is, if it
	 * contains two facts 'x=a' and 'x=b' where 'a' and 'b' are different
	 * constants.
	 */
	public boolean equivToBottom() {
		if (this == bottom)
			return false;
		boolean result = true;
		for (CPFactoid factoid1 : this) {
			for (CPFactoid factoid2 : this) {
				if (factoid1 != factoid2 && factoid1.lhs.equivTo(factoid2.lhs)) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	protected CPState(CPState copyFrom) {
		factoids = new HashSet<>(copyFrom.factoids);
	}

	/**
	 * A constructor just for initializing varToFactoid to null.
	 */
	protected CPState(boolean dummy) {
		this.factoids = null;
	}
}