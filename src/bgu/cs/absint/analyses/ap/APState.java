package bgu.cs.absint.analyses.ap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import soot.Local;
import bgu.cs.absint.ErrorState;
import bgu.cs.absint.constructor.ConjunctiveState;

/**
 * A set of varToFactoid of the form {@code x=a*y+b} for local variables 'x' and
 * 'y' and a integer constants 'a' and 'b'. The (symbolic) meaning of a state is
 * given by the conjunction of the varToFactoid.
 * 
 * @author romanm
 * 
 */
public class APState extends ConjunctiveState<Local, APFactoid> {
	/**
	 * An immutable bottom element.
	 */
	public static final APState bottom = new APState(false) {
		@Override
		public boolean add(APFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean remove(APFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}
	};

	/**
	 * An immutable top element.
	 */
	public static final APState top = new APState() {
		@Override
		public boolean add(APFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean remove(APFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}
	};

	/**
	 * Associates at most a single factoid with each variable. If a variable is
	 * not associated with any factoid it means the variable can have any
	 * integer value.
	 */
	protected final Map<Local, APFactoid> varToFactoid;

	/**
	 * Creates an error state.
	 */
	public static APState getErrorState(String errorMessage) {
		/**
		 * A class encapsulated within the enclosing method, since it is only
		 * used once.
		 * 
		 * @author romanm
		 * 
		 */
		class ErrorAPState extends APState implements ErrorState {
			public final String errorMessage;

			public ErrorAPState(String errorMessage) {
				this.errorMessage = errorMessage;
			}

			@Override
			public boolean add(APFactoid factoid) {
				throw new Error("Attempt to modify " + toString());
			}

			@Override
			public boolean remove(APFactoid factoid) {
				throw new Error("Attempt to modify " + toString());
			}

			@Override
			public boolean removeVar(Local lhs) {
				throw new Error("Attempt to modify " + toString());
			}

			@Override
			public String getMessages() {
				return errorMessage;
			}
		}
		;

		return new ErrorAPState(errorMessage);
	}

	/**
	 * Constructs a state with an empty set of varToFactoid (top).
	 */
	public APState() {
		this.varToFactoid = new HashMap<>();
	}

	/**
	 * Returns the set of variables mapped to a factoid.
	 */
	public Set<Local> getVars() {
		return varToFactoid.keySet();
	}

	@Override
	public APState copy() {
		if (this == bottom)
			return bottom;
		else
			return new APState(this);
	}

	@Override
	public Collection<APFactoid> getFactoids() {
		if (varToFactoid == null)
			return null;
		else
			return Collections.unmodifiableCollection(varToFactoid.values());
	}

	@Override
	public boolean add(APFactoid factoid) {
		assert factoid != null;
		// Avoid having two varToFactoid for the same variable.
		assert getFactoidForVar(factoid.var) == null : "Attempt to add a second factoid for the same variable! Possible fix: add the factoid by meet.";
		return varToFactoid.put(factoid.var, factoid) == null;
	}

	public boolean remove(APFactoid f) {
		return varToFactoid.remove(f.var) != null;
	}

	public boolean removeVar(Local lhs) {
		return varToFactoid.remove(lhs) != null;
	}

	/**
	 * Returns the factoid with the given variable or null if there is none,
	 * meaning the variable is mapped to top (can have any integer value).
	 */
	public APFactoid getFactoidForVar(Local var) {
		return varToFactoid.get(var);
	}

	/**
	 * Returns all the factoids that amount to a constant equality.
	 */
	public Collection<APFactoid> getConstantFactoids() {
		ArrayList<APFactoid> result = new ArrayList<>();
		for (APFactoid f : this) {
			if (f.isConstant())
				result.add(f);
		}
		return result;
	}

	/**
	 * Constructs a state containing the set of varToFactoid in the given input
	 * state.
	 */
	protected APState(APState copyFrom) {
		varToFactoid = new HashMap<>(copyFrom.varToFactoid);
	}

	/**
	 * A constructor just for initializing varToFactoid to null.
	 */
	protected APState(boolean dummy) {
		this.varToFactoid = null;
	}
}