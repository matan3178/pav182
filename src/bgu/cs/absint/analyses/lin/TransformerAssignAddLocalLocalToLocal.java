package bgu.cs.absint.analyses.lin;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form assume {@code x=y+z}.
 * 
 * @author romanm
 */
class TransformerAssignAddLocalLocalToLocal extends
		UnaryOperation<LinState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local op1;
	protected final Local op2;

	public TransformerAssignAddLocalLocalToLocal(Local lhs, Local op1,
			Local op2) {
		this.lhs = lhs;
		this.op1 = op1;
		this.op2 = op2;
	}

	@Override
	public LinState apply(LinState input) {
		// Special treatment for bottom.
		if (input.equals(LinState.bottom))
			return LinState.bottom;

		LinState result = input.copy();
		result.removeVar(lhs);

		// To handle x=y+z find cases where y=a*p+b and z=c*p+d and add
		// x=(a+c)*p+(b+d).
		for (LinFactoid op1Factoid : input.getFactoids(op1)) {
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
					result.add(newFactoid);
				}
			}
		}
		return result;
	}
}