package bgu.cs.absint.soot;

import soot.AbstractUnit;
import soot.UnitPrinter;
import soot.jimple.IfStmt;

/**
 * A fake unit for handling conditions by a pair of assume statements.
 * 
 * @author romanm
 */
@SuppressWarnings("serial")
public class Assume extends AbstractUnit {
	public final IfStmt stmt;
	public final boolean polarity;

	public Assume(IfStmt stmt, boolean branch) {
		this.stmt = stmt;
		this.polarity = branch;
	}

	@Override
	public boolean branches() {
		return true;
	}

	@Override
	public boolean fallsThrough() {
		return false;
	}

	@Override
	public void toString(UnitPrinter printer) {
		printer.literal("assume" + (polarity ? "" : "!"));
		printer.startUnit(stmt);
	}

	@Override
	public Object clone() {
		return new Assume(stmt, polarity);
	}
}