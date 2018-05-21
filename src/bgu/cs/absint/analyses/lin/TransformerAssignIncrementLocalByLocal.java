package bgu.cs.absint.analyses.lin;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form assume {@code assume x=x+y} for
 * variables 'x' and 'y'.
 * 
 * @author romanm
 */
class TransformerAssignIncrementLocalByLocal extends
		UnaryOperation<LinState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local op2;

	public TransformerAssignIncrementLocalByLocal(Local lhs, Local op2) {
		this.lhs = lhs;
		this.op2 = op2;
	}

	@Override
	public LinState apply(LinState input) {
		// Special treatment for bottom.
		if (input.equals(LinState.bottom))
			return LinState.bottom;

		// Find cases where x=a*p+b and y=c*p+d and add x=(a+c)*p+(b+d).
		LinState newFacts = new LinState();
		for (LinFactoid op1Factoid : input.getFactoids(lhs)) {
			Local rvar1 = op1Factoid.rvar;
			for (LinFactoid op2Factoid : input.getFactoids(op2)) {
				Local rvar2 = op2Factoid.rvar;
				if (rvar1.equivTo(rvar2)) {
					IntConstant coefficient = (IntConstant) op1Factoid.coefficient
							.add(op2Factoid.coefficient);
					IntConstant additive = (IntConstant) op1Factoid.additive
							.add(op2Factoid.additive);
					LinFactoid newFactoid = new LinFactoid(lhs, rvar1,
							coefficient, additive);
					newFacts.add(newFactoid);
				}
			}
		}

		LinState result = input.copy();
		result.removeVar(lhs);
		for (LinFactoid f : newFacts) {
			result.add(f);
		}

		return result;
	}
}