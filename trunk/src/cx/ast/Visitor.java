package cx.ast;

public abstract interface Visitor {

	// cardinal types

	public abstract void visitTrue(NodeTrue node);

	public abstract void visitFalse(NodeFalse node);

	public abstract void visitString(NodeString node);

	public abstract void visitNumber(NodeNumber node);

	public abstract void visitVar(NodeVar node);

	// compound types

	public abstract void visitArray(NodeArray node);

	public abstract void visitObject(NodeObject node);

	// declaration types

	public abstract void visitVariable(NodeVariable node);

	public abstract void visitFunction(NodeFunction node);

	// flow types

	public abstract void visitBlock(NodeBlock node);

	public abstract void visitIf(NodeIf node);

	public abstract void visitFor(NodeFor node);

	public abstract void visitWhile(NodeWhile node);

	public abstract void visitSwitch(NodeSwitch node);

	public abstract void visitCaseList(NodeCaseList node);

	public abstract void visitCase(NodeCase node);

	public abstract void visitTry(NodeTry node);

	public abstract void visitThrow(NodeThrow node);

	public abstract void visitBreak(NodeBreak node);

	public abstract void visitContinue(NodeContinue node);

	public abstract void visitReturn(NodeReturn node);

	// operation types

	public abstract void visitCall(NodeCall node);

	public abstract void visitAccess(NodeAccess node);

	public abstract void visitAssign(NodeAssign node);

	public abstract void visitUnary(NodeUnary node);

	public abstract void visitBinary(NodeBinary node);

	public abstract void visitTernary(NodeTernary node);
}