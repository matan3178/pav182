package bgu.cs.absint.soot;

import soot.Local;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.ConjunctiveState;

/**
 * A transformer that removes all factoids containing a given variable. It can
 * be used to conservatively handle any statement that has the effect of
 * modifying a given local variable (and only it).
 * 
 * @author romanm
 */
public class ForgetVarTransformer<F extends SootFactoid, StateType extends ConjunctiveState<Local, F>>
		extends UnaryOperation<StateType> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected Local lhs;

	/**
	 * Constructs a transformer for a specific local variable.
	 * 
	 * @param lhs
	 *            The variable being modified by the concrete semantics.
	 */
	public ForgetVarTransformer(Local lhs) {
		this.lhs = lhs;
	}

	@Override
	public StateType apply(StateType input) {
		// Special treatment for bottom and top.
		if (input.getFactoids() == null || input.getFactoids().isEmpty())
			return input;

		@SuppressWarnings("unchecked")
		StateType result = (StateType) input.empty();
		for (F factoid : input.getFactoids()) {
			if (!factoid.hasVar(lhs)) {
				result.add(factoid);
			}
		}
		return result;
	}
}
