package bgu.cs.absint.analyses.lin;

import bgu.cs.absint.UnaryOperation;
import soot.Local;
import soot.jimple.IntConstant;

/**
 * A transformer for statements of the form {@code x=x+c} and {@code x=c+x}.
 * 
 * @author romanm
 */
class TransformerAssignIncrementLocalByConstant extends
		UnaryOperation<LinState> {
	/**
	 * The variable being modified by the concrete semantics.
	 */
	protected final Local lhs;
	protected final IntConstant op2;

	public TransformerAssignIncrementLocalByConstant(Local lhs,
			IntConstant op2) {
		this.lhs = lhs;
		this.op2 = op2;
	}

	@Override
	public LinState apply(LinState input) {
		// Special treatment for bottom.
		if (input.equals(LinState.bottom))
			return LinState.bottom;

		LinState result = new LinState();
		for (LinFactoid f : input.getFactoids()) {
			if (!f.hasVar(lhs))
				result.add(f);

			// { x=a*y+b } x:=x+c { x=a*y+(b+c) }
			if (f.lvar.equivTo(lhs)) {
				LinFactoid newFactoid = new LinFactoid(f.lvar, f.rvar,
						f.coefficient, (IntConstant) f.additive.add(op2));
				result.add(newFactoid);
			}

			// { y=a*x+b } x:=x+c { y=a*(x-c)+b } { y=a*x+(b-a*c) }
			if (f.rvar.equivTo(lhs)) {
				int b = f.additive.value;
				int a = f.coefficient.value;
				int c = op2.value;
				IntConstant newAdditive = IntConstant.v(b - a * c);
				LinFactoid newFactoid = new LinFactoid(f.lvar, f.rvar,
						f.coefficient, newAdditive);
				result.add(newFactoid);
			}
		}
		return result;
	}
}