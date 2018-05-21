package bgu.cs.absint.analyses;

import soot.PackManager;
import soot.Transform;
import soot.Unit;
import bgu.cs.absint.analyses.cp.CPDomain;
import bgu.cs.absint.analyses.cp.CPState;
import bgu.cs.absint.constructor.DisjunctiveDomain;
import bgu.cs.absint.constructor.DisjunctiveState;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * The disjunctive completion of Constant Propagation (CP) with the Cartesian
 * join at loop heads.
 * 
 * @author romanm
 */
public class DisjCPMain {
	public static void main(String[] args) {
		PackManager.v().getPack("jtp")
				.add(new Transform("jtp.DisjCPAnalysis", new DisjCPAnalysis()));
		soot.Main.main(args);
	}

	public static class DisjCPAnalysis
			extends
			BaseAnalysis<DisjunctiveState<CPState>, DisjunctiveDomain<CPState, Unit>> {
		public DisjCPAnalysis() {
			super(new DisjunctiveDomain<>(CPDomain.v(), true));
		}
	}
}