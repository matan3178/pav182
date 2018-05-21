

/**
 * You may run this example with the following command-line arguments:<br>
 * <code>java bgu.cs.absint.analyses.ap.APMain -cp . -pp -f jimple -p jb use-original-names -p jb.ls enabled:false -p jb.ls enabled:false -keep-line-number -print-tags APBenchmarks</code>
 * 
 * @author romanm
 * 
 */
public class APBenchmarks {
	private void error(String message) {
	}

	private void analysisAssumeAPFactoid(int var, int base, int stride) {
	}

	private void analysisAssertLeqAPFactoid(int var, int base, int stride) {
	}

	// Test 1
	public void testAssumeAPFactoid(int x, int y) {
		if (x > y)
			analysisAssumeAPFactoid(x, 6, 2);
		else
			analysisAssumeAPFactoid(x, 8, 3);
		// Expected state: AP[x, 6, 1]
		analysisAssertLeqAPFactoid(x, 6, 1);
		if (x == 5)
			error("Imprecision with x==5");
	}

	// Test 2
	public void testJoinNonTrivialAPs(int x, int y) {
		if (x > y) {
			x = 6;
			y = 8;
		} else {
			x = 8;
			y = 11;
		}
		// Expected state: and(AP[y, 8, 3], AP[x, 6, 2])
		if (x == 5)
			error("Imprecision with x==5");
		analysisAssertLeqAPFactoid(x, 6, 2);
	}

	// Test 3
	public void testSum(int x, int y) {
		analysisAssumeAPFactoid(x, 6, 2);
		analysisAssumeAPFactoid(y, 19, 4);
		int z = x + y;
		analysisAssertLeqAPFactoid(z, 25, 2);
	}

	// Test 4
	public void testMulLocalConstant(int x) {
		analysisAssumeAPFactoid(x, 6, 2);
		int z = x * 19;
		analysisAssertLeqAPFactoid(z, 114, 38);
	}

	// Test 5
	public void testAssumeConstant(int x) {
		analysisAssumeAPFactoid(x, 6, 2);
		if (x == 5)
			error("Imprecision detected for x==5!");
		if (x == 7)
			error("Imprecision detected for x==7!");
	}

	// Test 6
	public void testLoop1(int x) {
		x = 10;
		while (x < 1000000)
			x = x + 5;
		analysisAssertLeqAPFactoid(x, 10, 5);
	}

	// Test 7
	public void testLoop2(int x, int y) {
		x = 10;
		y = 5;
		while (x < 1000000)
			x = x + y;
		analysisAssertLeqAPFactoid(x, 10, 5);
	}

	// Test 8
	public void testLoop3(int x, int y, int z) {
		x = 10;
		while (x < 1000000) {
			analysisAssertLeqAPFactoid(x, 10, 5);
			if (z == 0)
				y = 5;
			else
				y = 10;
			x = x + y;
		}
	}

	// Test 9
	public void testLoop4(int x, int y, int z) {
		x = 10;
		while (x > 0) {
			analysisAssertLeqAPFactoid(x, 0, 1);
			x = x - 1;
		}
		analysisAssertLeqAPFactoid(x, 0, 1);
	}
}