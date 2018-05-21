package bgu.cs.absint.analyses.set;

import soot.Local;

/**
 * A factoid representing the fact that a given set (referenced by a variable)
 * is the union of another set (referenced by a variable) and a single object
 * (referenced by a variable) .
 * 
 * @author romanm
 * 
 */
public class UnionFactoid extends SetFactoid {
	public final Local setLhs;
	public final Local setRhs;
	public final Local data;

	public UnionFactoid(Local setLhs, Local setRhs, Local data) {
		this.setLhs = setLhs;
		this.setRhs = setRhs;
		this.data = data;
		assert setLhs != null && setRhs != null && data != null : "Given Sets and data must not be null";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UnionFactoid))
			return false;
		UnionFactoid other = (UnionFactoid) obj;
		return this.setLhs.equivTo(other.setLhs) && this.setRhs.equivTo(other.setRhs) && this.data.equivTo(other.data) ;
	}
	
	@Override
	public boolean hasVar(Local var) {
		assert var != null;
		return this.setLhs.equivTo(var) || this.setRhs.equivTo(var) || this.data.equivTo(var);
	}
	
	@Override
	public String toString() {
		//Union(s,t,d)
		return "Union(" +setLhs.toString() + "," + setRhs.toString() + "," + data.toString() + ")"; 
		//return var.toString() + "=" + setRhs.toString() + "+" + data.toString();
	}
}