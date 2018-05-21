package bgu.cs.absint.analyses.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import bgu.cs.absint.ComposedOperation;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.EqualityRefiner;
import bgu.cs.absint.soot.ForgetVarTransformer;
import bgu.cs.absint.soot.TransformerMatcher;
import bgu.cs.util.Pair;
import soot.Local;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;

/**
 * Implementation of abstract operations for a static analysis for the Zone
 * abstract domain.
 * 
 * @author ???
 * 
 */
public class ZoneDomain extends AbstractDomain<ZoneState, Unit> implements EqualityRefiner<Local> {
	public static final IntConstant zero = IntConstant.v(0);

	/**
	 * Singleton value.
	 */
	private static final ZoneDomain v = new ZoneDomain();

	/**
	 * Matches statements to transformers.
	 */
	protected ZonesMatcher matcher = new ZonesMatcher();

	public static final ZoneDomain v() {
		return v;
	}

	@Override
	public ZoneState getBottom() {
		return ZoneState.bottom;
	}

	@Override
	public ZoneState getTop() {
		return ZoneState.top;
	}

	@Override
	public ZoneState ub(ZoneState first, ZoneState second) {
		if (first == ZoneState.bottom || second == ZoneState.top) {
			return second;
		} else if (second == ZoneState.bottom || first == ZoneState.top) {
			return first;
		} else {
			// Compute the max of the every entry of the two sets of {@link ZoneFactoids}.
			// The obvious max is infinite, which is the default. so if a relation only
			// appears in one difference matrix, we won't include it.
			ZoneState result = new ZoneState();
			for (ZoneFactoid fact : first.getFactoids()) {
				if (fact.existIn(second)) {
					ZoneFactoid newFact = fact.max(second);
					if (newFact != null)
						result.add(newFact);
				}
			}
			return ZoneDomain.essential(result);
		}
	}

	@Override
	public ZoneState lb(ZoneState first, ZoneState second) {
		if (first == ZoneState.bottom || second == ZoneState.bottom) {
			return ZoneState.bottom;
		} else {
			// Compute the min of the every entry of the two sets of {@link ZoneFactoids}.
			ZoneState result = new ZoneState(first);
			for (ZoneFactoid fact : second.getFactoids()) {
				result.add(fact.min(first));
			}
			return ZoneDomain.essential(result);
		}
	}

	@Override
	public boolean leq(ZoneState first, ZoneState second) {
		if (first == ZoneState.bottom || second == ZoneState.top) {
			return true;
		} else if (second == ZoneState.bottom || first == ZoneState.top) {
			// first != bottom
			return false;
		}
		// a loop over second.factoids and make sure each fact is covered by one of this
		// facts
		for (ZoneFactoid secondFact : second.getFactoids()) {
			boolean covered = false;
			for (ZoneFactoid firstFact : first.getFactoids()) {
				if (firstFact.imply(secondFact)) {
					covered = true;
					break;
				}
			}
			if (!covered)
				return false;
		}
		return true;
	}

	@Override
	public ZoneState widen(ZoneState first, ZoneState second) {
		if (first == ZoneState.bottom) {
			return second;
		}
		ZoneState newState = new ZoneState();
		ZoneState reducedSecond = reduce(second); // getting rid of redundancy
		for (ZoneFactoid f : first.getFactoids()) {
			if (!f.ImpliedByNEQ(reducedSecond))
				newState.add(f);
		}
		return newState;

	}

	@Override
	public ZoneState narrow(ZoneState first, ZoneState second) {
		if (second == ZoneState.bottom) {
			return first;
		}
		ZoneState newState = reduce(first);
		for (ZoneFactoid f : second.getFactoids()) {
			if (!f.existIn(newState)) {
				newState.add(f);
			}
		}
		return newState;
	}

	@Override
	public UnaryOperation<ZoneState> getTransformer(Unit stmt) {
		UnaryOperation<ZoneState> vanillaTransformer = matcher.getTransformer(stmt);
		if (vanillaTransformer.equals(IdOperation.v())) {
			// An optimization - no need to run a reduction after an identity
			// transformer.
			return vanillaTransformer;
		} else {
			return ComposedOperation.compose(vanillaTransformer, getReductionOperation());
		}
	}

