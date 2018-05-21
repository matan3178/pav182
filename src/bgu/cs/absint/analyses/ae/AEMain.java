package bgu.cs.absint.analyses.ae;

import bgu.cs.absint.soot.BaseAnalysis;
import bgu.cs.absint.soot.SimpleAnalysisRunner;

/**
 * Runs the Available Expressions (AE) analysis on a given class.
 * 
 * @author romanm
 */
public class AEMain {
	/**
	 * Entry point to the Available Expressions analysis application.
	 * 
	 * @param args
	 *            Should be either a single argument containing the name of the
	 *            class to analyze or a sequence of arguments passed to Soot.
	 */
	public static void main(String[] args) {
		SimpleAnalysisRunner.run(new AvailableExpressionsAnalysis(), args);
	}

	/**
	 * The analysis class for Available Expressions.
	 * 
	 * @author romanm
	 */
	public static class AvailableExpressionsAnalysis extends
			BaseAnalysis<AEState, AEDomain> {
		public AvailableExpressionsAnalysis() {
			super(AEDomain.v());
		}
	}
}