package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code x=y*c} and {@code x=c*y}.
 * 
 * @author ???
 */
class TransformerAssignMulLocalConstantToLocal extends
		UnaryOperation<ZoneState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local op1;
	protected final IntConstant op2;	

	public TransformerAssignMulLocalConstantToLocal(Local lhs, Local rhs,
			IntConstant coefficient) {
		this.lhs = lhs;
		this.op1 = rhs;
		this.op2 = coefficient;		
	}

	@Override
	public ZoneState apply(ZoneState input) {
		// Special treatment for bottom.
		if (input.equals(ZoneState.bottom))
			return ZoneState.bottom;

		ZoneState result = new ZoneState();
		// Apply the preservation rule to each factoid.
		for (ZoneFactoid factoid : input.factoids) {
			if (!factoid.hasVar(lhs)) {
				result.factoids.add(factoid);
			}
		}
		// do we have any new facts to add? i dont think so

		return result;
	}
}