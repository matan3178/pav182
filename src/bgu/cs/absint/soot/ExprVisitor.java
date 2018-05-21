package bgu.cs.absint.soot;

import soot.Local;
import soot.Value;
import soot.jimple.*;

/**
 * A visitor over Jimple expressions.
 * 
 * @author romanm
 * 
 */
public class ExprVisitor implements JimpleValueSwitch {

	@Override
	public void caseDoubleConstant(DoubleConstant v) {
	}

	@Override
	public void caseFloatConstant(FloatConstant v) {
	}

	@Override
	public void caseIntConstant(IntConstant v) {
	}

	@Override
	public void caseLongConstant(LongConstant v) {
	}

	@Override
	public void caseNullConstant(NullConstant v) {
	}

	@Override
	public void caseStringConstant(StringConstant v) {
	}

	@Override
	public void caseClassConstant(ClassConstant v) {
	}

	@Override
	public void defaultCase(Object object) {
	}

	@Override
	public void caseAddExpr(AddExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseAndExpr(AndExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseCmpExpr(CmpExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseCmpgExpr(CmpgExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseCmplExpr(CmplExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseDivExpr(DivExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseEqExpr(EqExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseNeExpr(NeExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseGeExpr(GeExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseGtExpr(GtExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseLeExpr(LeExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseLtExpr(LtExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseOrExpr(OrExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseRemExpr(RemExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseShlExpr(ShlExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseShrExpr(ShrExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseUshrExpr(UshrExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseXorExpr(XorExpr v) {
		v.getOp1().apply(this);
		v.getOp2().apply(this);
	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		for (Value arg : v.getArgs())
			arg.apply(this);
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		for (Value arg : v.getArgs())
			arg.apply(this);
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		for (Value arg : v.getArgs())
			arg.apply(this);
	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		for (Value arg : v.getArgs())
			arg.apply(this);
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
		for (Value arg : v.getArgs())
			arg.apply(this);
	}

	@Override
	public void caseCastExpr(CastExpr v) {
		v.getOp().apply(this);

	}

	@Override
	public void caseInstanceOfExpr(InstanceOfExpr v) {
		v.getOp().apply(this);
	}

	@Override
	public void caseNewArrayExpr(NewArrayExpr v) {
		v.getSize().apply(this);
	}

	@Override
	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		for (Object s : v.getSizes()) {
			if (s instanceof Value) {
				Value sv = (Value) s;
				sv.apply(this);
			}
		}
	}

	@Override
	public void caseNewExpr(NewExpr v) {
	}

	@Override
	public void caseLengthExpr(LengthExpr v) {
		v.getOp().apply(this);
	}

	@Override
	public void caseNegExpr(NegExpr v) {
		v.getOp().apply(this);
	}

	@Override
	public void caseArrayRef(ArrayRef v) {
		v.getBase().apply(this);
		v.getIndex().apply(this);
	}

	@Override
	public void caseStaticFieldRef(StaticFieldRef v) {
	}

	@Override
	public void caseInstanceFieldRef(InstanceFieldRef v) {
		v.getBase().apply(this);
	}

	@Override
	public void caseParameterRef(ParameterRef v) {
	}

	@Override
	public void caseCaughtExceptionRef(CaughtExceptionRef v) {
	}

	@Override
	public void caseThisRef(ThisRef v) {
	}

	@Override
	public void caseLocal(Local l) {
	}
}