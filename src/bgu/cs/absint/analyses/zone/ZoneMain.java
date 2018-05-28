package bgu.cs.absint.analyses.zone;

import soot.PackManager;
import soot.Transform;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the ZoneMain transform to Soot.
 * 
 * @author romanm
 */
public class ZoneMain {
	public static void main(String[] args) {
		PackManager.v().getPack("jtp").add(new Transform("jtp.ZonesAnalysis", new ZonesAnalysis()));
		soot.Main.main(args);
	}
	

	public static class ZonesAnalysis extends BaseAnalysis<ZoneState, ZoneDomain> {
		public ZonesAnalysis() {
			super(ZoneDomain.v());
			useWidening(true);
		}
	}
}