package bgu.cs.absint.analyses.lin;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code x=y+c} and {@code x=c+y}.
 * 
 * @author romanm
 */
class TransformerAssignAddLocalConstantToLocal extends
		UnaryOperation<LinState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local op1;
	protected final IntConstant op2;
	protected final LinFactoid newFactoid;

	public TransformerAssignAddLocalConstantToLocal(Local lhs, Local op1,
			IntConstant op2) {
		this.lhs = lhs;
		this.op1 = op1;
		this.op2 = op2;
		newFactoid = new LinFactoid(lhs, op1, IntConstant.v(1), op2);
	}

	@Override
	public LinState apply(LinState input) {
		// Special treatment for bottom.
		if (input.equals(LinState.bottom))
			return LinState.bottom;

		LinState result = input.copy();
		result.removeVar(lhs);
		result.add(newFactoid);
		return result;
	}
}