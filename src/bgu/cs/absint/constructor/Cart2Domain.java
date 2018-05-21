package bgu.cs.absint.constructor;

import bgu.cs.absint.ComposedOperation;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.util.Pair;

/**
 * A Cartesian combination of two abstract domains.
 * 
 * @author romanm
 * 
 * @param <StateType1>
 *            The type of abstract domain elements in the first abstract domain.
 * @param <StateType2>
 *            The type of abstract domain elements in the second abstract
 *            domain.
 */
public class Cart2Domain<StateType1, StateType2, ActionType> extends
		AbstractDomain<Pair<StateType1, StateType2>, ActionType> {
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
	protected final Pair<StateType1, StateType2> bottom;

	/**
	 * The top element of the Cartesian domain.
	 */
	protected final Pair<StateType1, StateType2> top;

	/**
	 * Constructs an abstract domain that is the Cartesian combination of
	 * {@link domain1} and {@link ops2}.
	 * 
	 * @param domain1
	 *            An implementation of the first domain.
	 * @param domain2
	 *            An implementation of the second domain.
	 */
	public Cart2Domain(AbstractDomain<StateType1, ActionType> domain1,
			AbstractDomain<StateType2, ActionType> domain2) {
		this.domain1 = domain1;
		this.domain2 = domain2;
		bottom = new Pair<StateType1, StateType2>(domain1.getBottom(),
				domain2.getBottom());
		top = new Pair<StateType1, StateType2>(domain1.getTop(),
				domain2.getTop());
	}

	/**
	 * Returns a pair of bottom elements, each from its corresponding domain.
	 */
	@Override
	public Pair<StateType1, StateType2> getBottom() {
		return bottom;
	}

	/**
	 * Returns a pair of top elements, each from its corresponding domain.
	 */
	@Override
	public Pair<StateType1, StateType2> getTop() {
		return top;
	}

	/**
	 * Performs a component-wise join.
	 */
	@Override
	public Pair<StateType1, StateType2> ub(Pair<StateType1, StateType2> elem1,
			Pair<StateType1, StateType2> elem2) {
		StateType1 join1 = domain1.ub(elem1.first, elem2.first);
		StateType2 join2 = domain2.ub(elem1.second, elem2.second);
		Pair<StateType1, StateType2> result = new Pair<>(join1, join2);
		return result;
	}

	@Override
	public boolean leq(Pair<StateType1, StateType2> elem1,
			Pair<StateType1, StateType2> elem2) {
		return domain1.leq(elem1.first, elem2.first)
				&& domain2.leq(elem1.second, elem2.second);
	}

	@Override
	public UnaryOperation<Pair<StateType1, StateType2>> getTransformer(
			ActionType stmt) {
		UnaryOperation<StateType1> transformer1 = domain1.getTransformer(stmt);
		UnaryOperation<StateType2> transformer2 = domain2.getTransformer(stmt);
		if (transformer1 == IdOperation.v() && transformer2 == IdOperation.v()) {
			// An optimization when the two transformers are identity.
			return IdOperation.v();
		} else {
			UnaryOperation<Pair<StateType1, StateType2>> componentWiseTransformer = new CartesianTransformer<>(
					transformer1, transformer2);
			return ComposedOperation.compose(componentWiseTransformer,
					getReductionOperation());
		}
	}

	/**
	 * A transformer that operates by applying a sub-transformer to each
	 * component separately.
	 * 
	 * @author romanm
	 * 
	 * @param <StateType1>
	 *            An abstract domain element type.
	 * @param <StateType2>
	 *            An abstract domain element type.
	 */
	public static class CartesianTransformer<StateType1, StateType2> extends
			UnaryOperation<Pair<StateType1, StateType2>> {
		private final UnaryOperation<StateType1> transformer1;
		private final UnaryOperation<StateType2> transformer2;

		public CartesianTransformer(UnaryOperation<StateType1> transformer1,
				UnaryOperation<StateType2> transformer2) {
			this.transformer1 = transformer1;
			this.transformer2 = transformer2;
		}

		@Override
		public Pair<StateType1, StateType2> apply(
				Pair<StateType1, StateType2> input) {
			StateType1 output1 = transformer1.apply(input.first);
			StateType2 output2 = transformer2.apply(input.second);
			Pair<StateType1, StateType2> result = new Pair<>(output1, output2);
			return result;
		}

		@Override
		public String toString() {
			return "[" + transformer1.toString() + ","
					+ transformer2.toString() + "]";
		}
	}
}