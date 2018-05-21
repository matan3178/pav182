
import java.util.HashSet;

/**
 * 
 * 
 * You may run this example with the following command-line arguments:<br>
 * java bgu.cs.aic.examples.set.SetMain -cp . -pp -f jimple -p jb
 * use-original-names -p jb.ls enabled:false -p jb.ls enabled:false
 * -keep-line-number -print-tags SetBenchmarks
 * 
 * @author romanm
 * 
 */
public class SetBenchmarks {
	@SuppressWarnings("unused")
	private void error(String message) {
	}

	public void example1(HashSet<Integer> x, HashSet<Integer> y, Integer e1,
			Integer e2) {
		// Init state
		// { }
		analysisAssumeNotNull(x);
		// { NotNull(x) }
		x.clear();
		// { Empty(x) }
		analysisAssumeNotNull(y);
		// { Empty(x) }
		y.clear();
		// { EqualSets(x, y) }
		x.add(e1);
		// { Union(x, y, e1) }
		y.add(e1);
		// { EqualSets(x, y) }
		x.add(e2);
		// { Union(x, y, e2) }
		y.add(e2);
		// { EqualSets(x, y) }
	}

	private void analysisAssumeNotNull(HashSet<?> x) {
	}
}