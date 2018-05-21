package bgu.cs.absint.analyses.set;

import soot.Local;
import soot.RefType;
import soot.Type;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.soot.TransformerMatcher;

public class SetDomain extends AbstractDomain<SetState, Unit> {
	protected SetMatcher matcher = new SetMatcher();

	@Override
	public SetState getBottom() {
		return SetState.bottom;
	}

	@Override
	public SetState getTop() {
		return SetState.top;
	}

	@Override
	public SetState ub(SetState elem1, SetState elem2) {
		if (elem1 == SetState.bottom) {
			return elem2;
		} else if (elem2 == SetState.bottom) {
			return elem1;
		} else {
			// Compute the intersection of the two sets of varToFactoid.
			SetState result = new SetState(elem1);
			result.factoids.retainAll(elem2.factoids);
			return result;
		}
	}

	@Override
	public SetState lb(SetState first, SetState second) {
		if (first == SetState.bottom || second == SetState.bottom) {
			return SetState.bottom;
		} else {
			// Compute the union of the two sets of varToFactoid.
			SetState result = new SetState(first);
			result.factoids.addAll(second.factoids);
			return result;
		}
	}

	@Override
	public boolean leq(SetState first, SetState second) {
		if (first == SetState.bottom) {
			return true;
		} else if (second == SetState.bottom) {
			// first != bottom
			return false;
		} else {
			return first.factoids.containsAll(second.factoids);
		}
	}

	@Override
	public UnaryOperation<SetState> getTransformer(Unit stmt) {
		return matcher.getTransformer(stmt);
	}

	/**
	 * Checks whether a given local variable has the type specified as the set
	 * class type.
	 */
	public boolean isSetRefType(Local var) {
		Type varType = var.getType();
		if (varType instanceof RefType) {
			RefType refType = (RefType) varType;
			if (refType.getClassName().contains("Set"))
				return true;
			else
				return false;
		} else {
			return false;
		}
	}

	/**
	 * A helper class for matching transformers to statements.
	 * 
	 * @author romanm
	 */
	protected class SetMatcher extends TransformerMatcher<SetState> {
		@Override
		public void caseInvokeStmt(InvokeStmt stmt) {
			InvokeExpr expr = stmt.getInvokeExpr();
			String methodName = expr.getMethod().getName();
			if (methodName.equals("analysisAssumeNotNull")) {
				if (expr.getArgCount() != 1) {
					throw new Error(methodName
							+ " expects one argument, but got "
							+ expr.getArgCount() + "!");
				}
				Local arg = (Local) expr.getArg(0);
				if (isSetRefType(arg)) {
					transformer = new AssumeNotNullTransformer(arg);
				}
			} else if (methodName.equals("add")) {
				if (expr.getArgCount() != 1) {
					throw new Error(methodName
							+ " expects one argument, but got "
							+ expr.getArgCount() + "!");
				}
				Local arg = (Local) expr.getArg(0);
				VirtualInvokeExpr vexpr = (VirtualInvokeExpr) expr;
				Local base = (Local) vexpr.getBase();
				if (isSetRefType(base)) {
					transformer = new AddTransformer(base, arg);
				}
			} else if (methodName.equals("clear")) {
				if (expr.getArgCount() != 0) {
					throw new Error(methodName
							+ " expects no arguments, but got "
							+ expr.getArgCount() + "!");
				}
				VirtualInvokeExpr vexpr = (VirtualInvokeExpr) expr;
				Local base = (Local) vexpr.getBase();
				if (isSetRefType(base)) {
					transformer = new ClearTransformer(base);
				}
			}
		}
	}

	/**
	 * A transformer for statements of the form {@code analysisAssumeNotNull(s)}
	 * .
	 * 
	 * @author romanm
	 */
	protected class AssumeNotNullTransformer extends UnaryOperation<SetState> {
		protected final Local lhs;

		public AssumeNotNullTransformer(Local lhs) {
			this.lhs = lhs;
		}

		@Override
		public SetState apply(SetState input) {
			SetState result = new SetState();
			for (EmptyFactoid f : input.getEmptyFactoids()) {
				result.add(f);
			}

			result.add(new NotNullFactoid(lhs));
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code s.clear()}.
	 * 
	 * @author romanm
	 */
	protected class ClearTransformer extends UnaryOperation<SetState> {
		protected final Local set;

		public ClearTransformer(Local set) {
			this.set = set;
		}

		@Override
		public SetState apply(SetState input) {
			SetState result = new SetState();
			for (NotNullFactoid f : input.getNotNullFactoids()) {
				result.add(f);
			}

			// pre { Empty(t) } post { EqualSets(s, t) }
			for (EmptyFactoid f : input.getEmptyFactoids()) {
				Local t = f.var;
				result.add(new EqualSetsFactoid(t, set));
				result.add(new EqualSetsFactoid(set, t));
			}

			// pre { NotNull(s) } post { Empty(s) }
			for (NotNullFactoid f : input.getNotNullFactoids()) {
				if (f.var.equivTo(set)) {
					result.add(new EmptyFactoid(set));
					break;
				}
			}
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code s.add(d)}.
	 * 
	 * @author romanm
	 */
	protected class AddTransformer extends UnaryOperation<SetState> {
		protected final Local set;
		protected final Local data;

		public AddTransformer(Local set, Local data) {
			this.set = set;
			this.data = data;
		}

		@Override
		public SetState apply(SetState input) {
			SetState result = new SetState();
			// pre { Union(t, s, d) } post { EqualSets(s, t) }
			for (UnionFactoid f : input.getUnionFactoids()) {
				if (f.setRhs.equivTo(set) && f.data.equivTo(data)) {
					result.add(new EqualSetsFactoid(set, f.setLhs));
					result.add(new EqualSetsFactoid(f.setLhs, set));
				}
			}

			// pre { EqualSets(s, t) } post { Union(s, t, d) }
			for (EqualSetsFactoid f : input.getEqualSetsFactoids()) {
				if (f.setLhs.equivTo(set)) {
					result.add(new UnionFactoid(set, f.setRhs, data));
				}
			}
			return result;
		}
	}
}