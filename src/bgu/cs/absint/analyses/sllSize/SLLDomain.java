package bgu.cs.absint.analyses.sllSize;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import bgu.cs.absint.AbstractDomain;
import bgu.cs.absint.AssumeTransformer;
import bgu.cs.absint.ErrorState;
import bgu.cs.absint.IdOperation;
import bgu.cs.absint.UnaryOperation;
import bgu.cs.absint.analyses.zone.ZoneDomain;
import bgu.cs.absint.analyses.zone.ZoneFactoid;
import bgu.cs.absint.analyses.zone.ZoneState;
import bgu.cs.absint.constructor.DisjunctiveState;
import bgu.cs.absint.soot.TransformerMatcher;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.Type;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.internal.JimpleLocal;

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
	private static int counter=0;

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
		return operation(elem1, elem2, (zs1, zs2) -> ZoneDomain.v().ub(zs1, zs2));
	}
	private interface Operation{
		ZoneState run(ZoneState zs1, ZoneState zs2);
	}
	private DisjunctiveState<SLLGraph> operation(DisjunctiveState<SLLGraph> elem1,
			DisjunctiveState<SLLGraph> elem2, Operation op){
		if (elem1 == getTop() || elem2 == getTop())
			return getTop();
		
		Set<SLLGraph> graphSet1 = new HashSet<>();
		Set<SLLGraph> graphSet2 = new HashSet<>();
		for(SLLGraph graph:elem1.getDisjuncts())
			graphSet1.add(graph.copy());
		for(SLLGraph graph:elem2.getDisjuncts())
			graphSet2.add(graph.copy());
				
		for(SLLGraph graph1:graphSet1){
			graph1.normalize();
			for(SLLGraph graph2: graphSet2){
				graph2.normalize();
			}
			for(SLLGraph graph2: graphSet2){
				if(graph1.equals(graph2)){
					ZoneState joinedState = op.run(graph1.sizes, graph2.sizes);
					graph1.sizes = joinedState;
					graph1.sizes = joinedState;
				}
			}
		}		
		DisjunctiveState<SLLGraph> result = new DisjunctiveState<SLLGraph>(
				graphSet1, graphSet2);
		return result;
	}
	@Override
	public DisjunctiveState<SLLGraph> widen(DisjunctiveState<SLLGraph> elem1,
			DisjunctiveState<SLLGraph> elem2) {
		return operation(elem1, elem2, (zs1, zs2) -> ZoneDomain.v().widen(zs1, zs2));
		
	}
	@Override
	public DisjunctiveState<SLLGraph> narrow(DisjunctiveState<SLLGraph> elem1,
			DisjunctiveState<SLLGraph> elem2) {
		return operation(elem1, elem2, (zs1, zs2) -> ZoneDomain.v().narrow(zs1, zs2));
	}
	@Override
	public DisjunctiveState<SLLGraph> reduce(DisjunctiveState<SLLGraph> input) {
		if (input == getTop())
			return getTop();
		Set<SLLGraph> graphSet = new HashSet<>();
		for(SLLGraph graph:input.getDisjuncts())
			graphSet.add(graph.copy());
		for(SLLGraph g:graphSet){
			g.sizes = ZoneDomain.v().reduce(g.sizes);
		}
		DisjunctiveState<SLLGraph> result= new DisjunctiveState<>(graphSet);
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
			return getTop();//widen narrow

		HashSet<SLLGraph> disjuncts = new HashSet<>();
		for (SLLGraph graph : elem1) {

			SLLGraph disjunct = generalize(graph);
			disjuncts.add(disjunct);//TODO
		}
		for (SLLGraph graph : elem2) {
			SLLGraph disjunct = generalize(graph);
			disjuncts.add(disjunct);//TODO
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
		allNullsGraph.sizes = new ZoneState();
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
	static int num = 1;
	public static Local makeLocal()//TODO
	{
		JimpleLocal  j =  new JimpleLocal("loc_var"+counter,IntType.v());
		if(counter == 2){
			System.out.println("WHAT");
		}
		j.setNumber(counter++);
		return j;
	}
//	private Local int2local(Local x)//TODO
//	{
//		JimpleLocal  j =  new JimpleLocal("loc_var"+counter++,IntType.v());
//		return j;
//	}
	public DisjunctiveState<SLLGraph> initAcyclic(Local x) {
		SLLGraph graph1 = makeAllNullsGraph();
		Local local1 = makeLocal();
		graph1.sizes.addFactoid(local1, ZoneFactoid.ZERO_VAR, IntConstant.v(1));
		Node ptXOne = new Node(graph1.nullNode, local1);//TODO
		
		graph1.addNode(ptXOne);
		graph1.mapLocal(x, ptXOne);

		SLLGraph graph2 = makeAllNullsGraph();
		Local local2 = makeLocal();
		graph1.sizes.addFactoid(ZoneFactoid.ZERO_VAR, local2, IntConstant.v(-2));
		Node ptXGt1 = new Node(graph2.nullNode, local2); //SUPER TODO
		

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
		Local local1 = makeLocal();
		result.sizes.addFactoid(local1, ZoneFactoid.ZERO_VAR, IntConstant.v(1));	
		result.sizes.removeVar(var);
		result.sizes.addFactoid(var, ZoneFactoid.ZERO_VAR, IntConstant.v(1));
		Node rhsNode = result.pointsTo(var);
		Node rhsNextNode = rhsNode.next;
		Node node = new Node(rhsNextNode, local1); 
		rhsNode.next = node;
		
		
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
		Local local1 = makeLocal();
		
		Collection<ZoneFactoid> collec = result.sizes.getFactoids();
		int len = -1;
		for(ZoneFactoid zf : collec){
			if(zf.equalVars(var, ZoneFactoid.ZERO_VAR))
				len = zf.bound.value;
		}
		result.sizes.addFactoid(local1, ZoneFactoid.ZERO_VAR, IntConstant.v(len-1));
		if(len == -1)
			throw new RuntimeException("FCK");
		result.sizes.removeVar(var);
		result.sizes.addFactoid(var, ZoneFactoid.ZERO_VAR, IntConstant.v(1));
		Node rhsNode = result.pointsTo(var);
		Node rhsNextNode = rhsNode.next;
		
		Node node = new Node(rhsNextNode, local1); 
		rhsNode.next = node;
			
		
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
					Local a = n.edgeLen;
					Local b = n.next.edgeLen;
					n.next = n.next.next;				
					
					
					int newVal = getConstant(result, a,  ZoneFactoid.ZERO_VAR).value;
					newVal += getConstant(result, b,  ZoneFactoid.ZERO_VAR).value;
					result.sizes.removeVar(a);
					result.sizes.addFactoid(a, ZoneFactoid.ZERO_VAR, IntConstant.v(newVal));
					result.sizes.removeVar(b);
					result.unmapLocal(b);
//					result.removeNode(result.pointsTo(b));
//					result.sizes.removeVar(a);
//					result.unmapLocal(a);
					
				}
			}
		}

		result.removeGarbageNodes();
		return result;
	}
	
	private IntConstant getConstant(SLLGraph graph, Local a, Local b){
		for(ZoneFactoid zf: graph.sizes.getFactoids()){
			if(zf.lhs==a && zf.rhs == b)
				return zf.bound;
		}
		return null;
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
				 transformer = new AssertNoGarbageTransformer(expr.getArg(0)+"");
			} else if (methodName.equals("analysisLengthDiff")){
				Local a = (Local)expr.getArg(0);
				Local b = (Local) expr.getArg(1);
				IntConstant c =  (IntConstant) expr.getArg(2);
				transformer = new AnalysisLengthDiffTransformer(a,b,c.value);
			}
			else if(methodName.equals("error")){
				transformer = new AnalysisError(expr.getArg(0)+"");
				
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
		@Override//TODO look at this function
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
				disjunct.sizes.removeVar(lhs);
				Node newNode = new Node(disjunct.nullNode, lhs);//TODO
				disjunct.sizes.addFactoid(lhs, ZoneFactoid.ZERO_VAR, IntConstant.v(1));
				disjunct.addNode(newNode);
				disjunct.mapLocal(lhs, newNode);
				disjuncts.add(disjunct);
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}

	protected class AssertNoGarbageTransformer extends
	UnaryOperation<DisjunctiveState<SLLGraph>> {
		String msg;
		public AssertNoGarbageTransformer(String message){
			msg = message;
		}
		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			for(SLLGraph graph: input.getDisjuncts()){
				Map<Node, Boolean> nodesMap = new HashMap<>();
				for(Node n:graph.nodes)
					nodesMap.put(n, false);
				for(Local l:graph.pointsTo.keySet()){
					Node n = graph.pointsTo(l);
					while(n != null){
						nodesMap.put(n, true);
						n = n.next;
					}					
				}
				boolean flag=false;
				for(Entry<Node,Boolean> s : nodesMap.entrySet()){
					if(!s.getValue())
						flag=true;
				}
				if(flag)
					return new ErrorDisjunctiveState(msg);
			}
			
			return input;
		}
	}
	protected class AnalysisLengthDiffTransformer extends
	UnaryOperation<DisjunctiveState<SLLGraph>> {
		private int diff;
		private Local headA, headB;
		public AnalysisLengthDiffTransformer(Local headA, Local headB, int diff) {
			this.diff = diff;
			this.headA = headA;
			this.headB = headB;
		}
		
		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			Set<SLLGraph> disjuncts = new HashSet<>();
			for (SLLGraph graph : input) {
				SLLGraph disjunct = graph.copy();
								
				Node nodeA = disjunct.pointsTo(headA);
				Node nodeB = disjunct.pointsTo(headB);
				int lenA = 0;
				int lenB = 0;
				while(nodeA != disjunct.nullNode){
					Local localA = nodeA.edgeLen;
					lenA += getConstant(disjunct, localA, ZoneFactoid.ZERO_VAR).value;
					nodeA = nodeA.next;
				}
				while(nodeB != disjunct.nullNode){
					Local localB = nodeB.edgeLen;
					lenB += getConstant(disjunct, localB, ZoneFactoid.ZERO_VAR).value;
					nodeB = nodeB.next;
				}
				if(Math.abs(lenA - lenB) == diff)
					disjuncts.add(disjunct);
				else
					return new ErrorDisjunctiveState("AnalysisLengthDiffTransformer ERROR!");
			}
			DisjunctiveState<SLLGraph> result = new DisjunctiveState<>(
					disjuncts);
			return result;
		}
	}
	protected class AnalysisError extends
	UnaryOperation<DisjunctiveState<SLLGraph>> {
		private String message;
		public AnalysisError(String message) {
			this.message=message;
		}
		
		@Override
		public DisjunctiveState<SLLGraph> apply(DisjunctiveState<SLLGraph> input) {
			ErrorDisjunctiveState result = new ErrorDisjunctiveState(message);
			return result;
		}
	}
	private class ErrorDisjunctiveState extends DisjunctiveState<SLLGraph> implements ErrorState{

		String message;
		public ErrorDisjunctiveState(String message){
			this.message = message;
		}
		@Override
		public String getMessages() {
			return message;
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
				disjunct.sizes.addFactoid(lhs, ZoneFactoid.ZERO_VAR, IntConstant.v(1));
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
	 * @author romanm//TODO getNext
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
				if (rhsNode == graph.nullNode ) {
					// Skip this graph as it raises a NullPointerException.
				} else if (getConstant(graph, rhs, ZoneFactoid.ZERO_VAR).value == 1) { 
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
					lhsNode.edgeLen = makeLocal();//TODO//TODO
					disjunct.sizes.addFactoid(lhsNode.edgeLen, ZoneFactoid.ZERO_VAR, IntConstant.v(1));
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
					lhsNode.edgeLen = makeLocal();//TODO
					disjunct.sizes.addFactoid(lhsNode.edgeLen, ZoneFactoid.ZERO_VAR, IntConstant.v(1));
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
	/*
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
			input1 = input2;
			return input2;
		
		}
		
		
	}*/

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