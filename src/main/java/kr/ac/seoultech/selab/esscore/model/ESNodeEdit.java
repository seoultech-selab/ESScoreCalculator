package kr.ac.seoultech.selab.esscore.model;

import java.io.Serializable;

public class ESNodeEdit implements Serializable {

	private static final long serialVersionUID = -1231598966718602060L;
	public static final String OP_INSERT = "Insert";
	public static final String OP_DELETE = "Delete";
	public static final String OP_MOVE = "Move";
	public static final String OP_UPDATE = "Update";
	public String type;
	public ESNode node;
	public ESNode location;
	public int position;

	public ESNodeEdit(String type, ESNode node, ESNode location, int position) {
		super();
		this.type = type;
		this.node = node;
		this.location = location;
		this.position = position;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ESNodeEdit){
			ESNodeEdit edit = (ESNodeEdit)obj;
			return edit.type.equals(this.type)
					&& edit.node.equals(this.node)
					&& ((edit.location == null && this.location == null)
							|| edit.location.equals(this.location))
					&& edit.position == this.position;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int locationHash = location == null ? -1 : location.hashCode();
		String s = type + "|" + node.hashCode() + "|" + locationHash + "|" + position;
		return s.hashCode();
	}

	@Override
	public String toString(){
		String strLoc = location == null ? "" : location.toString();
		return type + "|" + node.toString() + "|" + strLoc + "|" + position;
	}
}
