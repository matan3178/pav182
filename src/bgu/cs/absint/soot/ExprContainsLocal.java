package bgu.cs.absint.soot;

import soot.Local;
import soot.jimple.Expr;

/**
 * A visitor for checking whether an expression contains a local variable.
 * 
 * @author romanm
 * 
 */
public class ExprContainsLocal extends ExprVisitor {
	public static final ExprContainsLocal v = new ExprContainsLocal();
	public boolean result;

	protected Local l;

	public boolean check(Expr expr, Local l) {
		this.l = l;
		result = false;
		expr.apply(this);
		return result;
	}

	@Override
	public void caseLocal(Local l) {
		result |= l == this.l;
	}
}