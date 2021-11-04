package model;

import java.io.Serializable;

public class NodeEdit implements Serializable {

	private static final long serialVersionUID = -1231598966718602060L;
	public static final String OP_INSERT = "Insert";
	public static final String OP_DELETE = "Delete";
	public static final String OP_MOVE = "Move";
	public static final String OP_UPDATE = "Update";
	public String type;
	public Node node;
	public Node location;
	public int position;

	public NodeEdit(String type, Node node, Node location, int position) {
		super();
		this.type = type;
		this.node = node;
		this.location = location;
		this.position = position;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof NodeEdit){
			NodeEdit edit = (NodeEdit)obj;
			return edit.type.equals(this.type)
					&& edit.node.equals(this.node)
					&& edit.location.equals(this.location)
					&& edit.position == this.position;
		}
		return false;
	}

	@Override
	public int hashCode(){
		String s = type + "|" + node.hashCode() + "|" + location.hashCode() + "|" + position;
		return s.hashCode();
	}

	@Override
	public String toString(){
		return type + "|" + node.toString() + "|" + location.toString() + "|" + position;
	}
}
