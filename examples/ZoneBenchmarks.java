/**
 * Methods to test the soundness and precision of the Zone analysis.
 * 
 * @author romanm
 */
public class ZoneBenchmarks {
	private void error(String message) {
	}

	public void artihmeticExample() {
		int x = 7;
		int y = 10;
		int z = y + x;
		if (z < 18) {
		} else {
			error("Cannot prove z <= 17!");
		}
		y = y - 1;
		z = y + x;
		if (z < 17) {
		} else {
			error("Cannot prove z <= 16!");
		}
	}

	public void conditionExample(int x, boolean b) {
		x = 10;
		int y;
		if (b) {
			y = x + 5;
		} else {
			y = x + 15;
		}
		if (y < 26) {
		} else {
			error("Cannot prove y <= 25!");
		}
	}

	public void loopExample() {
		int x = 7;
		int y = x;
		while (x < 1000) {
			++x;
			++y;
		}
		if (y < 1001) {
		} else {
			error("Cannot prove y<=1000!");
		}
		if (x < 1001) {
		} else {
			error("Cannot prove x<=1000!");
		}
	}
}