package util;

import java.util.ArrayList;
import java.util.List;

import model.ESNode;
import model.ESNodeEdit;
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

	private static List<ESNodeEdit> convert(EditOp editOp) {
		List<ESNodeEdit> edits = new ArrayList<>();
		List<EditOp> editOps = editOp.getSubtreeEdit();
		for(EditOp op : editOps){
			ESNode node = new ESNode(op.getNode().getLabel(), op.getNode().getASTNode());
			ESNode location = new ESNode(op.getLocation().getLabel(), op.getLocation().getASTNode());
			String type = convertType(op.getType());
			ESNodeEdit edit = new ESNodeEdit(type, node, location, op.getPosition());
			edits.add(edit);
		}
		return edits;
	}

	private static String convertType(String type) {
		String converted = "";
		switch(type){
		case "insert":
			converted = ESNodeEdit.OP_INSERT;
			break;
		case "delete":
			converted = ESNodeEdit.OP_DELETE;
			break;
		case "move":
			converted = ESNodeEdit.OP_MOVE;
			break;
		case "update":
			converted = ESNodeEdit.OP_UPDATE;
			break;
		}
		return converted;
	}

}
