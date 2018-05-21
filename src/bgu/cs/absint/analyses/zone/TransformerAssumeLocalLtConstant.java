package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.AssumeTransformer;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code if (x<c)} and
 * {@code if (x>=c)} for variable 'x' and constant 'c'.
 * 
 * @author ???
 */
class TransformerAssumeLocalLtConstant extends
		AssumeTransformer<ZoneState> {
	protected final Local lhs;
	protected final IntConstant c;

	public TransformerAssumeLocalLtConstant(boolean polarity, Local lhs,
			IntConstant c) {
		super(polarity);
		this.lhs = lhs;
		this.c = c;
	}

	@Override
	public String toString() {
		if (polarity)
			return "Zones[" + lhs.toString() + "<" + c.toString() + "]";
		else
			return "Zones[" + lhs.toString() + ">=" + c.toString() + "]";
	}

	@Override
	public ZoneState apply(ZoneState input) {
		ZoneState res = new ZoneState(input);
		if (polarity) { //x<c -> x <= c-1
			res.add(new ZoneFactoid(lhs, ZoneFactoid.ZERO_VAR, IntConstant.v(c.value-1)));
		} else { //x>=c -> V0-x <= -c
			res.add(new ZoneFactoid(ZoneFactoid.ZERO_VAR, lhs, IntConstant.v(-c.value)));
		}
		return res;
	}
}