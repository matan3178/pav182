package bgu.cs.absint.analyses.cp;

import bgu.cs.absint.soot.ExprVisitor;
import soot.Local;
import soot.jimple.*;

/**
 * A class for evaluating expressions over a given state.
 * 
 * @author romanm
 * 
 */
public class CPExprEval extends ExprVisitor {
	/**
	 * The one and only instance of this this class (singleton pattern).
	 */
	public static final CPExprEval v = new CPExprEval();

	public boolean divByZero;

	protected CPState input;
	protected Constant result;

	public Constant eval(CPState input, Expr rhs) {
		this.input = input;
		this.divByZero = false;
		this.result = null;
		rhs.apply(this);
		return this.result;
	}

	// /////////////////////////////////////////////////////////
	// Expressions
	// /////////////////////////////////////////////////////////

	@Override
	public void caseAddExpr(AddExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		result = i1.add(i2);
	}

	@Override
	public void caseAndExpr(AndExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof ArithmeticConstant;
		assert c2 instanceof ArithmeticConstant;
		ArithmeticConstant i1 = (ArithmeticConstant) c1;
		ArithmeticConstant i2 = (ArithmeticConstant) c2;
		result = i1.and(i2);
	}

	@Override
	public void caseCastExpr(CastExpr v) {
		// Conservatively ignore this case.
		this.result = null;
	}

	@Override
	public void caseCmpExpr(CmpExpr v) {
		// TODO: find out what this expression corresponds to.
		// Conservatively ignore this case.
		this.result = null;
	}

	@Override
	public void caseCmpgExpr(CmpgExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof RealConstant;
		assert c2 instanceof RealConstant;
		RealConstant r1 = (RealConstant) c1;
		RealConstant r2 = (RealConstant) c2;
		result = r1.cmpg(r2);
	}

	@Override
	public void caseCmplExpr(CmplExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof RealConstant;
		assert c2 instanceof RealConstant;
		RealConstant r1 = (RealConstant) c1;
		RealConstant r2 = (RealConstant) c2;
		result = r1.cmpl(r2);
	}

	@Override
	public void caseDivExpr(DivExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		if (i2.equivTo(IntConstant.v(0))) {
			divByZero = true;
			this.result = null;
		} else {
			result = i1.divide(i2);
		}
	}

	@Override
	public void caseDynamicInvokeExpr(DynamicInvokeExpr v) {
		// Conservatively ignore this case.
		this.result = null;
	}

	@Override
	public void caseEqExpr(EqExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		if (c1 instanceof NumericConstant && c2 instanceof NumericConstant) {
			NumericConstant i1 = (NumericConstant) c1;
			NumericConstant i2 = (NumericConstant) c2;
			result = i1.equalEqual(i2);
		}
	}

	@Override
	public void caseGeExpr(GeExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		result = i1.greaterThanOrEqual(i2);
	}

	@Override
	public void caseGtExpr(GtExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		result = i1.greaterThan(i2);
	}

	@Override
	public void caseInstanceOfExpr(InstanceOfExpr v) {
		// Conservatively ignore this case.
		this.result = null;
	}

