package bgu.cs.absint.soot;

import soot.Local;
import bgu.cs.absint.constructor.Factoid;

/**
 * A factoid over local variables.
 * 
 * @author romanm
 *
 */
public class SootFactoid extends Factoid<Local> {
	@Override
	public final boolean equalVars(Local v1, Local v2) {
		return v1.equivTo(v2);
	}
}