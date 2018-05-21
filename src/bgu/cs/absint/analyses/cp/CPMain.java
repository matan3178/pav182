package bgu.cs.absint.analyses.cp;

import soot.PackManager;
import soot.Transform;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the Constant Propagation (CP) transform to Soot.
 * 
 * @author romanm
 */
public class CPMain {
	public static void main(String[] args) {
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.CPAnalysis", new CPAnalysis()));
		soot.Main.main(args);
	}

	public static class CPAnalysis extends BaseAnalysis<CPState, CPDomain> {
		public CPAnalysis() {
			super(CPDomain.v());
		}
	}
}