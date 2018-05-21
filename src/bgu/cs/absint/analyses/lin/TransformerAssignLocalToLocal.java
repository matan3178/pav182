package bgu.cs.absint.analyses.lin;

import bgu.cs.absint.UnaryOperation;
import bgu.cs.util.soot.CaseAssignLocal_LocalNonRef;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form assume {@code assume x=y} for
 * variables 'x' and 'y',
 * 
 * @author romanm
 */
class TransformerAssignLocalToLocal extends UnaryOperation<LinState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local rhs;
	protected final LinFactoid veqFactoid1;
	protected final LinFactoid veqFactoid2;

	public TransformerAssignLocalToLocal(CaseAssignLocal_LocalNonRef jimpleCase) {
		this(jimpleCase.lhsLocal, jimpleCase.rhsLocal);
	}

	public TransformerAssignLocalToLocal(Local lhs, Local rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		// lhs = c * 1 + 0
		veqFactoid1 = new LinFactoid(lhs, rhs, IntConstant.v(1), IntConstant.v(0));
		// c = lhs * 1 + 0
		veqFactoid2 = new LinFactoid(rhs, lhs, IntConstant.v(1), IntConstant.v(0));
	}

	@Override
	public LinState apply(LinState input) {
		// Special treatment for bottom.
		if (input.equals(LinState.bottom))
			return LinState.bottom;

		LinState result = input.copy();
		result.removeVar(lhs);
		LinFactoid constantRhs = result.getConstantFactoid(rhs);
		if (constantRhs != null)
			result.remove(constantRhs);
		result.add(veqFactoid1);
		result.add(veqFactoid2);

		return result;
	}
}