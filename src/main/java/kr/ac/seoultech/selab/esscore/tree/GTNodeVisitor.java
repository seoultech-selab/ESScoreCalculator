package kr.ac.seoultech.selab.esscore.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;

import kr.ac.seoultech.selab.esscore.model.ESNode;

public class GTNodeVisitor extends ASTVisitor {
	private Stack<ESNode> nodeStack;
	private ESNode root;
	public List<ESNode> nodes;

	public GTNodeVisitor(){
		root = new ESNode("root", null, -1, -1);
		this.nodeStack = new Stack<>();
		this.nodeStack.add(root);
		this.nodes = new ArrayList<>();
	}

	@Override
	public void postVisit(ASTNode node) {
		nodeStack.pop();
	}

	@Override
	public void preVisit(ASTNode node) {
		ESNode treeNode = getNode(node);
		nodeStack.push(treeNode);
		nodes.add(treeNode);
	}

	@Override
	public boolean visit(QualifiedName node){
		return false;
	}

	private ESNode getNode(ASTNode node) {
		ESNode treeNode = new ESNode(getLabel(node), node);
		if(!nodeStack.isEmpty()){
			nodeStack.peek().addChild(treeNode);
		}
		return treeNode;
	}

	protected String getLabel(ASTNode n) {
		if (n instanceof Name) return ((Name) n).getFullyQualifiedName();
		if (n instanceof Type) return n.toString();
		if (n instanceof Modifier) return n.toString();
		if (n instanceof StringLiteral) return ((StringLiteral) n).getEscapedValue();
		if (n instanceof NumberLiteral) return ((NumberLiteral) n).getToken();
		if (n instanceof CharacterLiteral) return ((CharacterLiteral) n).getEscapedValue();
		if (n instanceof BooleanLiteral) return ((BooleanLiteral) n).toString();
		if (n instanceof InfixExpression) return ((InfixExpression) n).getOperator().toString();
		if (n instanceof PrefixExpression) return ((PrefixExpression) n).getOperator().toString();
		if (n instanceof PostfixExpression) return ((PostfixExpression) n).getOperator().toString();
		if (n instanceof Assignment) return ((Assignment) n).getOperator().toString();

		return "";
	}
}
