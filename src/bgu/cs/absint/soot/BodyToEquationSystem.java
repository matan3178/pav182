package bgu.cs.absint.soot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bgu.cs.absint.AnalysisVar;
import bgu.cs.absint.BinaryOperation;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.Equation;
import bgu.cs.absint.EquationSystem;
import bgu.cs.absint.Operation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.solver.PhasedOperation;
import soot.Body;
import soot.Unit;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

/**
 * Builds a system of equations appropriate for analyzing a given method body.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 * 
 *            TODO: optimize-away goto's.
 */
public class BodyToEquationSystem<StateType> {
	protected Body b;
	protected AbstractDomain<StateType, Unit> domain;
	protected AnalysisVar<StateType> entryVar = null;
	protected UnitGraph g;
	protected int varCounter = 0;

	protected Map<Unit, List<AnalysisVar<StateType>>> unitToPredVars = new HashMap<>();
	protected Map<Unit, AnalysisVar<StateType>> unitToOutVar = new HashMap<>();
	protected Map<Unit, AnalysisVar<StateType>> ifStmtToAssumeFalseVar = new HashMap<>();
	protected Map<Unit, AnalysisVar<StateType>> unitToJoinVar = new HashMap<>();
	protected Map<Unit, AnalysisVar<StateType>> loopHeadUnitToBoxVar = new HashMap<>();

	protected Map<Equation<StateType>, Unit> equationToUnit = new HashMap<>();

	protected boolean useWidening;
	protected Operation<StateType> wideningNarrowingOperation;

	private Set<Unit> loopHeads = new HashSet<>();

	public BodyToEquationSystem(Body b, AbstractDomain<StateType, Unit> dom) {
		this.b = b;
		this.domain = dom;
		g = new ExceptionalUnitGraph(b);
	}

	public EquationSystem<StateType, Unit> build() {
		return build(false);
	}

	public EquationSystem<StateType, Unit> build(boolean useWidening) {
		this.useWidening = useWidening;
		markLoopHeads();
		assignPriorities();
		allocateVars();
		return createEquations();
	}

	public Map<Equation<StateType>, Unit> getEquationToUnit() {
		return equationToUnit;
	}

	protected void assignPriorities() {
		// TODO: implement me!
	}

	protected EquationSystem<StateType, Unit> createEquations() {
		EquationSystem<StateType, Unit> system = new EquationSystem<>();

		// Add an equation to initialize the entry variable to top.
		Equation<StateType> setTopToEntryVar = new Equation<StateType>(entryVar, domain.getTopOperation(),
				new ArrayList<AnalysisVar<StateType>>());
		system.addEquation(setTopToEntryVar);
		equationToUnit.put(setTopToEntryVar, g.getHeads().get(0));

		for (Unit unit : b.getUnits()) {
			// Handle control-flow joins
			List<AnalysisVar<StateType>> predVarsForUnit = unitToPredVars.get(unit);
			if (predVarsForUnit.size() == 2) {
				AnalysisVar<StateType> var1 = predVarsForUnit.get(0);
				AnalysisVar<StateType> var2 = predVarsForUnit.get(1);
				AnalysisVar<StateType> joinVar = unitToJoinVar.get(unit);
				BinaryOperation<StateType> joinOperation = loopHeads.contains(unit) ? domain.getUBLoopOperation()
						: domain.getUBOperation();
				Equation<StateType> joinEquation = new Equation<StateType>(joinVar, joinOperation, var1, var2);
				system.addEquation(joinEquation);
				equationToUnit.put(joinEquation, unit);

				if (useWidening && loopHeads.contains(unit)) {
					AnalysisVar<StateType> boxVar = loopHeadUnitToBoxVar.get(unit);
					PhasedOperation<StateType> boxOperation = new PhasedOperation<StateType>(
							domain.getWideningOperation(), domain.getNarrowingOperation()) {
						@Override
						public String toString() {
							return domain.getClass().getSimpleName() + "[Widening|Narrowing]";
						}
					};
					Equation<StateType> wideningEquation = new Equation<StateType>(boxVar, boxOperation, boxVar,
							joinVar);
					system.addEquation(wideningEquation);
					equationToUnit.put(wideningEquation, unit);
				}
			} else if (predVarsForUnit.size() > 2) {
				AnalysisVar<StateType> joinVar = unitToJoinVar.get(unit);
				Operation<StateType> joinOperation = loopHeads.contains(unit)
						? domain.getMultiUBLoopOperation((byte) predVarsForUnit.size())
						: domain.getMultiUBOperation((byte) predVarsForUnit.size());
				Equation<StateType> joinEquation = new Equation<StateType>(joinVar, joinOperation, predVarsForUnit);
				system.addEquation(joinEquation);
				equationToUnit.put(joinEquation, unit);
			}

			AnalysisVar<StateType> inputVar = predVarsForUnit.size() == 1 ? predVarsForUnit.get(0)
					: unitToJoinVar.get(unit);
			if (useWidening) {
				AnalysisVar<StateType> boxVar = loopHeadUnitToBoxVar.get(unit);
				if (boxVar != null)
					inputVar = boxVar;
			}

			if (unit instanceof IfStmt) {
				IfStmt ifStmt = (IfStmt) unit;
				Assume trueAssume = new Assume(ifStmt, true);
				Assume falseAssume = new Assume(ifStmt, false);

				// Add assume equation for true polarity.
				AnalysisVar<StateType> assumeTrueVar = unitToOutVar.get(unit);
				UnaryOperation<StateType> assumeTrueTransformer = domain.getTransformer(trueAssume);
				assert assumeTrueTransformer != null : domain.getClass().getSimpleName()
						+ ".assumeTrueTransformer return null for " + unit + "!";

				Equation<StateType> assumeTrueEquation = new Equation<>(assumeTrueVar, assumeTrueTransformer, inputVar);
				assumeTrueEquation.sourceDescription = "assume " + ifStmt.getCondition();
				system.addEquation(assumeTrueEquation);
				equationToUnit.put(assumeTrueEquation, unit);

				// Add assume equation for false polarity.
				AnalysisVar<StateType> assumeFalseVar = ifStmtToAssumeFalseVar.get(ifStmt);
				UnaryOperation<StateType> assumeFalseTransformer = domain.getTransformer(falseAssume);
				assert assumeFalseTransformer != null : domain.getClass().getSimpleName()
						+ ".assumeFalseTransformer returned null for " + unit + "!";
				Equation<StateType> assumeFalseEquation = new Equation<>(assumeFalseVar, assumeFalseTransformer,
						inputVar);
				assumeFalseEquation.sourceDescription = "assume !(" + ifStmt.getCondition() + ')';
				system.addEquation(assumeFalseEquation);
				equationToUnit.put(assumeFalseEquation, unit);
			} else {
				AnalysisVar<StateType> lhsVar = unitToOutVar.get(unit);
				UnaryOperation<StateType> unitTransformer = domain.getTransformer((Stmt) unit);
				assert unitTransformer != null : domain.getClass().getSimpleName()
						+ ".getTransformer returned null for " + unit + "!";
				Equation<StateType> unitEquation = new Equation<>(lhsVar, unitTransformer, inputVar);
				system.addEquation(unitEquation);
				equationToUnit.put(unitEquation, unit);
			}
		}

		for (Equation<StateType> equation : system.getEquations()) {
			if (equation.sourceDescription == null)
				equation.sourceDescription = equationToUnit.get(equation).toString();
		}

		// TODO: check that all variables in the system are reachable from
		// entryVar.
		return system;
	}

