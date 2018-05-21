package bgu.cs.absint.constructor;

import java.lang.reflect.Field;
import java.util.Set;

import soot.Local;

/**
 * A basic fact over program variables.
 * 
 * @author romanm
 *
 * @param <VarType>
 *            The implementation type of program variables.
 */
public abstract class Factoid<VarType> {
	/**
	 * Checks whether two objects refer to the same program variables.
	 */
	public abstract boolean equalVars(VarType v1, VarType v2);

	/**
	 * Determines whether the fact contains the given variable. The default
	 * implementation uses reflection to find variables in the fields of the
	 * class implementing the factoid (the class extending this one).
	 * 
	 * @param var
	 *            A program variable.
	 * @return true if the fact contains the given program variable.
	 */
	public boolean hasVar(VarType var) {
		for (Field field : Factoid.class.getFields()) {
			if (field.getType().equals(Local.class)) {
				try {
					@SuppressWarnings("unchecked")
					VarType varInField = (VarType) field.get(this);
					if (equalVars(var, varInField))
						return true;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 * Adds all local variables that appear in this factoid to the given
	 * collection. The default implementation uses reflection to find local
	 * variables in the fields of the class implementing the factoid (the class
	 * extending this one).
	 * 
	 * @param c
	 *            A collection of local variables.
	 */
	public void addVarsTo(Set<Local> c) {
		for (Field field : Factoid.class.getFields()) {
			if (field.getType().equals(Local.class)) {
				try {
					Local varInField = (Local) field.get(this);
					c.add(varInField);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Approximates the implication order between this factoid and another.
	 * Override this method to supply a more precise test.
	 * 
	 * @param other
	 *            A factoid.
	 * @return The default implementation is the most conservative answer: if
	 *         this object is equal to other then true else false.
	 */
	public boolean leq(Factoid<VarType> other) {
		return equals(other);
	}

	/**
	 * Determines whether one factoid is strictly lower than another with
	 * respect to the order relation defined for the corresponding semantic.
	 * 
	 * @param other
	 *            A factoid.
	 * @return true if this factoid is strictly lower than the given factoid
	 *         relative to the order relation defined for the corresponding
	 *         semantic domain.
	 */
	public boolean lt(Factoid<VarType> other) {
		boolean leq = this.leq(other);
		boolean geq = other.leq(this);
		return leq && !geq;
	}

	/**
	 * Determines whether one factoid is greater or equal to another with
	 * respect to the order relation defined for the corresponding semantic.
	 * 
	 * @param other
	 *            A factoid.
	 * @return true if this factoid is greater or equal to the given factoid
	 *         relative to the order relation defined for the corresponding
	 *         semantic domain.
	 */
	public boolean geq(Factoid<VarType> other) {
		return other.leq(this);
	}

	/**
	 * Determines whether one factoid is strictly greater than another with
	 * respect to the order relation defined for the corresponding semantic.
	 * 
	 * @param other
	 *            A factoid.
	 * @return true if this factoid is strictly greater than the given factoid
	 *         relative to the order relation defined for the corresponding
	 *         semantic domain.
	 */
	public boolean gt(Factoid<VarType> other) {
		return other.lt(this);
	}

	/**
	 * Determines whether two elements are equivalent with respect to the order
	 * relation defined for the corresponding semantic. That is, 'x' is less
	 * than or equal to 'y' and vice verse.
	 * 
	 * @param other
	 *            A factoid.
	 * @return true if this factoid is greater or equal to the given factoid and
	 *         vice verse relative to the order relation defined for the
	 *         corresponding semantic domain.
	 */
	public boolean eq(Factoid<VarType> other) {
		if (this == other)
			return true;
		boolean leq = this.leq(other);
		boolean geq = other.leq(this);
		return leq && geq;
	}

	/**
	 * Checks whether two elements are comparable with respect to the order
	 * relation defined for the corresponding semantic.
	 * 
	 * @param other
	 *            An abstract domain element.
	 * @return true if one element is less than or equal to the other.
	 */
	public boolean comparable(Factoid<VarType> other) {
		return this.leq(other) || other.leq(this);
	}
}