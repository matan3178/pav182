package bgu.cs.absint.solver;

import java.util.Collection;

import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.Equation;
import bgu.cs.absint.EquationSystem;

/**
 * The super-class of solver implementations for a system of equations.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 * @param <ActionType>
 *            The implementation type of program statements.
 */
public abstract class Solver<StateType, ActionType> {
	public boolean debug = true;

	protected EquationSystem<StateType, ActionType> system;
	protected AbstractDomain<StateType, ActionType> domain;

	/**
	 * Computes the fixed-point solution of the given {@link system} from below,
	 * using chaotic iterations. The solution is stored in the variables of the
	 * system.
	 * 
	 * @param system
	 *            The system of equations that needs to be solved.
	 * @param domain
	 *            Provides the partial order test operation.
	 */
	public abstract void solve(EquationSystem<StateType, ActionType> system,
			AbstractDomain<StateType, ActionType> domain);

	protected String equationsToLhsString(
			Collection<Equation<StateType>> workSet) {
		StringBuilder result = new StringBuilder("{");
		int i = 0;
		for (Equation<StateType> equation : workSet) {
			result.append(equation.getLhs());
			++i;
			if (i < workSet.size())
				result.append(", ");
		}
		result.append("}");
		return result.toString();
	}

	protected void printDebugSolution(
			EquationSystem<StateType, ActionType> system) {
		if (!debug)
			return;
		System.out.println("Solution = {");
		for (Equation<StateType> equation : system.getEquations()) {
			System.out.println("  " + equation.getLhs() + " : "
					+ equation.getVal());
		}
		System.out.println("}");
	}

	protected void printDebugMessage(String message) {
		if (debug)
			System.out.println(message);
	}
}