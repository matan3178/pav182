package bgu.cs.absint.analyses.sllSize;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.AssumeTransformer;
import bgu.cs.absint.BinaryOperation;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.constructor.DisjunctiveState;
import bgu.cs.absint.soot.TransformerMatcher;
import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.Type;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

/**
 * An abstract domain for the intraprocedural shape analysis of singly-linked
 * lists.
 * 
 * @author romanm
 */
public class SLLDomain extends AbstractDomain<DisjunctiveState<SLLGraph>, Unit> {
	/**
	 * Singleton value.
	 */
	private static final SLLDomain v = new SLLDomain();

	public static final SLLDomain v() {
		return v;
	}

	protected DisjunctiveState<SLLGraph> bottom;
	protected DisjunctiveState<SLLGraph> top;

	/**
	 * The set of local variables in the current method body.
	 */
	protected Set<Local> locals;

	/**
	 * A helper object that matches statements to transformers.
	 */
	protected SLLMatcher matcher = new SLLMatcher();

	/**
	 * The name of the list class.
	 */
	private String listClassName;

	/**
	 * The name of the next field of the list class.
	 */
	private String listClassField;

	public void setBodyLocals(Collection<Local> locals) {
		this.locals = new LinkedHashSet<>(locals);
	}

	public void setListClass(String listClassName, String listClassField) {
		this.listClassName = listClassName;
		this.listClassField = listClassField;
	}

	@Override
	public DisjunctiveState<SLLGraph> getBottom() {
		return bottom;
	}

	@Override
	public DisjunctiveState<SLLGraph> getTop() {
		return top;
	}

	/**
	 * Returns the set union of both input sets.
	 */
	@Override
	public DisjunctiveState<SLLGraph> ub(DisjunctiveState<SLLGraph> elem1,
			DisjunctiveState<SLLGraph> elem2) {
		// Special treatment for top.
		if (elem1 == getTop() || elem2 == getTop())
			return getTop();
		
		DisjunctiveState<SLLGraph> result = new DisjunctiveState<SLLGraph>(
				elem1.getDisjuncts(), elem2.getDisjuncts());
		return result;
	}

	/**
	 * Applies {@code generalize} to the shape graphs in both input sets.
	 */
	@Override
	public DisjunctiveState<SLLGraph> ubLoop(DisjunctiveState<SLLGraph> elem1,
			DisjunctiveState<SLLGraph> elem2) {
		// Special treatment for top.
		if (elem1 == getTop() || elem2 == getTop())
			return getTop();

		HashSet<SLLGraph> disjuncts = new HashSet<>();
		for (SLLGraph graph : elem1) {
			SLLGraph disjunct = generalize(graph);
			disjuncts.add(disjunct);
		}
		for (SLLGraph graph : elem2) {
			SLLGraph disjunct = generalize(graph);
			disjuncts.add(disjunct);
		}

		DisjunctiveState<SLLGraph> result = new DisjunctiveState<SLLGraph>(
				disjuncts);
		return result;
	}

	@Override
	public boolean leq(DisjunctiveState<SLLGraph> first,
			DisjunctiveState<SLLGraph> second) {
		// Special treatment for top.
		if (second == getTop())
			return true;
		else if (first == getTop())
			return false;
		else
			return second.getDisjuncts().containsAll(first.getDisjuncts());
	}

	@Override
	public UnaryOperation<DisjunctiveState<SLLGraph>> getTransformer(Unit stmt) {
		return matcher.getTransformer(stmt);
	}

	// ////////////////////////////////////////////////////////////////////////////
	// Utility methods for singly-linked list shape graphs.
	// ////////////////////////////////////////////////////////////////////////////

	/**
	 * Checks whether a given local variable has the type specified as the list
	 * class type.
	 */
	public boolean isListRefType(Local var) {
		Type varType = var.getType();
		if (varType instanceof RefType) {
			RefType refType = (RefType) varType;
			if (refType.getClassName().equals(listClassName))
				return true;
			else
				return false;
		} else {
			return false;
		}
	}

	/**
	 * Creates a shape graph where all list variables point to null.
	 */
	public SLLGraph makeAllNullsGraph() {
		SLLGraph allNullsGraph = new SLLGraph();
		for (Local var : locals) {
			if (isListRefType(var))
				allNullsGraph.mapLocal(var, allNullsGraph.nullNode);
		}
		return allNullsGraph;
	}

