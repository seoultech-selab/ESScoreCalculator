package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public class ESNode implements Serializable {

	private static final long serialVersionUID = -721628405080048569L;
	public String type;
	public int pos;
	public int length;
	public String label;
	public ESNode parent;
	public List<ESNode> children;
	public int posInParent;

	public ESNode(String label, String type, int pos, int length) {
		super();
		this.type = type;
		this.pos = pos;
		this.length = length;
		this.label = label;
		this.parent = null;
		this.children = new ArrayList<>();
		this.posInParent = -1;
	}

	public ESNode(String label, ASTNode node) {
		this(label, ASTNode.nodeClassForType(node.getNodeType()).getSimpleName(), node.getStartPosition(), node.getLength());
	}

	public void addChild(ESNode node){
		node.parent = this;
		node.posInParent = children.size();
		children.add(node);
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ESNode){
			ESNode n = (ESNode)obj;
			if(this.pos == -1 || n.pos == -1){
				//Handling GumTree Update operations without node information.
				return n.type != null && n.type.equals(this.type) && n.label.equals(this.label);
			}else{
				return n.type != null && n.type.equals(this.type)
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
		return String.join("", "[", label, "|", type, "|", String.valueOf(pos), "|", String.valueOf(length), "]");
	}
}
