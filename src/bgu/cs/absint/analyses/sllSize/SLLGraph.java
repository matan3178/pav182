package bgu.cs.absint.analyses.sllSize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import bgu.cs.absint.analyses.sll.AbsLen;
import bgu.cs.absint.analyses.zone.ZoneFactoid;
import bgu.cs.absint.analyses.zone.ZoneState;
import bgu.cs.absint.soot.LocalComparator;
import bgu.cs.util.StringUtils;
import soot.IntType;
import soot.Local;
import soot.jimple.IntConstant;
import soot.jimple.internal.JimpleLocal;

/**
 * An abstract element representing the abstraction of a bounded number of
 * interacting singly-linked lists (bounded by the number of local
 * variables).<br>
 * Each list segment is associated with a numeric variable that represents its
 * size and the numeric relations between these variables is represented by an
 * element of the Zone domain.
 * 
 * @author romanm
 */
public class SLLGraph {
	public final Node nullNode = new Node(null, null);

	protected Collection<Node> nodes = new ArrayList<Node>();
	protected Map<Local, Node> pointsTo = new TreeMap<Local, Node>(new LocalComparator());

	/**
	 * Maintains numeric relations between all list segments.
	 */
	protected ZoneState sizes;

	public SLLGraph() {
		nodes.add(nullNode);
	}

	
	
	public SLLGraph dropLocals(Collection<Local> locals) {
		SLLGraph simpler = this.copy();
		for (Local local : locals) {
			simpler.pointsTo.remove(local);
		}
		return simpler;
	}

	
	/**
	 * Creates an isomorphic shape graph.
	 * 
	 * @return A shape graph that is isomorphic to this one.
	 */
	public SLLGraph copy() {
		SLLGraph result = new SLLGraph();
		// Matches a node in this graph to a node in the result graph.
		Map<Node, Node> matching = new HashMap<>(nodes.size());
		for (Node node : nodes) {
			if (node == nullNode)
				continue;
			Node newNode = new Node();
			newNode.pointedBy = new HashSet<>(node.pointedBy);
			matching.put(node, newNode);
			result.addNode(newNode);
		}
		// Match the null node separately.
		matching.put(nullNode, result.nullNode);

		// Now update the next pointers.
		for (Map.Entry<Node, Node> entry : matching.entrySet()) {
			Node thisNode = entry.getKey();
			Node otherNode = entry.getValue();
			if (thisNode.next != null) {
				otherNode.next = matching.get(thisNode.next);
			}
			otherNode.edgeLen = thisNode.edgeLen;
		}

		// Finally, update the pointsTo map.
		for (Map.Entry<Local, Node> entry : pointsTo.entrySet()) {
			Local var = entry.getKey();
			Node node = entry.getValue();
			Node otherNode = matching.get(node);
			result.pointsTo.put(var, otherNode);
		}
		result.sizes = this.sizes.copy();
		return result;
	}

	public Collection<Node> getNodes() {
		return nodes;
	}

	public Set<Node> getPreds(Node n) {
		HashSet<Node> result = new HashSet<>();
		for (Node p : getNodes()) {
			if (p.next == n)
				result.add(p);
		}
		return result;
	}

	public Node pointsTo(Local v) {
		if(!pointsTo.containsKey(v))
			return nullNode;
		return pointsTo.get(v);
	}

	public void addNode(Node n) {
		assert !nodes.contains(n);
		if (n.next != null)
			assert nodes.contains(n.next) : "Attempt to add a node where the next node is not part of the same graph!";
		nodes.add(n);
	}

	public void removeNode(Node n) {
		assert n != nullNode;
		nodes.remove(n);
	}

	public void mapLocal(Local v, Node n) {
		assert nodes.contains(n);
		unmapLocal(v);
		n.addLocal(v);
		pointsTo.put(v, n);
	}

	public void unmapLocal(Local v) {
		Node n = pointsTo(v);
		if (n != null) {
			n.removeLocal(v);
			pointsTo.remove(v);
		}
	}

	public void removeGarbageNodes() {
		HashSet<Node> reachable = new HashSet<>(pointsTo.values());
		HashSet<Node> workset = new HashSet<>(pointsTo.values());
		while (!workset.isEmpty()) {
			Node n = workset.iterator().next();
			workset.remove(n);
			Node next = n.next;
			if (next != null && !reachable.contains(next)) {
				reachable.add(next);
				workset.add(next);
			}
		}
		nodes.retainAll(reachable);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + nodes.size();
		for (Node n : pointsTo.values()) {
			result = prime * result + n.pointedBy.hashCode();
		}
		return result;
	}

