package bgu.cs.absint.analyses.ve;

import soot.PackManager;
import soot.Transform;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the Variable Equalities (VE) transform to Soot.
 * 
 * @author romanm
 */
public class VEMain {
	public static void main(String[] args) {
		PackManager
				.v()
				.getPack("jtp")
				.add(new Transform("jtp.VEAnalysis", new VarEqualityAnalysis()));
		soot.Main.main(args);
	}

	public static class VarEqualityAnalysis extends
			BaseAnalysis<VEState, VEDomain> {
		public VarEqualityAnalysis() {
			super(VEDomain.v());
		}
	}
}