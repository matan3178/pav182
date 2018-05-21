package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code x=y+c} and {@code x=c+y}.
 * 
 * @author ???
 */
class TransformerAssignAddLocalConstantToLocal extends
		UnaryOperation<ZoneState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local op1;
	protected final IntConstant op2;

	public TransformerAssignAddLocalConstantToLocal(Local lhs, Local op1,
			IntConstant op2) {
		this.lhs = lhs;
		this.op1 = op1;
		this.op2 = op2;
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
		// Apply the new factoid rule.
		result.factoids.add(new ZoneFactoid(lhs, op1, op2));
		result.factoids.add(new ZoneFactoid(op1, lhs, IntConstant.v(-op2.value)));

		return result;
	}
}