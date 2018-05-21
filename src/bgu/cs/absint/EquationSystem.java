package bgu.cs.absint;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A system of equations providing an abstract semantics to a program.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public class EquationSystem<StateType, ActionType> {
	/**
	 * Maps the {@link AnalysisVar} on the left-hand side of an equation to the
	 * {@link Equation} on its right-hand side.
	 */
	protected LinkedHashMap<AnalysisVar<StateType>, Equation<StateType>> varToEquation = new LinkedHashMap<>();

	/**
	 * Maps a variable to all equations containing it on the right-hand side
	 * (i.e., as an argument of the operation on its right-hand side).
	 */
	protected HashMap<AnalysisVar<StateType>, Set<Equation<StateType>>> varToContainingEquations = new HashMap<>();

	/**
	 * Sets the values of all variables to the given {@link value}.
	 * 
	 * @param value
	 *            The value set for all variables.
	 */
	public void resetValues(StateType value) {
		for (AnalysisVar<StateType> var : getAllVars()) {
			var.value = value;
		}
	}

	/**
	 * Assigns the given value to uninitialized values and leaves initialized
	 * variables with their current values.
	 * 
	 * @param value
	 *            An abstract domain element.
	 */
	public void initializeValues(StateType value) {
		for (AnalysisVar<StateType> var : getAllVars()) {
			if (var.value == null)
				var.value = value;
		}
	}

	/**
	 * Sets the values of all variables to bottom.
	 * 
	 * @param ops
	 *            The object supplying the bottom value.
	 */
	public void resetBottom(AbstractDomain<StateType, ActionType> ops) {
		resetValues(ops.getBottom());
	}

	/**
	 * Checks whether all variables have been initialized.
	 * 
	 * @return true if all variables have been initialized.
	 */
	public boolean allVariablesInitialized() {
		for (AnalysisVar<StateType> var : getAllVars()) {
			if (var.value == null)
				return false;
		}
		return true;
	}

	/**
	 * Creates a map from each variable to the value set for it.
	 * 
	 * @return A map from each variable to the value set for it.
	 */
	public Map<AnalysisVar<StateType>, StateType> getSolution() {
		// Build the solution from the values stored in the variables.
		Map<AnalysisVar<StateType>, StateType> solution = new LinkedHashMap<>(
				getAllVars().size());
		for (AnalysisVar<StateType> var : getAllVars()) {
			solution.put(var, var.value);
		}
		return solution;

	}

	/**
	 * Returns the set of equations that do not depend on any variable.
	 * 
	 * @return The set of equations that do not depend on any variable.
	 */
	public Set<Equation<StateType>> getHeads() {
		Set<Equation<StateType>> result = new HashSet<Equation<StateType>>();
		for (Equation<StateType> equation : varToEquation.values()) {
			if (equation.getArgs().isEmpty())
				result.add(equation);
		}
		return result;
	}

	/**
	 * Adds a new equation to the system.
	 * 
	 * @param equation
	 *            An equation.
	 */
	public void addEquation(Equation<StateType> equation) {
		AnalysisVar<StateType> var = equation.lhs;
		if (varToEquation.containsKey(var)) {
			throw new Error(
					String.format(
							"Attempt to update the defining equation of variable %s from %s to %s!",
							var, varToEquation.get(var), equation));

		}
		varToEquation.put(var, equation);

		// Update varToContainingEquations.
		for (AnalysisVar<StateType> argVar : equation.getArgs()) {
			Set<Equation<StateType>> containingEquations = varToContainingEquations
					.get(argVar);
			if (containingEquations == null) {
				containingEquations = new HashSet<>();
				varToContainingEquations.put(argVar, containingEquations);
			}
			containingEquations.add(equation);
		}
	}

	/**
	 * Returns the equation containing {@link var} on its left-hand side if one
	 * exists.
	 * 
	 * @param var
	 *            A variable.
	 * @return The equation containing {@link var} on its left-hand side if one
	 *         exists and null otherwise.
	 */
	public Equation<StateType> getDefiningEquation(AnalysisVar<StateType> var) {
		return varToEquation.get(var);
	}

	/**
	 * Returns a collection view of the set of all equations in the system.
	 * 
	 * @return The set of all equations in the system.
	 */
	public Collection<Equation<StateType>> getEquations() {
		return varToEquation.values();
	}

	/**
	 * Returns the set of all equations containing {@link var} on their
	 * right-hand side.
	 * 
	 * @param var
	 *            A variable participating in this system.
	 * @return An unmodifiable set of equations containing {@link var} on their
	 *         right-hand side.
	 */
	public Set<Equation<StateType>> getDependentEquations(
			AnalysisVar<StateType> var) {
		Set<Equation<StateType>> result = varToContainingEquations.get(var);
		if (result == null)
			return Collections.emptySet();
		return Collections.unmodifiableSet(result);
	}

	/**
	 * Returns the set of all variables participating in this system.
	 * 
	 * @return An unmodifiable view of the set of variables in the system.
	 */
	public Set<AnalysisVar<StateType>> getAllVars() {
		Set<AnalysisVar<StateType>> result = new HashSet<>();
		for (Equation<StateType> equation : varToEquation.values()) {
			result.add(equation.lhs);
			result.addAll(equation.args);
		}
		return result;
	}

	/**
	 * Checks whether this system is well-formed according to some structural
	 * rules, e.g., every variable appears on the left-hand side of exactly one
	 * equation.
	 * 
	 * @return true if this system
	 */
	public boolean isWellFormed() {
		boolean result = true;
		for (AnalysisVar<StateType> var : findAllVars()) {
			if (!varToEquation.containsKey(var)) {
				result = false;
				break;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Equation<StateType> equation : varToEquation.values()) {
			result.append(equation.toString() + "\n");
		}
		return result.toString();
	}

	/**
	 * Traverses the equations and collects all variables found in them.
	 * 
	 * @return The set of variables found in the system.
	 */
	protected Set<AnalysisVar<StateType>> findAllVars() {
		Set<AnalysisVar<StateType>> result = new HashSet<AnalysisVar<StateType>>();
		for (Equation<StateType> equation : varToEquation.values()) {
			result.addAll(equation.getArgs());
		}
		return result;
	}
}