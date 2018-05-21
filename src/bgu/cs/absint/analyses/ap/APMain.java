package bgu.cs.absint.analyses.ap;

import soot.PackManager;
import soot.Transform;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the Arithmetic Progression (AP) analysis transform to Soot.
 * 
 * @author romanm
 */
public class APMain {
	public static void main(String[] args) {
		APAnalysis theAnalysis = new APAnalysis();
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.APAnalysis", theAnalysis));
		soot.Main.main(args);
		theAnalysis.reportErrors();
	}

	public static class APAnalysis extends BaseAnalysis<APState, APDomain> {
		public APAnalysis() {
			super(APDomain.v());
			useWidening(true);
		}
	}
}