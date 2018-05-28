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
		
	}

}