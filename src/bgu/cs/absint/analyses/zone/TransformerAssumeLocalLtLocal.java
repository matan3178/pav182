package bgu.cs.absint.analyses.zone;

import bgu.cs.absint.AssumeTransformer;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code if (x<y)} and
 * {@code if (x>=y)} for variable 'x' and 'y'.
 * 
 * @author ???
 */
class TransformerAssumeLocalLtLocal extends
		AssumeTransformer<ZoneState> {
	protected final Local lhs;
	protected final Local rhs;

	public TransformerAssumeLocalLtLocal(boolean polarity, Local lhs,
			Local rhs) {
		super(polarity);
		this.lhs = lhs;
		this.rhs = rhs;
	}

	@Override
	public String toString() {
		if (polarity)
			return "Zones[" + lhs.toString() + "<" + rhs.toString() + "]";
		else
			return "Zones[" + lhs.toString() + ">=" + rhs.toString() + "]";
	}

	@Override
	public ZoneState apply(ZoneState input) {
		ZoneState res = new ZoneState(input);
		if (polarity) { //x<y -> x-y<0 --> x-y<= -1
			res.add(new ZoneFactoid(lhs, rhs, IntConstant.v(-1)));
		} else { //x>=y -> y-x <=0
			res.add(new ZoneFactoid(rhs, lhs, IntConstant.v(0)));
		}
		return res;
	}
}