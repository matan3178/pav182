package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.UnaryOperation;
import soot.Local;

/**
 * A transformer for statements of the form assume {@code x=y+z}.
 * 
 * @author ???
 */
class TransformerAssignAddLocalLocalToLocal extends
		UnaryOperation<ZoneState> {
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
		ZoneState essentialRes = ZoneDomain.essential(result);
		//check if y<=c or z<=d exists here and add x-z<=c and z-y<=d in respect.
		// x-z=y  && y <= c --> x-z <=c
		for(ZoneFactoid f : essentialRes.factoids){
			if(f.rhs.equals(ZoneFactoid.ZERO_VAR)){
				if(f.lhs.equals(op1)){ //y<=c 
					result.factoids.add(new ZoneFactoid(lhs, op2, f.bound));
				} else if(f.lhs.equals(op2)) {  //z<=d
					result.factoids.add(new ZoneFactoid(lhs, op1, f.bound));
				}
			}

			// x-z=y  && y >= -c --> x-z >= -c ---> z-x <= c
			if(f.lhs.equals(ZoneFactoid.ZERO_VAR)){
				if(f.rhs.equals(op1)){ //y>=c 
					result.factoids.add(new ZoneFactoid(op2, lhs, f.bound));
				} else if(f.rhs.equals(op2)) {  //z>=d
					result.factoids.add(new ZoneFactoid(op1, lhs, f.bound));
				}
			}
		}

		return ZoneDomain.essential(result);
	}
}