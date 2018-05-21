package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.AssumeTransformer;
import soot.Local;

/**
 * A transformer for statements of the form {@code if (x==y)} and
 * {@code if (x!=y)} for variable 'x' and 'y'.
 * 
 * @author ???
 */
class TransformerAssumeLocalEqLocal extends
		AssumeTransformer<ZoneState> {
	protected final Local lhs;
	protected final Local rhs;

	public TransformerAssumeLocalEqLocal(boolean polarity, Local lhs,
			Local rhs) {
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
		ZoneState result = new ZoneState(input);
		if (polarity) {
			for (ZoneFactoid factoid : input.factoids) {
				//we might try to add existing items to the set - but disregard taht
				if (factoid.rhs.equals(rhs)){
					result.factoids.add(new ZoneFactoid(factoid.lhs, lhs, factoid.bound));
				}
				if(factoid.lhs.equals(rhs)){
					result.factoids.add(new ZoneFactoid(lhs, factoid.rhs, factoid.bound));
				}
				if (factoid.rhs.equals(lhs)){
					result.factoids.add(new ZoneFactoid(factoid.lhs, rhs, factoid.bound));
				}
				if(factoid.lhs.equals(lhs)){
					result.factoids.add(new ZoneFactoid(rhs, factoid.rhs, factoid.bound));
				}
			}
		} else {
			// is there anything to do here??
		}
		return ZoneDomain.essential(result);
	}
}