package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.UnaryOperation;
import bgu.cs.util.soot.CaseAssignLocal_LocalNonRef;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code x=y} for
 * variables 'x' and 'y',
 * 
 * @author ???
 */
class TransformerAssignLocalToLocal extends UnaryOperation<ZoneState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local rhs;

	public TransformerAssignLocalToLocal(CaseAssignLocal_LocalNonRef jimpleCase) {
		this(jimpleCase.lhsLocal, jimpleCase.rhsLocal);
	}

	public TransformerAssignLocalToLocal(Local lhs, Local rhs) {
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public ZoneState apply(ZoneState input) {
		// Special treatment for bottom.
		if (input.equals(ZoneState.bottom))
			return ZoneState.bottom;
		ZoneState result = new ZoneState();
		// Apply the preservation rule to each factoid - thats the only thing to do since we dont know anything about constants
		for (ZoneFactoid factoid : input.factoids) {
			if (!factoid.hasVar(lhs)) {
				result.factoids.add(factoid);
				//adding transitive facts. add any y fact as if its x
				if (factoid.rhs.equals(rhs)){
					result.factoids.add(new ZoneFactoid(factoid.lhs, lhs, factoid.bound));
				}
				if(factoid.lhs.equals(rhs)){
					result.factoids.add(new ZoneFactoid(lhs, factoid.rhs, factoid.bound));
				}
			}
		}
		//add equality between x and y
		result.factoids.add(new ZoneFactoid(rhs, lhs, IntConstant.v(0)));
		result.factoids.add(new ZoneFactoid(lhs, rhs, IntConstant.v(0)));
		return result;
	}
}