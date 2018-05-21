package bgu.cs.absint;

/**
 * The base class of transformers for assume statements.
 * 
 * @author romanm
 * 
 * @param <StateType>
 *            The abstract element type.
 */
public abstract class AssumeTransformer<StateType> extends UnaryOperation<StateType> {
	public final boolean polarity;

	public AssumeTransformer(boolean polarity) {
		this.polarity = polarity;
	}

	@Override
	public String toString() {
		String negStr = polarity ? "" : "!";
		return negStr + getClass().getSimpleName();
	}
}