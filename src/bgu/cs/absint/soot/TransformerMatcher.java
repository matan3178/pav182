package bgu.cs.absint.soot;

import soot.Unit;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;

/**
 * Obtains an abstract transformer for a given unit by pattern matching.
 * 
 * @author romanm
 */
public class TransformerMatcher<StateType> extends StmtMatcher {
	public UnaryOperation<StateType> transformer;

	public UnaryOperation<StateType> getTransformer(Unit stmt) {
		transformer = null;
		if (stmt instanceof Assume) {
			Assume assume = (Assume) stmt;
			matchAssume(assume);
		} else {
			stmt.apply(this);
		}
		if (transformer == null) {
			transformer = IdOperation.v();
		}
		return transformer;
	}
}