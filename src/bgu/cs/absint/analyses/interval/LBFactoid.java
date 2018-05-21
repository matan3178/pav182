package bgu.cs.absint.analyses.interval;

import soot.Local;
import soot.jimple.NumericConstant;

/**
 * A basic fact of the form c <= x for a variable 'x' and a numeric constant
 * 'c'.
 * 
 * @author romanm
 */
public class LBFactoid extends IntervalFactoid {
	public LBFactoid(Local lhs, NumericConstant rhs) {
		super(lhs, rhs);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LBFactoid) {
			LBFactoid other = (LBFactoid) obj;
			return this.lhs == other.lhs && this.rhs == other.rhs;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return lhs + ">=" + rhs;
	}
}