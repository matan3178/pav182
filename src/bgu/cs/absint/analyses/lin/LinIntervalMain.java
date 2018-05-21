package bgu.cs.absint.analyses.lin;

import soot.Local;
import soot.PackManager;
import soot.Transform;
import soot.Unit;
import bgu.cs.absint.analyses.interval.IntervalDomain;
import bgu.cs.absint.constructor.CartesianDomain;
import bgu.cs.absint.constructor.ProductState;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the analysis combining Linear Relations (Lin) with Intervals as a
 * transform to Soot.
 * 
 * @author romanm
 */
public class LinIntervalMain {
	public static void main(String[] args) {
		PackManager
				.v()
				.getPack("jtp")
				.add(new Transform("jtp.LinIntervalAnalysis",
						new LinIntervalAnalysis()));
		soot.Main.main(args);
	}

	public static class LinIntervalAnalysis extends
			BaseAnalysis<ProductState, CartesianDomain<Unit, Local>> {
		public LinIntervalAnalysis() {
			super(new CartesianDomain<Unit, Local>(LinDomain.v(),
					IntervalDomain.v()));
			useWidening(true);
		}
	}
}