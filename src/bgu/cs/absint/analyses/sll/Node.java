package bgu.cs.absint.analyses.sll;

import java.util.HashSet;
import java.util.Set;

import soot.Local;

/**
 * A node in an {@link SLLGraph}.
 * 
 * @author romanm
 */
public class Node {
	protected Node next;
	protected AbsLen edgeLen;

	protected Set<Local> pointedBy = new HashSet<>();

	public Node() {		
	}

	public Node(Node next, AbsLen outEdgeLen) {
		this.next = next;
		this.edgeLen = outEdgeLen;
	}

	public Node copy() {
		Node result = new Node(this.next, this.edgeLen);
		result.pointedBy = new HashSet<>(this.pointedBy);
		return result;
	}

	public boolean addLocal(Local var) {
		return pointedBy.add(var);
	}

	public void removeLocal(Local var) {
		pointedBy.remove(var);
	}
}