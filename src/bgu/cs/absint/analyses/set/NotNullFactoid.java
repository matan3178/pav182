package bgu.cs.absint.analyses.set;

import soot.Local;

/**
 * A factoid representing the fact that a given set reference is not null.
 * 
 * @author romanm
 * 
 */
public class NotNullFactoid extends SetFactoid {
	public final Local var;

	public NotNullFactoid(Local var) {
		this.var = var;
		assert var != null : "Set must not be null";
	}

	@Override
	public String toString() {
		return var + "!=null";
	}
}