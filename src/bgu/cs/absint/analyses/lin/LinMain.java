package bgu.cs.absint.analyses.lin;

import soot.PackManager;
import soot.Transform;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the Linear Relations (Lin) transform to Soot.
 * 
 * @author romanm
 */
public class LinMain {
	public static void main(String[] args) {
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.LinAnalysis", new LinAnalysis()));
		soot.Main.main(args);
	}

	public static class LinAnalysis extends BaseAnalysis<LinState, LinDomain> {
		public LinAnalysis() {
			super(LinDomain.v());
		}
	}
}