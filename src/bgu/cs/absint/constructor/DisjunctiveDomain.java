package bgu.cs.absint.constructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;

/**
 * The disjunctive completion of a (base) abstract domain.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The type of abstract domain elements in the base domain.
 */
public class DisjunctiveDomain<StateType, ActionType> extends
		AbstractDomain<DisjunctiveState<StateType>, ActionType> {
	/**
	 * The implementation of abstract operations on the base abstract domain.
	 */
	protected final AbstractDomain<StateType, ActionType> baseDomain;

	/**
	 * The least element of the Cartesian domain.
	 */
	protected final DisjunctiveState<StateType> bottom;

	/**
	 * The top element of the Cartesian domain.
	 */
	protected final DisjunctiveState<StateType> top;

	protected final boolean aggressiveUBAtLoopHeads;

	/**
	 * Constructs an abstract domain that is the disjunctive completion of a
	 * given base domain.
	 * 
	 * @param baseDomain
	 *            An implementation of the seed domain.
	 * @param aggressiveUBAtLoopHeads
	 *            Determines whether all disjuncts are joined via the sub-domain
	 *            upper-bound operator or not (default is true).
	 */
	public DisjunctiveDomain(AbstractDomain<StateType, ActionType> baseDomain,
			boolean aggressiveUBAtLoopHeads) {
		this.baseDomain = baseDomain;
		this.aggressiveUBAtLoopHeads = aggressiveUBAtLoopHeads;
		bottom = new DisjunctiveState<StateType>();
		top = new DisjunctiveState<StateType>(baseDomain.getTop());
	}

	public DisjunctiveDomain(AbstractDomain<StateType, ActionType> subDomain) {
		this(subDomain, true);
	}

	/**
	 * Returns a disjunctive state that is the empty set of elements from the
	 * base domain.
	 */
	@Override
	public DisjunctiveState<StateType> getBottom() {
		return bottom;
	}

	/**
	 * Returns a disjunctive state that contains the top element from the base
	 * domain.
	 */
	@Override
	public DisjunctiveState<StateType> getTop() {
		return top;
	}

	/**
	 * Takes the union of all elements from the base domain and removes
	 * ascending chains by keeping the maximal elements.
	 */
	@Override
	public DisjunctiveState<StateType> ub(DisjunctiveState<StateType> elem1,
			DisjunctiveState<StateType> elem2) {
		Collection<StateType> reducedUnion = new HashSet<StateType>(
				elem1.getDisjuncts());
		for (StateType sub2 : elem2) {
			boolean subsumed = false;
			for (StateType sub1 : elem1) {
				if (baseDomain.leq(sub1, sub2)) {
					subsumed = true;
					break;
				}
			}
			if (!subsumed)
				reducedUnion.add(sub2);
		}
		DisjunctiveState<StateType> result = new DisjunctiveState<StateType>(
				reducedUnion);
		return result;
	}

	/**
	 * Merges all elements in the set into a single element of the base domain
	 * by using its upper-bound operation.
	 */
	@Override
	public DisjunctiveState<StateType> ubLoop(
			DisjunctiveState<StateType> elem1, DisjunctiveState<StateType> elem2) {
		if (aggressiveUBAtLoopHeads) {
			StateType resultSub = baseDomain.getBottom();
			for (StateType subElem : elem1) {
				resultSub = baseDomain.ub(resultSub, subElem);
			}
			for (StateType subElem : elem2) {
				resultSub = baseDomain.ub(resultSub, subElem);
			}
			return new DisjunctiveState<StateType>(resultSub);
		} else {
			return ub(elem1, elem2);
		}
	}

	/**
	 * If both states are singleton we use the widening from the base domain.
	 * Otherwise, we first merge all disjuncts from each element into a single
	 * one using the upper-bound operation from the base domain and then widen
	 * using
	 */
	@Override
	public DisjunctiveState<StateType> widen(DisjunctiveState<StateType> elem1,
			DisjunctiveState<StateType> elem2) {
		if (elem1.size() == 1 && elem2.size() == 1) {
			StateType baseElem1 = elem1.iterator().next();
			StateType baseElem2 = elem2.iterator().next();
			StateType widenedElem = baseDomain.widen(baseElem1, baseElem2);
			DisjunctiveState<StateType> result = new DisjunctiveState<StateType>(
					widenedElem);
			return result;
		} else {
			StateType resultSub = baseDomain.getBottom();
			for (StateType subElem : elem1) {
				resultSub = baseDomain.ub(resultSub, subElem);
			}
			for (StateType subElem : elem2) {
				resultSub = baseDomain.ub(resultSub, subElem);
			}
			return new DisjunctiveState<StateType>(resultSub);
		}
	}

	@Override
	public boolean leq(DisjunctiveState<StateType> elem1,
			DisjunctiveState<StateType> elem2) {
		for (StateType sub1 : elem1) {
			boolean subsumed = false;
			for (StateType sub2 : elem2) {
				if (baseDomain.leq(sub1, sub2)) {
					subsumed = true;
					break;
				}
			}
			if (!subsumed)
				return false;
		}
		return true;
	}

	/**
	 * A transformer that operates by applying a sub-transformer to each
	 * component separately.
	 */
	@Override
	public UnaryOperation<DisjunctiveState<StateType>> getTransformer(
			ActionType action) {
		final UnaryOperation<StateType> subTransformer = baseDomain
				.getTransformer(action);
		if (subTransformer == IdOperation.v())
			return IdOperation.v();
		else
			return new UnaryOperation<DisjunctiveState<StateType>>() {
				@Override
				public DisjunctiveState<StateType> apply(
						DisjunctiveState<StateType> input) {
					Collection<StateType> outputs = new ArrayList<StateType>(
							input.size());
					for (StateType subElem : input) {
						StateType subOutput = subTransformer.apply(subElem);
						if (subOutput != baseDomain.getBottom())
							outputs.add(subOutput);
					}
					DisjunctiveState<StateType> result = new DisjunctiveState<StateType>(
							outputs);
					return result;
				}

				@Override
				public String toString() {
					return "P(" + subTransformer.toString() + ")";
				}
			};
	}
}