	@Override
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		// Conservatively ignore this case.
		this.result = null;
	}

	@Override
	public void caseLeExpr(LeExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		result = i1.lessThanOrEqual(i2);
	}

	@Override
	public void caseLengthExpr(LengthExpr v) {
		// Conservatively ignore this case.
		this.result = null;
	}

	@Override
	public void caseLtExpr(LtExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		result = i1.lessThan(i2);
	}

	@Override
	public void caseMulExpr(MulExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		result = i1.multiply(i2);
	}

	@Override
	public void caseNeExpr(NeExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		if (c1 instanceof NumericConstant && c2 instanceof NumericConstant) {
			NumericConstant i1 = (NumericConstant) c1;
			NumericConstant i2 = (NumericConstant) c2;
			result = i1.notEqual(i2);
		}
	}

	@Override
	public void caseNegExpr(NegExpr v) {
		v.getOp().apply(this);
		Constant c1 = this.result;
		this.result = null;
		if (c1 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		result = i1.negate();
	}

	@Override
	public void caseNewArrayExpr(NewArrayExpr v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseNewExpr(NewExpr v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void caseOrExpr(OrExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof ArithmeticConstant;
		assert c2 instanceof ArithmeticConstant;
		ArithmeticConstant i1 = (ArithmeticConstant) c1;
		ArithmeticConstant i2 = (ArithmeticConstant) c2;
		result = i1.or(i2);
	}

	@Override
	public void caseRemExpr(RemExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		if (i2.equivTo(IntConstant.v(0))) {
			divByZero = true;
			this.result = null;
		} else {
			result = i1.remainder(i2);
		}
	}

	@Override
	public void caseShlExpr(ShlExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof ArithmeticConstant;
		assert c2 instanceof ArithmeticConstant;
		ArithmeticConstant i1 = (ArithmeticConstant) c1;
		ArithmeticConstant i2 = (ArithmeticConstant) c2;
		result = i1.shiftLeft(i2);
	}

	@Override
	public void caseShrExpr(ShrExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof ArithmeticConstant;
		assert c2 instanceof ArithmeticConstant;
		ArithmeticConstant i1 = (ArithmeticConstant) c1;
		ArithmeticConstant i2 = (ArithmeticConstant) c2;
		result = i1.shiftRight(i2);
	}

	@Override
	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseSubExpr(SubExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof NumericConstant;
		assert c2 instanceof NumericConstant;
		NumericConstant i1 = (NumericConstant) c1;
		NumericConstant i2 = (NumericConstant) c2;
		result = i1.subtract(i2);
	}

	@Override
	public void caseUshrExpr(UshrExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof ArithmeticConstant;
		assert c2 instanceof ArithmeticConstant;
		ArithmeticConstant i1 = (ArithmeticConstant) c1;
		ArithmeticConstant i2 = (ArithmeticConstant) c2;
		result = i1.unsignedShiftRight(i2);
	}

	@Override
	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseXorExpr(XorExpr v) {
		v.getOp1().apply(this);
		Constant c1 = this.result;
		this.result = null;
		v.getOp2().apply(this);
		Constant c2 = this.result;
		this.result = null;
		if (c1 == null || c2 == null) {
			this.result = null;
			return;
		}

		assert c1 instanceof ArithmeticConstant;
		assert c2 instanceof ArithmeticConstant;
		ArithmeticConstant i1 = (ArithmeticConstant) c1;
		ArithmeticConstant i2 = (ArithmeticConstant) c2;
		result = i1.xor(i2);
	}

	// ////////////////////////////////////
	// Constants
	// ////////////////////////////////////

	@Override
	public void caseDoubleConstant(DoubleConstant v) {
		this.result = v;
	}

	@Override
	public void caseFloatConstant(FloatConstant v) {
		this.result = v;
	}

	@Override
	public void caseIntConstant(IntConstant v) {
		this.result = v;
	}

	@Override
	public void caseLongConstant(LongConstant v) {
		this.result = v;
	}

	@Override
	public void caseNullConstant(NullConstant v) {
		this.result = v;
	}

	@Override
	public void caseStringConstant(StringConstant v) {
		this.result = v;
	}

	@Override
	public void caseClassConstant(ClassConstant v) {
		this.result = v;
	}

	@Override
	public void defaultCase(Object object) {
		this.result = null;
	}

	@Override
	public void caseArrayRef(ArrayRef v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseStaticFieldRef(StaticFieldRef v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseInstanceFieldRef(InstanceFieldRef v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseParameterRef(ParameterRef v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseCaughtExceptionRef(CaughtExceptionRef v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseThisRef(ThisRef v) {
		throw new Error("Unexpected value type: "
				+ v.getClass().getSimpleName());
	}

	@Override
	public void caseLocal(Local l) {
		this.result = input.getConstantForVar(l);
		assert this.result != null;
	}

	// ///////////////////////////////////

	protected CPExprEval() {
	}
}