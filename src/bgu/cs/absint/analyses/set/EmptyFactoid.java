package bgu.cs.absint.analyses.set;

import soot.Local;

/**
 * A factoid representing the fact that a given set (referenced by a variable)
 * is empty
 * 
 * @author romanm
 * 
 */
public class EmptyFactoid extends SetFactoid {
	public final Local var;

	public EmptyFactoid(Local var) {
		this.var = var;
		assert var != null : "Set must not be null";
	}

	@Override
	public String toString() {
		return var + "=empty";
	}
}