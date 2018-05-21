package bgu.cs.absint.analyses.interval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.AddExpr;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.NumericConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import bgu.cs.absint.AssumeTransformer;
import bgu.cs.absint.ComposedOperation;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.EqualityRefiner;
import bgu.cs.absint.soot.TransformerMatcher;
import bgu.cs.util.Pair;

/**
 * Implementation of the interval abstract domain where each variable 'x' is
 * associated with at most two varToFactoid of the forms 'x>=c' (lower bound)
 * and 'x<=c' (upper bound) where 'c' is a numeric constant.
 * 
 * @author romanm
 */
public class IntervalDomain extends AbstractDomain<IntervalState, Unit> implements
		EqualityRefiner<Local> {
	/**
	 * Singleton value.
	 */
	private static final IntervalDomain v = new IntervalDomain();

	protected IntervalMatcher matcher = new IntervalMatcher();

	public static final IntervalDomain v() {
		return v;
	}

	@Override
	public IntervalState getBottom() {
		return IntervalState.bottom;
	}

	@Override
	public IntervalState getTop() {
		return IntervalState.top;
	}

	@Override
	public IntervalState ub(IntervalState first, IntervalState second) {
		if (first == IntervalState.bottom) {
			return second;
		} else if (second == IntervalState.bottom) {
			return first;
		} else {
			IntervalState result = new IntervalState();
			Set<Local> vars = first.getVars();
			vars.addAll(second.getVars());

			for (Local var : vars) {
				NumericConstant lb1 = first.getLB(var);
				NumericConstant lb2 = second.getLB(var);
				NumericConstant jointLB;
				if (lb1 == null || lb2 == null)
					jointLB = null;
				else if (lb1.lessThanOrEqual(lb2).equivTo(IntConstant.v(1)))
					jointLB = lb1;
				else
					jointLB = lb2;
				if (jointLB != null)
					result.addLBFactoid(var, jointLB);

				NumericConstant ub1 = first.getUB(var);
				NumericConstant ub2 = second.getUB(var);
				NumericConstant jointUB;
				if (ub1 == null || ub2 == null)
					jointUB = null;
				else if (ub1.lessThanOrEqual(ub2).equivTo(IntConstant.v(1)))
					jointUB = ub2;
				else
					jointUB = ub1;
				if (jointUB != null)
					result.addUBFactoid(var, jointUB);
			}
			return result;
		}
	}

	@Override
	public IntervalState lb(IntervalState first, IntervalState second) {
		if (first == IntervalState.bottom || second == IntervalState.bottom) {
			return IntervalState.bottom;
		} else {
			IntervalState result = new IntervalState();
			Set<Local> vars = first.getVars();
			vars.addAll(second.getVars());

			for (Local var : vars) {
				// Handle the lower-bound.
				NumericConstant lb1 = first.getLB(var);
				NumericConstant lb2 = second.getLB(var);
				NumericConstant jointLB;
				if (lb1 == null) {
					jointLB = lb2;
				} else if (lb2 == null) {
					jointLB = lb1;
				} else {
					if (lb1.lessThanOrEqual(lb2).equivTo(IntConstant.v(1)))
						jointLB = lb2;
					else
						jointLB = lb1;
				}
				if (jointLB != null)
					result.addLBFactoid(var, jointLB);

				// Handle the upper-bound.
				NumericConstant ub1 = first.getUB(var);
				NumericConstant ub2 = second.getUB(var);
				NumericConstant jointUB;

				if (ub1 == null) {
					jointUB = ub2;
				} else if (ub2 == null) {
					jointUB = ub1;
				} else {
					if (ub1.lessThanOrEqual(ub2).equivTo(IntConstant.v(1)))
						jointUB = ub1;
					else
						jointUB = ub2;
				}

				if (jointUB != null)
					result.addUBFactoid(var, jointUB);
			}
			if (!result.isConsistent())
				return getBottom();
			else
				return result;
		}
	}

	@Override
	public boolean leq(IntervalState first, IntervalState second) {
		if (first == IntervalState.bottom) {
			return true;
		} else if (second == IntervalState.bottom) {
			// first != bottom
			return false;
		} else {
			Set<Local> vars = first.getVars();
			vars.addAll(second.getVars());
			for (Local var : vars) {
				// Handle the lower-bounds.
				NumericConstant lb1 = first.getLB(var);
				NumericConstant lb2 = second.getLB(var);
				if (lb2 == null || lb1 != null
						&& lb2.lessThanOrEqual(lb1).equivTo(IntConstant.v(1))) {
				} else {
					return false;
				}

				// Handle the upper-bounds.
				NumericConstant ub1 = first.getUB(var);
				NumericConstant ub2 = second.getUB(var);
				if (ub2 == null || ub1 != null
						&& ub1.lessThanOrEqual(ub2).equivTo(IntConstant.v(1))) {
				} else {
					return false;
				}

			}
			return true;
		}
	}

	@Override
	public IntervalState widen(IntervalState first, IntervalState second) {
		if (first == IntervalState.bottom) {
			return second;
		} else if (second == IntervalState.bottom) {
			return first;
		} else {
			IntervalState result = new IntervalState();
			Set<Local> vars = first.getVars();
			vars.addAll(second.getVars());

			for (Local var : vars) {
				// Handle the lower-bound.
				NumericConstant lb1 = first.getLB(var);
				NumericConstant lb2 = second.getLB(var);
				NumericConstant jointLB;
				if (lb1 == null || lb2 == null)
					jointLB = null;
				else if (lb1.lessThanOrEqual(lb2).equivTo(IntConstant.v(1))) {
					jointLB = lb1;
				} else {
					// widen the lower-bound to -infinity by
					// dropping the lower-bound constraint.
					jointLB = null;
				}
				if (jointLB != null)
					result.addLBFactoid(var, jointLB);

				// Handle the upper-bound.
				NumericConstant ub1 = first.getUB(var);
				NumericConstant ub2 = second.getUB(var);
				NumericConstant jointUB;
				if (ub1 == null || ub2 == null)
					jointUB = null;
				else if (ub2.lessThanOrEqual(ub1).equivTo(IntConstant.v(1))) {
					jointUB = ub1;
				} else {
					// widen the upper-bound to infinity by
					// dropping the upper-bound constraint.
					jointUB = null;
				}
				if (jointUB != null)
					result.addUBFactoid(var, jointUB);
			}
			return result;
		}
	}

	@Override
	public IntervalState narrow(IntervalState first, IntervalState second) {
		if (first == IntervalState.bottom) {
			return second;
		} else if (second == IntervalState.bottom) {
			return first;
		} else {
			IntervalState result = new IntervalState();
			Set<Local> vars = first.getVars();
			vars.addAll(second.getVars());

			for (Local var : vars) {
				// Handle the lower-bound.
				NumericConstant lb1 = first.getLB(var);
				NumericConstant lb2 = second.getLB(var);
				NumericConstant jointLB;
				if (lb1 == null)
					jointLB = lb2;
				else
					jointLB = lb1;
				if (jointLB != null)
					result.addLBFactoid(var, jointLB);

				// Handle the upper-bound.
				NumericConstant ub1 = first.getUB(var);
				NumericConstant ub2 = second.getUB(var);
				NumericConstant jointUB;
				if (ub1 == null)
					jointUB = ub2;
				else
					jointUB = ub1;
				if (jointUB != null)
					result.addUBFactoid(var, jointUB);
			}
			return result;
		}
	}

	@Override
	public UnaryOperation<IntervalState> getTransformer(Unit stmt) {
		UnaryOperation<IntervalState> vanillaTransformer = matcher
				.getTransformer(stmt);
		return ComposedOperation.compose(vanillaTransformer,
				getReductionOperation());
	}

	/**
	 * Returns the identity operation to increase efficiency.
	 */
	@Override
	public UnaryOperation<IntervalState> getReductionOperation() {
		return IdOperation.v();
	}

	@Override
	public Object refineByEqualities(Object state,
			Collection<Pair<Local, Local>> equalities) {
		IntervalState istate = (IntervalState) state;
		// Special treatment for bottom.
		if (istate == getBottom())
			return getBottom();

		// Apply the following rule: {x=a} and {x=y} implies {y=a}.
		IntervalState result = istate.copy();
		for (Pair<Local, Local> pair : equalities) {
			LBFactoid firstLb = result.getLBFactoid(pair.first);
			UBFactoid firstUb = result.getUBFactoid(pair.first);
			LBFactoid secondLb = result.getLBFactoid(pair.second);
			UBFactoid secondUb = result.getUBFactoid(pair.second);

			IntervalState meetState = new IntervalState();
			if (secondLb != null) {
				LBFactoid newFirstLB = new LBFactoid(pair.first, secondLb.rhs);
				meetState.add(newFirstLB);
			}
			if (secondUb != null) {
				UBFactoid newFirstUB = new UBFactoid(pair.first, secondUb.rhs);
				meetState.add(newFirstUB);
			}
			if (firstLb != null) {
				LBFactoid newSecondLB = new LBFactoid(pair.second, firstLb.rhs);
				meetState.add(newSecondLB);
			}
			if (firstUb != null) {
				UBFactoid newSecondUB = new UBFactoid(pair.second, firstUb.rhs);
				meetState.add(newSecondUB);
			}
			result = lb(result, meetState);
		}
		if (eq(istate, result))
			return null;
		else
			return result;
	}

	@Override
	public Collection<Pair<Local, Local>> inferEqualities(Object state) {
		IntervalState istate = (IntervalState) state;
		ArrayList<Pair<Local, Local>> result = new ArrayList<>();
		// Special treatment for bottom.
		if (istate == getBottom())
			return result;

		// Look for {x=a, y=a} and return x=y.
		for (IntervalFactoid f1 : istate.getConstantFactoids()) {
			for (IntervalFactoid f2 : istate.getConstantFactoids()) {
				if (f1.rhs.equivTo(f2.rhs) && !f1.lhs.equivTo(f2.rhs))
					result.add(new Pair<Local, Local>(f1.lhs, f2.lhs));
			}
		}
		return result;
	}

	/**
	 * Singleton pattern.
	 */
	private IntervalDomain() {
	}

	/**
	 * A helper class for matching transformers to statements.<br>
	 * TODO: Handle statements of the following forms: 'x=y*z', 'assume x==y',
	 * 'assume x!=c'.
	 * 
	 * @author romanm
	 */
	protected class IntervalMatcher extends TransformerMatcher<IntervalState> {
		@Override
		public void matchAssignToLocal(AssignStmt stmt, Local lhs) {
			super.matchAssignToLocal(stmt, lhs);
			if (transformer == null)
				transformer = new ForgetVarTransformer(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs,
				ParameterRef rhs) {
			transformer = new ForgetVarTransformer(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ThisRef rhs) {
			transformer = new ForgetVarTransformer(lhs);
		}

		@Override
		public void matchAssignLocalToLocal(AssignStmt stmt, Local lhs,
				Local rhs) {
			if (lhs.equals(rhs)) {
				transformer = IdOperation.v();
			} else {
				transformer = new AssignVarToVarTransformer(lhs, rhs);
			}
		}

		@Override
		public void matchAssignConstantToLocal(AssignStmt stmt, Local lhs,
				Constant rhs) {
			if (lhs.equals(rhs)) {
				transformer = IdOperation.v();
			} else {
				if (rhs instanceof NumericConstant)
					transformer = new AssignConstantToVarTransformer(lhs,
							(NumericConstant) rhs);
				else
					transformer = new ForgetVarTransformer(lhs);
			}
		}

		/**
		 * Handle statements of the form x=a+b where 'a' and 'b' are either
		 * variables of constants.
		 */
		@Override
		public void matchAssignExprToLocal(AssignStmt stmt, Local lhs, Expr rhs) {
			if (rhs instanceof AddExpr) {
				transformer = new AssignAddExprToVarTransformer(lhs,
						(AddExpr) rhs);
			} else {
				transformer = new ForgetVarTransformer(lhs);
			}
		}

		// //////////////////////////////////////////////////////////////////////
		// Assume statements.
		// //////////////////////////////////////////////////////////////////////

		@Override
		public void matchAssumeLocalLtConstant(IfStmt stmt, boolean polarity,
				Local lhs, Constant rhs) {
			assert rhs instanceof NumericConstant;
			transformer = new AssumeLocalLtConstantTransformer(polarity, lhs,
					(NumericConstant) rhs);
		}

		@Override
		public void matchAssumeLocalGtConstant(IfStmt stmt, boolean polarity,
				Local lhs, Constant rhs) {
			assert rhs instanceof NumericConstant;
			transformer = new AssumeLocalGtConstantTransformer(polarity, lhs,
					(NumericConstant) rhs);
		}

		@Override
		public void matchAssumeLocalEqConstant(IfStmt stmt, boolean polarity,
				Local lhs, Constant rhs) {
			assert rhs instanceof NumericConstant;
			transformer = new AssumeLocalEqConstantTransformer(polarity, lhs,
					(NumericConstant) rhs);
		}
	}

	/**
	 * A transformer for statements of the form assume {@code assume x=a+b} for
	 * a variable 'x' and operands 'a' and 'b', which may be either local
	 * variables or constants,
	 * 
	 * @author romanm
	 */
	protected static class AssignAddExprToVarTransformer extends
			UnaryOperation<IntervalState> {
		/**
		 * The variable being modified by the concrete semantics.
		 */
		protected final Local lhs;
		protected final AddExpr rhs;

		public AssignAddExprToVarTransformer(Local lhs, AddExpr rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public IntervalState apply(IntervalState input) {
			// Special treatment for bottom.
			if (input.equals(IntervalState.bottom))
				return IntervalState.bottom;

			IntervalState result = input.copy();
			result.removeVar(lhs);

			// Compute the lower and upper bounds of the addition expression,
			// create the corresponding varToFactoid, and add them to the output
			// state.
			NumericConstant lbLhs = null;
			NumericConstant ubLhs = null;

			Value op1 = rhs.getOp1();
			Value op2 = rhs.getOp2();

			// Handle the lower bound
			NumericConstant c1, c2;
			if (op1 instanceof Local) {
				Local op1Local = (Local) op1;
				c1 = input.getLB(op1Local);
			} else if (op1 instanceof NumericConstant) {
				c1 = (NumericConstant) op1;
			} else {
				c1 = null;
			}

			if (op2 instanceof Local) {
				Local op2Local = (Local) op2;
				c2 = input.getLB(op2Local);
			} else if (op2 instanceof NumericConstant) {
				c2 = (NumericConstant) op2;
			} else {
				c2 = null;
			}

			if (c1 != null && c2 != null) {
				lbLhs = c1.add(c2);
				result.addLBFactoid(lhs, lbLhs);
			}

			// Handle the upper bound
			if (op1 instanceof Local) {
				Local op1Local = (Local) op1;
				c1 = input.getUB(op1Local);
			} else if (op1 instanceof NumericConstant) {
				c1 = (NumericConstant) op1;
			} else {
				c1 = null;
			}

			if (op2 instanceof Local) {
				Local op2Local = (Local) op2;
				c2 = input.getUB(op2Local);
			} else if (op2 instanceof NumericConstant) {
				c2 = (NumericConstant) op2;
			} else {
				c2 = null;
			}

			if (c1 != null && c2 != null) {
				ubLhs = c1.add(c2);
				result.addUBFactoid(lhs, ubLhs);
			}

			return result;
		}
	}

	/**
	 * A transformer for statements of the form assume {@code assume x=y} for
	 * variables 'x' and 'y',
	 * 
	 * @author romanm
	 */
	protected static class AssignVarToVarTransformer extends
			UnaryOperation<IntervalState> {
		/**
		 * The variable being modified by the concrete semantics.
		 */
		protected final Local lhs;
		protected final Local rhs;

		public AssignVarToVarTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public IntervalState apply(IntervalState input) {
			// Special treatment for bottom.
			if (input.equals(IntervalState.bottom))
				return IntervalState.bottom;

			IntervalState result = input.copy();
			result.removeVar(lhs);

			NumericConstant lbRhs = input.getLB(rhs);
			NumericConstant ubRhs = input.getUB(rhs);
			if (lbRhs != null)
				result.addLBFactoid(lhs, lbRhs);
			if (ubRhs != null)
				result.addUBFactoid(lhs, ubRhs);

			return result;
		}
	}

	/**
	 * A transformer for statements of the form assume {@code assume x=c} for a
	 * variable 'x' and constant 'c',
	 * 
	 * @author romanm
	 */
	protected static class AssignConstantToVarTransformer extends
			UnaryOperation<IntervalState> {
		protected final Local lhs;
		protected final NumericConstant rhs;

		public AssignConstantToVarTransformer(Local lhs, NumericConstant rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			assert !lhs.equals(rhs);
		}

		@Override
		public IntervalState apply(IntervalState input) {
			// Special treatment for bottom.
			if (input.equals(IntervalState.bottom))
				return IntervalState.bottom;

			IntervalState result = new IntervalState(input);
			result.removeVar(lhs);
			result.addLBFactoid(lhs, rhs);
			result.addUBFactoid(lhs, rhs);

			return result;
		}
	}

	/**
	 * A transformer that removes all varToFactoid containing a given variable.
	 * It can be used to conservatively handle any statement that has the effect
	 * of modifying a given local variable (and only it).
	 * 
	 * @author romanm
	 */
	protected static class ForgetVarTransformer extends
			UnaryOperation<IntervalState> {
		/**
		 * The variable being modified by the concrete semantics.
		 */
		protected Local lhs;

		/**
		 * Constructs a transformer for a specific local variable.
		 * 
		 * @param lhs
		 *            The variable being modified by the concrete semantics.
		 */
		public ForgetVarTransformer(Local lhs) {
			this.lhs = lhs;
		}

		@Override
		public IntervalState apply(IntervalState input) {
			// Special treatment for bottom.
			if (input.equals(IntervalState.bottom))
				return IntervalState.bottom;

			IntervalState result = new IntervalState();
			for (LBFactoid factoid : input.lbFactoids) {
				if (!factoid.hasVar(lhs)) {
					result.lbFactoids.add(factoid);
				}
			}
			for (UBFactoid factoid : input.ubFactoids) {
				if (!factoid.hasVar(lhs)) {
					result.ubFactoids.add(factoid);
				}
			}
			return result;
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// Transformers for assume statements.
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * A transformer for statements of the form assume {@code assume x<c} for a
	 * variable 'x' and constant 'c',
	 * 
	 * @author romanm
	 */
	protected class AssumeLocalLtConstantTransformer extends
			AssumeTransformer<IntervalState> {
		protected final Local lhs;
		protected final NumericConstant rhs;
		protected final IntervalState conditionInterval;

		public AssumeLocalLtConstantTransformer(boolean polarity, Local lhs,
				NumericConstant rhs) {
			super(polarity);
			this.lhs = lhs;
			this.rhs = rhs;
			conditionInterval = new IntervalState();
			if (polarity)
				conditionInterval.addUBFactoid(lhs,
						rhs.subtract(IntConstant.v(1)));
			else
				conditionInterval.addLBFactoid(lhs, rhs);
			assert !lhs.equals(rhs);
		}

		@Override
		public String toString() {
			if (polarity)
				return "Interval[" + lhs.toString() + "<" + rhs.toString()
						+ "]";
			else
				return "Interval[" + lhs.toString() + ">=" + rhs.toString()
						+ "]";
		}

		@Override
		public IntervalState apply(IntervalState input) {
			IntervalState result = lb(conditionInterval, input);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code assume x>c} and
	 * {@code assume x<c} for a variable 'x' and constant 'c',
	 * 
	 * @author romanm
	 */
	protected class AssumeLocalGtConstantTransformer extends
			AssumeTransformer<IntervalState> {
		protected final Local lhs;
		protected final NumericConstant rhs;
		protected final IntervalState conditionInterval;

		public AssumeLocalGtConstantTransformer(boolean polarity, Local lhs,
				NumericConstant rhs) {
			super(polarity);
			this.lhs = lhs;
			this.rhs = rhs;
			conditionInterval = new IntervalState();

			if (polarity)
				conditionInterval.addLBFactoid(lhs, rhs.add(IntConstant.v(1)));
			else
				conditionInterval.addUBFactoid(lhs, rhs);

			assert !lhs.equals(rhs);
		}

		@Override
		public String toString() {
			if (polarity)
				return "Interval[" + lhs.toString() + ">" + rhs.toString()
						+ "]";
			else
				return "Interval[" + lhs.toString() + "<=" + rhs.toString()
						+ "]";
		}

		@Override
		public IntervalState apply(IntervalState input) {
			IntervalState result = lb(conditionInterval, input);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code if (x==c)} and
	 * {@code if (x!=c)} for a variable 'x' and a constant 'c'.
	 * 
	 * @author romanm
	 */
	protected class AssumeLocalEqConstantTransformer extends
			AssumeTransformer<IntervalState> {
		protected final Local lhs;
		protected final NumericConstant rhs;
		protected final IntervalState conditionInterval;

		public AssumeLocalEqConstantTransformer(boolean polarity, Local lhs,
				NumericConstant rhs) {
			super(polarity);
			this.lhs = lhs;
			this.rhs = rhs;
			conditionInterval = new IntervalState();
			conditionInterval.addLBFactoid(lhs, rhs);
			conditionInterval.addUBFactoid(lhs, rhs);
		}

		@Override
		public String toString() {
			if (polarity)
				return "Interval[" + lhs.toString() + "==" + rhs.toString()
						+ "]";
			else
				return "Interval[" + lhs.toString() + "!=" + rhs.toString()
						+ "]";
		}

		@Override
		public IntervalState apply(IntervalState input) {
			if (polarity) {
				IntervalState result = lb(conditionInterval, input);
				return result;
			} else {
				IntervalState result = input.copy();
				if (input.getLB(lhs) != null && input.getLB(lhs).equivTo(rhs)) {
					// Update the lower bound by incrementing it by one.
					IntervalState newLBInterval = new IntervalState();
					newLBInterval.addLBFactoid(lhs, rhs.add(IntConstant.v(1)));
					result = lb(newLBInterval, result);
				}
				if (input.getUB(lhs) != null && input.getUB(lhs).equivTo(rhs)) {
					// Update the upper bound by decrementing it by one.
					IntervalState newUBInterval = new IntervalState();
					newUBInterval.addUBFactoid(lhs,
							rhs.subtract(IntConstant.v(1)));
					result = lb(newUBInterval, result);
				}
				return result;
			}
		}
	}
}