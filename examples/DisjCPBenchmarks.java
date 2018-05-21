
/**
 * Demonstrates different cases for the analysis using the disjunctive
 * completion of CP with aggressive joins at loop heads.<br>
 * 
 * You may run this example by entering:<br>
 * java bgu.cs.aic.examples.DisjCPMain -cp . -pp -f jimple -p jb
 * use-original-names -p jb.ls enabled:false -keep-line-number -print-tags
 * DisjCPBenchmarks *
 * 
 * @author romanm
 * 
 */
public class DisjCPBenchmarks {
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
		if (a != 17) // We need to maintains disjunctions to prove this.
			error("Unable to prove a==17!");
		b = b * 5;
		c = c * 5;
	}

	public int rand() {
		return 5;
	}

	public void relationalProductExample(int a, int b, int c, int d) {
		if (a > 5) {
			b = 8;
			a = c;
		} else {
			b = 9;
			a = d;
		}

		if (b == 8) {
			if (a != c)
				error("Unable to prove a==c!");
		} else if (b == 9) {
			if (a != d)
				error("Unable to prove a==d!");
		} else {
			error("Can't get here");
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
		// interval analysis.
		error("Unable to prove that loop does not terminate!");
	}
}