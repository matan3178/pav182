package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.AssumeTransformer;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code if (x==c)} and
 * {@code if (x!=c)} for a variable 'x' and a constant 'c'.
 * 
 * @author ???
 */
class TransformerAssumeLocalEqConstant extends
		AssumeTransformer<ZoneState> {
	protected final Local lhs;
	protected final IntConstant rhs;	

	public TransformerAssumeLocalEqConstant(boolean polarity, Local lhs,
			IntConstant rhs) {
		super(polarity);
		this.lhs = lhs;
		this.rhs = rhs;		
	}

	@Override
	public String toString() {
		if (polarity)
			return "Zones[" + lhs.toString() + "==" + rhs.toString() + "]";
		else
			return "Zones[" + lhs.toString() + "!=" + rhs.toString() + "]";
	}

	@Override
	public ZoneState apply(ZoneState input) {
		// Special treatment for bottom.
		if (input.equals(ZoneState.bottom))
			return ZoneState.bottom;

		ZoneState result = new ZoneState(input);
		if (polarity) {
			result.factoids.add(new ZoneFactoid(lhs, ZoneFactoid.ZERO_VAR, rhs));
			result.factoids.add(new ZoneFactoid(ZoneFactoid.ZERO_VAR, lhs, IntConstant.v(-rhs.value)));
		} else { //if lhs!=rhs, we'll see if lhs<=rhs and change that to lhr<rhs. same for lhs>=rhs
			for (ZoneFactoid f : input.factoids) {
				if (f.lhs.equals(lhs) && f.rhs.equals(ZoneFactoid.ZERO_VAR) && f.bound.equals(rhs)){
					result.factoids.add(new ZoneFactoid(f.lhs, f.rhs, IntConstant.v(rhs.value-1)));
				}
				if (f.lhs.equals(ZoneFactoid.ZERO_VAR) && f.rhs.equals(lhs) && f.bound.equals(IntConstant.v(-rhs.value))){
					result.factoids.add(new ZoneFactoid(f.lhs, f.rhs, IntConstant.v(rhs.value+1)));
				}
			}
		}
		return ZoneDomain.essential(result);
	}
}