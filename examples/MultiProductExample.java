
/**
 * An example for the Cartesian product analysis of CPxVExAE with mutual
 * reductions.<br>
 * You may run it by entering java bgu.cs.aic.examples.CPVEAEMain<br>
 * -cp . -pp -f jimple -p jb use-original-names -p jb.ls enabled:false
 * -keep-line-number -print-tags MultiProductExample
 * 
 * @author romanm
 * 
 */
public class MultiProductExample {
	private void error(String message) {
	}

	public void example() {
		int a, b, c, d;

		a = 9;
		b = 9;
		c = a;
		if (a != b)
			error("Unable to prove a==b!");

		c = a + b;
		d = a + b;
		if (c != d)
			error("Unable to prove c==d!");
	}
}
