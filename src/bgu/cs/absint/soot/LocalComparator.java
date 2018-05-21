package bgu.cs.absint.soot;

import java.util.Comparator;

import soot.Local;

/**
 * A comparator for Soot locals, based on their internal number.
 * 
 * @author romanm
 */
public class LocalComparator implements Comparator<Local> {
	@Override
	public int compare(Local o1, Local o2) {
		return o2.getNumber() - o1.getNumber();
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}