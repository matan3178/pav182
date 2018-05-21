package bgu.cs.absint.analyses.zone;

import soot.Local;
import soot.jimple.IntConstant;
import bgu.cs.absint.constructor.ConjunctiveState;

/**
 * A set of {@link ZoneFactoid}s of the form {@code x - y <= c} for two local
 * variables. The (symbolic) meaning of a state is given by the conjunction of
 * the {@link ZoneFactoid}s.
 * 
 * @author romanm
 * 
 */
public class ZoneState extends ConjunctiveState<Local, ZoneFactoid> {
	/**
	 * An immutable bottom element.
	 */
	public static final ZoneState bottom = new ZoneState(false) {
		@Override
		public boolean addFactoid(Local lhs, Local rhs, IntConstant bound) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(ZoneFactoid factoid) {
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
	public static final ZoneState top = new ZoneState() {
		@Override
		public boolean addFactoid(Local lhs, Local rhs, IntConstant bound) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(ZoneFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}
	};

	public ZoneState() {
		super();
	}

	@Override
	public ZoneState copy() {
		if (this == bottom)
			return bottom;
		else
			return new ZoneState(this);
	}

	public boolean addFactoid(Local lhs, Local rhs, IntConstant bound) {
		// TODO check if there's already a factoid with the same lhs and rhs, and choose
		// minimum bound.
		// also check if rhs=o.lhs and lhs=o.rhs and in this case this is bottom -
		// but leave this for a later calc by the domain
		return factoids.add(new ZoneFactoid(lhs, rhs, bound));
	}

	/**
	 * Checks whether this state is equivalent to bottom. That is, if this bound +
	 * the symmetrical bound <0.
	 */
	public boolean equivToBottom() {
		if (this == bottom)
			return true;
		for (ZoneFactoid f1 : getFactoids()) {
			if (f1.lhs.equals(f1.rhs) && f1.bound.lessThan(IntConstant.v(0)).equals(IntConstant.v(1)))
				return true;
			for (ZoneFactoid f2 : getFactoids()) {
				if (f1.rhs.equals(f2.lhs)) {
					IntConstant add = IntConstant.v(f1.bound.value + f2.bound.value);
					if (f1.lhs.equals(f2.rhs) && add.lessThan(IntConstant.v(0)).equals(IntConstant.v(1))) {
						// its the same two vars in opposite order - the two consts sum should be
						// non-negative for a valid solution.
						return true;
					}
				}
			}
		}
		return false;
	}

	protected ZoneState(boolean dummy) {
		super(false);
	}

	protected ZoneState(ZoneState copyFrom) {
		super(copyFrom.factoids);
	}
}