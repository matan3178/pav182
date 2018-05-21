package bgu.cs.absint.constructor;

import java.util.ArrayList;
import java.util.Collection;

import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.util.Pair;

/**
 * An abstract domain of functions from a first sub-domain to a second
 * sub-domain.<br>
 * =================================================================== WARNING:
 * The implementation is incomplete so don't use it just yet.
 * ===================================================================
 * 
 * @author romanm
 * 
 * @param <StateType1>
 *            The type of abstract domain elements in the first abstract domain.
 * @param <StateType2>
 *            The type of abstract domain elements in the second abstract
 *            domain.
 */
public class MapDomain<StateType1, StateType2, ActionType> extends
		AbstractDomain<DisjunctiveState<Pair<StateType1, StateType2>>, ActionType> {
	/**
	 * The implementation of abstract operations on the first abstract domain.
	 */
	protected final AbstractDomain<StateType1, ActionType> domain1;

	/**
	 * The implementation of abstract operations on the second abstract domain.
	 */
	protected final AbstractDomain<StateType2, ActionType> domain2;

	/**
	 * The least element of the Cartesian domain.
	 */
	protected final DisjunctiveState<Pair<StateType1, StateType2>> bottom;

	/**
	 * The top element of the Cartesian domain.
	 */
	protected final DisjunctiveState<Pair<StateType1, StateType2>> top;

	/**
	 * Constructs an abstract domain that is the Cartesian combination of
	 * {@link domain1} and {@link ops2}.
	 * 
	 * @param domain1
	 *            An implementation of the first domain.
	 * @param domain2
	 *            An implementation of the second domain.
	 */
	public MapDomain(AbstractDomain<StateType1, ActionType> domain1,
			AbstractDomain<StateType2, ActionType> domain2) {
		this.domain1 = domain1;
		this.domain2 = domain2;
		bottom = new DisjunctiveState<Pair<StateType1, StateType2>>();
		top = new DisjunctiveState<Pair<StateType1, StateType2>>(
				new Pair<StateType1, StateType2>(domain1.getTop(),
						domain2.getTop()));
	}

	@Override
	public DisjunctiveState<Pair<StateType1, StateType2>> getBottom() {
		return bottom;
	}

	@Override
	public DisjunctiveState<Pair<StateType1, StateType2>> getTop() {
		return top;
	}

	@Override
	public DisjunctiveState<Pair<StateType1, StateType2>> ub(
			DisjunctiveState<Pair<StateType1, StateType2>> elem1,
			DisjunctiveState<Pair<StateType1, StateType2>> elem2) {
		throw new UnsupportedOperationException();
		// HashMap<Pair<StateType1, StateType2>, Pair<StateType1, StateType2>>
		// elemToUBElem = new HashMap<>(
		// elem1.size());
		// for (Pair<StateType1, StateType2> subElem1 : elem1) {
		// elemToUBElem.put(subElem1, subElem1);
		// }
		// for (Pair<StateType1, StateType2> subElem1 : elem1) {
		// elemToUBElem.put(subElem1, subElem1);
		// }
		//
		// // A sub-optimal join, but it's good enough for now.
		// DisjunctiveState<Pair<StateType1, StateType2>> result = new
		// DisjunctiveState<Pair<StateType1, StateType2>>(
		// elem1.getDisjuncts(), elem2.getDisjuncts());
		// return result;
	}

	@Override
	public boolean leq(DisjunctiveState<Pair<StateType1, StateType2>> first,
			DisjunctiveState<Pair<StateType1, StateType2>> second) {
		// A sub-optimal comparison, but it's good enough for now.
		return second.getDisjuncts().containsAll(first.getDisjuncts());
	}

	@Override
	public UnaryOperation<DisjunctiveState<Pair<StateType1, StateType2>>> getTransformer(
			ActionType stmt) {
		final UnaryOperation<StateType1> subTransformer1 = domain1
				.getTransformer(stmt);
		final UnaryOperation<StateType2> subTransformer2 = domain2
				.getTransformer(stmt);

		return new UnaryOperation<DisjunctiveState<Pair<StateType1, StateType2>>>() {
			@Override
			public DisjunctiveState<Pair<StateType1, StateType2>> apply(
					DisjunctiveState<Pair<StateType1, StateType2>> input) {
				Collection<Pair<StateType1, StateType2>> outputs = new ArrayList<Pair<StateType1, StateType2>>(
						input.size());
				for (Pair<StateType1, StateType2> subElem : input) {
					StateType1 subOutput1 = subTransformer1
							.apply(subElem.first);
					StateType2 subOutput2 = subTransformer2
							.apply(subElem.second);

					if (subOutput1 != domain1.getBottom()
							&& subOutput2 != domain2.getBottom()) {
						Pair<StateType1, StateType2> outputPair = new Pair<>(
								subOutput1, subOutput2);
						outputs.add(outputPair);
					}
				}
				DisjunctiveState<Pair<StateType1, StateType2>> result = new DisjunctiveState<Pair<StateType1, StateType2>>(
						outputs);
				return result;
			}

			@Override
			public String toString() {
				return "P(" + subTransformer1.toString() + ","
						+ subTransformer1.toString() + ")";
			}
		};
	}
}