package bgu.cs.absint.analyses.lin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Local;
import soot.jimple.IntConstant;
import bgu.cs.absint.constructor.ConjunctiveState;

/**
 * A set of varToFactoid of the form {@code x=a*y+b} for local variables 'x' and
 * 'y' and a integer constants 'a' and 'b'. The (symbolic) meaning of a state is
 * given by the conjunction of the varToFactoid.
 * 
 * @author romanm
 * 
 */
public class LinState extends ConjunctiveState<Local, LinFactoid> {
	/**
	 * An immutable bottom element.
	 */
	public static final LinState bottom = new LinState(false) {
		@Override
		public boolean add(LinFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean remove(LinFactoid factoid) {
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
	public static final LinState top = new LinState() {
		@Override
		public boolean add(LinFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean remove(LinFactoid factoid) {
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
	protected final Set<LinFactoid> factoids;

	/**
	 * Constructs a state with an empty set of varToFactoid (top).
	 */
	public LinState() {
		this.factoids = new HashSet<>();
	}

	/**
	 * Constructs a state containing the given set of varToFactoid.
	 */
	public LinState(Collection<LinFactoid> factoids) {
		this.factoids = new HashSet<>(factoids);
	}

	@Override
	public LinState copy() {
		if (this == bottom)
			return bottom;
		else
			return new LinState(this);
	}

	@Override
	public Collection<LinFactoid> getFactoids() {
		if (factoids == null)
			return null;
		else
			return Collections.unmodifiableSet(factoids);
	}

	/**
	 * Returns all varToFactoid of the form {@code x=a*y+c} where 'a' is
	 * different from 0.
	 * 
	 * @return All varToFactoid of the form {@code x=a*y+c} where 'a' is
	 *         different from 0.
	 */
	public Set<LinFactoid> getLinFactoids() {
		HashSet<LinFactoid> result = new HashSet<>();
		for (LinFactoid f : this) {
			if (!f.isConstant())
				result.add(f);
		}
		return result;
	}

	/**
	 * Returns the set of linear varToFactoid with given left-hand side and
	 * variable and right-hand side variable.
	 */
	public LinFactoid getLinFactoid(Local lhs, Local rhs) {
		for (LinFactoid f : this) {
			if (f.lvar.equivTo(lhs) && f.rvar.equivTo(rhs) && f.isLinear())
				return f;
		}
		return null;
	}

	/**
	 * Returns the linear varToFactoid where the left-hand side variable is
	 * give.
	 */
	public Set<LinFactoid> getLinFactoids(Local lhs) {
		HashSet<LinFactoid> result = new HashSet<>();
		for (LinFactoid f : this) {
			if (f.lvar.equivTo(lhs) && f.isLinear())
				result.add(f);
		}
		return result;
	}

	@Override
	public boolean add(LinFactoid factoid) {
		if (factoid.lvar.equivTo(factoid.rvar)
				&& factoid.coefficient.equivTo(IntConstant.v(1))) {
			// Ignore varToFactoid of the form x=1*x+0.
			assert factoid.additive.equivTo(IntConstant.v(0));
			return false;
		} else {
			return factoids.add(factoid);
		}
	}

	public boolean remove(LinFactoid f) {
		return factoids.remove(f);
	}

	@Override
	public boolean removeVar(Local lhs) {
		boolean result = false;
		for (Iterator<LinFactoid> iter = factoids.iterator(); iter.hasNext();) {
			LinFactoid factoid = iter.next();
			if (factoid.hasVar(lhs)) {
				iter.remove();
				result = true;
			}
		}
		return result;
	}

	/**
	 * Returns the set of varToFactoid defining the given variable.
	 * 
	 * @param var
	 *            The variable on the left-hand side of the linear relation.
	 * @return A set of linear relation varToFactoid with their {@code var}
	 *         field equal to {@code var}.
	 */
	public Set<LinFactoid> getFactoids(Local var) {
		HashSet<LinFactoid> result = new HashSet<>();
		for (LinFactoid f : this) {
			if (f.lvar.equivTo(var))
				result.add(f);
		}
		return result;
	}

	/**
	 * Returns all varToFactoid of the form {@code x=0*y+c}.
	 * 
	 * @return All varToFactoid of the form {@code x=0*y+c}.
	 */
	public Set<LinFactoid> getConstantFactoids() {
		HashSet<LinFactoid> result = new HashSet<>();
		for (LinFactoid f : this) {
			if (f.isConstant())
				result.add(f);
		}
		return result;
	}

	/**
	 * Returns the factoid x=c for the given variable, if one exists in this
	 * state.
	 */
	public LinFactoid getConstantFactoid(Local var) {
		for (LinFactoid constantF : getConstantFactoids()) {
			if (constantF.hasVar(var))
				return constantF;
		}
		return null;
	}

	/**
	 * Constructs a state containing the set of varToFactoid in the given input
	 * state.
	 */
	protected LinState(LinState copyFrom) {
		factoids = new HashSet<>(copyFrom.factoids);
	}

	/**
	 * A constructor just for initializing varToFactoid to null.
	 */
	protected LinState(boolean dummy) {
		this.factoids = null;
	}
}