package bgu.cs.absint.analyses.set;

import soot.PackManager;
import soot.Transform;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the static analysis of sets to Soot.
 * 
 */
public class SetMain {
	public static void main(String[] args) {
		SetAnalysis analysis = new SetAnalysis();
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.SetAnalysis", analysis));
		soot.Main.main(args);
	}

	public static class SetAnalysis extends
			BaseAnalysis<SetState, SetDomain> {
		public SetAnalysis() {
			super(new SetDomain());
		}
	}
}