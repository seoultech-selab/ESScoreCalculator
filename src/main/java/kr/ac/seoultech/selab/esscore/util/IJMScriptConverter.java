package kr.ac.seoultech.selab.esscore.util;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import com.github.gumtreediff.tree.ITree;

import at.aau.softwaredynamics.classifier.entities.NodeInfo;
import at.aau.softwaredynamics.classifier.entities.SourceCodeChange;
import kr.ac.seoultech.selab.esscore.model.ESNode;
import kr.ac.seoultech.selab.esscore.model.ESNodeEdit;
import kr.ac.seoultech.selab.esscore.model.Script;

public class IJMScriptConverter {

	public static Script convert(List<SourceCodeChange> script, boolean ignoreImport, boolean combineTree){
		Script convertedScript = new Script();
		for(SourceCodeChange op : script){
			//Ignore import declaration related changes.
			if(ignoreImport && (op.getNode().getType() == ASTNode.IMPORT_DECLARATION
					|| op.getNode().getParent().getType() == ASTNode.IMPORT_DECLARATION))
				continue;
			ESNodeEdit edit = convert(op);
			convertedScript.editOps.add(edit);
		}
		return convertedScript;
	}

	public static Script convert(List<SourceCodeChange> script){
		return convert(script, true, false);
	}

	public static ESNode convert(ITree n, List<ESNode> nodes) {
		ESNode node = convertNode(n);
		nodes.add(node);
		for(ITree c : n.getChildren()) {
			node.addChild(convert(c, nodes));
		}
		return node;
	}

	private static ESNodeEdit convert(SourceCodeChange op) {
		ESNode node = null;
		ESNode location = null;
		String converted = "";
		switch(op.getAction().getName()){
		case "INS":
			converted = ESNodeEdit.OP_INSERT;
			node = convertNode(op.getDstInfo());
			// TODO: need to make adjustment for startPos, length later.
			// Consider offset diff. when computing scores for now.
			location = convertNode(op.getNode().getParent());
			break;
		case "DEL":
			converted = ESNodeEdit.OP_DELETE;
			node = convertNode(op.getSrcInfo());
			location = convertNode(op.getNode().getParent());
			break;
		case "MOV":
			converted = ESNodeEdit.OP_MOVE;
			node = convertNode(op.getSrcInfo());
			location = convertNode(op.getDstInfo());
			break;
		case "UPD":
			converted = ESNodeEdit.OP_UPDATE;
			node = convertNode(op.getSrcInfo());
			location = convertNode(op.getDstInfo());
			break;
		}
		ESNodeEdit edit = new ESNodeEdit(converted, node, location, op.getPosition());
		return edit;
	}

	public static ESNode convertNode(NodeInfo info) {
		int pos = info.getPosition() - info.getStartLineNumber() + 1;
		String typeName = CodeHandler.getTypeName(info.getNodeType());
		return new ESNode(info.getLabel(), typeName, pos, info.getLength());
	}

	public static ESNode convertNode(ITree node) {
		//Need to generate nodes from src/dst info.
		return new ESNode(node.getLabel(), CodeHandler.getTypeName(node.getType()), node.getPos(), node.getLength());
	}
}