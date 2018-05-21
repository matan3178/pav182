package bgu.cs.absint.analyses;

import soot.Local;
import soot.PackManager;
import soot.Transform;
import soot.Unit;
import bgu.cs.absint.analyses.cp.CPDomain;
import bgu.cs.absint.analyses.ve.VEDomain;
import bgu.cs.absint.constructor.CartesianDomain;
import bgu.cs.absint.constructor.DisjunctiveDomain;
import bgu.cs.absint.constructor.DisjunctiveState;
import bgu.cs.absint.constructor.ProductState;
import bgu.cs.absint.soot.BaseAnalysis;

/**
 * Adds the relational product of Constant Propagation (CP) and Variable
 * Equalities (VE) transform to Soot.
 * 
 * @author romanm
 */
public class RelProdOfCPVEMain {
	public static void main(String[] args) {
		PackManager
				.v()
				.getPack("jtp")
				.add(new Transform("jtp.DisjOfCPVEAEAnalysis",
						new RelProdOfCPVEAnalysis()));
		soot.Main.main(args);
	}

	public static class RelProdOfCPVEAnalysis
			extends
			BaseAnalysis<DisjunctiveState<ProductState>, DisjunctiveDomain<ProductState, Unit>> {
		public RelProdOfCPVEAnalysis() {
			super(
					new DisjunctiveDomain<ProductState, Unit>(
							new CartesianDomain<Unit, Local>(CPDomain.v(),
									VEDomain.v())));
		}
	}
}