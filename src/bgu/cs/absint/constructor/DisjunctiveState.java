package bgu.cs.absint.constructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import bgu.cs.util.StringUtils;

/**
 * A state representing a disjunction of elements from the base domain.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The type of elements from the initial domain.
 */
public class DisjunctiveState<StateType> implements Iterable<StateType> {
	protected final Set<StateType> disjuncts = new HashSet<>();

	public DisjunctiveState() {
	}

	public DisjunctiveState(StateType disjunct) {
		this.disjuncts.add(disjunct);
	}

	@SafeVarargs
	public DisjunctiveState(StateType... disjuncts) {
		for (StateType disjunct : disjuncts)
			this.disjuncts.add(disjunct);
	}

	public DisjunctiveState(Collection<StateType> disjuncts) {
		this.disjuncts.addAll(disjuncts);
	}

	public DisjunctiveState(Collection<StateType> disjuncts1,
			Collection<StateType> disjuncts2) {
		this.disjuncts.addAll(disjuncts1);
		this.disjuncts.addAll(disjuncts2);
	}

	public int size() {
		return disjuncts.size();
	}

	@Override
	public Iterator<StateType> iterator() {
		return disjuncts.iterator();
	}

	public Set<StateType> getDisjuncts() {
		return disjuncts;
	}

	@Override
	public String toString() {
		if (disjuncts.isEmpty()) {
			return "false";
		} else if (disjuncts.size() == 1) {
			return disjuncts.iterator().next().toString();
		} else {

			StringBuilder result = new StringBuilder("or(");
			result.append(StringUtils.toString(disjuncts));
			result.append(")");
			return result.toString();
		}
	}
}