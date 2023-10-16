package kr.ac.seoultech.selab.esscore.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import kr.ac.seoultech.selab.esscore.model.ESNode;
import kr.ac.seoultech.selab.esscore.model.ESNodeEdit;
import kr.ac.seoultech.selab.esscore.model.Script;
import script.model.EditOp;
import script.model.EditScript;
import tree.TreeNode;

public class LASScriptConverter {

	public static Script convert(EditScript script){
		Script convertedScript = new Script();
		for(EditOp op : script.getEditOps()){
			//Default - do not ignore import declarations, separate node-level edits.
			convertedScript.editOps.addAll(convert(op, false, false));
		}
		return convertedScript;
	}

	public static Script convert(EditScript script, boolean ignoreImport, boolean combineTree){
		Script convertedScript = new Script();
		for(EditOp op : script.getEditOps()){
			convertedScript.editOps.addAll(convert(op, ignoreImport, combineTree));
		}
		return convertedScript;
	}

	private static List<ESNodeEdit> convert(EditOp editOp, boolean ignoreImport, boolean combineTree) {
		List<ESNodeEdit> edits = new ArrayList<>();
		if(combineTree) {
			if(ignoreImport && editOp.getNode().getType() == ASTNode.IMPORT_DECLARATION
					|| (editOp.getNode().getParent() != null && editOp.getNode().getParent().getType() == ASTNode.IMPORT_DECLARATION))
				return edits;
			ESNode node = new ESNode(editOp.getNode().getLabel(), editOp.getNode().getASTNode());
			ESNode location = new ESNode(editOp.getLocation().getLabel(), editOp.getLocation().getASTNode());
			String type = convertType(editOp.getType());
			ESNodeEdit edit = new ESNodeEdit(type, node, location, editOp.getPosition());
			edits.add(edit);
		} else {
			List<EditOp> editOps = editOp.getSubtreeEdit();
			for(EditOp op : editOps){
				if(ignoreImport && op.getNode().getType() == ASTNode.IMPORT_DECLARATION
						|| (op.getNode().getParent() != null && op.getNode().getParent().getType() == ASTNode.IMPORT_DECLARATION))
					continue;
				ESNode node = new ESNode(op.getNode().getLabel(), op.getNode().getASTNode());
				ESNode location = new ESNode(op.getLocation().getLabel(), op.getLocation().getASTNode());
				String type = convertType(op.getType());
				ESNodeEdit edit = new ESNodeEdit(type, node, location, op.getPosition());
				edits.add(edit);
			}
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
		default:
			converted = type;
		}
		return converted;
	}

	public static ESNode convert(TreeNode n, List<ESNode> nodes) {
		ESNode node = convertNode(n);
		nodes.add(node);
		for(TreeNode c : n.children) {
			node.addChild(convert(c, nodes));
		}
		return node;
	}

	public static ESNode convertNode(TreeNode n) {
		ESNode node = new ESNode(n.getLabel(), getTypeName(n.getType()), n.getStartPosition(), n.getLength());
		return node;
	}

	public static String getTypeName(int type) {
		String typeName = null;
		try {
			typeName = ASTNode.nodeClassForType(type).getSimpleName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return typeName;
	}

}
