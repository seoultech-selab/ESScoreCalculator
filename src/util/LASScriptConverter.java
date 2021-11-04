package util;

import java.util.ArrayList;
import java.util.List;

import model.Node;
import model.NodeEdit;
import model.Script;
import script.model.EditOp;
import script.model.EditScript;

public class LASScriptConverter {

	public static Script convert(EditScript script){
		Script convertedScript = new Script();
		for(EditOp op : script.getEditOps()){
			convertedScript.editOps.addAll(convert(op));
		}
		return convertedScript;
	}

	private static List<NodeEdit> convert(EditOp editOp) {
		List<NodeEdit> edits = new ArrayList<>();
		List<EditOp> editOps = editOp.getSubtreeEdit();
		for(EditOp op : editOps){
			Node node = new Node(op.getNode().getLabel(), op.getNode().getASTNode());
			Node location = new Node(op.getLocation().getLabel(), op.getLocation().getASTNode());
			String type = convertType(op.getType());
			NodeEdit edit = new NodeEdit(type, node, location, op.getPosition());
			edits.add(edit);
		}
		return edits;
	}

	private static String convertType(String type) {
		String converted = "";
		switch(type){
		case "insert":
			converted = NodeEdit.OP_INSERT;
			break;
		case "delete":
			converted = NodeEdit.OP_DELETE;
			break;
		case "move":
			converted = NodeEdit.OP_MOVE;
			break;
		case "update":
			converted = NodeEdit.OP_UPDATE;
			break;
		}
		return converted;
	}

}
