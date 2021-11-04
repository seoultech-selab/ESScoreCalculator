package tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;

import model.Node;

public class NodeVisitor extends ASTVisitor {

	private static final boolean ENABLE_GUMTREE_AST =  System.getProperty("las.enable.gumtree.ast") == null ? false : Boolean.parseBoolean(System.getProperty("las.enable.gumtree.ast"));
	private Stack<Node> nodeStack;
	private Node root;
	public List<Node> nodes;
	private boolean parsingSwitchCase = false;

	public NodeVisitor(){
		root = new Node("root", -1, -1, -1);
		this.nodeStack = new Stack<Node>();
		this.nodeStack.add(root);
		this.nodes = new ArrayList<Node>();
	}

	@Override
	public void postVisit(ASTNode node) {
		if(ENABLE_GUMTREE_AST ||
				!(node instanceof ExpressionStatement)){
			//Put statements next to a switch case under the switch case.
			Node currentNode = nodeStack.pop();
			if(!ENABLE_GUMTREE_AST){
				if(node instanceof SwitchCase){
					//Push again to attach statements next to the switch case.
					nodeStack.push(currentNode);
				}else if(node instanceof SwitchStatement && parsingSwitchCase){
					//All switch cases are parsed.
					parsingSwitchCase = false;
					nodeStack.pop();	//Remove the switch statement.
				}
			}
		}
	}

	@Override
	public void preVisit(ASTNode node) {
		//Ignore ExpressionStatement.
		if(ENABLE_GUMTREE_AST ||
				!(node instanceof ExpressionStatement)){
			Node treeNode = getNode(node);
			//If currently a switch case is being parsed, and node is a new switch case, move to next.
			if(!ENABLE_GUMTREE_AST && node instanceof SwitchCase){
				if(parsingSwitchCase)
					nodeStack.pop();
				else
					parsingSwitchCase = true;
			}
			nodeStack.push(treeNode);
			nodes.add(treeNode);
		}
	}

	@Override
	public boolean visit(QualifiedName node){
		return false;
	}

	@Override
	public boolean visit(SimpleType node){
		if(ENABLE_GUMTREE_AST){
			return super.visit(node);
		}
		return false;
	}

	@Override
	public boolean visit(QualifiedType node){
		if(ENABLE_GUMTREE_AST){
			return super.visit(node);
		}
		return false;
	}

	private Node getNode(ASTNode node) {
		Node treeNode = new Node(getLabel(node), node);
		if(!nodeStack.isEmpty()){
			nodeStack.peek().addChild(treeNode);
		}
		return treeNode;
	}

	private String getLabel(ASTNode node){
		String label = node.getClass().getSimpleName();
		if(node instanceof Assignment)
			label += TreeNode.DELIM + ((Assignment)node).getOperator().toString();
		if(node instanceof BooleanLiteral
				|| node instanceof Modifier
				|| node instanceof SimpleType
				|| node instanceof QualifiedType
				|| node instanceof PrimitiveType)
			label += TreeNode.DELIM + node.toString();
		if(node instanceof CharacterLiteral)
			label += TreeNode.DELIM + ((CharacterLiteral)node).getEscapedValue();
		if(node instanceof NumberLiteral)
			label += TreeNode.DELIM + ((NumberLiteral)node).getToken();
		if(node instanceof StringLiteral)
			label += TreeNode.DELIM + ((StringLiteral)node).getEscapedValue();
		if(node instanceof InfixExpression)
			label += TreeNode.DELIM + ((InfixExpression)node).getOperator().toString();
		if(node instanceof PrefixExpression)
			label += TreeNode.DELIM + ((PrefixExpression)node).getOperator().toString();
		if(node instanceof PostfixExpression)
			label += TreeNode.DELIM + ((PostfixExpression)node).getOperator().toString();
		if(node instanceof SimpleName)
			label += TreeNode.DELIM + ((SimpleName)node).getIdentifier();
		if(node instanceof QualifiedName)
			label += TreeNode.DELIM + ((QualifiedName)node).getFullyQualifiedName();
		return label;
	}
}
