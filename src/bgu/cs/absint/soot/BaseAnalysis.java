package bgu.cs.absint.soot;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.Equation;
import bgu.cs.absint.EquationSystem;
import bgu.cs.absint.ErrorState;
import bgu.cs.absint.solver.ChaoticIterationSolver;
import bgu.cs.absint.solver.WideningNarrowingSolver;
import bgu.cs.util.StringUtils;
import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.tagkit.StringTag;

/**
 * A {@link BodyTransformer} that applies the analysis to the given body and
 * stores the results as tags on the body units.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public class BaseAnalysis<StateType, DomType extends AbstractDomain<StateType, Unit>>
		extends BodyTransformer {
	protected boolean debug = true;

	protected boolean useWidening = false;
	protected boolean useNarrowing = false;

	protected boolean ignoreStaticInitializers = true;
	protected boolean ignoreConstructors = true;
	protected boolean ignoreAnalysisPrefix = true;

	protected int totalNumberOfErrors = 0;

	protected DomType domain;
	protected Collection<Unit> errorUnits = new HashSet<>();
	public static Map<Body, Collection<Unit>> bodyToErrorUnits = new HashMap<>();

	public BaseAnalysis(DomType domain) {
		this.domain = domain;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void useWidening(boolean value) {
		this.useWidening = value;
	}

	public void useNarrowing(boolean value) {
		this.useNarrowing = value;
	}

	public void setIgnoreStaticInitializers(boolean value) {
		this.ignoreStaticInitializers = value;
	}

	public void setIgnoreConstructors(boolean value) {
		this.ignoreConstructors = value;
	}

	public void setIgnoreAnalysisPrefix(boolean value) {
		this.ignoreAnalysisPrefix = value;
	}

	public void reportErrors() {
		System.err.println("*** Total number of errors: " + totalNumberOfErrors
				+ " ***");
		for (Map.Entry<Body, Collection<Unit>> entry : bodyToErrorUnits
				.entrySet()) {
			Body b = entry.getKey();
			Collection<Unit> errorUnits = entry.getValue();
			System.err.println(StringUtils.addUnderline("Number of errors for "
					+ b.getMethod().getName() + ": " + errorUnits.size()));
			String errorLines = StringUtils.toString(errorUnits, "\n");
			System.err.println(errorLines);
		}
	}

	@Override
	protected void internalTransform(Body b, String phaseName,
			@SuppressWarnings("rawtypes") Map options) {
		if (!filter(b))
			return;

		if (debug) {
			System.err.println();
			String message = "Analyzing method " + b.getMethod().getName();
			String underLinedMessage = StringUtils.addUnderline(message);
			System.err.println(underLinedMessage);
		}
		analyzeAndTag(b);
	}

	/**
	 * Checks whether a method should be analyzed.
	 * 
	 * @param b
	 *            A method body.
	 * @return true if the method should be analyzed and false otherwise.
	 */
	protected boolean filter(Body b) {
		// Skip the artificial error method.
		if (b.getMethod().getName().equals("error"))
			return false;
		// Skip static initializers.
		if (ignoreStaticInitializers
				&& b.getMethod().getName().equals("<clinit>"))
			return false;
		// Skip constructors.
		if (ignoreConstructors && b.getMethod().getName().equals("<init>"))
			return false;
		// Skip special analysis methods.
		if (ignoreAnalysisPrefix
				&& b.getMethod().getName().startsWith("analysis"))
			return false;
		return true;
	}

	protected void analyzeAndTag(Body b) {
		errorUnits = new HashSet<>();
		BodyToEquationSystem<StateType> systemBuilder = new BodyToEquationSystem<>(
				b, domain);
		EquationSystem<StateType, Unit> system = systemBuilder
				.build(useWidening);
		system.resetBottom(domain); // Start analysis from bottom values.
		ChaoticIterationSolver<StateType, Unit> solver = useWidening ? new WideningNarrowingSolver<StateType, Unit>()
				: new ChaoticIterationSolver<StateType, Unit>();
		solver.debug = this.debug;
		solver.solve(system, domain);
		Map<Equation<StateType>, Unit> equationToUnit = systemBuilder
				.getEquationToUnit();
		tagUnits(equationToUnit);
		checkForErrors(b, equationToUnit);
	}

	protected void checkForErrors(Body b,
			Map<Equation<StateType>, Unit> equationToUnit) {
		for (Map.Entry<Equation<StateType>, Unit> entry : equationToUnit
				.entrySet()) {
			Equation<StateType> equation = entry.getKey();
			Unit u = entry.getValue();
			if (u instanceof InvokeStmt) {
				InvokeStmt invoke = (InvokeStmt) u;
				if (invoke.getInvokeExpr().getMethod().getName()
						.equals("error")
						&& !equation.getLhs().value.equals(domain.getBottom())) {
					errorUnits.add(u);
					u.addTag(new StringTag("Possible error!", "WarningMessage"));
				}
			}

			StateType state = equation.getLhs().value;
			if (state instanceof ErrorState) {
				errorUnits.add(u);
				u.addTag(new StringTag("Possible error!", "WarningMessage"));
			}
		}
		if (debug) {
			System.err.println(errorUnits.size() + " possible error(s) found.");
			totalNumberOfErrors += errorUnits.size();
			if (!errorUnits.isEmpty()) {
				System.err.println(errorUnits);
				bodyToErrorUnits.put(b, errorUnits);
			}
		}
	}

	protected void tagUnits(Map<Equation<StateType>, Unit> equationToUnit) {
		for (Map.Entry<Equation<StateType>, Unit> entry : equationToUnit
				.entrySet()) {
			Equation<StateType> equation = entry.getKey();
			Unit u = entry.getValue();
			u.addTag(new StringTag(equation.toString(), "AnalysisEquation"));
			u.addTag(new StringTag(equation.getLhs() + " : "
					+ equation.getLhs().value, "AnalysisResult"));
		}
	}
}
