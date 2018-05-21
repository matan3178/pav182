package bgu.cs.absint.soot;

import soot.Local;
import soot.Value;
import soot.jimple.*;
import soot.jimple.internal.*;

/**
 * Substitutes a local variable for another in an expression.
 * 
 * @author romanm
 * 
 */
public class LocalSubstituter extends ExprVisitor {
	protected Local from;
	protected Local to;
	protected Expr tmpExpr;
	protected Value tmpVal;

	public Expr substitute(Expr expr, Local from, Local to) {
		this.from = from;
		this.to = to;
		expr.apply(this);
		return tmpExpr;
	}

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
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JAddExpr(op1Value, op2Value);
	}

	@Override
	public void caseAndExpr(AndExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JAndExpr(op1Value, op2Value);
	}

	@Override
	public void caseCmpExpr(CmpExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JCmpExpr(op1Value, op2Value);
	}

	@Override
	public void caseCmpgExpr(CmpgExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JCmpgExpr(op1Value, op2Value);
	}

	@Override
	public void caseCmplExpr(CmplExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JCmplExpr(op1Value, op2Value);
	}

	@Override
	public void caseDivExpr(DivExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JDivExpr(op1Value, op2Value);
	}

	@Override
	public void caseEqExpr(EqExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JEqExpr(op1Value, op2Value);
	}

	@Override
	public void caseNeExpr(NeExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JNeExpr(op1Value, op2Value);
	}

	@Override
	public void caseGeExpr(GeExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JGeExpr(op1Value, op2Value);
	}

	@Override
	public void caseGtExpr(GtExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JGtExpr(op1Value, op2Value);
	}

	@Override
	public void caseLeExpr(LeExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JLeExpr(op1Value, op2Value);
	}

	@Override
	public void caseLtExpr(LtExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JLtExpr(op1Value, op2Value);
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JMulExpr(op1Value, op2Value);
	}

	@Override
	public void caseOrExpr(OrExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JOrExpr(op1Value, op2Value);
	}

	@Override
	public void caseRemExpr(RemExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JRemExpr(op1Value, op2Value);
	}

	@Override
	public void caseShlExpr(ShlExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JShlExpr(op1Value, op2Value);
	}

	@Override
	public void caseShrExpr(ShrExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JShrExpr(op1Value, op2Value);
	}

	@Override
	public void caseUshrExpr(UshrExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JUshrExpr(op1Value, op2Value);
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JSubExpr(op1Value, op2Value);
	}

	@Override
	public void caseXorExpr(XorExpr v) {
		v.getOp1().apply(this);
		Value op1Value = tmpVal;
		v.getOp2().apply(this);
		Value op2Value = tmpVal;
		tmpVal = new JXorExpr(op1Value, op2Value);
	}

	//////////////////////
	// Stopped here!
	//////////////////////
	
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
		if (l.equivTo(from))
			tmpVal = to;
	}
}