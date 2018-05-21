package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code x=x+y} for
 * variables 'x' and 'y'.
 * 
 * @author ???
 */
class TransformerAssignIncrementLocalByLocal extends
		UnaryOperation<ZoneState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final Local op2;

	public TransformerAssignIncrementLocalByLocal(Local lhs, Local op2) {
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
			}
		}
		/* Apply the new factoid rule.
		 * if x-z<c and y<d -> x+y-z (==x'-z)< c+d
		 * if z-x<c and y>d -> z-x-y (==x'-z)< c+d
		 */
		IntConstant min = null;
		IntConstant negMin = null;
		for(ZoneFactoid f : result.getFactoids()){
			if(f.lhs.equals(op2) && f.rhs.equals(ZoneFactoid.ZERO_VAR)){
				min = f.min(result).bound;
			}
			if(f.rhs.equals(op2) && f.lhs.equals(ZoneFactoid.ZERO_VAR)){
				negMin = f.min(result).bound;
			}
		}		

		if(min!= null){
			for (ZoneFactoid f : input.factoids) {
				if (f.lhs.equals(lhs)) { //if x-z<c and y<d -> x+y-z (==x'-z)< c+d
					result.add(new ZoneFactoid(f.lhs, f.rhs, (IntConstant)f.bound.add(min)));
				}
			}
		}
		if(negMin!= null){
			for (ZoneFactoid f : input.factoids) {
				if (f.rhs.equals(lhs)) { //if z-x<c and y>d -> z-x-y (==x'-z)< c+d
					result.add(new ZoneFactoid(f.lhs, f.rhs, (IntConstant)f.bound.add(negMin)));
				}
			}
		}
		return result;
	}
}