package bgu.cs.absint.analyses.interval;

import java.util.Set;

import soot.Local;
import soot.jimple.NumericConstant;
import bgu.cs.absint.soot.SootFactoid;

/**
 * An inequality of a variable and a constant.
 * 
 * @author romanm
 */
public abstract class IntervalFactoid extends SootFactoid implements
		Comparable<IntervalFactoid> {
	public final Local lhs;
	public final NumericConstant rhs;

	public IntervalFactoid(Local lhs, NumericConstant rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		assert lhs != null && rhs != null;
	}

	@Override
	public boolean hasVar(Local var) {
		assert var != null;
		return this.lhs == var;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lhs.hashCode();
		result = prime * result + rhs.hashCode();
		return result;
	}

	@Override
	public int compareTo(IntervalFactoid other) {
		if (lhs.getNumber() != other.lhs.getNumber()) {
			return other.lhs.getNumber() - lhs.getNumber();
		} else {
			return rhs.toString().compareTo(other.rhs.toString());
		}
	}

	@Override
	public void addVarsTo(Set<Local> c) {
		c.add(lhs);
	}
}