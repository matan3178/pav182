package bgu.cs.absint.analyses.ve;

import soot.Local;
import bgu.cs.absint.constructor.ConjunctiveState;

/**
 * A set of {@link VEFactoid}s of the form {@code x=y} for two local variables.
 * The (symbolic) meaning of a state is given by the conjunction of the
 * {@link VEFactoid}s. The set does not contain symmetric {@link VEFactoid}s
 * (i.e., if {@code x=y} is in the set then {@code y=x} is not in the set) and
 * trivial {@link VEFactoid}s (i.e., {@code x=x}).
 * 
 * @author romanm
 * 
 */
public class VEState extends ConjunctiveState<Local, VEFactoid> {
	/**
	 * An immutable bottom element.
	 */
	public static final VEState bottom = new VEState(false) {
		@Override
		public boolean addFactoid(Local lhs, Local rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(VEFactoid factoid) {
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
	public static final VEState top = new VEState() {
		@Override
		public boolean addFactoid(Local lhs, Local rhs) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean add(VEFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}
	};

	public VEState() {
		super();
	}

	@Override
	public VEState copy() {
		if (this == bottom)
			return bottom;
		else
			return new VEState(this);
	}

	public boolean addFactoid(Local lhs, Local rhs) {
		return factoids.add(new VEFactoid(lhs, rhs));
	}

	protected VEState(boolean dummy) {
		super(false);
	}

	protected VEState(VEState copyFrom) {
		super(copyFrom.factoids);
	}
}