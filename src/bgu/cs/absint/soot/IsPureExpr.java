package bgu.cs.absint.soot;

import soot.jimple.*;

/**
 * Conservatively checks whether a given expression is pure. An expression is
 * pure if it always returns the same result.
 * 
 * @author romanm
 * 
 */
public class IsPureExpr extends ExprVisitor {
	public static final IsPureExpr v = new IsPureExpr();
	public boolean result;

	public boolean check(Expr expr) {
		result = true;
		expr.apply(this);
		return result;
	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		result = false;
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		result = false;
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		result = false;
	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		result = false;
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
		result = false;
	}

	@Override
	public void caseNewArrayExpr(NewArrayExpr v) {
		result = false;
	}

	@Override
	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		result = false;
	}

	@Override
	public void caseNewExpr(NewExpr v) {
		result = false;
	}

	@Override
	public void caseLengthExpr(LengthExpr v) {
		result = false;
	}

	@Override
	public void caseArrayRef(ArrayRef v) {
		result = false;
	}

	@Override
	public void caseStaticFieldRef(StaticFieldRef v) {
		result = false;
	}

	@Override
	public void caseInstanceFieldRef(InstanceFieldRef v) {
		result = false;
	}
}