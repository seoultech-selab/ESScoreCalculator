package kr.ac.seoultech.selab.esscore.util;

import java.util.List;
import java.util.TreeMap;

import org.eclipse.jdt.core.dom.ASTNode;

import com.github.gumtreediff.tree.ITree;

import at.aau.softwaredynamics.classifier.entities.NodeInfo;
import at.aau.softwaredynamics.classifier.entities.SourceCodeChange;
import kr.ac.seoultech.selab.esscore.model.ESNode;
import kr.ac.seoultech.selab.esscore.model.ESNodeEdit;
import kr.ac.seoultech.selab.esscore.model.Script;

public class IJMScriptConverter {

	private static final String NO_POS_LINE_MAP_ERR = "Position-Line map must be computed before conversion. Try computePosLineMap(oldCode, newCode) first.";
	public static TreeMap<Integer, Integer> oldPosLineMap = null;
	public static TreeMap<Integer, Integer> newPosLineMap = null;

	public static Script convert(List<SourceCodeChange> script, boolean ignoreImport, boolean combineTree) throws Exception {
		if(oldPosLineMap == null || newPosLineMap == null) {
			throw new Exception(NO_POS_LINE_MAP_ERR);
		}
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

	public static Script convert(List<SourceCodeChange> script) throws Exception{
		return convert(script, true, false);
	}

	public static ESNode convert(ITree n, List<ESNode> nodes, boolean fromOldCode) throws Exception {
		if((fromOldCode && oldPosLineMap == null)
				|| (!fromOldCode && newPosLineMap == null)) {
			throw new Exception(NO_POS_LINE_MAP_ERR);
		}
		TreeMap<Integer, Integer> posLineMap = fromOldCode ? oldPosLineMap : newPosLineMap;
		return convert(n, nodes, posLineMap);
	}

	public static ESNode convert(ITree n, List<ESNode> nodes) throws Exception {
		return convert(n, nodes, null);
	}

	public static ESNode convert(ITree n, List<ESNode> nodes, TreeMap<Integer, Integer> posLineMap) throws Exception {
		ESNode node = convertNode(n, posLineMap);
		nodes.add(node);
		for(ITree c : n.getChildren()) {
			node.addChild(convert(c, nodes, posLineMap));
		}
		return node;
	}

	private static ESNodeEdit convert(SourceCodeChange op) {
		ESNode node = null;
		ESNode location = null;
		String converted = "";
		int pos = -1;
		switch(op.getAction().getName()){
		case "INS":
			converted = ESNodeEdit.OP_INSERT;
			node = convertNode(op.getDstInfo());
			location = convertNode(op.getNode().getParent(), newPosLineMap);
			pos = findPosInParent(op.getNode());
			break;
		case "DEL":
			converted = ESNodeEdit.OP_DELETE;
			node = convertNode(op.getSrcInfo());
			location = convertNode(op.getNode().getParent(), oldPosLineMap);
			pos = findPosInParent(op.getNode());
			break;
		case "MOV":
			converted = ESNodeEdit.OP_MOVE;
			node = convertNode(op.getSrcInfo());
			location = convertNode(op.getDstInfo());
			//position should be new node's position.
			pos = findPosInParent(op.getDstInfo().getNode());
			break;
		case "UPD":
			converted = ESNodeEdit.OP_UPDATE;
			node = convertNode(op.getSrcInfo());
			location = convertNode(op.getDstInfo());
			//In case of update, using default position.
			break;
		}
		ESNodeEdit edit = new ESNodeEdit(converted, node, location, pos);
		return edit;
	}

	private static int findPosInParent(ITree node) {
		ITree parent = node.getParent();
		if(parent != null) {
			return parent.getChildPosition(node);
		}
		return -1;
	}

	public static ESNode convertNode(NodeInfo info) {
		int pos = info.getPosition() - info.getStartLineNumber() + 1;
		String typeName = CodeHandler.getTypeName(info.getNodeType());
		return new ESNode(info.getLabel(), typeName, pos, info.getLength());
	}

	public static ESNode convertNode(ITree node, boolean fromOldCode) throws Exception {
		if(oldPosLineMap == null || newPosLineMap == null) {
			throw new Exception(NO_POS_LINE_MAP_ERR);
		}
		if(fromOldCode) {
			return convertNode(node, oldPosLineMap);
		} else {
			return convertNode(node, newPosLineMap);
		}
	}

	public static ESNode convertNode(ITree node) {
		return convertNode(node, null);
	}

	public static ESNode convertNode(ITree node, TreeMap<Integer, Integer> posLineMap) {
		if(posLineMap == null)
			return new ESNode(node.getLabel(), CodeHandler.getTypeName(node.getType()), node.getPos(), node.getLength());
		int pos = getAdjustedPos(node.getPos(), posLineMap);
		return pos == -1 ? null : new ESNode(node.getLabel(), CodeHandler.getTypeName(node.getType()), pos, node.getLength());
	}

	public static void computePosLineMap(String oldCode, String newCode) {
		oldPosLineMap = computePosLineMap(oldCode);
		newPosLineMap = computePosLineMap(newCode);
	}

	public static TreeMap<Integer, Integer> computePosLineMap(String code) {
		//Given a position of a node from IJM, compute the start line number for offset adjustment.
		TreeMap<Integer, Integer> posLineMap = new TreeMap<>();
		String[] lines = code.split("\\n");
		int lineEndPos = 0;
		int lineNum = 1;
		int lineOffset = 0;
		for(String line : lines) {
			lineEndPos += line.length()+1;
			posLineMap.put(lineEndPos+lineOffset++, lineNum++);
		}
		//Must add final lineEndPos for positions exceed the last line.
		posLineMap.put(lineEndPos+lines.length, lineNum);

		return posLineMap;
	}

	public static int getAdjustedPos(int pos, TreeMap<Integer, Integer> posLineMap) {
		int lineNum = getLineNumber(pos, posLineMap);
		return lineNum == -1 ? -1 : pos - lineNum + 1;
	}

	private static int getLineNumber(int pos, TreeMap<Integer, Integer> posLineMap) {
		if(posLineMap == null)
			return -1;
		Integer key = posLineMap.ceilingKey(pos);
		return key == null ? -1 : posLineMap.get(key);
	}

}