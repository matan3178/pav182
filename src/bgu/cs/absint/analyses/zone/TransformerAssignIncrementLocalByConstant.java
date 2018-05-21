package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code x=x+c} and {@code x=c+x}.
 * 
 * @author ???
 */
class TransformerAssignIncrementLocalByConstant extends UnaryOperation<ZoneState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final IntConstant op2;

	public TransformerAssignIncrementLocalByConstant(Local lhs, IntConstant op2) {
		this.lhs = lhs;
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
			} else if(factoid.rhs.equals(lhs)){ // Transform each relevant factoid.
				result.add(new ZoneFactoid(factoid.lhs, factoid.rhs, (IntConstant)factoid.bound.subtract(op2)));
			} else {
				result.add(new ZoneFactoid(factoid.lhs, factoid.rhs, (IntConstant)factoid.bound.add(op2)));
			}
		}
		return ZoneDomain.essential(result);
	}
}