
public class LinBenchmarks {
	@SuppressWarnings("unused")
	private void error(String message) {
	}

	public void conditionExample1(int a, int x, int y, int z, int w) {
		if (a > 9) {
			x = 1;
			y = 2;
			z = 6;
		} else {
			x = 2;
			y = 4;
			z = 12;
		}
		w = z + 2;
	}

	public void conditionExample2(int a, int x, int y) {
		if (a > 9) {
			x = 1;
			y = 9;
		} else {
			x = 2;
			y = 11;
		}
	}

	@SuppressWarnings("unused")
	public void assignmentExample1(int a, int x, int y) {
		y = x;
		int z = y + 1;
		// ++x;
	}
}