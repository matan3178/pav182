package bgu.cs.absint.solver;

import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.Equation;
import bgu.cs.absint.EquationSystem;
import bgu.cs.absint.Operation;
import bgu.cs.util.StringUtils;

/**
 * A solver that first iterates up using widening and then iterates down using
 * narrowing.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The type implementing abstract elements.
 * @param <ActionType>
 *            The implementation type of program statements.
 */
public class WideningNarrowingSolver<StateType, ActionType> extends
		ChaoticIterationSolver<StateType, ActionType> {
	@Override
	public void solve(EquationSystem<StateType, ActionType> system,
			AbstractDomain<StateType, ActionType> domain) {
		this.system = system;
		this.domain = domain;
		printDebugMessage("Solving the following equation system = "
				+ StringUtils.newLine + system);
		initializeValues();
		printDebugMessage("Starting chaotic iteration: widening phase...");
		iterateUp();
		printDebugMessage("Reached fixed-point after " + iterationCounter
				+ " iterations.");
		printDebugSolution(system);

		// Switch all widening operations to narrowing operations.
		for (Equation<StateType> equation : system.getEquations()) {
			Operation<StateType> op = equation.getOp();
			if (op instanceof PhasedOperation) {
				PhasedOperation<StateType> wideningNarrowingOp = (PhasedOperation<StateType>) op;
				wideningNarrowingOp.advance();
			}
		}

		printDebugMessage("");
		printDebugMessage("Starting chaotic iteration: narrowing phase...");
		iterateDown();
		printDebugMessage("Reached fixed-point after " + iterationCounter
				+ " iterations.");

		printDebugSolution(system);

		assert system.allVariablesInitialized() : "Solution resulted with null-valued variables!";
	}
}