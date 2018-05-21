package bgu.cs.absint.constructor;

import bgu.cs.util.StringUtils;
import bgu.cs.util.Tuple;

/**
 * An abstract state that is a tuple of abstract states from corresponding
 * abstract domains.
 * 
 * @author romanm
 */
public class ProductState extends Tuple implements Cloneable {
	public ProductState(Object... components) {
		super(components);
	}

	/**
	 * Returns a shallow copy of this tuple. That is, the individual components
	 * are the same.
	 */
	@Override
	public ProductState clone() {
		Object[] newComponents = new Object[components.length];
		for (int i = 0; i < newComponents.length; ++i) {
			newComponents[i] = components[i];
		}
		ProductState result = new ProductState(newComponents);
		return result;
	}

	@Override
	public String toString() {
		return "and(" + StringUtils.toString(components) + ")";
	}
}