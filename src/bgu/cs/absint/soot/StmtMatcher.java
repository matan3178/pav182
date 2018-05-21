package bgu.cs.absint.soot;

import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.Type;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AddExpr;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.EqExpr;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;

/**
 * Matches statements for various patterns.
 * 
 * @author romanm
 */
public class StmtMatcher extends AbstractStmtSwitch {
	@Override
	public void caseIdentityStmt(IdentityStmt stmt) {
		Local leftOp = (Local) stmt.getLeftOp();
		Value rhs = stmt.getRightOp();
		if (rhs instanceof ParameterRef)
			matchIdentityStmt(stmt, leftOp, (ParameterRef) rhs);
		else if (rhs instanceof ThisRef)
			matchIdentityStmt(stmt, leftOp, (ThisRef) rhs);
		else
			throw new Error(
					"Encountered an unexpected right-hand side of an identity statement: "
							+ stmt);
	}

	@Override
	public void caseIfStmt(IfStmt stmt) {
		throw new Error("You should get to matchAssume instead of this method!");
	}

	@Override
	public void caseInvokeStmt(InvokeStmt stmt) {
	}

	// //////////////////////////////////////////////////////////////////////////
	// Identity statements.
	// //////////////////////////////////////////////////////////////////////////

	public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ParameterRef rhs) {
	}

	public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ThisRef rhs) {
	}

	// //////////////////////////////////////////////////////////////////////////
	// Assignments.
	// //////////////////////////////////////////////////////////////////////////

	@Override
	public void caseAssignStmt(AssignStmt stmt) {
		Value lhs = stmt.getLeftOp();
		if (lhs instanceof Local) {
			matchAssignToLocal(stmt, (Local) stmt.getLeftOp());
		} else if (lhs instanceof FieldRef) {
			matchAssignToFieldRef(stmt, (FieldRef) lhs);
		}
	}

	/**
	 * Matches assignments to local variables.
	 */
	public void matchAssignToLocal(AssignStmt stmt, Local lhs) {
		Value rhs = stmt.getRightOp();
		if (rhs instanceof Local) {
			matchAssignLocalToLocal(stmt, lhs, (Local) rhs);
		} else if (rhs instanceof Constant) {
			matchAssignConstantToLocal(stmt, lhs, (Constant) rhs);
		} else if (rhs instanceof Expr) {
			matchAssignExprToLocal(stmt, lhs, (Expr) rhs);
		} else if (rhs instanceof FieldRef) {
			matchAssignFieldRefToLocal(stmt, lhs, (FieldRef) rhs);
		}
	}

	/**
	 * Matches statements of the form {@code x=y.f} where 'x' is a local
	 * variable.
	 */
	public void matchAssignFieldRefToLocal(AssignStmt stmt, Local lhs,
			FieldRef rhs) {
		if (rhs instanceof InstanceFieldRef) {
			InstanceFieldRef ifref = (InstanceFieldRef) rhs;
			matchAssignInstanceFieldRefToLocal(stmt, lhs,
					(Local) ifref.getBase(), ifref.getField());
		} else {
			assert rhs instanceof StaticFieldRef;
			StaticFieldRef sfref = (StaticFieldRef) rhs;
			matchAssignStaticFieldRefToLocal(stmt, lhs, sfref.getType(),
					sfref.getField());
		}
	}

	/**
	 * Matches statements of the form {@code x=C.f} where 'x' is a local
	 * variable and 'C' is a class type.
	 */
	public void matchAssignStaticFieldRefToLocal(AssignStmt stmt, Local lhs,
			Type rhs, SootField field) {
	}

	/**
	 * Matches statements of the form {@code x=y.f} where 'x' and 'y' are local
	 * variables.
	 */
	public void matchAssignInstanceFieldRefToLocal(AssignStmt stmt, Local lhs,
			Local rhs, SootField field) {
	}

	/**
	 * Matches statements of the form {@code x=c} where 'x' is a local variable
	 * and 'x' is a constant.
	 */
	public void matchAssignConstantToLocal(AssignStmt stmt, Local lhs,
			Constant rhs) {
		if (rhs instanceof NullConstant)
			matchAssignNullToRef(lhs);
	}

	public void matchAssignExprToLocal(AssignStmt stmt, Local lhs, Expr rhs) {
		if (rhs instanceof InvokeExpr) {
			matchAssignInvokeStmt(stmt, lhs);
		} else if (rhs instanceof AddExpr) {
			AddExpr expr = (AddExpr) rhs;
			matchAssignAddExprToLocal(stmt, lhs, expr.getOp1(), expr.getOp2());
		} else if (rhs instanceof SubExpr) {
			SubExpr expr = (SubExpr) rhs;
			matchAssignSubExprToLocal(stmt, lhs, expr.getOp1(), expr.getOp2());
		} else if (rhs instanceof MulExpr) {
			MulExpr expr = (MulExpr) rhs;
			matchAssignMulExprToLocal(stmt, lhs, expr.getOp1(), expr.getOp2());
		} else if (rhs instanceof NewExpr) {
			NewExpr expr = (NewExpr) rhs;
			matchAssignNewExprToLocal(stmt, lhs, expr.getBaseType());
		}
	}

	public void matchAssignNewExprToLocal(AssignStmt stmt, Local lhs,
			RefType baseType) {
	}

	public void matchAssignInvokeStmt(AssignStmt stmt, Local lhs) {
	}

	public void matchAssignLocalToLocal(AssignStmt stmt, Local lhs, Local rhs) {
		if (lhs.getType() instanceof RefType
				&& rhs.getType() instanceof RefType) {
			matchAssignRefToRef(lhs, rhs);
		}
	}

	public void matchAssignAddExprToLocal(AssignStmt stmt, Local lhs,
			Value op1, Value op2) {
		if (op1 instanceof Local && op2 instanceof Local) {
			Local op1Local = (Local) op1;
			Local op2Local = (Local) op2;
			matchAssignAddLocalLocalToLocal(stmt, lhs, op1Local, op2Local);
		} else if (op1 instanceof Local && op2 instanceof Constant) {
			Local op1Local = (Local) op1;
			Constant op2Constant = (Constant) op2;
			matchAssignAddLocalConstantToLocal(stmt, lhs, op1Local, op2Constant);
		} else if (op2 instanceof Local && op1 instanceof Constant) {
			Local op2Local = (Local) op2;
			Constant op1Constant = (Constant) op1;
			matchAssignAddLocalConstantToLocal(stmt, lhs, op2Local, op1Constant);
		}
	}

	public void matchAssignSubExprToLocal(AssignStmt stmt, Local lhs,
			Value op1, Value op2) {
		if (op1 instanceof Local && op2 instanceof Local) {
			Local op1Local = (Local) op1;
			Local op2Local = (Local) op2;
			matchAssignSubLocalLocalToLocal(stmt, lhs, op1Local, op2Local);
		} else if (op1 instanceof Local && op2 instanceof Constant) {
			Local op1Local = (Local) op1;
			Constant op2Constant = (Constant) op2;
			matchAssignSubLocalConstantToLocal(stmt, lhs, op1Local, op2Constant);
		} else if (op2 instanceof Local && op1 instanceof Constant) {
			Local op2Local = (Local) op2;
			Constant op1Constant = (Constant) op1;
			matchAssignSubConstantLocalToLocal(stmt, lhs, op2Local, op1Constant);
		}
	}

	public void matchAssignMulExprToLocal(AssignStmt stmt, Local lhs,
			Value op1, Value op2) {
		if (op1 instanceof Local && op2 instanceof Local) {
			Local op1Local = (Local) op1;
			Local op2Local = (Local) op2;
			matchAssignMulLocalLocalToLocal(stmt, lhs, op1Local, op2Local);
		} else if (op1 instanceof Local && op2 instanceof Constant) {
			Local op1Local = (Local) op1;
			Constant op2Constant = (Constant) op2;
			matchAssignMulLocalConstantToLocal(stmt, lhs, op1Local, op2Constant);
		} else if (op2 instanceof Local && op1 instanceof Constant) {
			Local op2Local = (Local) op2;
			Constant op1Constant = (Constant) op1;
			matchAssignMulLocalConstantToLocal(stmt, lhs, op2Local, op1Constant);
		}
	}

	/**
	 * Matches statements of the form {@code x=y+c} and {@code x=c+y}.
	 */
	public void matchAssignAddLocalConstantToLocal(AssignStmt stmt, Local lhs,
			Local op1, Constant op2) {
	}

	/**
	 * Matches statements of the form {@code x=y+z}.
	 */
	public void matchAssignAddLocalLocalToLocal(AssignStmt stmt, Local lhs,
			Local op1, Local op2) {
	}

	/**
	 * Matches statements of the form {@code x=y-c}.
	 */
	public void matchAssignSubLocalConstantToLocal(AssignStmt stmt, Local lhs,
			Local op1, Constant op2) {
	}

	/**
	 * Matches statements of the form {@code x=c-y}.
	 */
	public void matchAssignSubConstantLocalToLocal(AssignStmt stmt, Local lhs,
			Local op1, Constant op2) {
	}

	/**
	 * Matches statements of the form {@code x=y-z}.
	 */
	public void matchAssignSubLocalLocalToLocal(AssignStmt stmt, Local lhs,
			Local op1, Local op2) {
	}

	/**
	 * Matches statements of the form {@code x=y*c} and {@code x=c*y}.
	 */
	public void matchAssignMulLocalConstantToLocal(AssignStmt stmt, Local lhs,
			Local op1, Constant op2) {
	}

	/**
	 * Matches statements of the form {@code x=y*z}.
	 */
	public void matchAssignMulLocalLocalToLocal(AssignStmt stmt, Local lhs,
			Local op1, Local op2) {
	}

	/**
	 * Matches assignments of the form {@code x=y} where both 'x' and 'y' are
	 * reference variables.
	 */
	public void matchAssignRefToRef(Local lhs, Local rhs) {
	}

	/**
	 * Matches assignments of the form {@code x=null} where 'x' is a reference
	 * variable.
	 */
	public void matchAssignNullToRef(Local lhs) {
	}

	// //////////////////////////////////////////////////////////////////////////
	// Assignments to fields.
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Matches statements of the form {@code X.f = y} where 'X' is either a
	 * class name (for a static field) or a local variable (for an instance
	 * field).
	 */
	public void matchAssignToFieldRef(AssignStmt stmt, FieldRef lhs) {
		SootField field = lhs.getField();

		if (lhs instanceof StaticFieldRef) {
			StaticFieldRef ref = (StaticFieldRef) lhs;
			matchAssignToStaticFieldRef(stmt, ref);
		} else {
			InstanceFieldRef ref = (InstanceFieldRef) lhs;
			Value base = ref.getBase();
			matchAssignToInstanceFieldRef(stmt, base, field);
		}
	}

	/**
	 * Matches statements of the form {@code C.f = y} where 'C' is a class and
	 * 'y' is a local variable.
	 */
	public void matchAssignToStaticFieldRef(AssignStmt stmt, StaticFieldRef lhs) {
	}

	/**
	 * Matches statements of the form {@code x.f = Y} where 'x' is a local
	 * variable.
	 */
	public void matchAssignToInstanceFieldRef(AssignStmt stmt, Value base,
			SootField field) {
		Value rhs = stmt.getRightOp();
		if (rhs instanceof NullConstant) {
			matchAssignNullToInstanceFieldRef((Local) base, field);
		} else if (rhs instanceof Local) {
			matchAssignLocalToInstanceFieldRef((Local) base, field, (Local) rhs);
		}
	}

	/**
	 * Matches statements of the form {@code x.n = y} where 'x' and 'y' are
	 * local variables.
	 */
	public void matchAssignLocalToInstanceFieldRef(Local base, SootField field,
			Local rhs) {
	}

	/**
	 * Matches statements of the form {@code x.n = null} where 'x' is a local
	 * variable.
	 */
	public void matchAssignNullToInstanceFieldRef(Local base, SootField field) {
	}

	// //////////////////////////////////////////////////////////////////////////
	// Conditions.
	// //////////////////////////////////////////////////////////////////////////

	public void matchAssume(Assume unit) {
		IfStmt stmt = unit.stmt;
		boolean polarity = unit.polarity;
		Value condition = stmt.getCondition();
		if (condition instanceof NegExpr) {
			NegExpr negExpr = (NegExpr) condition;
			condition = negExpr.getOp();
			polarity = !polarity;
		}

		if (condition instanceof EqExpr) {
			EqExpr expr = (EqExpr) condition;
			Value op1 = expr.getOp1();
			Value op2 = expr.getOp2();
			if (op1 instanceof Local && op2 instanceof Local) {
				matchAssumeLocalEqLocal(stmt, polarity, (Local) op1,
						(Local) op2);
			} else if (op1 instanceof Local && op2 instanceof Constant) {
				matchAssumeLocalEqConstant(stmt, polarity, (Local) op1,
						(Constant) op2);
			} else if (op2 instanceof Local && op1 instanceof Constant) {
				matchAssumeLocalEqConstant(stmt, polarity, (Local) op2,
						(Constant) op1);
			}
		} else if (condition instanceof NeExpr) {
			NeExpr expr = (NeExpr) condition;
			Value op1 = expr.getOp1();
			Value op2 = expr.getOp2();
			if (op1 instanceof Local && op2 instanceof Local) {
				matchAssumeLocalEqLocal(stmt, !polarity, (Local) op1,
						(Local) op2);
			} else if (op1 instanceof Local && op2 instanceof Constant) {
				matchAssumeLocalEqConstant(stmt, !polarity, (Local) op1,
						(Constant) op2);
			} else if (op2 instanceof Local && op1 instanceof Constant) {
				matchAssumeLocalEqConstant(stmt, !polarity, (Local) op2,
						(Constant) op1);
			}
		} else if (condition instanceof LtExpr) {
			LtExpr expr = (LtExpr) condition;
			Value op1 = expr.getOp1();
			Value op2 = expr.getOp2();
			if (op1 instanceof Local && op2 instanceof Local) {
				matchAssumeLocalLtLocal(stmt, polarity, (Local) op1,
						(Local) op2);
			} else if (op1 instanceof Local && op2 instanceof Constant) {
				matchAssumeLocalLtConstant(stmt, polarity, (Local) op1,
						(Constant) op2);
			}
		}
		// else if (condition instanceof LeExpr) {
		// LeExpr expr = (LeExpr) condition;
		// if (expr.getOp1() instanceof Local
		// && expr.getOp2() instanceof Local) {
		// matchLocalLtLocal(stmt, polarity, (Local) expr.getOp1(),
		// (Local) expr.getOp2());
		// }
		// else if (expr.getOp1() instanceof Local && expr.getOp2() instanceof
		// Constant) {
		// matchLocalLtConstant(stmt, polarity, (Local) expr.getOp1(),
		// (Constant) expr.getOp2());
		// }
		// }
		else if (condition instanceof GtExpr) {
			GtExpr expr = (GtExpr) condition;
			Value op1 = expr.getOp1();
			Value op2 = expr.getOp2();
			if (op1 instanceof Local && op2 instanceof Local) {
				matchAssumeLocalGtLocal(stmt, polarity, (Local) op1,
						(Local) op2);
			} else if (op1 instanceof Local && op2 instanceof Constant) {
				matchAssumeLocalGtConstant(stmt, polarity, (Local) op1,
						(Constant) op2);
			}
		}
	}

	/**
	 * Matches statements of the form x==null.
	 */
	public void matchAssumeLocalEqNull(IfStmt stmt, boolean polarity, Local op1) {
	}

	/**
	 * Matches statements of the form x==y for two local variables 'x' and 'y'.
	 */
	public void matchAssumeLocalEqLocal(IfStmt stmt, boolean polarity,
			Local lhs, Local rhs) {
	}

	/**
	 * Matches statements of the form x==c and c==x for a local variable 'x' and
	 * constant 'c'.
	 */
	public void matchAssumeLocalEqConstant(IfStmt stmt, boolean polarity,
			Local lhs, Constant rhs) {
		if (rhs instanceof NullConstant) {
			matchAssumeLocalEqNull(stmt, polarity, lhs);
		}
	}

	/**
	 * Matches statements of the form x<y for two local variables 'x' and 'y'.
	 */
	public void matchAssumeLocalLtLocal(IfStmt stmt, boolean polarity,
			Local lhs, Local rhs) {
	}

	/**
	 * Matches statements of the form x>y for two local variables 'x' and 'y'.
	 */
	public void matchAssumeLocalGtLocal(IfStmt stmt, boolean polarity,
			Local lhs, Local rhs) {
	}

	/**
	 * Matches statements of the form x<c for a local variable 'x' and constant
	 * 'c'.
	 */
	public void matchAssumeLocalLtConstant(IfStmt stmt, boolean polarity,
			Local lhs, Constant rhs) {
	}

	/**
	 * Matches statements of the form x>c for a local variable 'x' and constant
	 * 'c'.
	 */
	public void matchAssumeLocalGtConstant(IfStmt stmt, boolean polarity,
			Local lhs, Constant rhs) {
	}
}