
/**
 * List-manipulating methods to test the shape analysis.
 * 
 * You may run this example with the following command-line arguments:<br>
 * <code>java bgu.cs.absint.examples.SLL.SLLMain -cp . -pp -f jimple -p jb use-original-names -p jb.ls enabled:false -p jb.ls enabled:false -keep-line-number -print-tags SLLBenchmarks</code>
 * 
 * @author romanm
 */
public class SLLBenchmarks {
	public SLLBenchmarks next;
	public int data;
	
	///////////////////////////////////////////////////////////////////////////
	// Analysis helper methods.
	///////////////////////////////////////////////////////////////////////////

	/**
	 * The postcondition of this method is a state where all variables point to
	 * null, except 'x', which points to a list of size >=0.
	 */
	public static void analysisInitAcyclic(SLLBenchmarks x) {
	}

	/**
	 * The postcondition of this method is a state where all variables point to
	 * null.
	 */
	public static void analysisInitAllNulls() {
	}

	/**
	 * Asserts that 'x' is not null.<br>
	 * TODO: implement assert states in SLLAnalysis.
	 */
	public static void analysisAssertNotNull(SLLBenchmarks x, String message) {
	}

	/**
	 * Asserts that 'y' is reachable from 'x'. That is, starting from 'x' and
	 * following 'next' fields gets to a node referenced by 'y', or to null when
	 * 'y' is null.<br>
	 * TODO: implement assert states in SLLAnalysis.
	 */
	public static void analysisAssertReachable(SLLBenchmarks x, SLLBenchmarks y, String message) {
	}

	/**
	 * Asserts that 'x' and 'y' are disjoint. That is, following 'next' fields
	 * from 'x' and from 'y' does not lead to a common node.<br>
	 * TODO: implement assert states in SLLAnalysis.
	 */
	public static void analysisAssertDisjoint(SLLBenchmarks x, SLLBenchmarks y, String message) {
	}

	/**
	 * Asserts that the list starting from 'x' is acyclic. That is, starting
	 * from 'x' and following the 'next' field eventually gets you to null.<br>
	 * TODO: implement assert states in SLLAnalysis.
	 */
	public static void analysisAssertAcyclic(SLLBenchmarks x, String message) {
	}

	/**
	 * Asserts that 'x' references a cyclic list. That is, starting from 'x' and
	 * following the 'next' field gets you back to 'x'.<br>
	 * TODO: implement assert states in SLLAnalysis.
	 */
	public static void analysisAssertCyclic(SLLBenchmarks x, String message) {
	}
	
	/**
	 * Asserts that all list node are reachable from some variable. Note that
	 * this analysis can only be effectively used within loop bodies, as garbage
	 * nodes are automatically collected at loop heads as part of the
	 * abstraction. The best place therefore to have this assertion is as the
	 * last statement of a loop.<br>
	 * TODO: implement assert states in SLLAnalysis.
	 */
	public static void analysisAssertNoGarbage(String message) {
	}

	///////////////////////////////////////////////////////////////////////////
	// End of analysis helper methods.
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Searches for a cell with the given data field value.
	 * 
	 * @return the cell holds the given value, or null if there is none.
	 */
	public SLLBenchmarks find(int key) {
		analysisInitAcyclic(this); // Start with an acyclic list.

		SLLBenchmarks result = null;
		SLLBenchmarks curr = this;
		while (curr != null) {
			if (curr.data == key) {
				result = curr;
				break;
			}
			curr = curr.next;
		}
		return result;
	}

	/**
	 * Creates an acyclic singly-linked lists of a given size. The data field of
	 * a cell holds its position in the list.
	 * 
	 * @param size
	 *            The number of cells in the list.
	 * @return An acyclic singly-linked list.
	 */
	public static SLLBenchmarks create(int size) {
		analysisInitAllNulls(); // Start with a state where all variables are
								// initialized to null.
		
		SLLBenchmarks result = null;
		for (int i = 0; i < size; ++i) {
			SLLBenchmarks n = new SLLBenchmarks();
			n.next = result;
			n.data = i;
			result = n;
			analysisAssertNoGarbage("Unable to prove absence of garbage in create!");
		}

		analysisAssertAcyclic(result, "Unable to assert that 'result' points to an acyclic list!");
		return result;
	}
}