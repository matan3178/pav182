
/**
 * Demonstrates different cases for the analysis using the disjunctive
 * completion of CP with aggressive joins at loop heads.<br>
 * 
 * You may run this example by entering:<br>
 * java bgu.cs.aic.examples.DisjCPMain -cp . -pp -f jimple -p jb
 * use-original-names -p jb.ls enabled:false -keep-line-number -print-tags
 * DisjCPBenchmarks_bug
 * 
 * @author romanm
 * 
 */
public class DisjCPBenchmarks_bug {
	private void error(String message) {
	}

	public void conditionExample1(int a) {
		int b, c;

		if (a > 5) {
			b = 8;
			c = 9;
		} else {
			b = 9;
			c = 8;
		}
		a = b + c;
		if (a != 17) // We need to maintain disjunctions to prove this.
			error("Unable to prove a==17!");
		b = b * 5;
		c = c * 5;
	}

	@SuppressWarnings("unused")
	public void relationalProductExample(int a, int c, int d) {
		int b, e;

		if (a > 5) {
			b = 8;
			c = b + d;
		} else {
			b = 9;
			e = b + d;
		}
	}

	/**
	 * If we do not use aggressive joins at loop heads, the analysis will not
	 * terminate.
	 */
	public void infiniteLoop() {
		int x = 1;
		while (x > 0) {
			++x;
		}
		// We cannot prove this with Disj(CP), we need something
		// like interval analysis.
		error("Unable to prove that loop does not terminate!");
	}
}