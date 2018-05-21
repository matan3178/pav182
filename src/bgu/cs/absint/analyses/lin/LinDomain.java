package bgu.cs.absint.analyses.lin;

import java.util.Collection;
import java.util.HashSet;

import soot.Local;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ThisRef;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.ConjunctiveDomain;
import bgu.cs.absint.constructor.EqualityRefiner;
import bgu.cs.absint.soot.ForgetVarTransformer;
import bgu.cs.absint.soot.TransformerMatcher;
import bgu.cs.util.Pair;

/**
 * Abstract domain operations for the domain of linear relations, which infers
 * facts of the form {@code y=a*x+b} for local variables 'x' and 'y' and integer
 * constants 'a' and 'b'.
 * 
 * @author romanm
 */
public class LinDomain extends
		ConjunctiveDomain<Local, LinFactoid, LinState, Unit> implements
		EqualityRefiner<Local> {
	/**
	 * Singleton value.
	 */
	static final LinDomain v = new LinDomain();

	protected LinMatcher matcher = new LinMatcher();

	public static final LinDomain v() {
		return v;
	}

	@Override
	public LinState getBottom() {
		return LinState.bottom;
	}

	@Override
	public LinState getTop() {
		return LinState.top;
	}

	@Override
	public LinState ub(LinState elem1, LinState elem2) {
		if (elem1.equals(getBottom()))
			return elem2;
		if (elem2.equals(getBottom()))
			return elem1;
		// An optimization for joining with top.
		if (elem1.equals(getTop()) || elem2.equals(getTop()))
			return getTop();

		// Start by taking the intersection of the two sets of facts.
		HashSet<LinFactoid> intersection = new HashSet<>();
		intersection.addAll(elem1.getFactoids());
		intersection.retainAll(elem2.getFactoids());
		LinState result = new LinState(intersection);

		// Generalize pairs of constant varToFactoid
		// (x=a1, y=b1) from elem1 and (x=a2, y=b2) from elem2
		// such that (a2-a1) divides (b2-b1) into
		// alpha = (b2-b1)/(a2-a1)
		// y = alpha*x + a2-alpha*a1
		for (LinFactoid e1f1 : elem1.getConstantFactoids()) {
			for (LinFactoid e1f2 : elem1.getConstantFactoids()) {
				if (e1f2 == e1f1)
					continue;
				for (LinFactoid e2f1 : elem2.getConstantFactoids()) {
					if (!e1f1.lvar.equivTo(e2f1.lvar))
						continue;
					for (LinFactoid e2f2 : elem2.getConstantFactoids()) {
						if (e2f2 == e2f1)
							continue;
						if (!e1f2.lvar.equivTo(e2f2.lvar))
							continue;
						int deltaY = e2f2.additive.value - e1f2.additive.value;
						int deltaX = e2f1.additive.value - e1f1.additive.value;
						if (deltaX == 0)
							continue;
						if (deltaY % deltaX == 0) {
							int coefficient = deltaY / deltaX;
							int additive = e1f2.additive.value - coefficient
									* e1f1.additive.value;
							LinFactoid newFactoid = new LinFactoid(e1f2.lvar,
									e1f1.lvar, IntConstant.v(coefficient),
									IntConstant.v(additive));
							result.add(newFactoid);
						}
					}
				}
			}
		}

		// Now look for cases of (x=a1, y=b1) in elem1 and y=a*x+b from elem2
		// such that b1=a*a1+b and vice verse (reversing the roles of elem1 and
		// elem2).
		for (LinFactoid f2 : elem2.getLinFactoids()) {
			for (LinFactoid a1f : elem1.getConstantFactoids()) {
				for (LinFactoid b1f : elem1.getConstantFactoids()) {
					if (a1f == b1f)
						continue;
					int a1 = a1f.additive.value;
					int b1 = b1f.additive.value;

					int a = f2.coefficient.value;
					int b = f2.additive.value;

					if (b1 == a * a1 + b)
						result.add(f2);
				}
			}
		}

		for (LinFactoid f1 : elem1.getLinFactoids()) {
			for (LinFactoid a2f : elem2.getConstantFactoids()) {
				for (LinFactoid b2f : elem2.getConstantFactoids()) {
					if (a2f == b2f)
						continue;
					int a1 = a2f.additive.value;
					int b1 = b2f.additive.value;

					int a = f1.coefficient.value;
					int b = f1.additive.value;

					if (b1 == a * a1 + b)
						result.add(f1);
				}
			}
		}

		return result;
	}

	@Override
	public LinState createEmpty() {
		return new LinState();
	}

	@Override
	public LinState reduce(LinState input) {
		// Special treatment for bottom.
		if (input == getBottom())
			return getBottom();

		// First infer more linear equalities from combinations of
		// linear equalities.
		boolean change = true;
		while (change) {
			change = false;
			LinState nextState = input.copy();
			for (LinFactoid f1 : input) {
				Local x = f1.lvar;
				IntConstant a = f1.coefficient;
				Local y = f1.rvar;
				IntConstant b = f1.additive;
				// x = a*y+b

				for (LinFactoid f2 : input) {
					Local z = f2.lvar;
					IntConstant c = f2.coefficient;
					Local w = f2.rvar;
					IntConstant d = f2.additive;
					// z = c*w+d

					if (y.equivTo(z)) {
						// x = a*(c*w+d)+b = (a*c)*w + (a*d+b)
						IntConstant coefficient = (IntConstant) a.multiply(c);
						IntConstant additive = (IntConstant) a.multiply(d).add(
								b);
						LinFactoid newFactoid = new LinFactoid(x, w,
								coefficient, additive);
						change |= nextState.add(newFactoid);
					}

					// Handle equal right-hand sides: x=a*y+b and z=c*w+d
					// where a=c and y=w and b=d.
					if (y.equivTo(w) && a.equivTo(c) && b.equivTo(d)) {
						LinFactoid newFactoid1 = new LinFactoid(x, z);
						change |= nextState.add(newFactoid1);
						LinFactoid newFactoid2 = new LinFactoid(z, x);
						change |= nextState.add(newFactoid2);
					}
				}
			}
			input = nextState;
		}

		// Now look for inconsistent linear varToFactoid: x=a*y+b and x=c*y+d
		// where either a!=c or b!=d.
		for (LinFactoid f1 : input.getLinFactoids()) {
			LinFactoid f2 = input.getLinFactoid(f1.lvar, f1.rvar);
			if (f2 != null && !f1.equals(f2))
				return getBottom();
		}

		// Now look for inconsistent constant varToFactoid: x=0*y+b and x=0*y+d
		// where b!=d.
		for (LinFactoid f1 : input.getConstantFactoids()) {
			LinFactoid f2 = input.getConstantFactoid(f1.lvar);
			if (f2 != null && !f1.additive.equivTo(f2.additive))
				return getBottom();
		}

		// Reduce {x=a, y=b, y=c*x+d} (which were already checked for
		// consistency) into {x=y, y=b}.
		LinState result = new LinState(input.getConstantFactoids());
		for (LinFactoid f : input.getLinFactoids()) {
			if (input.getConstantFactoid(f.lvar) != null
					&& input.getConstantFactoid(f.rvar) != null)
				continue;
			else
				result.add(f);
		}

		return result;
	}

	@Override
	public Object refineByEqualities(Object state,
			Collection<Pair<Local, Local>> equalities) {
		LinState input = (LinState) state;
		// Special treatment for bottom.
		if (input == getBottom())
			return null;

		boolean change = false;
		LinState result = input.copy();
		for (Pair<Local, Local> pair : equalities) {
			LinFactoid newFactoid = new LinFactoid(pair.first, pair.second);
			change |= result.add(newFactoid);
		}
		if (change == false)
			return null;
		else
			return result;
	}

	@Override
	public Collection<Pair<Local, Local>> inferEqualities(Object state) {
		LinState input = (LinState) state;
		HashSet<Pair<Local, Local>> result = new HashSet<>();
		// Special treatment for bottom.
		if (input == getBottom())
			return result;

		// If x=1*y+0 and x and y are different variables then x=y.
		for (LinFactoid f : input.getLinFactoids()) {
			if (!f.lvar.equivTo(f.rvar) && f.coefficient.value == 1
					&& f.additive.value == 0)
				result.add(new Pair<Local, Local>(f.lvar, f.rvar));
		}

		for (LinFactoid f1 : input.getConstantFactoids()) {
			for (LinFactoid f2 : input.getConstantFactoids()) {
				if (!f1.lvar.equivTo(f2.lvar)
						&& f1.additive.equivTo(f2.additive))
					result.add(new Pair<Local, Local>(f1.lvar, f2.lvar));
			}
		}
		return result;
	}

	@Override
	public UnaryOperation<LinState> getTransformer(Unit stmt) {
		return matcher.getTransformer(stmt);
	}

	/**
	 * Singleton pattern.
	 */
	private LinDomain() {
	}

	/**
	 * A helper class for matching transformers to statements.<br>
	 * TODO: Handle statements of the following forms: 'x=y*z', 'assume x==y',
	 * 'assume x!=c'.
	 * 
	 * @author romanm
	 */
	protected class LinMatcher extends TransformerMatcher<LinState> {
		@Override
		public void matchAssignToLocal(AssignStmt stmt, Local lhs) {
			super.matchAssignToLocal(stmt, lhs);
			if (transformer == null)
				transformer = new ForgetVarTransformer<LinFactoid, LinState>(
						lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs,
				ParameterRef rhs) {
			transformer = new ForgetVarTransformer<LinFactoid, LinState>(lhs);
		}

		@Override
		public void matchIdentityStmt(IdentityStmt stmt, Local lhs, ThisRef rhs) {
			transformer = new ForgetVarTransformer<LinFactoid, LinState>(lhs);
		}

		@Override
		public void matchAssignLocalToLocal(AssignStmt stmt, Local lhs,
				Local rhs) {
			if (lhs.equals(rhs)) {
				transformer = IdOperation.v();
			} else {
				transformer = new TransformerAssignLocalToLocal(lhs, rhs);
			}
		}

		/**
		 * Handle statements of the form x=c where 'c' is an integer constant.
		 */
		@Override
		public void matchAssignConstantToLocal(AssignStmt stmt, Local lhs,
				Constant rhs) {
			if (rhs instanceof IntConstant)
				transformer = new TransformerAssignConstantToVar(lhs,
						(IntConstant) rhs);
		}

		/**
		 * Handle statements of the form x=a+b where 'a' and 'b' are either
		 * variables of constants.
		 */
		@Override
		public void matchAssignAddLocalLocalToLocal(AssignStmt stmt, Local lhs,
				Local op1, Local op2) {
			if (lhs.equivTo(op1)) {
				transformer = new TransformerAssignIncrementLocalByLocal(lhs,
						op2);
			} else if (lhs.equivTo(op2)) {
				transformer = new TransformerAssignIncrementLocalByLocal(lhs,
						op1);
			} else {
				transformer = new TransformerAssignAddLocalLocalToLocal(lhs,
						op1, op2);
			}
		}

		/**
		 * Matches statements of the form {@code x=y+c} and {@code x=c+y}.
		 */
		@Override
		public void matchAssignAddLocalConstantToLocal(AssignStmt stmt,
				Local lhs, Local op1, Constant op2) {
			if (op2 instanceof IntConstant) {
				// Special case x=x+c.
				if (lhs.equivTo(op1))
					transformer = new TransformerAssignIncrementLocalByConstant(
							lhs, (IntConstant) op2);
				// General case.
				else
					transformer = new TransformerAssignAddLocalConstantToLocal(
							lhs, op1, (IntConstant) op2);
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
				// Special case x=x+c.
				if (lhs.equivTo(op1))
					transformer = new TransformerAssignIncrementLocalByConstant(
							lhs, (IntConstant) op2);
				// General case.
				else
					transformer = new TransformerAssignAddLocalConstantToLocal(
							lhs, op1, (IntConstant) op2);
			}
		}

		@Override
		public void matchAssignMulLocalConstantToLocal(AssignStmt stmt,
				Local lhs, Local op1, Constant op2) {
			if (op2 instanceof IntConstant)
				transformer = new TransformerAssignMulLocalConstantToLocal(lhs,
						op1, (IntConstant) op2);
		}

		@Override
		public void matchAssumeLocalEqConstant(IfStmt stmt, boolean polarity,
				Local lhs, Constant rhs) {
			if (rhs instanceof IntConstant)
				transformer = new TransformerAssumeLocalEqConstant(polarity,
						lhs, (IntConstant) rhs);
		}

		@Override
		public void matchAssumeLocalEqLocal(IfStmt stmt, boolean polarity,
				Local lhs, Local rhs) {
			transformer = new TransformerAssumeLocalEqLocal(LinDomain.this, polarity, lhs, rhs);
		}
	}

}