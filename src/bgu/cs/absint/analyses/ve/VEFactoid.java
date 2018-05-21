package bgu.cs.absint.analyses.ve;

import java.util.Set;

import soot.Local;
import bgu.cs.absint.soot.SootFactoid;

/**
 * A basic fact of the form x=y for two variables 'x' and 'y'.
 * 
 * @author romanm
 */
public class VEFactoid extends SootFactoid {
	public final Local lhs;
	public final Local rhs;

	public VEFactoid(Local lhs, Local rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		assert lhs != null && rhs != null;
	}

	public Local getOtherVar(Local var) {
		assert hasVar(var);
		if (var.equivTo(lhs))
			return rhs;
		else
			return lhs;
	}

	@Override
	public boolean hasVar(Local var) {
		assert var != null;
		return this.lhs.equivTo(var) || this.rhs.equivTo(var);
	}

	@Override
	public int hashCode() {
		// Since we consider (lhs,c) equal to (c,lhs), we need to compute
		// the same hash code for both. To achieve this, we first sort lhs and
		// c lexicographically and then compute the hash code for the
		// (consistently) ordered pair.
		Local first = lhs.getName().compareTo(rhs.getName()) < 0 ? lhs : rhs;
		Local second = first == lhs ? rhs : lhs;

		final int prime = 31;
		int result = 1;
		result = prime * result + first.hashCode();
		result = prime * result + second.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		VEFactoid other = (VEFactoid) obj;
		if (this.lhs == other.lhs && this.rhs == other.rhs
				|| this.lhs == other.rhs && this.rhs == other.lhs)
			return true;
		return false;
	}

	@Override
	public String toString() {
		return lhs + "=" + rhs;
	}

	@Override
	public void addVarsTo(Set<Local> c) {
		c.add(lhs);
		c.add(rhs);
	}
}