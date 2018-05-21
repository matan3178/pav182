package bgu.cs.absint.analyses.lin;

import bgu.cs.absint.AssumeTransformer;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code if (x==y)} and
 * {@code if (x!=y)} for variable 'x' and 'y'.
 * 
 * @author romanm
 */
class TransformerAssumeLocalEqLocal extends
		AssumeTransformer<LinState> {
	/**
	 * 
	 */
	private final LinDomain linDomain;
	protected final Local lhs;
	protected final Local rhs;
	protected final LinState conditionState;

	public TransformerAssumeLocalEqLocal(LinDomain linDomain, boolean polarity, Local lhs,
			Local rhs) {
		super(polarity);
		this.linDomain = linDomain;
		this.lhs = lhs;
		this.rhs = rhs;
		this.conditionState = new LinState();
		conditionState.add(new LinFactoid(lhs, rhs));
		conditionState.add(new LinFactoid(rhs, lhs)); // Semantic reduction.
	}

	@Override
	public String toString() {
		if (polarity)
			return "Lin[" + lhs.toString() + "==" + rhs.toString() + "]";
		else
			return "Lin[" + lhs.toString() + "!=" + rhs.toString() + "]";
	}

	@Override
	public LinState apply(LinState input) {
		if (polarity) {
			return this.linDomain.lb(input, conditionState);
		} else {
			// If the input state has lhs=a and c=b and a=b then we have
			// a contradiction.
			LinFactoid lhsVal = input.getConstantFactoid(lhs);
			LinFactoid rhsVal = input.getConstantFactoid(rhs);
			if (lhsVal != null && rhsVal != null
					&& lhsVal.additive.equivTo(rhsVal.additive)) {
				return LinState.bottom;
			}

			// If the input state has lhs=1*c+0 then we have
			// a contradiction.
			LinFactoid eqFactoid = input.getLinFactoid(lhs, rhs);
			if (eqFactoid != null
					&& eqFactoid.coefficient.equivTo(IntConstant.v(1))
					&& eqFactoid.additive.equivTo(IntConstant.v(0)))
				return LinState.bottom;

			return input;
		}
	}
}