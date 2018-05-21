package bgu.cs.absint.analyses.interval;

import soot.PackManager;
import soot.Transform;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the Interval analysis transform to Soot.
 * 
 * @author romanm
 */
public class IntervalMain {
	public static void main(String[] args) {
		PackManager
				.v()
				.getPack("jtp")
				.add(new Transform("jtp.IntervalAnalysis",
						new IntervalAnalysis()));
		soot.Main.main(args);
	}

	public static class IntervalAnalysis extends
			BaseAnalysis<IntervalState, IntervalDomain> {
		public IntervalAnalysis() {
			super(IntervalDomain.v());
			useWidening(true);
		}
	}
}