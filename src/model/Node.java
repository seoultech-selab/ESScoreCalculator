package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class Node implements Serializable {

	private static final long serialVersionUID = -721628405080048569L;
	public int type;
	public int pos;
	public int length;
	public String label;
	public Node parent;
	public List<Node> children;
	public int posInParent;

	public Node(String label, int type, int pos, int length) {
		super();
		this.type = type;
		this.pos = pos;
		this.length = length;
		this.label = label;
		this.parent = null;
		this.children = new ArrayList<>();
		this.posInParent = -1;
	}

	public Node(String label, ASTNode node) {
		this(label, node.getNodeType(), node.getStartPosition(), node.getLength());
	}

	public void addChild(Node node){
		node.parent = this;
		node.posInParent = children.size();
		children.add(node);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Node){
			Node n = (Node)obj;
			if(this.pos == -1 || n.pos == -1){
				//Handling GumTree Update operations without node information.
				return n.type==this.type && n.label.equals(this.label);
			}else{
				return n.type==this.type
						&& n.pos == this.pos && n.length == this.length
						&& n.label.equals(this.label);
			}
		}
		return false;
	}

	@Override
	public int hashCode(){
		String s = label + "|" + type + "|" + pos;
		return s.hashCode();
	}

	@Override
	public String toString(){
		return "[" + label + "|" + ASTNode.nodeClassForType(type).getSimpleName() + "|" + pos + "|" + length + "]";
	}
}
