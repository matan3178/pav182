package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.UnaryOperation;
import soot.Local;

/**
 * A transformer for statements of the form assume {@code x=y-z}.
 * 
 * @author ???
 */
class TransformerAssignSubLocalLocalToLocal extends
		UnaryOperation<ZoneState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local op1;
	protected final Local op2;

	public TransformerAssignSubLocalLocalToLocal(Local lhs, Local op1,
			Local op2) {
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
		// Apply the preservation rule to each factoid - thats the only thing to do since we dont know anything about constants
		for (ZoneFactoid f : input.factoids) {
			if (!f.hasVar(lhs)) {
				result.factoids.add(f);
			}
		}
		
		//check if y-z <=c or z-y <=d exists here and add x<=c or x<=d
		for(ZoneFactoid f : input.factoids){
			if(f.lhs.equals(op1) && f.rhs.equals(op2)){ //y-z<=c -> x<=c
				result.add(new ZoneFactoid(lhs, ZoneFactoid.ZERO_VAR, f.bound));
			} else if(f.lhs.equals(op2) && f.rhs.equals(op1)) { //y-z<=d -> -x<=d 
				result.add(new ZoneFactoid(ZoneFactoid.ZERO_VAR, lhs, f.bound));
			}
		}

		return ZoneDomain.essential(result);
	}
}