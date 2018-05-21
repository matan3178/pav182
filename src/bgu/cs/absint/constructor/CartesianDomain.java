package bgu.cs.absint.constructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import bgu.cs.absint.ComposedOperation;
import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.util.Pair;
import bgu.cs.util.StringUtils;

/**
 * A generic Cartesian combination of at least two of abstract domains.
 * 
 * @author romanm
 */
public class CartesianDomain<ActionType, VarType> extends
		AbstractDomain<ProductState, ActionType> {
	private UnaryOperation<ProductState> cachedReductionByEqualityOperation = null;

	/**
	 * The implementations of abstract operations of each of the component
	 * abstract domain.
	 */
	protected final AbstractDomain<?, ActionType>[] domains;

	/**
	 * The number of components in the product.
	 */
	protected final int size;

	/**
	 * The least element of the Cartesian domain.
	 */
	protected final ProductState bottom;

	/**
	 * The top element of the Cartesian domain.
	 */
	protected final ProductState top;

	@SafeVarargs
	public CartesianDomain(AbstractDomain<?, ActionType>... domains) {
		assert domains != null && domains.length > 1;
		this.domains = domains;
		this.size = domains.length;

		Object[] bottomsArray = new Object[size];
		for (int i = 0; i < size; ++i) {
			bottomsArray[i] = domains[i].getBottom();
		}
		bottom = new ProductState(bottomsArray) {
			@Override
			public String toString() {
				return "false";
			}

			@Override
			public void set(int i, Object o) {
				throw new Error("Attempt to modify "
						+ getClass().getSimpleName() + "_bottom");
			}
		};

		Object[] topsArray = new Object[size];
		for (int i = 0; i < size; ++i) {
			topsArray[i] = domains[i].getTop();
		}
		top = new ProductState(topsArray) {
			@Override
			public String toString() {
				return "true";
			}

			@Override
			public void set(int i, Object o) {
				throw new Error("Attempt to modify "
						+ getClass().getSimpleName() + "_top");
			}
		};
	}

	/**
	 * Returns a ProductState of bottom elements, each from its corresponding
	 * domain.
	 */
	@Override
	public ProductState getBottom() {
		return bottom;
	}

	/**
	 * Returns a ProductState of top elements, each from its corresponding
	 * domain.
	 */
	@Override
	public ProductState getTop() {
		return top;
	}

	/**
	 * Computes the upper-bound on a point-wise basis.
	 */
	@Override
	public ProductState ub(ProductState elem1, ProductState elem2) {
		Object[] answerArray = new Object[size];
		for (int i = 0; i < size; ++i) {
			answerArray[i] = domains[i].unsafeUB(elem1.get(i), elem2.get(i));
		}
		ProductState result = new ProductState(answerArray);
		return result;
	}

	/**
	 * Computes the lower-bound on a point-wise basis.
	 */
	@Override
	public ProductState lb(ProductState elem1, ProductState elem2) {
		Object[] answerArray = new Object[size];
		for (int i = 0; i < size; ++i) {
			answerArray[i] = domains[i].unsafeLB(elem1.get(i), elem2.get(i));
		}
		ProductState result = new ProductState(answerArray);
		return result;
	}

	/**
	 * Computes the widening on a point-wise basis.
	 */
	@Override
	public ProductState widen(ProductState elem1, ProductState elem2) {
		Object[] answerArray = new Object[size];
		for (int i = 0; i < size; ++i) {
			answerArray[i] = domains[i].unsafeWiden(elem1.get(i), elem2.get(i));
		}
		ProductState result = new ProductState(answerArray);
		return result;
	}

	/**
	 * Computes the narrowing on a point-wise basis.
	 */
	@Override
	public ProductState narrow(ProductState elem1, ProductState elem2) {
		Object[] answerArray = new Object[size];
		for (int i = 0; i < size; ++i) {
			answerArray[i] = domains[i]
					.unsafeNarrow(elem1.get(i), elem2.get(i));
		}
		ProductState result = new ProductState(answerArray);
		return result;
	}

	/**
	 * Checks whether the order relation holds for each component.
	 */
	@Override
	public boolean leq(ProductState elem1, ProductState elem2) {
		for (int i = 0; i < size; ++i) {
			if (!domains[i].unsafeLeq(elem1.get(i), elem2.get(i)))
				return false;
		}
		return true;
	}

	@Override
	public UnaryOperation<ProductState> getTransformer(ActionType action) {
		boolean allIdTransformers = true;
		UnaryOperation<?>[] transformers = new UnaryOperation<?>[size];
		for (int i = 0; i < size; ++i) {
			transformers[i] = domains[i].getTransformer(action);
			allIdTransformers &= transformers[i] == IdOperation.v();
		}

		if (allIdTransformers) {
			// An optimization when all transformers are identity.
			return IdOperation.v();
		} else {
			UnaryOperation<ProductState> componentWiseTransformer = new MultiCartTransformer(
					transformers);
			return ComposedOperation.compose(componentWiseTransformer,
					getReductionOperation());
		}
	}

	@Override
	public UnaryOperation<ProductState> getReductionOperation() {
		if (cachedReductionByEqualityOperation == null)
			cachedReductionByEqualityOperation = new UnaryOperation<ProductState>() {
				@Override
				public ProductState apply(ProductState input) {
					assert input != null && arity() == 1;
					return reduceViaEqualities(input);
				}

				@Override
				public String toString() {
					return "Reduce_" + getClass().getSimpleName();
				}
			};
		return cachedReductionByEqualityOperation;
	}

	/**
	 * Uses the equalities inferred from each sub-domain to refine the others,
	 * until no more refinement it possible.
	 * 
	 * @param input
	 *            A ProductState of sub-states corresponding to domains.
	 * @return The refined ProductState.
	 */
	protected ProductState reduceViaEqualities(ProductState input) {
		ProductState result = input.clone();

		List<Collection<Pair<VarType, VarType>>> equalitySets = new ArrayList<>(
				size);
		boolean change = true;
		while (change) {
			change = false;
			equalitySets.clear();
			for (int i = 0; i < size; ++i) {
				Object subState = result.get(i);
				if (subState == domains[i].getBottom())
					return bottom;

				Collection<Pair<VarType, VarType>> equalities;
				if (domains[i] instanceof EqualityRefiner) {
					@SuppressWarnings("unchecked")
					EqualityRefiner<VarType> refiner = (EqualityRefiner<VarType>) domains[i];
					equalities = refiner.inferEqualities(subState);
				} else {
					equalities = Collections.emptyList();
				}
				equalitySets.add(i, equalities);
			}

			// Now refine each sub-state using the equality sets inferred from
			// the other sub-states.
			for (int i = 0; i < size; ++i) {
				Object subState = result.get(i);
				for (int j = 0; j < size; ++j) {
					if (i == j)
						continue;
					Collection<Pair<VarType, VarType>> otherEqualities = equalitySets
							.get(j);
					Object refinedSubstate = null;
					if (domains[i] instanceof EqualityRefiner) {
						@SuppressWarnings("unchecked")
						EqualityRefiner<VarType> refiner = (EqualityRefiner<VarType>) domains[i];
						refinedSubstate = refiner.refineByEqualities(subState,
								otherEqualities);
					}
					// The sub-state has decreased due to the refinement.
					if (refinedSubstate != null
							&& domains[i].unsafeLt(refinedSubstate, subState)) {
						change = true;
						refinedSubstate = domains[i]
								.unsafeReduce(refinedSubstate);
						// If any of the components is bottom, we reduce the
						// final result to bottom.
						if (refinedSubstate.equals(domains[i].getBottom()))
							return getBottom();
						result.set(i, refinedSubstate);
					}
				}
			}
		}

		return result;
	}

	/**
	 * A transformer that operates by applying a sub-transformer to each
	 * component separately.
	 * 
	 * @author romanm
	 */
	public static class MultiCartTransformer extends
			UnaryOperation<ProductState> {
		private final UnaryOperation<?>[] transformers;

		public MultiCartTransformer(UnaryOperation<?>[] transformers) {
			this.transformers = transformers;
		}

		@Override
		public ProductState apply(ProductState input) {
			Object[] outArray = new Object[transformers.length];
			for (int i = 0; i < transformers.length; ++i) {
				outArray[i] = transformers[i].unsafeApply(input.get(i));
			}
			ProductState result = new ProductState(outArray);
			return result;
		}

		@Override
		public String toString() {
			return "[" + StringUtils.toString(transformers) + "]";
		}
	}
}