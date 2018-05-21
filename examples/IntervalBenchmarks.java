
public class IntervalBenchmarks {
	private void error(String message) {
	}

//	public int conditionExample1(int a, int b, int c) {
//		if (a > 9)
//			b = 7;
//		else
//			b = 5;
//		return 9;
//	}

//	public void loopExample() {
//		int x = 7;
//		while (x < 1000) {
//			++x;
//		}
//		if (!(x == 1000))
//			error("Unable to prove x == 1000!");
//	}

	public void loopExample2() {
		int x = 7;
		int y = x;
		while (x < 1000) {
			++x;
			++y;
		}
		if (!(y == 1000))
			error("Unable to prove y == 1000!");
	}
}