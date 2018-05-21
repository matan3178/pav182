package bgu.cs.absint.analyses.ap;

import java.util.Set;

import soot.Local;
import bgu.cs.absint.constructor.Factoid;
import bgu.cs.absint.soot.SootFactoid;

/**
 * A constraint of the form {@code AP[x,b,s]}, meaning
 * {@code exists k>=0 . x=b*k+s} where {@code x} is a variable and {@code b} and
 * {@code s} are integer constants such that {@code s} is non-negative.
 * 
 * @author romanm
 */
public class APFactoid extends SootFactoid {
	public final Local var;
	public final int base;
	public final int stride;

	public APFactoid(Local var, int base, int stride) {
		assert var != null;
		this.var = var;
		this.base = base;
		this.stride = stride;
	}

	/**
	 * Constructs a constant factoid of the form x=a+[0].
	 * 
	 * @param var
	 *            A variable.
	 * @param val
	 *            The value of the constant.
	 */
	public APFactoid(Local var, int val) {
		assert var != null;
		this.var = var;
		this.base = val;
		this.stride = 0;
	}

	public boolean isConstant() {
		return stride == 0;
	}

	public boolean isInterval() {
		return stride == 1;
	}

	/**
	 * Returns true if the set of values denoted by this factoid is contained in
	 * the set of values denoted by 'other'.
	 */
	@Override
	public boolean leq(Factoid<Local> other) {
		if (equals(other))
			return true;
		APFactoid otherFactoid = (APFactoid) other;

		// Only factoids over the same variables are comparable.
		if (!this.var.equivTo(otherFactoid.var))
			return false;

		if (this.base < otherFactoid.base)
			return false;

		if (this.isConstant()) {
			// Check whether the two varToFactoid denote the same constant
			// value.
			if (otherFactoid.isConstant()) {
				return this.base == otherFactoid.base;
			}

			assert otherFactoid.stride != 0;
			// exists k. b1=b2+s2*k
			if ((otherFactoid.base - this.base) % otherFactoid.stride == 0)
				return true;
			else
				return false;
		} else if (otherFactoid.isConstant()) {
			return false;
		} else {
			return ((otherFactoid.base - this.base) % otherFactoid.stride == 0)
					&& (otherFactoid.stride % this.stride == 0);
		}
	}

	@Override
	public boolean hasVar(Local var) {
		return this.var.equivTo(var);
	}

	@Override
	public void addVarsTo(Set<Local> c) {
		c.add(var);
	}

	@Override
	public String toString() {
		if (isConstant()) {
			return var + "=" + base;
		} else {
			if (stride == 1) {
				return var + ">=" + base;
			} else {
				return "AP[" + var + ", " + base + ", " + stride + "]";
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + var.hashCode();
		result = prime * result + stride;
		result = prime * result + base;
		if (base != 0)
			result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		APFactoid other = (APFactoid) obj;
		if (!var.equivTo(other.var))
			return false;
		if (stride != other.stride)
			return false;
		if (base != other.base)
			return false;
		return true;
	}
}