package bgu.cs.absint.analyses.ve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import soot.Local;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import bgu.cs.absint.ComposedOperation;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.EqualityRefiner;
import bgu.cs.absint.soot.TransformerMatcher;
import bgu.cs.util.Pair;

/**
 * Implementation of abstract operations for a static analysis for tracking
 * equalities between variables.
 * 
 * @author romanm
 * 
 */
public class VEDomain extends AbstractDomain<VEState, Unit> implements
		EqualityRefiner<Local> {
	/**
	 * Singleton value.
	 */
	private static final VEDomain v = new VEDomain();

	protected VEMatcher matcher = new VEMatcher();

	public static final VEDomain v() {
		return v;
	}

	@Override
	public VEState getBottom() {
		return VEState.bottom;
	}

	@Override
	public VEState getTop() {
		return VEState.top;
	}

	@Override
	public VEState ub(VEState first, VEState second) {
		if (first == VEState.bottom) {
			return second;
		} else if (second == VEState.bottom) {
			return first;
		} else {
			// Compute the intersection of the two sets of {@link VEFactois}.
			VEState result = new VEState(first);
			result.factoids.retainAll(second.factoids);
			return result;
		}
	}

	@Override
	public VEState lb(VEState first, VEState second) {
		if (first == VEState.bottom || second == VEState.bottom) {
			return VEState.bottom;
		} else {
			// Compute the union of the two sets of varToFactoid.
			VEState result = new VEState(first);
			result.factoids.addAll(second.factoids);
			return result;
		}
	}

	@Override
	public boolean leq(VEState first, VEState second) {
		if (first == VEState.bottom) {
			return true;
		} else if (second == VEState.bottom) {
			// first != bottom
			return false;
		} else {
			return first.factoids.containsAll(second.factoids);
		}
	}

	@Override
	public UnaryOperation<VEState> getTransformer(Unit stmt) {
		UnaryOperation<VEState> vanillaTransformer = matcher
				.getTransformer(stmt);
		if (vanillaTransformer.equals(IdOperation.v())) {
			// An optimization - no need to run a reduction after an identity
			// transformer.
			return vanillaTransformer;
		} else {
			return ComposedOperation.compose(vanillaTransformer,
					getReductionOperation());
		}
	}

	/**
	 * A reduction operator (we called it Explicate in class) for the variable
	 * equalities abstract domain. The operator adds varToFactoid by computing
	 * the transitive closure of the equality relation.
	 */
	@Override
	public VEState reduce(VEState input) {
		// Special treatment for bottom.
		if (input.equals(VEState.bottom))
			return VEState.bottom;
		if (input.equals(VEState.top))
			return VEState.top;

		VEState result = new VEState(input);
		boolean change = true;
		while (change) {
			change = false;
			for (VEFactoid factoid1 : result.factoids) {
				for (VEFactoid factoid2 : result.factoids) {
					if (factoid1.equals(factoid2))
						continue;

					// Check whether there is a common variable, which means
					// we may apply the transitive closure rule and get a
					// new factoid.
					if (factoid2.hasVar(factoid1.lhs)
							|| factoid2.hasVar(factoid1.rhs)) {
						Local lhs = null;
						Local rhs = null;
						if (factoid1.lhs != factoid2.lhs) {
							lhs = factoid1.lhs;
							rhs = factoid2.lhs;
						} else if (factoid1.lhs != factoid2.rhs) {
							lhs = factoid1.lhs;
							rhs = factoid2.rhs;
						} else if (factoid1.rhs != factoid2.lhs) {
							lhs = factoid1.rhs;
							rhs = factoid2.lhs;
						} else if (factoid1.rhs != factoid2.rhs) {
							lhs = factoid1.rhs;
							rhs = factoid2.rhs;
						}

						if (lhs != rhs) {
							VEFactoid newFactoid = new VEFactoid(lhs, rhs);
							if (!result.factoids.contains(newFactoid)) {
								result.factoids.add(newFactoid);
								change = true;
							}
						}
					}
					if (change) // Avoids concurrent modification
								// exceptions.
						break;
				}
				if (change) // Avoids concurrent modification exceptions.
					break;
			}
		}
		return result;
	}

	@Override
	public VEState refineByEqualities(Object input,
			Collection<Pair<Local, Local>> equalities) {
		VEState state = (VEState) input;
		if (state == getBottom())
			return null;

		VEState result = state.copy();
		boolean change = false;
		for (Pair<Local, Local> equality : equalities) {
			change |= result.addFactoid(equality.first, equality.second);
		}
		if (change)
			return result;
		else
			return null;
	}

	@Override
	public Collection<Pair<Local, Local>> inferEqualities(Object input) {
		VEState state = (VEState) input;
		if (state == getBottom())
			return Collections.emptyList();

		ArrayList<Pair<Local, Local>> result = new ArrayList<>();
		for (VEFactoid factoid : state.factoids) {
			result.add(new Pair<>(factoid.lhs, factoid.rhs));
		}
		return result;
	}

	/**
	 * Singleton pattern.
	 */
	private VEDomain() {
	}

	/**
	 * A helper class for matching transformers to statements.
	 * 
	 * @author romanm
	 */
	protected class VEMatcher extends TransformerMatcher<VEState> {
		@Override
		public void matchAssumeLocalEqLocal(IfStmt stmt, boolean polarity,
				Local lhs, Local rhs) {
			if (lhs.equals(rhs)) {
				transformer = IdOperation.v();
			} else {
				if (polarity == true) { // Meaning assume lhs=c.
					transformer = new AssignAssumeVarEqualVarTransformer(lhs,
							rhs);
				} else { // Meaning assume lhs!=c
					transformer = new AssumeVarNeqVarTransformer(lhs, rhs);
				}
			}
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
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs,
				ParameterRef rhs) {
			transformer = new AssignTopTransformer(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ThisRef rhs) {
			transformer = new AssignTopTransformer(lhs);
		}

		/**
		 * A conservatively handling assignments from expressions by projecting
		 * out the assigned variable.
		 */
		@Override
		public void matchAssignExprToLocal(AssignStmt stmt, Local lhs, Expr rhs) {
			transformer = new AssignTopTransformer(lhs);
		}
	}

	/**
	 * A transformer for assume lhs!=c.
	 * 
	 * @author romanm
	 * 
	 */
	protected static class AssumeVarNeqVarTransformer extends
			UnaryOperation<VEState> {
		protected final Local lhs;
		protected final Local rhs;
		protected final VEFactoid checkedFactoid;

		public AssumeVarNeqVarTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			assert !lhs.equals(rhs);
			checkedFactoid = new VEFactoid(lhs, rhs);
		}

		/**
		 * Check whether the set of varToFactoid contains lhs=c, which negates
		 * the assumed expression. If so, return bottom, otherwise return the
		 * input state.
		 */
		@Override
		public VEState apply(VEState input) {
			// Special treatment for bottom.
			if (input.equals(VEState.bottom))
				return VEState.bottom;

			if (input.factoids.contains(checkedFactoid))
				return VEState.bottom;
			else
				return input;
		}
	}

	/**
	 * A transformer for an assignment between two different variables. (Can
	 * handle assignment of a variable to itself, but loses precision
	 * needlessly.)
	 * 
	 * @author romanm
	 */
	protected static class AssignVarToVarTransformer extends
			UnaryOperation<VEState> {
		protected final Local lhs;
		protected final Local rhs;
		protected final VEFactoid newFactoid;

		public AssignVarToVarTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			assert !lhs.equals(rhs);
			newFactoid = new VEFactoid(lhs, rhs);
		}

		/**
		 * Apply the following two rules:<br>
		 * 1. Preservation rule: {a=b} lhs=c {a=b} if a!=lhs and b!=lhs<br>
		 * 2. New factoid rule: { } lhs=c {lhs=c}
		 */
		@Override
		public VEState apply(VEState input) {
			// Special treatment for bottom.
			if (input.equals(VEState.bottom))
				return VEState.bottom;

			VEState result = new VEState();
			// Apply the preservation rule to each factoid.
			for (VEFactoid factoid : input.factoids) {
				if (!factoid.hasVar(lhs)) {
					result.factoids.add(factoid);
				}
			}
			// Apply the new factoid rule.
			result.factoids.add(newFactoid);

			return result;
		}
	}

	/**
	 * A transformer for an assume x==y.
	 * 
	 * @author romanm
	 */
	protected static class AssignAssumeVarEqualVarTransformer extends
			UnaryOperation<VEState> {
		protected final Local lhs;
		protected final Local rhs;
		protected final VEFactoid newFactoid;

		public AssignAssumeVarEqualVarTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
			assert !lhs.equals(rhs);
			newFactoid = new VEFactoid(lhs, rhs);
		}

		/**
		 * Apply the following rule:<br>
		 * New factoid rule: { } lhs=c {lhs=c}
		 */
		@Override
		public VEState apply(VEState input) {
			// Special treatment for bottom.
			if (input.equals(VEState.bottom))
				return VEState.bottom;

			VEState result = input.copy();
			// Apply the new factoid rule.
			result.factoids.add(newFactoid);

			return result;
		}
	}

	/**
	 * A transformer that removes all varToFactoid containing a given variable.
	 * It can be used to conservatively handle any statement that has the effect
	 * of modifying a given local variable (and only it).
	 * 
	 * @author romanm
	 * 
	 */
	protected static class AssignTopTransformer extends UnaryOperation<VEState> {
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
		public AssignTopTransformer(Local lhs) {
			this.lhs = lhs;
		}

		@Override
		public VEState apply(VEState input) {
			// Special treatment for bottom.
			if (input.equals(VEState.bottom))
				return VEState.bottom;

			VEState result = new VEState();
			for (VEFactoid factoid : input.factoids) {
				if (!factoid.hasVar(lhs)) {
					result.factoids.add(factoid);
				}
			}
			return result;
		}
	}
}