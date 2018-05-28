

/**
 * Demonstrates different cases for the Constant Propagation analysis.<br>
 * 
 * You may run this example with the following command-line arguments:<br>
 * <code>java bgu.cs.aic.examples.cp.CPMain -cp . -pp -f jimple -p jb use-original-names -p jb.ls enabled:false -p jb.ls enabled:false -keep-line-number -print-tags CPBenchmarks</code>
 * 
 * @author romanm
 * 
 */
public class CPBenchmarks {
	private void error(String message) {
	}

	public void example1() {
		int a = 5;
		a = a + 1;
		int b = 7;
		int c = a + b;
		int d = a + b;
		// CP should be able to infer the following: b=7, d=13, c=13, a=6.
		if (b != 7 || d != 13 || c != 13 || a != 6)
			error("Unable to prove b == 7 && d == 13 && c == 13 && a == 6!");
	}
/*
	public void example2() {
		int x = 100;
		int y;
		int z;
		int d = 1;
		while (x > 0) {
			if (x > 50) {
				y = 51;
				z = 52;
				d = z - y;
			} else {
				y = 50;
				z = 51;
				d = z - y;
			}
			--x;
		}
		if (d != 1) // CP can verify this!
			error("Unable to prove d==1!");
	}

	@SuppressWarnings("unused")
	public void infiniteLoop() {
		int x = 100;
		int y;
		int z;
		int d = 1;
		while (x > 0) {
			if (x > 50) {
				y = 51;
				z = 52;
				d = z - y;
			} else {
				y = 50;
				z = 51;
				d = z - y;
			}
		}
		error("Unable to prove that loop does not terminate!");
	}

	public void example3(int x) {
		int y;
		int z;
		int d = 1;

		if (x > 50) {
			y = 51;
			z = 53;
			d = z - y;
		} else {
			y = 50;
			z = 51;
			d = z - y;
		}
		if (d < 0) // CP cannot verify this!
			error("Unable to prove d>=0!");
		--x;
	}

	public void example4(int b) {
		if (b > 0)
			b = 5;
		else
			b = -5;

		if (b > 0)
			b = b - 5;
		else
			b = b + 5;
		if (b != 0)
			error("Unable to prove b==0");
	}

	public void example4Loop(int b, int c) {
		while (b < c) {
			if (b > 0)
				b = 5;
			else
				b = -5;

			if (b > 0)
				b = b - 5;
			else
				b = b + 5;
			if (b != 0)
				error("Unable to prove b==0");
		}
	}
	*/
}
