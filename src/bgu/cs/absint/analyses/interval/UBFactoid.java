package bgu.cs.absint.analyses.interval;

import soot.Local;
import soot.jimple.NumericConstant;

/**
 * A basic fact of the form x <= c for a variable 'x' and a numeric constant
 * 'c'.
 * 
 * @author romanm
 */
public class UBFactoid extends IntervalFactoid {
	public UBFactoid(Local lhs, NumericConstant rhs) {
		super(lhs, rhs);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof UBFactoid) {
			UBFactoid other = (UBFactoid) obj;
			return this.lhs == other.lhs && this.rhs == other.rhs;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return lhs + "<=" + rhs;
	}
}