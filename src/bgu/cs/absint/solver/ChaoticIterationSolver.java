package bgu.cs.absint.solver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;

import bgu.cs.absint.AnalysisVar;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.Equation;
import bgu.cs.absint.EquationSystem;
import bgu.cs.util.StringUtils;

/**
 * Solves a given system of equations by computing a fixed-point from below
 * using the chaotic iteration method.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 * @param <ActionType>
 *            The implementation type of program statements.
 */
public class ChaoticIterationSolver<StateType, ActionType> extends
		Solver<StateType, ActionType> {
	protected int iterationCounter = 0;

	protected void initializeValues() {
		system.initializeValues(domain.getBottom());
	}

	@Override
	public void solve(EquationSystem<StateType, ActionType> system,
			AbstractDomain<StateType, ActionType> domain) {
		this.system = system;
		this.domain = domain;
		printDebugMessage("Solving the following equation system = "
				+ StringUtils.newLine + system);
		initializeValues();
		printDebugMessage("Starting chaotic iterations...");
		iterateUp();
		printDebugMessage("Reached fixed-point after " + iterationCounter
				+ " iterations.");
		printDebugSolution(system);

		assert system.allVariablesInitialized() : "Solution resulted with null-valued variables!";
	}

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
	public void iterateUp() {
		Collection<Equation<StateType>> initialEquations = system.getHeads();
		if (initialEquations.isEmpty())
			initialEquations.addAll(system.getEquations());

		// Iterate until reaching a fixed-point.
		PriorityQueue<Equation<StateType>> workSet = new PriorityQueue<>(
				initialEquations);
		while (!workSet.isEmpty()) {
			++iterationCounter;
			printDebugMessage("              workSet = "
					+ equationsToLhsString(workSet));

			Equation<StateType> equation = workSet.remove();

			printDebugMessage("Iteration " + iterationCounter + ": processing "
					+ equation.toString());

			AnalysisVar<StateType> lhs = equation.getLhs();
			StateType currentValue = lhs.value;

			if (debug) {
				printDebugMessage("              " + equation.getLhs() + " : "
						+ equation.getLhs().value);
				for (AnalysisVar<StateType> arg : equation.getArgs()) {
					printDebugMessage("              " + arg + " : "
							+ arg.value);
				}
			}
			equation.update();
			printDebugMessage("              " + equation.getLhs() + "' : "
					+ equation.getLhs().value);

			if (!domain.leq(lhs.value, currentValue)) {
				Collection<Equation<StateType>> nextEquations = new ArrayList<>();
				for (Equation<StateType> nextEquation : system
						.getDependentEquations(lhs)) {
					if (nextEquation == equation) {
						continue;
					} else {
						workSet.add(nextEquation);
						nextEquations.add(nextEquation);
					}
				}

				printDebugMessage("              Adding " + nextEquations);
			}
		}
	}

	public void iterateDown() {
		// Iterate until reaching a fixed-point.
		PriorityQueue<Equation<StateType>> workSet = new PriorityQueue<>(
				system.getEquations());
		while (!workSet.isEmpty()) {
			++iterationCounter;
			printDebugMessage("              workSet = "
					+ equationsToLhsString(workSet));

			Equation<StateType> equation = workSet.remove();

			printDebugMessage("Iteration " + iterationCounter + ": processing "
					+ equation.toString());

			AnalysisVar<StateType> lhs = equation.getLhs();
			StateType currentValue = lhs.value;

			if (debug) {
				printDebugMessage("              " + equation.getLhs() + " : "
						+ equation.getLhs().value);
				for (AnalysisVar<StateType> arg : equation.getArgs()) {
					printDebugMessage("              " + arg + " : "
							+ arg.value);
				}
			}
			equation.update();
			printDebugMessage("              " + equation.getLhs() + "' : "
					+ equation.getLhs().value);

			boolean newLeq = domain.leq(lhs.value, currentValue);
			boolean newGeq = domain.leq(currentValue, lhs.value);
			boolean neqLt = newLeq && !newGeq;
			if (neqLt) {
				Collection<Equation<StateType>> nextEquations = new ArrayList<>();
				for (Equation<StateType> nextEquation : system
						.getDependentEquations(lhs)) {
					if (nextEquation == equation) {
						continue;
					} else {
						workSet.add(nextEquation);
						nextEquations.add(nextEquation);
					}
				}

				printDebugMessage("              Adding " + nextEquations);
			}
		}
	}
}