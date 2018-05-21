package bgu.cs.absint.analyses.lin;

import java.util.Set;

import soot.Local;
import soot.jimple.IntConstant;
import bgu.cs.absint.constructor.Factoid;
import bgu.cs.absint.soot.SootFactoid;

/**
 * A constraint of the form {@code x=a*y+b} where {@code x, y} are all distinct
 * variables and {@code a, b} are integer constants.
 * 
 * @author romanm
 */
public class LinFactoid extends SootFactoid {
	public final Local lvar;
	public final Local rvar;
	public final IntConstant coefficient;
	public final IntConstant additive;

	public LinFactoid(Local lvar, Local rvar, IntConstant coefficient,
			IntConstant additive) {
		assert lvar != null && rvar != null && coefficient != null
				&& additive != null;
		this.lvar = lvar;
		this.rvar = rvar;
		this.coefficient = coefficient;
		this.additive = additive;
	}

	/**
	 * Constructs a constant factoid of the form x=0*x+a.
	 * 
	 * @param lvar
	 *            A variable.
	 * @param additive
	 *            An stride constant.
	 */
	public LinFactoid(Local lvar, IntConstant additive) {
		assert lvar != null && additive != null;
		this.lvar = lvar;
		this.rvar = lvar;
		this.coefficient = IntConstant.v(0);
		this.additive = additive;
	}

	/**
	 * Constructs a factoid of the form x=y.
	 * 
	 * @param lvar
	 *            A variable.
	 * @param rvar
	 *            A variable.
	 */
	public LinFactoid(Local lvar, Local rvar) {
		this(lvar, rvar, IntConstant.v(1), IntConstant.v(0));
	}

	public boolean isConstant() {
		return coefficient.equivTo(IntConstant.v(0));
	}

	public boolean isLinear() {
		return !this.isConstant();
	}

	@Override
	public boolean leq(Factoid<Local> other) {
		if (equals(other))
			return true;
		LinFactoid otherLin = (LinFactoid) other;
		if (isConstant() && otherLin.lvar.equivTo(lvar)
				&& otherLin.additive.equivTo(additive))
			return true;
		return false;
	}

	@Override
	public boolean hasVar(Local var) {
		return lvar.equivTo(var) || rvar.equivTo(var);
	}

	@Override
	public void addVarsTo(Set<Local> c) {
		c.add(lvar);
		c.add(rvar);
	}

	@Override
	public String toString() {
		if (isConstant()) {
			return lvar + "=" + additive;
		} else {
			String coefficientStr = coefficient.equivTo(IntConstant.v(1)) ? ""
					: coefficient.toString() + "*";
			String additiveStrStr = additive.equivTo(IntConstant.v(0)) ? ""
					: "+" + additive.toString();
			return lvar + "=" + coefficientStr + rvar + additiveStrStr;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lvar.hashCode();
		result = prime * result + additive.value;
		result = prime * result + coefficient.value;
		if (coefficient.value != 0)
			result = prime * result + rvar.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		LinFactoid other = (LinFactoid) obj;
		if (!additive.equivTo(other.additive))
			return false;
		if (!coefficient.equivTo(other.coefficient))
			return false;
		if (!lvar.equivTo(other.lvar))
			return false;
		if (!coefficient.equivTo(IntConstant.v(0)) && !rvar.equivTo(other.rvar))
			return false;
		return true;
	}
}