	/**
	 * A reduction operator. The operator adds factoids by computing the transitive
	 * closure of the zone relation.
	 */
	@Override
	public ZoneState reduce(ZoneState input) {
		// Special treatment for bottom.
		if (input.equals(ZoneState.bottom))
			return ZoneState.bottom;
		if (input.equals(ZoneState.top))
			return ZoneState.top;

		ZoneState result = new ZoneState(input);
		boolean changed = true;
		while (changed) {
			result = essential(result);
			if (result.equivToBottom()) {
				return ZoneState.bottom;
			}
			changed = false;
			for (ZoneFactoid f1 : result.getFactoids()) {
				for (ZoneFactoid f2 : result.getFactoids()) { // checking if f1 and f2 has a transitive fact for f1.lhs
					if (f1.rhs.equals(f2.lhs)) {
						IntConstant add = IntConstant.v(f1.bound.value + f2.bound.value);
						ZoneFactoid transFact = new ZoneFactoid(f1.lhs, f2.rhs, add);
						if (!transFact.ImpliedBy(result) && !transFact.lhs.equals(transFact.rhs)) {
							result.add(transFact);
							changed = true;
							break; // loop invalid after changing result
						}
					}
				}
				if (changed)
					break; // loop invalid after changing result
			}
		}
		result = essential(result);
		if (result.equivToBottom()) {
			return ZoneState.bottom;
		}
		return result;
	}

	// removes redundant factoids.
	static public ZoneState essential(ZoneState input) {
		ZoneState finalRes = new ZoneState();
		for (ZoneFactoid f1 : input.getFactoids()) {
			finalRes.add(f1.min(input));
		}
		return finalRes;
	}

	@Override
	public ZoneState refineByEqualities(Object input, Collection<Pair<Local, Local>> equalities) {
		ZoneState state = (ZoneState) input;
		if (state == getBottom())
			return null;

		ZoneState result = state.copy();
		boolean change = false;
		for (Pair<Local, Local> equality : equalities) {
			change |= result.addFactoid(equality.first, equality.second, IntConstant.v(0));
		}
		if (change)
			return result;
		else
			return null;
	}

	@Override
	public Collection<Pair<Local, Local>> inferEqualities(Object input) {
		ZoneState state = (ZoneState) input;
		if (state == getBottom())
			return Collections.emptyList();

		ArrayList<Pair<Local, Local>> result = new ArrayList<>();
		for (ZoneFactoid factoid : state.factoids) {
			if (factoid.bound.equivTo(zero))
				result.add(new Pair<>(factoid.lhs, factoid.rhs));
		}
		return result;
	}

	/**
	 * Singleton pattern.
	 */
	private ZoneDomain() {
	}