	/**
	 * Checks whether this graph is isomorphic to the given one.
	 */
	@Override
	public boolean equals(Object o) {
		SLLGraph other = (SLLGraph) o;
		this.normalize();
		other.normalize();
		if (nodes.size() != other.nodes.size())
			return false;
		
		// Attempt to create an initial matching of nodes based on
		// the pointed-by sets.
		Map<Node, Node> matching = new HashMap<>();
		for (Map.Entry<Local, Node> entry : pointsTo.entrySet()) {
			Local v = entry.getKey();
			Node n = entry.getValue();
			assert n != null;
			Node otherNode = other.pointsTo(v);
			if (otherNode == null)
				return false;
			if (!n.pointedBy.equals(otherNode.pointedBy))
				return false;
			matching.put(n, otherNode);
		}

		// Complete the matching based on the 'next' relation.
		HashSet<Node> workset = new HashSet<>();
		workset.addAll(matching.keySet());
		while (!workset.isEmpty()) {
			Node nodeToCheck = workset.iterator().next();
			workset.remove(nodeToCheck);
			Node matchedNodeToCheck = matching.get(nodeToCheck);
			assert matchedNodeToCheck != null;
			Node nextNode = nodeToCheck.next;
			if (nextNode != null) {
				matching.put(nextNode, matchedNodeToCheck.next);
				if (!matching.containsKey(nextNode)) {
					workset.add(nextNode);
				}
			}
		}

		// Finally, check that the matching preserves the 'next' relation.
		for (Map.Entry<Node, Node> entry : matching.entrySet()) {
			Node thisNode = entry.getKey();
			Node otherNode = entry.getValue();
			//TODO: 
			if (thisNode.edgeLen != otherNode.edgeLen)
				return false;
			if (thisNode.next == null) {
				if (otherNode.next != null)
					return false;
			} else {
				Node matchedNextNode = matching.get(thisNode.next);
				if (matchedNextNode == null)
					return false;
				if (matchedNextNode != otherNode.next)
					return false;
			}
		}

		return true;
	}
	
	@Override
	public String toString() {
		ArrayList<String> substrings = new ArrayList<>();
		Map<Node, String> nodeToName = new HashMap<>();
		int i = 0;
		for (Node n : nodes) {
			if (n == nullNode) // Name the null node separately.
				continue;
			nodeToName.put(n, "n" + i);
			++i;
		}
		nodeToName.put(nullNode, "null");

		for (Map.Entry<Local, Node> entry : pointsTo.entrySet()) {
			Local v = entry.getKey();
			Node n = entry.getValue();
			substrings.add(v + "=" + nodeToName.get(n));
		}
		for (Node n : nodes) {
			if (n == nullNode) // Skip null node.
				continue;
			assert n.next != null;
			String nextNodeName = nodeToName.get(n.next);
			int len = getConstant(this, n.edgeLen, ZoneFactoid.ZERO_VAR).value;
			String edgeLenStr = len == 1 ?
					".next=" 
					: "~["+len+"]~>";
			substrings.add(nodeToName.get(n) + edgeLenStr + nextNodeName);
		}
		StringBuilder result = new StringBuilder("graph = {");
		result.append(StringUtils.toString(substrings));
		result.append("}");
		return result.toString();
	}
	
	
	
	
	
	
	static ArrayList<Local> vars;
	

	public static Local nextLocal(int index){
		
		if(vars==null)
		{
			vars = new ArrayList<Local>();
		}
		int counter = vars.size();
		while(vars.size()<=index)
		{
			vars.add(new JimpleLocal("var"+counter,IntType.v()));
			counter++;
		}
		return vars.get(index);
		
	}
	HashMap<Integer,Node> mapper = new HashMap<Integer,Node>(); 
	public void normalize()
	{
//		SLLGraph res = new SLLGraph();
		
		Iterator<Node> iter = this.nodes.iterator();
		int numOfNodes =this.nodes.size();
//		if(numOfNodes>1)
//			System.out.println("");
		for(int i=0;i<numOfNodes;i++){		
			mapper.put(i,iter.next());
		}
		DFS(0,numOfNodes);
		
	}
	
	public int revMap(Node n)
	{
		for(Entry<Integer, Node> a : mapper.entrySet())
		{
			if(a.getValue().equals(n))
				return a.getKey();
		}
		return -1;
	}
	int DFSUtil(int v,boolean visited[])
    {
        // Mark the current node as visited and print it
		if(v>=visited.length)
			return v;
        visited[v] = true;
        
//        System.out.println("Visited " + v);
//        Node n = this.mapper.get(v).next;
        Node n = this.mapper.get(v);
        if(n != nullNode)
        {
	        
	        Local oldLen = n.edgeLen;
	        IntConstant intConst = getConstant(this, oldLen, ZoneFactoid.ZERO_VAR);
	        this.sizes.removeVar(oldLen);
	        n.edgeLen=nextLocal(v);
	        v++;
	        this.sizes.addFactoid(n.edgeLen, ZoneFactoid.ZERO_VAR, intConst);	        
//	        v = revMap(n.next);
	        
	        if (v<visited.length && !visited[v])
	            v = DFSUtil(v, visited);
        }
        else v++;
        return v;
    }
	private IntConstant getConstant(SLLGraph graph, Local a, Local b){
		for(ZoneFactoid zf: graph.sizes.getFactoids()){
			if(zf.lhs==a && zf.rhs == b)
				return zf.bound;
		}
		return IntConstant.v(1);
	}

 
    // The function to do DFS traversal. It uses recursive DFSUtil()
    void DFS(int v,int size)
    {
        // Mark all the vertices as not visited(set as
        // false by default in java)
        boolean visited[] = new boolean[size];
 
        for(boolean b:visited){
        	if(!b)
        		v = DFSUtil(v, visited);
        }
        // Call the recursive helper function to print DFS traversal
        
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}