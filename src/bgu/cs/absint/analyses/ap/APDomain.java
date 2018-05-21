package bgu.cs.absint.analyses.ap;

import java.util.Collection;
import java.util.HashSet;

import soot.Local;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NumericConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import bgu.cs.absint.AssumeTransformer;
import bgu.cs.absint.ErrorState;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.ConjunctiveDomain;
import bgu.cs.absint.constructor.EqualityRefiner;
import bgu.cs.absint.soot.ForgetVarTransformer;
import bgu.cs.absint.soot.TransformerMatcher;
import bgu.cs.util.Pair;

/**
 * Abstract domain operations for the domain of arithmetic progressions, which
 * infers facts of the form {@code (x-b)%s = 0} for a local variable 'x' and
 * integer constants 'b' and 's'.
 * 
 * @author romanm
 */
public class APDomain extends
		ConjunctiveDomain<Local, APFactoid, APState, Unit> implements
		EqualityRefiner<Local> {
	protected APMatcher matcher = new APMatcher();

	/**
	 * Singleton value.
	 */
	private static final APDomain v = new APDomain();

	public static final APDomain v() {
		return v;
	}

	@Override
	public APState getBottom() {
		return APState.bottom;
	}

	@Override
	public APState getTop() {
		return APState.top;
	}

	@Override
	public APState ub(APState elem1, APState elem2) {
		// /////////////////////////////////
		// Corner cases with bottom and top
		// /////////////////////////////////
		if (elem1.equals(getBottom()))
			return elem2;
		if (elem2.equals(getBottom()))
			return elem1;
		// An optimization for joining with top.
		if (elem1.equals(getTop()) || elem2.equals(getTop()))
			return getTop();

		APState result = new APState();
		for (APFactoid factoid1 : elem1) {
			// By construction, factoid1 is not top (non-null).
			assert factoid1 != null;
			APFactoid factoid2 = elem2.getFactoidForVar(factoid1.var);

			// /////////////////////////////////////////
			// Corner case: a variable is mapped to top.
			// /////////////////////////////////////////
			if (factoid2 == null) {
				// Top.
				continue;
			} else if (factoid1.equals(factoid2)) {
				result.add(factoid1);
			} else {
				int minBase = factoid1.base <= factoid2.base ? factoid1.base
						: factoid2.base;
				int stride = gcdX(factoid1.stride, factoid2.stride,
						factoid1.base - minBase, factoid2.base - minBase);
				APFactoid factoid = new APFactoid(factoid1.var, minBase, stride);
				result.add(factoid);
			}
		}
		return result;
	}

	@Override
	public APState lb(APState elem1, APState elem2) {
		// /////////////////////////////////
		// Corner cases with bottom and top
		// /////////////////////////////////
		if (elem1.equals(getTop()))
			return elem2;
		if (elem2.equals(getTop()))
			return elem1;
		// An optimization for meeting with bottom.
		if (elem1.equals(getBottom()) || elem2.equals(getBottom()))
			return getBottom();

		APState result = new APState();

		HashSet<Local> locals = new HashSet<>();
		locals.addAll(elem1.getVars());
		locals.addAll(elem2.getVars());
		for (Local var : locals) {
			APFactoid factoid1 = elem1.getFactoidForVar(var);
			APFactoid factoid2 = elem2.getFactoidForVar(var);

			if (factoid2 == null) {
				// /////////////////////////////////////////
				// Corner case: a variable is mapped to top.
				// /////////////////////////////////////////
				result.add(factoid1);
				continue;
			} else if (factoid1 == null) {
				// /////////////////////////////////////////
				// Corner case: a variable is mapped to top.
				// /////////////////////////////////////////
				result.add(factoid2);
				continue;
			} else if (factoid1.leq(factoid2)) {
				result.add(factoid1);
				continue;
			} else if (factoid2.leq(factoid1)) {
				result.add(factoid2);
				continue;
			}

			if (factoid1.stride > 0 && factoid2.stride > 0) {
				int baseDist = factoid2.base - factoid1.base;
				boolean base2InVals1 = baseDist == 0
						|| baseDist % factoid1.stride == 0;
				if (base2InVals1) {
					APFactoid newFactoid = new APFactoid(factoid1.var,
							factoid2.base,
							lcm(factoid1.stride, factoid2.stride));
					result.add(newFactoid);
					continue;
				}

				baseDist = factoid1.base - factoid2.base;
				boolean base1InVals2 = baseDist == 0
						|| baseDist % factoid2.stride == 0;
				if (base1InVals2) {
					APFactoid newFactoid = new APFactoid(factoid1.var,
							factoid1.base,
							lcm(factoid1.stride, factoid2.stride));
					result.add(newFactoid);
					continue;
				}
			}
			return getBottom();
		}
		return result;
	}

	@Override
	public APState widen(APState elem1, APState elem2) {
		if (elem2.equals(getBottom()))
			return elem1;
		if (elem1.equals(getBottom()))
			return elem2;
		APState result = new APState();
		HashSet<Local> locals = new HashSet<>();
		locals.addAll(elem1.getVars());
		locals.addAll(elem2.getVars());
		for (Local var : locals) {
			APFactoid f1 = elem1.getFactoidForVar(var);
			APFactoid f2 = elem2.getFactoidForVar(var);
			// Now compute f1 widen f2.
			APState s1 = new APState();
			if (f1 != null)
				s1.add(f1);
			APState s2 = new APState();
			if (f2 != null)
				s2.add(f2);
			APState joinS1andS2 = ub(s1, s2);
			APFactoid joinedFactoid = joinS1andS2.getFactoidForVar(var);
			if (joinedFactoid == null) {
				// result is top so no need to add a factoid.
			} else {
				if (joinedFactoid.base == f1.base) {
					if (joinedFactoid.stride < f1.stride) {
						APFactoid widenedFactoid = new APFactoid(var, f1.base,
								1);
						result.add(widenedFactoid);
					} else {
						result.add(joinedFactoid);
					}

				} else {
					// top
				}
			}
		}
		return result;
	}

	@Override
	public APState createEmpty() {
		return new APState();
	}

	@Override
	public Object refineByEqualities(Object state,
			Collection<Pair<Local, Local>> equalities) {
		APState input = (APState) state;
		// Special treatment for bottom.
		if (input == getBottom())
			return null;

		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Pair<Local, Local>> inferEqualities(Object state) {
		APState input = (APState) state;
		HashSet<Pair<Local, Local>> result = new HashSet<>();
		// Special treatment for bottom.
		if (input == getBottom())
			return result;

		Collection<APFactoid> constants = input.getConstantFactoids();
		for (APFactoid c1 : constants) {
			for (APFactoid c2 : constants) {
				if (c2 == c1)
					continue;
				if (c1.base == c2.base) {
					result.add(new Pair<>(c1.var, c2.var));
					result.add(new Pair<>(c2.var, c1.var));
				}
			}
		}

		return result;
	}

	@Override
	public UnaryOperation<APState> getTransformer(Unit stmt) {
		return matcher.getTransformer(stmt);
	}

	/**
	 * Computes the greatest common divisor of two positive integers.
	 */
	private int gcd(int a, int b) {
		assert a > 0 && b > 0;
		while (a != b) {
			if (a > b)
				a -= b;
			else
				b -= a;
		}
		return a;
	}

	/**
	 * Computes the least common multiple of two positive integers.
	 */
	private int lcm(int a, int b) {
		assert a > 0 && b > 0;
		return (a * b) / gcd(a, b);
	}

	/**
	 * Computes the greatest common divisor of the absolute values of a
	 * non-empty sequence of integers, ignoring zero values. If all elements are
	 * zero the result is zero.
	 */
	private int gcdX(int... args) {
		boolean nonZeroElementFound = false;
		assert args != null && args.length > 0;
		int result = 1;
		for (int i = 0; i < args.length; ++i) {
			int arg = args[i];
			if (arg == 0)
				continue;
			nonZeroElementFound = true;
			if (arg < 0)
				arg = -arg;
			result = gcd(result, arg);
		}
		if (!nonZeroElementFound)
			return 0;
		else
			return result;
	}

	/**
	 * Singleton pattern.
	 */
	private APDomain() {
	}

	/**
	 * A helper class for matching transformers to statements.<br>
	 * TODO: Handle statements of the following forms: 'x=y*z', 'assume x==y',
	 * 'assume x!=c'.
	 * 
	 * @author romanm
	 */
	protected class APMatcher extends TransformerMatcher<APState> {
		@Override
		public void caseInvokeStmt(InvokeStmt stmt) {
			InvokeExpr expr = stmt.getInvokeExpr();
			String methodName = expr.getMethod().getName();
			if (methodName.equals("analysisAssumeAPFactoid")) {
				if (expr.getArgCount() != 3) {
					throw new Error(
							"assumeAPFactoid expects three arguments, but got "
									+ expr.getArgCount() + "!");
				}

				Local var = null;
				if (expr.getArg(0) instanceof Local) {
					var = (Local) expr.getArg(0);
				} else {
					throw new Error(
							"analysisAssumeAPFactoid expects an argument of type Local but got "
									+ expr.getArg(0).getClass() + "!");
				}

				int base;
				if (expr.getArg(1) instanceof IntConstant) {
					IntConstant arg = (IntConstant) expr.getArg(1);
					base = arg.value;
				} else {
					throw new Error(
							"analysisAssumeAPFactoid expects an argument of type int but got "
									+ expr.getArg(0).getClass() + "!");
				}

				int stride;
				if (expr.getArg(2) instanceof IntConstant) {
					IntConstant arg = (IntConstant) expr.getArg(2);
					stride = arg.value;
				} else {
					throw new Error(
							"analysisAssumeAPFactoid expects an argument of type int but got "
									+ expr.getArg(0).getClass() + "!");
				}

				APFactoid f = new APFactoid(var, base, stride);
				transformer = new AnalysisAssumeFactoidTransformer(f);
			}

			String assertMethod = "analysisAssertLeqAPFactoid";
			if (methodName.equals(assertMethod)) {
				if (expr.getArgCount() != 3) {
					throw new Error(assertMethod
							+ " expects three arguments, but got "
							+ expr.getArgCount() + "!");
				}

				Local var = null;
				if (expr.getArg(0) instanceof Local) {
					var = (Local) expr.getArg(0);
				} else {
					throw new Error(assertMethod
							+ " expects an argument of type Local but got "
							+ expr.getArg(0).getClass() + "!");
				}

				int base;
				if (expr.getArg(1) instanceof IntConstant) {
					IntConstant arg = (IntConstant) expr.getArg(1);
					base = arg.value;
				} else {
					throw new Error(assertMethod
							+ " expects an argument of type int but got "
							+ expr.getArg(0).getClass() + "!");
				}

				int stride;
				if (expr.getArg(2) instanceof IntConstant) {
					IntConstant arg = (IntConstant) expr.getArg(2);
					stride = arg.value;
				} else {
					throw new Error(assertMethod
							+ " expects an argument of type int but got "
							+ expr.getArg(0).getClass() + "!");
				}

				APFactoid f = new APFactoid(var, base, stride);
				transformer = new AnalysisAssertLeqFactoidTransformer(f);
			}

		}

		@Override
		public void matchAssignToLocal(AssignStmt stmt, Local lhs) {
			super.matchAssignToLocal(stmt, lhs);
			if (transformer == null)
				transformer = new ForgetVarTransformer<APFactoid, APState>(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs,
				ParameterRef rhs) {
			transformer = new ForgetVarTransformer<APFactoid, APState>(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ThisRef rhs) {
			transformer = new ForgetVarTransformer<APFactoid, APState>(lhs);
		}

		@Override
		public void matchAssignLocalToLocal(AssignStmt stmt, Local lhs,
				Local rhs) {
			if (lhs.equals(rhs)) {
				transformer = IdOperation.v();
			} else {
				transformer = new AssignLocalToLocalTransformer(lhs, rhs);
			}
		}

		/**
		 * Handle statements of the form x=c where 'c' is an integer constant.
		 */
		@Override
		public void matchAssignConstantToLocal(AssignStmt stmt, Local lhs,
				Constant rhs) {
			if (rhs instanceof IntConstant)
				transformer = new AssignConstantToVarTransformer(lhs,
						(IntConstant) rhs);
		}

		/**
		 * Handle statements of the form x=a+b where 'a' and 'b' are either
		 * variables of constants.
		 */
		@Override
		public void matchAssignAddLocalLocalToLocal(AssignStmt stmt, Local lhs,
				Local op1, Local op2) {
			transformer = new AssignAddLocalLocalToLocalTransformer(lhs, op1,
					op2);
		}

		/**
		 * Matches statements of the form {@code x=y+c} and {@code x=c+y}.
		 */
		@Override
		public void matchAssignAddLocalConstantToLocal(AssignStmt stmt,
				Local lhs, Local op1, Constant op2) {
			if (op2 instanceof IntConstant) {
				transformer = new AssignAddLocalConstantToLocalTransformer(lhs,
						op1, (IntConstant) op2);
			}
		}

		/**
		 * Matches statements of the form {@code x=y-c}.
		 */
		@Override
		public void matchAssignSubLocalConstantToLocal(AssignStmt stmt,
				Local lhs, Local op1, Constant op2) {
			op2 = ((IntConstant) op2).multiply(IntConstant.v(-1));
			if (op2 instanceof IntConstant) {
				transformer = new AssignAddLocalConstantToLocalTransformer(lhs,
						op1, (IntConstant) op2);
			}
		}

		@Override
		public void matchAssignMulLocalConstantToLocal(AssignStmt stmt,
				Local lhs, Local op1, Constant op2) {
			if (op2 instanceof IntConstant)
				transformer = new AssignMulLocalConstantToLocalTransformer(lhs,
						op1, (IntConstant) op2);
		}

		@Override
		public void matchAssumeLocalEqConstant(IfStmt stmt, boolean polarity,
				Local lhs, Constant rhs) {
			if (rhs instanceof IntConstant)
				transformer = new AssumeLocalEqConstantTransformer(polarity,
						lhs, (IntConstant) rhs);
		}

		@Override
		public void matchAssumeLocalEqLocal(IfStmt stmt, boolean polarity,
				Local lhs, Local rhs) {
			transformer = new AssumeLocalEqLocalTransformer(polarity, lhs, rhs);
		}

		@Override
		public void matchAssumeLocalGtConstant(IfStmt stmt, boolean polarity,
				Local lhs, Constant rhs) {
			assert rhs instanceof NumericConstant;
			if (rhs instanceof IntConstant)
				transformer = new AssumeLocalGtConstantTransformer(polarity,
						lhs, (IntConstant) rhs);
		}
	}

	// //////////////////////////////////////////////////////////////////////
	// Assignment transformers.
	// //////////////////////////////////////////////////////////////////////

	/**
	 * A transformer for statements of the form assume {@code x=y+z}.<br>
	 * DONE!
	 * 
	 * @author romanm
	 */
	protected static class AssignAddLocalLocalToLocalTransformer extends
			UnaryOperation<APState> {
		/**
		 * The variable being modified by the concrete semantics.
		 */
		protected final Local lhs;
		protected final Local op1;
		protected final Local op2;

		public AssignAddLocalLocalToLocalTransformer(Local lhs, Local op1,
				Local op2) {
			this.lhs = lhs;
			this.op1 = op1;
			this.op2 = op2;
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			APState result = input.copy();
			result.removeVar(lhs);
			// {b1 + k1*s1} + {b2 + k2*s2} = {(b1+b2) + k1*s1+k2*s2}
			APFactoid op1Factoid = input.getFactoidForVar(op1);
			APFactoid op2Factoid = input.getFactoidForVar(op2);

			if (op1Factoid == null || op2Factoid == null)
				return result;

			APFactoid sumFactoid = null;
			int base = op1Factoid.base + op2Factoid.base;
			int minStride = op1Factoid.stride < op2Factoid.stride ? op1Factoid.stride
					: op2Factoid.stride;
			int maxStride = op1Factoid.stride > op2Factoid.stride ? op1Factoid.stride
					: op2Factoid.stride;
			if (minStride == 0 && maxStride == 0) { // sum of two constants
				sumFactoid = new APFactoid(lhs, base, 0);
			} else if (minStride == 0) {
				assert maxStride != 0;
				sumFactoid = new APFactoid(lhs, base, maxStride);
			} else {
				assert minStride != 0 && maxStride != 0;
				if (maxStride % minStride == 0)
					sumFactoid = new APFactoid(lhs, base, minStride);
				else
					sumFactoid = new APFactoid(lhs, base, 1);
			}

			result.add(sumFactoid);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code x=y+c} and {@code x=c+y}.<br>
	 * DONE!
	 * 
	 * @author romanm
	 */
	protected static class AssignAddLocalConstantToLocalTransformer extends
			UnaryOperation<APState> {
		/**
		 * The variable being modified by the concrete semantics.
		 */
		protected final Local lhs;
		protected final Local op1;
		protected final int op2;

		public AssignAddLocalConstantToLocalTransformer(Local lhs, Local op1,
				IntConstant op2) {
			this.lhs = lhs;
			this.op1 = op1;
			this.op2 = op2.value;
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			APState result = input.copy();
			result.removeVar(lhs);
			APFactoid op1Factoid = input.getFactoidForVar(op1);
			// Special case: op1 is mapped to top.
			if (op1Factoid == null)
				return result;

			APFactoid newFactoid = new APFactoid(lhs, op1Factoid.base + op2,
					op1Factoid.stride);
			result.add(newFactoid);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code x=y*c} and {@code x=c*y}.<br>
	 * DONE!
	 * 
	 * @author romanm
	 */
	protected static class AssignMulLocalConstantToLocalTransformer extends
			UnaryOperation<APState> {
		/**
		 * The variable being modified by the concrete semantics.
		 */
		protected final Local lhs;
		protected final Local op1;
		protected final int op2;

		public AssignMulLocalConstantToLocalTransformer(Local lhs, Local rhs,
				IntConstant coefficient) {
			this.lhs = lhs;
			this.op1 = rhs;
			this.op2 = coefficient.value;
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			APState result = input.copy();
			result.removeVar(lhs);
			APFactoid op1Factoid = input.getFactoidForVar(op1);
			// Special case: op1 is mapped to top.
			if (op1Factoid == null)
				return result;

			APFactoid newFactoid = new APFactoid(lhs, op1Factoid.base * op2,
					op1Factoid.stride * op2);
			result.add(newFactoid);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form assume {@code assume x=y} for
	 * variables 'x' and 'y'.<br>
	 * DONE!
	 * 
	 * @author romanm
	 */
	protected static class AssignLocalToLocalTransformer extends
			UnaryOperation<APState> {
		/**
		 * The variable being modified by the concrete semantics.
		 */
		protected final Local lhs;
		protected final Local rhs;

		public AssignLocalToLocalTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			APState result = input.copy();
			result.removeVar(lhs);

			APFactoid rhsFactoid = input.getFactoidForVar(lhs);
			// Special case: op1 is mapped to top.
			if (rhsFactoid == null)
				return result;

			APFactoid newFactoid = new APFactoid(lhs, rhsFactoid.base,
					rhsFactoid.stride);
			result.add(newFactoid);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form assume {@code assume x=c} for a
	 * variable 'x' and constant 'c'.<br>
	 * DONE!
	 * 
	 * @author romanm
	 */
	protected static class AssignConstantToVarTransformer extends
			UnaryOperation<APState> {
		protected final Local lhs;
		protected final int rhs;
		protected final APFactoid newFactoid;

		public AssignConstantToVarTransformer(Local lhs, IntConstant rhs) {
			this.lhs = lhs;
			this.rhs = rhs.value;
			assert !lhs.equals(rhs);
			newFactoid = new APFactoid(lhs, rhs.value);
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			APState result = new APState(input);
			result.removeVar(lhs);
			result.add(newFactoid);
			return result;
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// Transformers for assume statements.
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * A transformer for an artificial statement of the form assume AP[x,b,s].
	 * 
	 * @author romanm
	 */
	protected class AnalysisAssumeFactoidTransformer extends
			UnaryOperation<APState> {
		protected final APFactoid assumedFactoid;
		protected final APState assumedState;

		public AnalysisAssumeFactoidTransformer(APFactoid assumedFactoid) {
			assumedState = new APState();
			this.assumedFactoid = assumedFactoid;
			assumedState.add(assumedFactoid);
		}

		@Override
		public String toString() {
			return "[|assume " + assumedFactoid + "|]";
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			APState output = lb(input, assumedState);
			return output;
		}
	}

	/**
	 * A transformer for an artificial statement of the form assert <=
	 * AP[x,b,s].
	 * 
	 * @author romanm
	 */
	protected class AnalysisAssertLeqFactoidTransformer extends
			UnaryOperation<APState> {
		protected final APFactoid factoid;

		public AnalysisAssertLeqFactoidTransformer(APFactoid factoid) {
			this.factoid = factoid;
		}

		@Override
		public String toString() {
			return "[|assert <= " + factoid + "|]";
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			APFactoid inputFactoid = input.getFactoidForVar(factoid.var);
			if (inputFactoid != null && inputFactoid.leq(factoid))
				return input;
			else
				return APState.getErrorState("assert failed: " + input
						+ " is not less than or equal to " + factoid + "!");
		}
	}

	/**
	 * A transformer for statements of the form {@code if (x==c)} and
	 * {@code if (x!=c)} for a variable 'x' and a constant 'c'.<br>
	 * DONE!
	 * 
	 * @author romanm
	 */
	protected class AssumeLocalEqConstantTransformer extends
			AssumeTransformer<APState> {
		protected final Local lhs;
		protected final int rhs;
		protected final APState conditionState;

		public AssumeLocalEqConstantTransformer(boolean polarity, Local lhs,
				IntConstant rhs) {
			super(polarity);
			this.lhs = lhs;
			this.rhs = rhs.value;
			this.conditionState = new APState();
			conditionState.add(new APFactoid(lhs, rhs.value));
		}

		@Override
		public String toString() {
			if (polarity)
				return "[|assume " + lhs.toString() + "==" + rhs + "|]";
			else
				return "[|assume " + lhs.toString() + "!=" + rhs + "|]";
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			if (polarity) {
				// assume x == c
				return lb(input, conditionState);
			} else {
				// assume x != c
				APFactoid factoidForRhs = input.getFactoidForVar(lhs);
				if (factoidForRhs == null)
					return input;
				else if (factoidForRhs.isConstant()
						&& factoidForRhs.base != rhs)
					return getBottom();
				else {
					if (factoidForRhs.base == rhs) {
						APState result = new APState(input);
						result.remove(factoidForRhs);
						APFactoid newFactoid = new APFactoid(lhs, rhs
								+ factoidForRhs.stride, factoidForRhs.stride);
						result.add(newFactoid);
						return result;
					} else {
						if ((rhs - factoidForRhs.base) % factoidForRhs.stride == 0)
							return getBottom();
						else
							return input;
					}
				}
			}
		}
	}

	/**
	 * A transformer for statements of the form {@code if (x==y)} and
	 * {@code if (x!=y)} for variable 'x' and 'y'.
	 * 
	 * @author romanm
	 */
	protected class AssumeLocalEqLocalTransformer extends
			AssumeTransformer<APState> {
		protected final Local lhs;
		protected final Local rhs;
		protected final APState conditionState;

		public AssumeLocalEqLocalTransformer(boolean polarity, Local lhs,
				Local rhs) {
			super(polarity);
			this.lhs = lhs;
			this.rhs = rhs;
			this.conditionState = new APState();
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			if (polarity)
				return "[|assume " + lhs.toString() + "==" + rhs.toString()
						+ "|]";
			else
				return "[|assume " + lhs.toString() + "!=" + rhs.toString()
						+ "|]";
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			if (polarity) {
				return lb(input, conditionState);
			} else {
				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * A transformer for statements of the form {@code assume x>c} and
	 * {@code assume x<c} for a variable 'x' and constant 'c'.<br>
	 * DONE!
	 * 
	 * @author romanm
	 */
	protected class AssumeLocalGtConstantTransformer extends
			AssumeTransformer<APState> {
		protected final Local lhs;
		protected final int rhs;
		protected final APState conditionInterval;

		public AssumeLocalGtConstantTransformer(boolean polarity, Local lhs,
				IntConstant rhs) {
			super(polarity);
			this.lhs = lhs;
			this.rhs = rhs.value;
			conditionInterval = new APState();

			if (polarity) {
				APFactoid gtFactoid = new APFactoid(lhs, this.rhs + 1, 1);
				conditionInterval.add(gtFactoid);
			}

			assert !lhs.equals(rhs);
		}

		@Override
		public String toString() {
			if (polarity)
				return "[|assume " + lhs.toString() + ">" + rhs + "|]";
			else
				return "[|assume " + lhs.toString() + "<=" + rhs + "|]";
		}

		@Override
		public APState apply(APState input) {
			// Ensure strictness in bottom and error states.
			if (input.equals(APState.bottom) || input instanceof ErrorState)
				return APState.bottom;

			if (polarity) {
				APState result = lb(conditionInterval, input);
				return result;
			} else {
				// x <= c
				APFactoid lhsFactoid = input.getFactoidForVar(lhs);
				if (lhsFactoid != null && lhsFactoid.base > rhs)
					return getBottom();
				else
					return input;
			}
		}
	}
}