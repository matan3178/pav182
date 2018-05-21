package bgu.cs.absint.analyses.ae;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import soot.Local;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import bgu.cs.absint.ComposedOperation;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.EqualityRefiner;
import bgu.cs.absint.soot.ExprContainsLocal;
import bgu.cs.absint.soot.ForgetVarTransformer;
import bgu.cs.absint.soot.IsPureExpr;
import bgu.cs.absint.soot.TransformerMatcher;
import bgu.cs.util.Pair;

/**
 * Implementation of abstract operations for a static analysis for tracking
 * equalities between variables.
 * 
 * @author romanm
 * 
 */
public class AEDomain extends AbstractDomain<AEState, Unit> implements
		EqualityRefiner<Local> {
	/**
	 * Singleton value.
	 */
	private static final AEDomain v = new AEDomain();

	protected AEMatcher matcher = new AEMatcher();

	public static final AEDomain v() {
		return v;
	}

	@Override
	public AEState getBottom() {
		return AEState.bottom;
	}

	@Override
	public AEState getTop() {
		return AEState.top;
	}

	@Override
	public AEState ub(AEState first, AEState second) {
		if (first == AEState.bottom) {
			return second;
		} else if (second == AEState.bottom) {
			return first;
		} else {
			// Compute the intersection of the two sets of varToFactoid.
			AEState result = new AEState(first);
			result.factoids.retainAll(second.factoids);
			return result;
		}
	}

	@Override
	public AEState lb(AEState first, AEState second) {
		if (first == AEState.bottom || second == AEState.bottom) {
			return AEState.bottom;
		} else {
			// Compute the union of the two sets of varToFactoid.
			AEState result = new AEState(first);
			result.factoids.addAll(second.factoids);
			return result;
		}
	}

	@Override
	public boolean leq(AEState first, AEState second) {
		if (first == AEState.bottom) {
			return true;
		} else if (second == AEState.bottom) {
			// first != bottom
			return false;
		} else {
			return first.factoids.containsAll(second.factoids);
		}
	}

	@Override
	public AEState refineByEqualities(Object input,
			Collection<Pair<Local, Local>> equalities) {
		AEState state = (AEState) input;
		if (state == getBottom())
			return null;

		AEState result = state.copy();
		boolean change = false;
		for (Pair<Local, Local> equality : equalities) {
			for (AEFactoid factoid : state.factoids) {
				if (factoid.hasVar(equality.first)) {
					if (factoid.lhs.equals(equality.first)) {
						// Substitute the left-hand side variable of the factoid
						// with equality.second.
						AEFactoid newFactoid = new AEFactoid(equality.second,
								factoid.rhs);
						change |= result.add(newFactoid);

						// TODO: substitute in the right-hand side expression.
					}

				}
				if (factoid.hasVar(equality.second)) {
					if (factoid.lhs.equals(equality.second)) {
						// Substitute the left-hand side variable of the factoid
						// with equality.first.
						AEFactoid newFactoid = new AEFactoid(equality.first,
								factoid.rhs);
						change |= result.add(newFactoid);

						// TODO: substitute in the right-hand side expression.
					}
				} else {
				}
			}
		}

		if (change)
			return result;
		else
			return null;
	}

	@Override
	public Collection<Pair<Local, Local>> inferEqualities(Object input) {
		AEState state = (AEState) input;
		if (state == getBottom())
			return Collections.emptyList();

		HashSet<Pair<Local, Local>> result = new HashSet<>();
		for (AEFactoid factoid1 : state.factoids) {
			for (AEFactoid factoid2 : state.factoids) {
				if (factoid1 != factoid2 && factoid1.rhs.equivTo(factoid2.rhs)) {
					result.add(new Pair<>(factoid1.lhs, factoid2.lhs));
				}
			}
		}
		return result;
	}

	@Override
	public UnaryOperation<AEState> getTransformer(Unit stmt) {
		UnaryOperation<AEState> vanillaTransformer = matcher
				.getTransformer(stmt);
		return ComposedOperation.compose(vanillaTransformer,
				getReductionOperation());
	}

	/**
	 * Returns the identity operation to increase efficiency.
	 */
	@Override
	public UnaryOperation<AEState> getReductionOperation() {
		return IdOperation.v();
	}

	/**
	 * Singleton pattern.
	 */
	private AEDomain() {
	}

	/**
	 * A helper class for matching transformers to statements.
	 * 
	 * @author romanm
	 */
	protected class AEMatcher extends TransformerMatcher<AEState> {
		@Override
		public void matchAssignToLocal(AssignStmt stmt, Local lhs) {
			super.matchAssignToLocal(stmt, lhs);
			if (transformer == null)
				transformer = new ForgetVarTransformer<AEFactoid, AEState>(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs,
				ParameterRef rhs) {
			transformer = new ForgetVarTransformer<AEFactoid, AEState>(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ThisRef rhs) {
			transformer = new ForgetVarTransformer<AEFactoid, AEState>(lhs);
		}

		/**
		 * A conservatively handling assignments from expressions by projecting
		 * out the assigned variable.
		 */
		@Override
		public void matchAssignExprToLocal(AssignStmt stmt, Local lhs, Expr rhs) {
			if (IsPureExpr.v.check(rhs) && !ExprContainsLocal.v.check(rhs, lhs)) {
				transformer = new AssignExprToVarTransformer(lhs, rhs);
			} else {
				transformer = new ForgetVarTransformer<AEFactoid, AEState>(lhs);
			}
		}
	}

	protected static class AssignExprToVarTransformer extends
			UnaryOperation<AEState> {
		/**
		 * The variable being modified by the concrete semantics.
		 */
		protected Local lhs;
		protected Expr rhs;

		protected final AEFactoid newFactoid;

		public AssignExprToVarTransformer(Local lhs, Expr rhs) {
			this.lhs = lhs;
			newFactoid = new AEFactoid(lhs, rhs);
		}

		@Override
		public AEState apply(AEState input) {
			// Special treatment for bottom.
			if (input.equals(AEState.bottom))
				return AEState.bottom;

			AEState result = new AEState();
			for (AEFactoid factoid : input.factoids) {
				if (!factoid.hasVar(lhs)) {
					result.factoids.add(factoid);
				}
			}
			if (newFactoid != null)
				result.factoids.add(newFactoid);
			return result;
		}
	}
}