package bgu.cs.absint.solver;

import java.util.ArrayList;
import java.util.List;

import bgu.cs.absint.Operation;

/**
 * A list of operations of the same type (same arity) with a cursor marking the
 * operation that will be used on a call to apply.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The implementation type of abstract states.
 */
public class PhasedOperation<StateType> extends Operation<StateType> {
	protected List<Operation<StateType>> phases;
	protected int currentPhase = 0;

	public PhasedOperation(List<Operation<StateType>> phases) {
		assert phases != null;
		assert !phases.isEmpty();
		int arity = phases.get(0).arity();
		for (Operation<StateType> phase : phases) {
			assert phase.arity() == arity;
		}

		this.phases = phases;
	}

	public PhasedOperation(Operation<StateType> phase1,
			Operation<StateType> phase2) {
		assert phase1 != null && phase2 != null;
		int arity = phase1.arity();
		assert phase2.arity() == arity;
		this.phases = new ArrayList<>(2);
		this.phases.add(phase1);
		this.phases.add(phase2);
	}

	/**
	 * Move the cursor to the next operation in line.
	 */
	public void advance() {
		assert currentPhase < phases.size() : "advance called on last phase!";
		++currentPhase;
	}

	/**
	 * Set the cursor to the operation in the corresponding position.
	 * 
	 * @param index
	 *            The index of an operation in the list of operations.
	 */
	public void setPhase(int index) {
		assert index >= 0 && index < phases.size() : "Illegal phase index!";
		this.currentPhase = index;
	}

	@Override
	public byte arity() {
		return phases.get(0).arity();
	}

	@Override
	public StateType apply(List<StateType> inputs) {
		return phases.get(currentPhase).apply(inputs);
	}

	@Override
	public StateType apply() {
		return phases.get(currentPhase).apply();
	}

	@Override
	public StateType apply(StateType input) {
		return phases.get(currentPhase).apply(input);
	}

	@Override
	public StateType apply(StateType input1, StateType input2) {
		return phases.get(currentPhase).apply(input1, input2);
	}

	@Override
	public StateType apply(StateType[] inputs) {
		return phases.get(currentPhase).apply(inputs);
	}
}