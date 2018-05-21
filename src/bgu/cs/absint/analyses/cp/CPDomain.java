package bgu.cs.absint.analyses.cp;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import soot.Immediate;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.Expr;
import soot.jimple.IntConstant;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.EqualityRefiner;
import bgu.cs.absint.soot.Assume;
import bgu.cs.absint.soot.ForgetVarTransformer;
import bgu.cs.absint.soot.TransformerMatcher;
import bgu.cs.util.Pair;

/**
 * Implementation of abstract operations for a static analysis for tracking
 * equalities between variables.
 * 
 * @author romanm
 * 
 */
public class CPDomain extends AbstractDomain<CPState, Unit> implements
		EqualityRefiner<Local> {
	/**
	 * Singleton value.
	 */
	private static final CPDomain v = new CPDomain();

	protected CPMatcher matcher = new CPMatcher();

	public static final CPDomain v() {
		return v;
	}

	@Override
	public CPState getBottom() {
		return CPState.bottom;
	}

	@Override
	public CPState getTop() {
		return CPState.top;
	}

	@Override
	public CPState ub(CPState first, CPState second) {
		if (first == CPState.bottom) {
			return second;
		} else if (second == CPState.bottom) {
			return first;
		} else {
			// Compute the intersection of the two sets of varToFactoid.
			CPState result = new CPState(first);
			result.factoids.retainAll(second.factoids);
			return result;
		}
	}

	@Override
	public CPState lb(CPState first, CPState second) {
		if (first == CPState.bottom || second == CPState.bottom) {
			return CPState.bottom;
		} else {
			// Compute the union of the two sets of varToFactoid.
			CPState result = new CPState(first);
			result.factoids.addAll(second.factoids);
			return result;
		}
	}

	@Override
	public boolean leq(CPState first, CPState second) {
		if (first == CPState.bottom) {
			return true;
		} else if (second == CPState.bottom) {
			// first != bottom
			return false;
		} else {
			return first.factoids.containsAll(second.factoids);
		}
	}

	@Override
	public CPState refineByEqualities(Object input,
			Collection<Pair<Local, Local>> equalities) {
		CPState state = (CPState) input;
		if (state == getBottom())
			return null;

		CPState result = state.copy();
		boolean change = false;
		for (Pair<Local, Local> equality : equalities) {
			Constant c1 = state.getConstantForVar(equality.first);
			Constant c2 = state.getConstantForVar(equality.second);
			if (c1 == null && c2 == null)
				continue;
			if (c1 == null) {
				assert c2 != null;
				result.addFactoid(equality.first, c2);
				change = true;
			} else if (c2 == null) {
				assert c1 != null;
				result.addFactoid(equality.second, c1);
				change = true;
			} else if (!c1.equals(c2)) {
				result = getBottom();
			}
		}

		if (change)
			return result;
		else
			return null;
	}

	@Override
	public Collection<Pair<Local, Local>> inferEqualities(Object input) {
		CPState state = (CPState) input;
		if (state == getBottom())
			return Collections.emptyList();

		HashSet<Pair<Local, Local>> result = new HashSet<>();
		for (CPFactoid factoid1 : state.factoids) {
			for (CPFactoid factoid2 : state.factoids) {
				if (factoid1 != factoid2 && factoid1.rhs.equals(factoid2.rhs))
					result.add(new Pair<>(factoid1.lhs, factoid2.lhs));
			}
		}
		return result;
	}

	/**
	 * Returns the identity operation to increase efficiency.
	 */
	@Override
	public UnaryOperation<CPState> getReductionOperation() {
		return IdOperation.v();
	}

	@Override
	public UnaryOperation<CPState> getTransformer(Unit stmt) {
		UnaryOperation<CPState> vanillaTransformer = matcher
				.getTransformer(stmt);
		return vanillaTransformer;
	}

	protected static boolean isConcreteExpr(CPState state, Expr expr) {
		boolean result = true;
		for (Object o : expr.getUseBoxes()) {
			ValueBox b = (ValueBox) o;
			Value v = b.getValue();
			if (v instanceof Constant) {
			} else if (v instanceof Local) {
				Local l = (Local) v;
				if (state.getConstantForVar(l) == null)
					result = false;
			} else {
				result = false;
			}
			if (!result)
				break;
		}
		return result;
	}

	/**
	 * Singleton pattern.
	 */
	private CPDomain() {
	}

	/**
	 * A helper class for matching transformers to statements.
	 * 
	 * @author romanm
	 */
	protected class CPMatcher extends TransformerMatcher<CPState> {
		@Override
		public UnaryOperation<CPState> getTransformer(Unit stmt) {
			transformer = null;
			if (stmt instanceof Assume) {
				Assume assume = (Assume) stmt;
				matchAssume(assume);
				if (transformer == null) {
					transformer = new AssumeExprTransformer(
							(Expr) assume.stmt.getCondition(), assume.polarity);
				}
			} else {
				stmt.apply(this);
				if (transformer == null) {
					transformer = IdOperation.v();
				}

			}
			return transformer;
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
				transformer = new AssignConstantToVarTransformer(lhs, rhs);
			}
		}

		/**
		 * A conservatively handling assignments from expressions by projecting
		 * out the assigned variable.
		 */
		@Override
		public void matchAssignExprToLocal(AssignStmt stmt, Local lhs, Expr rhs) {
			boolean canInterpret = true;
			for (Object o : rhs.getUseBoxes()) {
				ValueBox b = (ValueBox) o;
				Value v = b.getValue();
				if (!(v instanceof Immediate)) {
					canInterpret = false;
				}
			}

			if (canInterpret)
				transformer = new AssignExprToVarTransformer(lhs, rhs);
			else
				transformer = new ForgetVarTransformer<CPFactoid, CPState>(lhs);
		}
	}

	/**
	 * A transformer for statements of the form x=expr.
	 * 
	 * @author romanm
	 */
	protected static class AssignExprToVarTransformer extends
			UnaryOperation<CPState> {
		protected final Local lhs;
		protected final Expr rhs;

		public AssignExprToVarTransformer(Local lhs, Expr rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			assert !lhs.equals(rhs);
		}

		@Override
		public CPState apply(CPState input) {
			// Special treatment for bottom.
			if (input.equals(CPState.bottom))
				return CPState.bottom;

			if (isConcreteExpr(input, rhs)) {
				Constant resultVal = CPExprEval.v.eval(input, rhs);
				if (resultVal != null) {
					CPState result = new CPState(input);
					result.removeVar(lhs);
					result.addFactoid(lhs, resultVal);
					return result;
				}
			}

			CPState result = new CPState(input);
			result.removeVar(lhs);
			return result;
		}
	}

	/**
	 * A transformer for an assignment between two different variables. (Can
	 * handle assignment of a variable to itself, but loses precision
	 * needlessly.)
	 * 
	 * @author romanm
	 * 
	 */
	protected static class AssignVarToVarTransformer extends
			UnaryOperation<CPState> {
		protected final Local lhs;
		protected final Local rhs;

		public AssignVarToVarTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			assert !lhs.equals(rhs);
		}

		/**
		 * Apply the following two rules:<br>
		 * 1. Preservation rule: {a=b} lhs=c {a=b} if a!=lhs and b!=lhs<br>
		 * 2. New factoid rule: { } lhs=c {lhs=c}
		 */
		@Override
		public CPState apply(CPState input) {
			// Special treatment for bottom.
			if (input.equals(CPState.bottom))
				return CPState.bottom;

			CPState result = new CPState(input);
			result.removeVar(lhs);
			Constant value = input.getConstantForVar(rhs);
			if (value != null) {
				CPFactoid newFactoid = new CPFactoid(lhs, value);
				result.factoids.add(newFactoid);
			}
			return result;
		}
	}

	/**
	 * A transformer for assume lhs!=c.
	 * 
	 * @author romanm
	 * 
	 */
	protected static class AssumeVarNeqConstantTransformer extends
			UnaryOperation<CPState> {
		protected final Local lhs;
		protected final Constant rhs;
		protected final CPFactoid checkedFactoid;

		public AssumeVarNeqConstantTransformer(Local lhs, Constant rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			assert !lhs.equals(rhs);
			checkedFactoid = new CPFactoid(lhs, rhs);
		}

		/**
		 * Check whether the set of varToFactoid contains lhs=c, which negates
		 * the assumed expression. If so, return bottom, otherwise return the
		 * input state.
		 */
		@Override
		public CPState apply(CPState input) {
			// Special treatment for bottom.
			if (input.equals(CPState.bottom))
				return CPState.bottom;

			if (input.factoids.contains(checkedFactoid)) {
				return CPState.bottom;
			} else {
				return input;
			}
		}
	}

	protected static class AssumeExprTransformer extends
			UnaryOperation<CPState> {
		protected final Expr expr;
		protected final boolean polarity;

		public AssumeExprTransformer(Expr expr, boolean polarity) {
			this.expr = expr;
			this.polarity = polarity;
		}

		/**
		 * Check whether the set of varToFactoid contains lhs=c, which negates
		 * the assumed expression. If so, return bottom, otherwise return the
		 * input state.
		 */
		@Override
		public CPState apply(CPState input) {
			// Special treatment for bottom.
			if (input.equals(CPState.bottom))
				return CPState.bottom;

			if (isConcreteExpr(input, expr)) {
				Constant resultVal = CPExprEval.v.eval(input, expr);
				if (resultVal != null) {
					boolean conditionHolds = resultVal.equals(IntConstant.v(1));
					if (conditionHolds == polarity)
						return input;
					else
						return CPState.bottom;
				}
			}

			return input;
		}
	}

	/**
	 * A transformer for an assignment between two different variables. (Can
	 * handle assignment of a variable to itself, but loses precision
	 * needlessly.)
	 * 
	 * @author romanm
	 * 
	 */
	protected static class AssignConstantToVarTransformer extends
			UnaryOperation<CPState> {
		protected final Local lhs;
		protected final Constant rhs;
		protected final CPFactoid newFactoid;

		public AssignConstantToVarTransformer(Local lhs, Constant rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			assert !lhs.equals(rhs);
			newFactoid = new CPFactoid(lhs, rhs);
		}

		/**
		 * Apply the following two rules:<br>
		 * 1. Preservation rule: {a=b} lhs=c {a=b} if a!=lhs and b!=lhs<br>
		 * 2. New factoid rule: { } lhs=c {lhs=c}
		 */
		@Override
		public CPState apply(CPState input) {
			// Special treatment for bottom.
			if (input.equals(CPState.bottom))
				return CPState.bottom;

			CPState result = new CPState();
			// Apply the preservation rule to each factoid.
			for (CPFactoid factoid : input.factoids) {
				if (!factoid.hasVar(this.lhs)) {
					result.factoids.add(factoid);
				}
			}
			// Apply the new factoid rule.
			result.factoids.add(newFactoid);
			return result;
		}
	}

	@Override
	public CPState reduce(CPState input) {
		return input;
	}
}