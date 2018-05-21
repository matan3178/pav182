package bgu.cs.absint.soot;

import java.util.Set;

import soot.Local;
import soot.jimple.Expr;

/**
 * A visitor for adding all locals within an expression to a given set.
 * 
 * @author romanm
 * 
 */
public class LocalsInExpr extends ExprVisitor {
	public static final LocalsInExpr v = new LocalsInExpr();
	public Set<Local> c;

	public void get(Expr expr, Set<Local> c) {
		this.c = c;
		expr.apply(this);
	}

	@Override
	public void caseLocal(Local l) {
		c.add(l);
	}
}