	/**
	 * A helper class for matching transformers to statements.
	 * 
	 * @author romanm
	 */
	protected class ZonesMatcher extends TransformerMatcher<ZoneState> {
		@Override
		public void matchAssignToLocal(AssignStmt stmt, Local lhs) {
			super.matchAssignToLocal(stmt, lhs);
			if (transformer == null)
				transformer = new ForgetVarTransformer<ZoneFactoid, ZoneState>(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ParameterRef rhs) {
			transformer = new ForgetVarTransformer<ZoneFactoid, ZoneState>(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ThisRef rhs) {
			transformer = new ForgetVarTransformer<ZoneFactoid, ZoneState>(lhs);
		}

		@Override
		public void matchAssignLocalToLocal(AssignStmt stmt, Local lhs, Local rhs) {
			if (lhs.equals(rhs)) {
				transformer = IdOperation.v();
			} else {
				transformer = new TransformerAssignLocalToLocal(lhs, rhs);
			}
		}

		/**
		 * Matches statements of the form {@code x=y-z}.
		 */
		@Override
		public void matchAssignSubLocalLocalToLocal(AssignStmt stmt, Local lhs, Local op1, Local op2) {
			transformer = new TransformerAssignSubLocalLocalToLocal(lhs, op1, op2);

		}

		/**
		 * Handle statements of the form x=c where 'c' is an integer constant.
		 */
		@Override
		public void matchAssignConstantToLocal(AssignStmt stmt, Local lhs, Constant rhs) {
			if (rhs instanceof IntConstant)
				transformer = new TransformerAssignConstantToVar(lhs, (IntConstant) rhs);
		}

		/**
		 * Handle statements of the form x=a+b where 'a' and 'b' are either variables of
		 * constants.
		 */
		@Override
		public void matchAssignAddLocalLocalToLocal(AssignStmt stmt, Local lhs, Local op1, Local op2) {
			if (lhs.equivTo(op1)) {
				transformer = new TransformerAssignIncrementLocalByLocal(lhs, op2);
			} else if (lhs.equivTo(op2)) {
				transformer = new TransformerAssignIncrementLocalByLocal(lhs, op1);
			} else {
				transformer = new TransformerAssignAddLocalLocalToLocal(lhs, op1, op2);
			}
		}

		/**
		 * Matches statements of the form {@code x=y+c} and {@code x=c+y}.
		 */
		@Override
		public void matchAssignAddLocalConstantToLocal(AssignStmt stmt, Local lhs, Local op1, Constant op2) {
			if (op2 instanceof IntConstant) {
				// Special case x=x+c.
				if (lhs.equivTo(op1))
					transformer = new TransformerAssignIncrementLocalByConstant(lhs, (IntConstant) op2);
				// General case.
				else
					transformer = new TransformerAssignAddLocalConstantToLocal(lhs, op1, (IntConstant) op2);
			}
		}

		/**
		 * Matches statements of the form {@code x=y-c}.
		 */
		@Override
		public void matchAssignSubLocalConstantToLocal(AssignStmt stmt, Local lhs, Local op1, Constant op2) {
			op2 = ((IntConstant) op2).multiply(IntConstant.v(-1));
			if (op2 instanceof IntConstant) {
				// Special case x=x+c.
				if (lhs.equivTo(op1))
					transformer = new TransformerAssignIncrementLocalByConstant(lhs, (IntConstant) op2);
				// General case.
				else
					transformer = new TransformerAssignAddLocalConstantToLocal(lhs, op1, (IntConstant) op2);
			}
		}

		@Override
		public void matchAssignMulLocalConstantToLocal(AssignStmt stmt, Local lhs, Local op1, Constant op2) {
			if (op2 instanceof IntConstant)
				transformer = new TransformerAssignMulLocalConstantToLocal(lhs, op1, (IntConstant) op2);
		}

		@Override
		public void matchAssumeLocalEqConstant(IfStmt stmt, boolean polarity, Local lhs, Constant rhs) {
			if (rhs instanceof IntConstant)
				transformer = new TransformerAssumeLocalEqConstant(polarity, lhs, (IntConstant) rhs);
		}

		@Override
		public void matchAssumeLocalEqLocal(IfStmt stmt, boolean polarity, Local lhs, Local rhs) {
			transformer = new TransformerAssumeLocalEqLocal(polarity, lhs, rhs);
		}

		/**
		 * Matches statements of the form x<y for two local variables 'x' and 'y'.
		 */
		@Override
		public void matchAssumeLocalLtLocal(IfStmt stmt, boolean polarity, Local lhs, Local rhs) {
			transformer = new TransformerAssumeLocalLtLocal(polarity, lhs, rhs);
		}

		/**
		 * Matches statements of the form x>y for two local variables 'x' and 'y'.
		 */
		@Override
		public void matchAssumeLocalGtLocal(IfStmt stmt, boolean polarity, Local lhs, Local rhs) {
			assert false : "should not be matched!";
		}

		/**
		 * Matches statements of the form x<c for a local variable 'x' and constant 'c'.
		 */
		@Override
		public void matchAssumeLocalLtConstant(IfStmt stmt, boolean polarity, Local lhs, Constant rhs) {
			if (rhs instanceof IntConstant)
				transformer = new TransformerAssumeLocalLtConstant(polarity, lhs, (IntConstant) rhs);
		}

		/**
		 * Matches statements of the form x>c for a local variable 'x' and constant 'c'.
		 */
		@Override
		public void matchAssumeLocalGtConstant(IfStmt stmt, boolean polarity, Local lhs, Constant rhs) {
			assert false : "should not be matched!";
		}
	}
}