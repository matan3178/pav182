package bgu.cs.absint.analyses.cp;

import java.util.Set;

import soot.Local;
import soot.jimple.Constant;
import bgu.cs.absint.soot.SootFactoid;

/**
 * A basic fact of the form x=c for a variable 'x' and a constant 'c'.
 * 
 * @author romanm
 */
public class CPFactoid extends SootFactoid {
	public final Local lhs;
	public final Constant rhs;

	public CPFactoid(Local lhs, Constant rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		assert lhs != null && rhs != null;
	}

	@Override
	public boolean hasVar(Local var) {
		assert var != null;
		return this.lhs.equivTo(var);
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
	public boolean equals(Object obj) {
		CPFactoid other = (CPFactoid) obj;
		return this.lhs.equivTo(other.lhs) && this.rhs.equivTo(other.rhs);
	}

	@Override
	public String toString() {
		return lhs + "=" + rhs;
	}

	@Override
	public void addVarsTo(Set<Local> c) {
		c.add(lhs);
	}
}