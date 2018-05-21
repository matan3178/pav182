package bgu.cs.absint.analyses.ae;

import java.util.ArrayList;
import java.util.Set;

import soot.Local;
import soot.jimple.Expr;
import bgu.cs.absint.soot.ExprVisitor;
import bgu.cs.absint.soot.LocalsInExpr;
import bgu.cs.absint.soot.SootFactoid;

/**
 * A basic fact of the form x=expr.
 * 
 * @author romanm
 */
public class AEFactoid extends SootFactoid {
	public final Local lhs;
	public final Expr rhs;

	public AEFactoid(Local lhs, Expr rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	/**
	 * Checks whether the factoid contains the given variable as its left-hand
	 * side on in its right-hand side expression.
	 */
	@Override
	public boolean hasVar(final Local var) {
		assert var != null;
		boolean result = false;
		result |= lhs.equivTo(var);
		final ArrayList<Local> exprLocals = new ArrayList<>();
		rhs.apply(new ExprVisitor() {
			@Override
			public void caseLocal(Local v) {
				exprLocals.add(v);
			}
		});
		result |= exprLocals.contains(var);
		return result;
	}

	@Override
	public void addVarsTo(Set<Local> c) {
		c.add(lhs);
		LocalsInExpr.v.get(rhs, c);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lhs.hashCode();
		result = prime * result + rhs.equivHashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		AEFactoid other = (AEFactoid) obj;
		return lhs.equivTo(other.lhs) && rhs.equivTo(other.rhs);
	}

	@Override
	public String toString() {
		return lhs + "=" + rhs;
	}
}