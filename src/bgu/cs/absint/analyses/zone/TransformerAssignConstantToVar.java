package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form  {@code x=c} for a
 * variable 'x' and constant 'c',
 * 
 * @author ???
 */
class TransformerAssignConstantToVar extends UnaryOperation<ZoneState> {
	protected final Local lhs;
	protected final IntConstant rhs;

	public TransformerAssignConstantToVar(Local lhs, IntConstant rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
		assert !lhs.equals(rhs);
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
		result.add(new ZoneFactoid(lhs, ZoneFactoid.ZERO_VAR, rhs));
		result.add(new ZoneFactoid(ZoneFactoid.ZERO_VAR, lhs, IntConstant.v(-rhs.value)));

		return result;
	}
}