package bgu.cs.absint.analyses.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import soot.Local;
import bgu.cs.absint.constructor.ConjunctiveState;

/**
 * A set of varToFactoid representing SetAnalysis. The (symbolic) meaning of a
 * state is given by the conjunction of the varToFactoid.
 * 
 * @author romanm
 * 
 */
public class SetState extends ConjunctiveState<Local, SetFactoid> {
	/**
	 * An immutable bottom element.
	 */
	public static final SetState bottom = new SetState(false) {
		@Override
		public boolean add(SetFactoid factoid) {
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
	public static final SetState top = new SetState() {
		@Override
		public boolean add(SetFactoid factoid) {
			throw new Error("Attempt to modify " + toString());
		}

		@Override
		public boolean removeVar(Local lhs) {
			throw new Error("Attempt to modify " + toString());
		}
	};

	/**
	 * An element is a set of varToFactoid.
	 */
	protected final Set<SetFactoid> factoids;

	public SetState() {
		factoids = new HashSet<>();
	}

	@Override
	public SetState copy() {
		if (this == bottom)
			return bottom;
		else
			return new SetState(this);
	}

	@Override
	public Collection<SetFactoid> getFactoids() {
		return factoids;
	}

	public Collection<EmptyFactoid> getEmptyFactoids() {
		ArrayList<EmptyFactoid> result = new ArrayList<>();
		for (SetFactoid f : getFactoids()) {
			if (f instanceof EmptyFactoid)
				result.add((EmptyFactoid) f);
		}
		return result;
	}

	public Collection<NotNullFactoid> getNotNullFactoids() {
		ArrayList<NotNullFactoid> result = new ArrayList<>();
		for (SetFactoid f : getFactoids()) {
			if (f instanceof NotNullFactoid)
				result.add((NotNullFactoid) f);
		}
		return result;
	}

	public Collection<UnionFactoid> getUnionFactoids() {
		ArrayList<UnionFactoid> result = new ArrayList<>();
		for (SetFactoid f : getFactoids()) {
			if (f instanceof UnionFactoid)
				result.add((UnionFactoid) f);
		}
		return result;
	}

	public Collection<EqualSetsFactoid> getEqualSetsFactoids() {
		ArrayList<EqualSetsFactoid> result = new ArrayList<>();
		for (SetFactoid f : getFactoids()) {
			if (f instanceof EqualSetsFactoid)
				result.add((EqualSetsFactoid) f);
		}
		return result;
	}

	@Override
	public boolean add(SetFactoid factoid) {
		assert factoid != null;
		return factoids.add(factoid);
	}

	/**
	 * Removes any factoid containing the given variable.
	 * 
	 * @param lhs
	 *            The variable to be removed from the state.
	 */
	@Override
	public boolean removeVar(Local lhs) {
		for (Iterator<SetFactoid> iter = factoids.iterator(); iter.hasNext();) {
			SetFactoid factoid = iter.next();
			if (factoid.hasVar(lhs)) {
				iter.remove();
				return true;
			}
		}
		return false;
	}

	protected SetState(SetState copyFrom) {
		factoids = new HashSet<>(copyFrom.factoids);
	}

	/**
	 * A constructor just for initializing varToFactoid to null.
	 */
	protected SetState(boolean dummy) {
		this.factoids = null;
	}
}