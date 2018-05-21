package bgu.cs.absint.analyses.set;

import soot.Local;

/**
 * A factoid representing the fact that a given set (referenced by a variable)
 * is is equal another set (referenced by a variable)
 * 
 * @author romanm
 * 
 */
public class EqualSetsFactoid extends SetFactoid {
	public final Local setLhs;
	public final Local setRhs;
	
	public EqualSetsFactoid(Local setLhs, Local setRhs) {
		this.setLhs = setLhs;
		this.setRhs = setRhs;
		assert setLhs != null && setRhs != null : "Sets must not be null";
	}

	@Override
	public String toString() {
		return setLhs.toString() + "~" + setRhs.toString();
	}
}