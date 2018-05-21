package bgu.cs.absint.analyses.zone;

import java.util.Set;

import bgu.cs.absint.soot.SootFactoid;
import soot.IntType;
import soot.Local;
import soot.jimple.IntConstant;
import soot.jimple.internal.JimpleLocal;

/**
 * A basic fact of the form {@code x - y <= c} for two variables 'x' and 'y' and
 * an integer 'c'.
 * 
 * @author ???
 */
public class ZoneFactoid extends SootFactoid {
	/**
	 * An artificial variables used to represent facts of the form
	 * {@code x <= c} as {@code x - V0 <= c}.
	 */
	public static final Local ZERO_VAR = new JimpleLocal("V0", IntType.v());

	public final Local lhs;
	public final Local rhs;
	public final IntConstant bound;

	public ZoneFactoid(Local lhs, Local rhs, IntConstant bound) {
		this.lhs = lhs;
		this.rhs = rhs;
		this.bound = bound;
		assert lhs != null && rhs != null && bound != null;
	}

	public ZoneFactoid(ZoneFactoid o) {
		this.lhs = o.lhs;
		this.rhs = o.rhs;
		this.bound = o.bound;
		assert lhs != null && rhs != null && bound != null;
	}

	@Override
	public boolean hasVar(Local var) {
		assert var != null;
		return this.lhs.equivTo(var) || this.rhs.equivTo(var);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bound.hashCode();
		result = prime * result + lhs.hashCode();
		result = prime * result + rhs.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		ZoneFactoid other = (ZoneFactoid) obj;
		return (this.lhs == other.lhs && this.rhs == other.rhs && this.bound.equivTo(other.bound));
	}

	@Override
	public String toString() {
		return lhs + "-" + rhs + "<=" + bound;
	}

	@Override
	public void addVarsTo(Set<Local> c) {
		c.add(lhs);
		c.add(rhs);
	}

	public boolean imply(ZoneFactoid o) {
			return (rhs == o.rhs && lhs == o.lhs && bound.lessThanOrEqual(o.bound).equals(IntConstant.v(1)));
	}

	public boolean implyNEQ(ZoneFactoid o) {
			return imply(o) && !bound.equals(o.bound);
	}

	public boolean existIn(ZoneState zs) {
		for(ZoneFactoid o : zs.getFactoids()){
			if(rhs == o.rhs && lhs == o.lhs) return true;
		}
		return false;
	}
	
	public boolean ImpliedBy(ZoneState zs) {
		for(ZoneFactoid o : zs.getFactoids()){
			if(o.imply(this)) return true;
		}
		return false;
	}	
	
	public boolean ImpliedByNEQ(ZoneState zs) {
		for(ZoneFactoid o : zs.getFactoids()){
			if(o.implyNEQ(this)) return true;
		}
		return false;
	}
	
	public ZoneFactoid min(ZoneState zs) {
		IntConstant min = this.bound;
		for(ZoneFactoid o : zs.getFactoids()){
			if(rhs.equals(o.rhs) && lhs.equals(o.lhs) && o.bound.lessThan(min).equals(IntConstant.v(1))){
				min = o.bound;
			}
		}
		return new ZoneFactoid(lhs, rhs, min);
	}
	
	public ZoneFactoid max(ZoneState zs) {
		if(!existIn(zs)) return null;
		IntConstant max = bound;
		for(ZoneFactoid o : zs.getFactoids()){
			if(rhs.equals(o.rhs) && lhs.equals(o.lhs) &&		
					(o.bound.greaterThanOrEqual(max).equals(IntConstant.v(1))))	max = o.bound;
		}
		return new ZoneFactoid(lhs, rhs, max);
	}
}