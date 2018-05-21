package bgu.cs.absint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An equation defines the value of an {@link AnalysisVar} in terms of an
 * abstract {@link Operation}.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public class Equation<StateType> implements Comparable<Equation<StateType>> {
	public String sourceDescription = null;

	/**
	 * Indicates the intended priority used for determining the order of
	 * processing equations - equations with lower priorities get processed
	 * first.
	 */
	public int priority;

	/**
	 * The left-hand side of the equation.
	 */
	protected final AnalysisVar<StateType> lhs;

	/**
	 * The right-hand side of the equation.
	 */
	protected final Operation<StateType> op;

	/**
	 * The variables arguments of {@link op}.
	 */
	protected final ArrayList<AnalysisVar<StateType>> args;

	public Equation(AnalysisVar<StateType> lhs, Operation<StateType> op,
			List<AnalysisVar<StateType>> args) {
		assert lhs != null && op != null && args != null;
		this.lhs = lhs;
		this.op = op;
		this.args = new ArrayList<>();
		this.args.addAll(args);
	}

	public Equation(AnalysisVar<StateType> lhs, Operation<StateType> op,
			AnalysisVar<StateType> arg) {
		assert lhs != null && op != null && arg != null;
		this.lhs = lhs;
		this.op = op;
		this.args = new ArrayList<>();
		this.args.add(arg);
	}

	public Equation(AnalysisVar<StateType> lhs, Operation<StateType> op,
			AnalysisVar<StateType> arg1, AnalysisVar<StateType> arg2) {
		assert lhs != null && op != null && arg1 != null && arg2 != null;
		this.lhs = lhs;
		this.op = op;
		this.args = new ArrayList<>();
		this.args.add(arg1);
		this.args.add(arg2);
	}

	public AnalysisVar<StateType> getLhs() {
		return lhs;
	}

	public StateType getVal() {
		return lhs.value;
	}

	public Operation<StateType> getOp() {
		return op;
	}

	public List<AnalysisVar<StateType>> getArgs() {
		return Collections.unmodifiableList(args);
	}

	/**
	 * Computes the value of the right-hand side of the equation for the current
	 * values of {@link args} and assigns it to {@link lhs} thereby providing a
	 * local solution for this equation.
	 */
	public void update() {
		StateType newValue;
		switch (op.arity()) {
		// Handle unary operations.
		case 1:
			newValue = op.apply(args.get(0).value);
			break;
		// Handle binary operations.
		case 2:
			newValue = op.apply(args.get(0).value, args.get(1).value);
			break;
		default:
			// Handle operations with more than two arguments.
			ArrayList<StateType> argStates = new ArrayList<>();
			for (int i = 0; i < op.arity(); ++i) {
				argStates.add(args.get(i).value);
			}
			newValue = op.apply(argStates);
		}
		assert newValue != null;
		lhs.value = newValue;
	}

	@Override
	public String toString() {
		return lhs + " = " + op.toString(args)
				+ (sourceDescription == null ? "" : " // " + sourceDescription);
	}

	@Override
	public int compareTo(Equation<StateType> other) {
		return other.priority - this.priority;
	}
}