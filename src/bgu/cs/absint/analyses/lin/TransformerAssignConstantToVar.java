package bgu.cs.absint.analyses.lin;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form assume {@code assume x=c} for a
 * variable 'x' and constant 'c',
 * 
 * @author romanm
 */
class TransformerAssignConstantToVar extends
		UnaryOperation<LinState> {
	protected final Local lhs;
	protected final IntConstant rhs;
	protected final LinFactoid newFactoid;

	public TransformerAssignConstantToVar(Local lhs, IntConstant rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		assert !lhs.equals(rhs);
		newFactoid = new LinFactoid(lhs, rhs);
	}

	@Override
	public LinState apply(LinState input) {
		// Special treatment for bottom.
		if (input.equals(LinState.bottom))
			return LinState.bottom;

		LinState result = new LinState(input);
		result.removeVar(lhs);
		result.add(newFactoid);
		return result;
	}
}