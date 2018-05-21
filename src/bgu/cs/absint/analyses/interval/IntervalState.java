package bgu.cs.absint.analyses.interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import soot.Local;
import soot.jimple.IntConstant;
import soot.jimple.NumericConstant;
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
public class IntervalState extends ConjunctiveState<Local, IntervalFactoid> {
	/**
	 * An immutable bottom element.
	 */
	public static final IntervalState bottom = new IntervalState(false) {
		@Override
		public boolean isConsistent() {
			return true;
		}

		@Override
		public boolean addLBFactoid(Local lhs, NumericConstant rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean addUBFactoid(Local lhs, NumericConstant rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(IntervalFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public String toString() {
			return "false";
		}
	};

	/**
	 * An immutable top element.
	 */
	public static final IntervalState top = new IntervalState() {
		@Override
		public boolean addLBFactoid(Local lhs, NumericConstant rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean addUBFactoid(Local lhs, NumericConstant rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(IntervalFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public String toString() {
			return "true";
		}
	};

	/**
	 * An element is a set of varToFactoid.
	 */
	protected final Set<LBFactoid> lbFactoids;
	protected final Set<UBFactoid> ubFactoids;

	public IntervalState() {
		lbFactoids = new TreeSet<>();
		ubFactoids = new TreeSet<>();
	}

	@Override
	public IntervalState copy() {
		if (this == bottom)
			return bottom;
		else
			return new IntervalState(this);
	}

	public Collection<LBFactoid> getLBFactoids() {
		return lbFactoids;
	}

	public Collection<UBFactoid> getUBFactoids() {
		return ubFactoids;
	}

	public boolean addLBFactoid(Local lhs, NumericConstant rhs) {
		return lbFactoids.add(new LBFactoid(lhs, rhs));
	}

	public boolean addLBFactoid(LBFactoid factoid) {
		return lbFactoids.add(factoid);
	}

	public boolean addUBFactoid(Local lhs, NumericConstant rhs) {
		return ubFactoids.add(new UBFactoid(lhs, rhs));
	}

	public boolean addUBFactoid(UBFactoid factoid) {
		return ubFactoids.add(factoid);
	}

	public NumericConstant getUB(Local lhs) {
		NumericConstant result = null;
		for (Iterator<UBFactoid> iter = ubFactoids.iterator(); iter.hasNext();) {
			UBFactoid factoid = iter.next();
			if (factoid.lhs.equals(lhs)) {
				return factoid.rhs;
			}
		}
		return result;
	}

	public NumericConstant getLB(Local lhs) {
		NumericConstant result = null;
		for (Iterator<LBFactoid> iter = lbFactoids.iterator(); iter.hasNext();) {
			LBFactoid factoid = iter.next();
			if (factoid.lhs.equals(lhs)) {
				return factoid.rhs;
			}
		}
		return result;
	}

	public LBFactoid getLBFactoid(Local lhs) {
		LBFactoid result = null;
		for (Iterator<LBFactoid> iter = lbFactoids.iterator(); iter.hasNext();) {
			LBFactoid factoid = iter.next();
			if (factoid.lhs.equals(lhs)) {
				return factoid;
			}
		}
		return result;
	}

	public UBFactoid getUBFactoid(Local lhs) {
		UBFactoid result = null;
		for (Iterator<UBFactoid> iter = ubFactoids.iterator(); iter.hasNext();) {
			UBFactoid factoid = iter.next();
			if (factoid.lhs.equals(lhs)) {
				return factoid;
			}
		}
		return result;
	}

	/**
	 * Removes any factoid containing the given variable.
	 * 
	 * @param lhs
	 *            The variable to be removed out of the state.
	 */
	@Override
	public boolean removeVar(Local lhs) {
		boolean result = false;
		for (Iterator<LBFactoid> iter = lbFactoids.iterator(); iter.hasNext();) {
			LBFactoid factoid = iter.next();
			if (factoid.lhs.equals(lhs)) {
				iter.remove();
				result = true;
				break;
			}
		}

		for (Iterator<UBFactoid> iter = ubFactoids.iterator(); iter.hasNext();) {
			UBFactoid factoid = iter.next();
			if (factoid.lhs.equals(lhs)) {
				iter.remove();
				result = true;
				break;
			}
		}
		return result;
	}

	@Override
	public boolean add(IntervalFactoid factoid) {
		if (factoid instanceof LBFactoid)
			return addLBFactoid((LBFactoid) factoid);
		else
			return addUBFactoid((UBFactoid) factoid);

	}

	@Override
	public Collection<IntervalFactoid> getFactoids() {
		if (this == bottom)
			return null;

		ArrayList<IntervalFactoid> result = new ArrayList<>();
		result.addAll(lbFactoids);
		result.addAll(ubFactoids);
		return result;
	}

	/**
	 * Returns the set of (lower-bound) varToFactoid such that both lower-bound
	 * and upper-bound match for the corresponding variable.
	 */
	public Collection<IntervalFactoid> getConstantFactoids() {
		if (this == bottom)
			return null;

		ArrayList<IntervalFactoid> result = new ArrayList<>();
		for (LBFactoid lbFactoid : lbFactoids) {
			for (UBFactoid ubFactoid : ubFactoids) {
				if (!lbFactoid.lhs.equivTo(ubFactoid.lhs))
					continue;
				if (ubFactoid.rhs.equivTo(lbFactoid.rhs))
					result.add(lbFactoid);
			}
		}
		return result;
	}

	public boolean isConsistent() {
		for (Local var : getVars()) {
			NumericConstant lb = getLB(var);
			NumericConstant ub = getUB(var);
			if (lb != null && ub != null
					&& ub.lessThan(lb).equivTo(IntConstant.v(1)))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		if (lbFactoids.isEmpty() && ubFactoids.isEmpty())
			return "true";

		StringBuilder result = new StringBuilder("and(");
		Set<Local> vars = getVars();
		int size = vars.size();
		for (Local var : vars) {
			NumericConstant lb = getLB(var);
			NumericConstant ub = getUB(var);
			if (lb == null) {
				result.append(var + "<=" + ub.toString());
			} else if (ub == null) {
				result.append(var + ">=" + lb.toString());
			} else {
				if (lb.equivTo(ub)) {
					result.append(var.toString() + "=" + lb);
				} else {
					result.append(lb.toString() + "<=" + var + "<="
							+ ub.toString());
				}
			}
			--size;
			if (size > 0)
				result.append(", ");
		}
		result.append(")");
		return result.toString();
	}

	protected IntervalState(IntervalState copyFrom) {
		lbFactoids = new TreeSet<>();
		ubFactoids = new TreeSet<>();
		this.lbFactoids.addAll(copyFrom.lbFactoids);
		this.ubFactoids.addAll(copyFrom.ubFactoids);
	}

	/**
	 * A constructor just for initializing varToFactoid to null.
	 */
	protected IntervalState(boolean dummy) {
		this.lbFactoids = null;
		this.ubFactoids = null;
	}
}