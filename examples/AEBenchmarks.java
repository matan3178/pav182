
/**
 * Benchmarks for testing the Available Expressions analysis in
 * {@link bgu.cs.absint.analyses.ae.AEMain}.<br>
 * You may run this example with the following command-line arguments:<br>
 * <code>java bgu.cs.aic.examples.ae.AEMain -cp . -pp -f jimple -p jb use-original-names -p jb.ls enabled:false -p jb.ls enabled:false -keep-line-number -print-tags AEBenchmarks</code>
 * 
 * @author romanm
 * 
 */
public class AEBenchmarks {
	private void error(String message) {
	}

	int f;

	@SuppressWarnings("unused")
	public int expressions(int a, int b, int c) {
		a = 9;
		b = 8;
		c = 9;
		a = b + c;
		b = a * 5;
		c = b / 5;

		// In the following statement, the expression on the right-hand side
		// contains the variable on the left so we cannot add a+1 as an
		// available expression.
		a = a + 1;

		// A new expression is not pure. That is, it may return a different
		// value every time it is evaluated. Therefore, we do not add it
		// as an available expression.
		AEBenchmarks i = new AEBenchmarks();

		// We treat method calls conservatively as impure expressions.
		int p = expressions(5, 5, 5);

		boolean b1 = i instanceof AEBenchmarks;

		// We conservatively ignore heap access expressions (fields and arrays)
		// since their values may be modified indirectly (that is, from other
		// variables).
		c = i.f;
		a = -c;

		return 9;
	}

	public int loopExample1(int x, int y, int a) {
		int w = 9;
		int z = x + y;
		z = w;
		int b = z;
		b = loopExample1(b, b, b);

		while (a == b) {
			a = a - 1;
			int c = x + y + w;
			b = c + a;
		}
		return b;
	}

	public int loopExample2(int x, int y, int a) {
		int w = 9;
		int z = x + y;
		w = z;
		int b = z;
		b = loopExample2(b, b, b);

		while (a == b) {
			a = a - 1;
			int c = x + y + w;
			b = c + a;
		}
		if (z != x + y)
			error("Unable to prove z==x+y!");
		return b;
	}
}