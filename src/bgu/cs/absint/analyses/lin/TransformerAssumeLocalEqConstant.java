package bgu.cs.absint.analyses.lin;

import bgu.cs.absint.AssumeTransformer;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code if (x==c)} and
 * {@code if (x!=c)} for a variable 'x' and a constant 'c'.
 * 
 * @author romanm
 */
class TransformerAssumeLocalEqConstant extends
		AssumeTransformer<LinState> {
	protected final Local lhs;
	protected final IntConstant rhs;
	protected final LinState conditionState;

	public TransformerAssumeLocalEqConstant(boolean polarity, Local lhs,
			IntConstant rhs) {
		super(polarity);
		this.lhs = lhs;
		this.rhs = rhs;
		this.conditionState = new LinState();
		conditionState.add(new LinFactoid(lhs, rhs));
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
		// Special treatment for bottom.
		if (input.equals(LinState.bottom))
			return LinState.bottom;

		if (polarity) {
			return LinDomain.v.lb(input, conditionState);
		} else {
			// If the input state has lhs=a and the condition is lhs!=a then
			// we have a contradiction.
			LinFactoid lhsVal = input.getConstantFactoid(lhs);
			if (lhsVal != null && lhsVal.additive.equivTo(rhs))
				return LinState.bottom;
			else
				return input;
		}
	}
}