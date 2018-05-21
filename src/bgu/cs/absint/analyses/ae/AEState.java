package bgu.cs.absint.analyses.ae;

import soot.Local;
import soot.jimple.Expr;
import bgu.cs.absint.constructor.ConjunctiveState;

/**
 * A conjunctive set of factoids of the form {@code x=y+z} for three local
 * variables.
 * 
 * @author romanm
 */
public class AEState extends ConjunctiveState<Local, AEFactoid> {
	/**
	 * An immutable bottom element.
	 */
	public static final AEState bottom = new AEState(false) {
		@Override
		public boolean addFactoid(Local lhs, Expr rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(AEFactoid factoid) {
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
	public static final AEState top = new AEState() {
		@Override
		public boolean addFactoid(Local lhs, Expr rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(AEFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}
	};

	public AEState() {
		super();
	}

	@Override
	public AEState copy() {
		if (this == bottom)
			return bottom;
		else
			return new AEState(this);
	}

	public boolean addFactoid(Local lhs, Expr rhs) {
		return factoids.add(new AEFactoid(lhs, rhs));
	}

	protected AEState(AEState copyFrom) {
		super(copyFrom.factoids);
	}

	/**
	 * A constructor just for initializing factoids to null, used to represent
	 * the bottom state.
	 */
	protected AEState(boolean dummy) {
		super(false);
	}
}