	/**
	 * Creates a state containing a shape graph where all list variables point
	 * to null.
	 */
	public DisjunctiveState<SLLGraph> initNulls() {
		DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
				makeAllNullsGraph());
		return result;
	}

	/**
	 * Creates state containing two shape graphs where all list variables point
	 * to null, except a given list variable, which points to an acyclic list of
	 * size >= 0.
	 */
	public DisjunctiveState<SLLGraph> initAcyclic(Local x) {
		SLLGraph graph1 = makeAllNullsGraph();
		Node ptXOne = new Node(graph1.nullNode, 1);
		graph1.addNode(ptXOne);
		graph1.mapLocal(x, ptXOne);

		SLLGraph graph2 = makeAllNullsGraph();
		Node ptXGt1 = new Node(graph2.nullNode, 2); //SUPER TODO
		graph2.addNode(ptXGt1);
		graph2.mapLocal(x, ptXGt1);

		DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
				makeAllNullsGraph(), graph1, graph2);
		return result;
	}

	/**
	 * Materializes the next node of 'var' by replacing the outgoing edge with
	 * two edges connected by a new node. The first edge has length 1 and the
	 * second also has length 1. This represents the case where the list
	 * outgoing from 'var' has length exactly 2.
	 */
	public SLLGraph focusOne(SLLGraph graph, Local var) {
		//TODO
		SLLGraph result = graph.copy();
		result.pointsTo(var).edgeLen = 1;
		return result;
	}

	/**
	 * Materializes the next node of 'var' by replacing the outgoing edge with
	 * two edges connected by a new node. The first edge has length 1 and the
	 * second has length >1. This represents the case where the list outgoing
	 * from 'var' has length >2.
	 */
	public SLLGraph focusGtOne(SLLGraph graph, Local var) {
		SLLGraph result = graph.copy();
		Node rhsNode = result.pointsTo(var);
		Node rhsNextNode = rhsNode.next;
		
		int currentLen = rhsNode.edgeLen; // the current length of the node
		
		Node newNextNode = new Node(rhsNextNode, currentLen - 1);
		result.addNode(newNextNode);
		rhsNode.next = newNextNode;
		rhsNode.edgeLen = 1;
		return result;
	}

	/**
	 * Replaces non-maximal (uninterrupted) list segments with a single maximal
	 * list segment of length >1 and then removes garbage nodes.
	 */
	public SLLGraph generalize(SLLGraph graph) {
		SLLGraph result = graph;

		boolean change = true;
		while (change) {
			change = false;
			result = result.copy();
			for (Node n : result.nodes) {
				if (n == result.nullNode)
					continue;
				// Self-loops are a special case.
				if (n.next == n)
					continue;
				if (n.next == result.nullNode)
					continue;
				boolean isNextInterruption = !n.next.pointedBy.isEmpty()
						|| result.getPreds(n.next).size() > 1;
				if (!isNextInterruption) {
					change = true;
					n.next = n.next.next;
					n.edgeLen = n.edgeLen + n.next.edgeLen; // update the length of the edge
				}
			}
		}

		result.removeGarbageNodes();
		return result;
	}

	/**
	 * Singleton pattern.
	 */
	private SLLDomain() {
		top = new DisjunctiveState<SLLGraph>() {
			@Override
			public int size() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Iterator<SLLGraph> iterator() {
				return new Iterator<SLLGraph>() {
					
					@Override
					public SLLGraph next() {
						return null;
					}
					
					@Override
					public boolean hasNext() {
						// TODO Auto-generated method stub
						return false;
					}
				};
			}

			@Override
			public Set<SLLGraph> getDisjuncts() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String toString() {
				return "true";
			}
		};

		bottom = new DisjunctiveState<SLLGraph>();
	}

	/**
	 * A helper class for matching transformers to statements.
	 * 
	 * @author romanm
	 */
	protected class SLLMatcher extends
			TransformerMatcher<DisjunctiveState<SLLGraph>> {
		@Override
		public void caseInvokeStmt(InvokeStmt stmt) {
			InvokeExpr expr = stmt.getInvokeExpr();
			String methodName = expr.getMethod().getName();
			if (methodName.equals("analysisInitAllNulls")) {
				transformer = new InitAllNullsTransformer();
			} else if (methodName.equals("analysisInitAcyclic")) {
				if (expr.getArgCount() != 1) {
					throw new Error(
							"initAcyclicList expects one argument, but got "
									+ expr.getArgCount() + "!");
				}
				if (expr.getArg(0) instanceof Local)
					transformer = new InitAcyclicTransformer(
							(Local) expr.getArg(0));
				else
					throw new Error(
							"initAcyclicList expects one argument of type Local, but got "
									+ expr.getArg(0).getClass() + "!");
			} else if (methodName.equals("analysisAssertNotNull")) {
				transformer = new AssertNotNullTransformer(
						(Local) expr.getArg(0));
			} else if (methodName.equals("analysisAssertReachable")) {
				// transformer = new AssertReachablelTransformer();
			} else if (methodName.equals("analysisAssertAcyclic")) {
				// transformer = new AssertAcyclicTransformer();
			} else if (methodName.equals("analysisAssertCyclic")) {
				// transformer = new AssertCyclicTransformer();
			} else if (methodName.equals("analysisAssertDisjoint")) {
				// transformer = new AssertDisjointTransformer();
			} else if (methodName.equals("analysisAssertNoGarbage")) {
				// transformer = new AssertNoGarbageTransformer();
			}
		}

		@Override
		public void matchAssignRefToRef(Local lhs, Local rhs) {
			if (isListRefType(lhs) && isListRefType(rhs)) {
				if (lhs.equivTo(rhs))
					transformer = IdOperation.v();
				else
					transformer = new AssignRefToRefTransformer(lhs, rhs);
			}
		}

		@Override
		public void matchAssignNewExprToLocal(AssignStmt stmt, Local lhs,
				RefType baseType) {
			if (isListRefType(lhs))
				transformer = new AssignNewExprToLocalTransformer(lhs);
		}

		@Override
		public void matchAssignNullToRef(Local lhs) {
			if (isListRefType(lhs))
				transformer = new AssignNullTransformer(lhs);
		}

		/**
		 * Matches statements of the form {@code x=y.f} where 'x' and 'y' are
		 * local variables.
		 */
		@Override
		public void matchAssignInstanceFieldRefToLocal(AssignStmt stmt,
				Local lhs, Local rhs, SootField field) {
			if (field.getName().equals(listClassField)) {
				transformer = new AssignNextToLocalTransformer(lhs, rhs);
			}
		}

		@Override
		public void matchAssignLocalToInstanceFieldRef(Local base,
				SootField field, Local rhs) {
			if (isListRefType(base) && field.getName().equals(listClassField)) {
				transformer = new AssignLocalToNextFieldTransformer(base, rhs);
			}
		}

		@Override
		public void matchAssignNullToInstanceFieldRef(Local base,
				SootField field) {
			if (isListRefType(base))
				transformer = new AssignNextNullTransformer(base);
		}

		@Override
		public void matchAssumeLocalEqNull(IfStmt stmt, boolean polarity,
				Local op1) {
			transformer = new AssumeLocalEqNullTransformer(polarity, op1);
		}

		@Override
		public void matchAssumeLocalEqLocal(IfStmt stmt, boolean polarity,
				Local op1, Local op2) {
			transformer = new AssumeLocalEqLocalTransformer(polarity, op1, op2);
		}
	}

	/**
	 * A transformer for statements of the form {@code x=new SLLBenchmarks()}.
	 * 
	 * @author romanm
	 */
	protected class AssignNewExprToLocalTransformer extends
			UnaryOperation<DisjunctiveState<SLLGraph>> {
		protected final Local lhs;

		public AssignNewExprToLocalTransformer(Local lhs) {
			this.lhs = lhs;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				SLLGraph disjunct = graph.copy();
				disjunct.unmapLocal(lhs);
				Node newNode = new Node(disjunct.nullNode, 1);//TODO
				disjunct.addNode(newNode);
				disjunct.mapLocal(lhs, newNode);
				disjuncts.add(disjunct);
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code x=null}.
	 * 
	 * @author romanm
	 */
	protected class AssignNullTransformer extends
			UnaryOperation<DisjunctiveState<SLLGraph>> {
		protected final Local lhs;

		public AssignNullTransformer(Local lhs) {
			this.lhs = lhs;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				SLLGraph disjunct = graph.copy();
				disjunct.unmapLocal(lhs);
				disjunct.mapLocal(lhs, disjunct.nullNode);
				disjuncts.add(disjunct);
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code x=y}.
	 * 
	 * @author romanm
	 */
	protected class AssignRefToRefTransformer extends
			UnaryOperation<DisjunctiveState<SLLGraph>> {
		protected final Local lhs;
		protected final Local rhs;

		public AssignRefToRefTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				SLLGraph disjunct = graph.copy();
				disjunct.unmapLocal(lhs);
				disjunct.mapLocal(lhs, disjunct.pointsTo(rhs));
				disjuncts.add(disjunct);
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code x=y.next}.
	 * 
	 * @author romanm
	 */
	protected class AssignNextToLocalTransformer extends
			UnaryOperation<DisjunctiveState<SLLGraph>> {
		protected final Local lhs;
		protected final Local rhs;

		public AssignNextToLocalTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				Node rhsNode = graph.pointsTo(rhs);
				if (rhsNode == graph.nullNode) {
					// Skip this graph as it raises a NullPointerException.
				} else if (rhsNode.edgeLen == 1) { //TODO
					SLLGraph disjunct = graph.copy();
					Node rhsNextNode = disjunct.pointsTo(rhs).next;
					disjunct.unmapLocal(lhs);
					disjunct.mapLocal(lhs, rhsNextNode);
					disjuncts.add(disjunct);
				} else {
					// Focus on the edge.
					SLLGraph focusOne = focusOne(graph, rhs);
					Node rhsNextNode = focusOne.pointsTo(rhs).next;
					focusOne.unmapLocal(lhs);
					focusOne.mapLocal(lhs, rhsNextNode);
					disjuncts.add(focusOne);

					SLLGraph focusGtOne = focusGtOne(graph, rhs);
					rhsNextNode = focusGtOne.pointsTo(rhs).next;
					focusGtOne.unmapLocal(lhs);
					focusGtOne.mapLocal(lhs, rhsNextNode);
					disjuncts.add(focusGtOne);
				}
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code x.n=null}.
	 * 
	 * @author romanm
	 */
	protected class AssignNextNullTransformer extends
			UnaryOperation<DisjunctiveState<SLLGraph>> {
		protected final Local lhs;

		public AssignNextNullTransformer(Local lhs) {
			this.lhs = lhs;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				Node lhsNode = graph.pointsTo(lhs);
				if (lhsNode == graph.nullNode) {
					// Skip this graph as it raises a NullPointerException.
				} else {
					SLLGraph disjunct = graph.copy();
					lhsNode.next = disjunct.nullNode;
					lhsNode.edgeLen = 1;//TODO
					disjuncts.add(disjunct);
				}
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}

	/**
	 * A transformer for statements of the form {@code x.n=y}.
	 * 
	 * @author romanm
	 */
	protected class AssignLocalToNextFieldTransformer extends
			UnaryOperation<DisjunctiveState<SLLGraph>> {
		protected final Local lhs;
		protected final Local rhs;

		public AssignLocalToNextFieldTransformer(Local lhs, Local rhs) {
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				if (graph.pointsTo(lhs) == graph.nullNode) {
					// Skip this graph as it raises a NullPointerException.
				} else {
					SLLGraph disjunct = graph.copy();
					Node lhsNode = disjunct.pointsTo(lhs);
					Node rhsNode = disjunct.pointsTo(rhs);
					lhsNode.next = rhsNode;
					lhsNode.edgeLen = 1;//TODO
					disjuncts.add(disjunct);
				}
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}

	/**
	 * A transformer that always returns a state containing a single shape graph
	 * where all list variables point to null.
	 * 
	 * @author romanm
	 */
	protected class InitAllNullsTransformer extends
			UnaryOperation<DisjunctiveState<SLLGraph>> {
		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			return initNulls();
		}
	}

	/**
	 * A transformer that always returns a state containing three shape graphs
	 * where all list variables point to null, except a given list variable,
	 * which points to an acyclic list of size >= 0.
	 * 
	 * @author romanm
	 */
	protected class InitAcyclicTransformer extends
			UnaryOperation<DisjunctiveState<SLLGraph>> {
		protected final Local var;

		public InitAcyclicTransformer(Local var) {
			this.var = var;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			return initAcyclic(var);
		}
	}

	/**
	 * A transformer that checks whether a given list reference variable
	 * is/isn't null.
	 * 
	 * @author romanm
	 */
	protected class AssumeLocalEqNullTransformer extends
			AssumeTransformer<DisjunctiveState<SLLGraph>> {
		protected final Local var;

		public AssumeLocalEqNullTransformer(boolean polarity, Local var) {
			super(polarity);
			this.var = var;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				boolean varEqNull = graph.pointsTo(var) == graph.nullNode;
				if (varEqNull == polarity)
					disjuncts.add(graph);
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}
	protected class AnalysisLengthDiffTransformer extends
		BinaryOperation<DisjunctiveState<SLLGraph>>{
		Node list1;
		Node list2;
		int diff;
		public AnalysisLengthDiffTransformer(Node list1, Node list2, int diff){
			this.diff = diff;
			this.list1 = list1;
			this.list2 = list2;
		}
		
		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input1, DisjunctiveState<SLLGraph> input2) {
			return input2;
		
		}
		
		
	}

	/**
	 * A transformer that checks whether two given list reference variables are
	 * equal, i.e., both reference the same node or both are null.
	 * 
	 * @author romanm
	 */
	protected class AssumeLocalEqLocalTransformer extends
			AssumeTransformer<DisjunctiveState<SLLGraph>> {
		protected final Local lhs;
		protected final Local rhs;

		public AssumeLocalEqLocalTransformer(boolean polarity, Local lhs,
				Local rhs) {
			super(polarity);
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				boolean conditionHolds = graph.pointsTo(lhs) == graph
						.pointsTo(rhs);
				if (conditionHolds == polarity)
					disjuncts.add(graph);
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}

	/**
	 * A transformer used for asserting that a variable is not null.
	 * 
	 * @author romanm
	 */
	protected class AssertNotNullTransformer extends
			AssumeLocalEqNullTransformer {

		public AssertNotNullTransformer(Local var) {
			super(true, var);
		}

		public AssertNotNullTransformer(boolean polarity, Local var) {
			super(polarity, var);
		}
	}
}