	/**
	 * Store all units at loop heads in a specialized collection.
	 */
	protected void markLoopHeads() {
		LoopFinder loopFinder = new LoopFinder();
		loopFinder.transform(b);
		Collection<Loop> loops = loopFinder.loops();
		for (Loop loop : loops) {
			loopHeads.add(loop.getHead());
		}
	}

	/**
	 * Adds the given variable as an input variable to the given unit.
	 * 
	 * @param u
	 *            A unit.
	 * @param predVar
	 *            An analysis variable.
	 */
	protected void addPredVarToUnit(Unit u, AnalysisVar<StateType> predVar) {
		List<AnalysisVar<StateType>> predVars = unitToPredVars.get(u);
		if (predVars == null) {
			predVars = new ArrayList<>(2);
			unitToPredVars.put(u, predVars);
		}
		assert !predVars.contains(predVar);
		predVars.add(predVar);
	}

	/**
	 * Allocates variables to be used in the equation system.
	 */
	protected void allocateVars() {
		// Create a variable for the entry.
		entryVar = new AnalysisVar<StateType>(getNewVar());

		// Allocate variables for the inputs and outputs of each unit.
		for (Unit unit : b.getUnits()) {
			AnalysisVar<StateType> var = new AnalysisVar<StateType>(getNewVar());
			unitToOutVar.put(unit, var);

			// Conditions need a second variable for the false polarity.
			if (unit instanceof IfStmt) {
				IfStmt ifStmt = (IfStmt) unit;
				Unit trueTarget = ifStmt.getTarget();
				addPredVarToUnit(trueTarget, var);

				ArrayList<Unit> succs = new ArrayList<>(g.getSuccsOf(unit));
				assert succs.size() == 2;
				succs.remove(trueTarget);
				assert succs.size() == 1;
				Unit falseTarget = succs.get(0);
				AnalysisVar<StateType> assumeFalseVar = new AnalysisVar<StateType>(getNewVar());
				ifStmtToAssumeFalseVar.put(unit, assumeFalseVar);
				addPredVarToUnit(falseTarget, assumeFalseVar);
			} else if (unit instanceof GotoStmt) {
				GotoStmt gotoStmt = (GotoStmt) unit;
				Unit target = gotoStmt.getTarget();
				addPredVarToUnit(target, var);
			} else {
				List<Unit> succs = g.getSuccsOf(unit);
				for (Unit target : succs) {
					addPredVarToUnit(target, var);
				}
			}

			if (g.getPredsOf(unit).isEmpty()) {
				addPredVarToUnit(unit, entryVar);
			}

			// Allocate a variable to join the variables of its predecessors.
			if (g.getPredsOf(unit).size() > 1) {
				var = new AnalysisVar<StateType>(getNewVar());
				unitToJoinVar.put(unit, var);

				// Allocate variables used for widening/narrowing at loop heads.
				if (useWidening && loopHeads.contains(unit)) {
					var = new AnalysisVar<StateType>(getNewVar());
					loopHeadUnitToBoxVar.put(unit, var);
				}
			}
		}
	}

	/**
	 * Creates a new variable and names it.
	 * 
	 * @return An analysis variable.
	 */
	protected String getNewVar() {
		return "V[" + varCounter++ + "]";
	}

	// protected boolean isSystemConnected(EquationSystem<StateType> system) {
	// boolean result = true;
	// Collection<AnalysisVar<StateType>> reachable = new HashSet<>();
	// reachable.add(entryVar);
	//
	// return result;
	// }
}