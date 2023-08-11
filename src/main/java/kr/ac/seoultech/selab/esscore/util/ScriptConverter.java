package kr.ac.seoultech.selab.esscore.util;

import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import kr.ac.seoultech.selab.esscore.model.ESNode;
import kr.ac.seoultech.selab.esscore.model.ESNodeEdit;
import kr.ac.seoultech.selab.esscore.model.TreeEdit;
import tree.TreeNode;

public class ScriptConverter implements ConvertScript {

	public static final String TREE_TYPE_LAS = "LAS";
	public static final String TREE_TYPE_GT = "GumTree";
	public static final String TREE_TYPE_IJM = "IJM";

	@Override
	public kr.ac.seoultech.selab.esscore.model.Script convert(EditScript script, List<ESNode> oldNodes, List<ESNode> newNodes){
		return convert(script, oldNodes, newNodes, TREE_TYPE_LAS);
	}

	@Override
	public kr.ac.seoultech.selab.esscore.model.Script convert(EditScript script, List<ESNode> oldNodes, List<ESNode> newNodes, String treeType){
		kr.ac.seoultech.selab.esscore.model.Script converted = new kr.ac.seoultech.selab.esscore.model.Script(toTextScript(script));
		for(EditOp op : script.getEditOps()){
			converted.editOps.addAll(convert(op, oldNodes, newNodes, treeType));
		}
		return converted;
	}

	private String toTextScript(EditScript script) {
		List<EditOp> editOps = script.getEditOps();
		if (editOps.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (EditOp op : editOps) {
				sb.append("\n\n");
				sb.append(toOpString(op));
			}
			return sb.toString().substring(2);
		}else{
			return "";
		}
	}

	private String toOpString(EditOp op) {
		StringBuffer sb = new StringBuffer();
		String type = op.getType();
		sb.append(type);
		sb.append("\t");
		if(type.equals(EditOp.OP_INSERT)){
			sb.append(op.getNewCode());
			sb.append(" (Line ");
			sb.append(op.getNewStartLine());
			sb.append(")[");
			sb.append(op.getNewStartPos());
			sb.append(",");
			sb.append(op.getNewLength());
			sb.append("]");
		}else if(type.equals(EditOp.OP_DELETE)){
			sb.append(op.getOldCode());
			sb.append(" (Line ");
			sb.append(op.getOldStartLine());
			sb.append(")[");
			sb.append(op.getOldStartPos());
			sb.append(",");
			sb.append(op.getOldLength());
			sb.append("]");
		}else{
			sb.append(op.getOldCode());
			sb.append(" (Line ");
			sb.append(op.getOldStartLine());
			sb.append(")[");
			sb.append(op.getOldStartPos());
			sb.append(",");
			sb.append(op.getOldLength());
			sb.append("]\n");
			sb.append("to\t");
			sb.append(op.getNewCode());
			sb.append(" (Line ");
			sb.append(op.getNewStartLine());
			sb.append(")[");
			sb.append(op.getNewStartPos());
			sb.append(",");
			sb.append(op.getNewLength());
			sb.append("]");
		}
		return sb.toString();
	}

	@Override
	public List<ESNodeEdit> convert(EditOp op, List<ESNode> oldNodes, List<ESNode> newNodes) {
		return convert(op, oldNodes, newNodes, TREE_TYPE_LAS);
	}

	@Override
	public List<ESNodeEdit> convert(EditOp op, List<ESNode> oldNodes, List<ESNode> newNodes, String treeType) {
		List<ESNodeEdit> edits = new ArrayList<>();
		List<ESNode> nodes = null;
		int oldStart = op.getOldStartPos();
		int oldEnd = op.getOldStartPos() + op.getOldLength();
		int newStart = op.getNewStartPos();
		int newEnd = op.getNewStartPos() + op.getNewLength();
		if (op.getOldCode() != null) {
			oldStart += getLTrim(op.getOldCode());
			oldEnd -= getRTrim(op.getOldCode());
		}
		if (op.getNewCode() != null) {
			newStart += getLTrim(op.getNewCode());
			newEnd -= getRTrim(op.getNewCode());
		}
		switch(op.getType()){

		case EditOp.OP_INSERT:
			nodes = findNodes(newNodes, newStart, newEnd);
			nodes.forEach(n -> edits.add(new ESNodeEdit(ESNodeEdit.OP_INSERT, n, n.parent, n.posInParent)));
			break;
		case EditOp.OP_DELETE:
			nodes = findNodes(oldNodes, oldStart, oldEnd);
			nodes.forEach(n -> edits.add(new ESNodeEdit(ESNodeEdit.OP_DELETE, n, n.parent, n.posInParent)));
			break;
		case EditOp.OP_MOVE:
			ESNode node = findSubtreeRoot(oldNodes, oldStart, oldEnd);
			ESNode newNode = findSubtreeRoot(newNodes, newStart, newEnd);
			if(node != null && newNode != null && isValid(node)){
				edits.add(new ESNodeEdit(ESNodeEdit.OP_MOVE, node, newNode.parent, newNode.posInParent));
			}
			break;
		case EditOp.OP_UPDATE:
			node = findSubtreeRoot(oldNodes, oldStart, oldEnd);
			newNode = findSubtreeRoot(newNodes, newStart, newEnd);
			if(node != null && newNode != null){
				//For Update operation, all nodes should have values.
				if(node.type != null && node.type.equals(newNode.type) && isValid(node)
						&& checkNode(node, treeType) && checkNode(newNode, treeType)){
					edits.add(new ESNodeEdit(ESNodeEdit.OP_UPDATE, node, newNode, -1));
				}else{
					//If not, separate this update to one deletion and one insertion.
					nodes = findNodes(oldNodes, oldStart, oldEnd);
					nodes.forEach(n -> edits.add(new ESNodeEdit(ESNodeEdit.OP_DELETE, n, n.parent, n.posInParent)));
					nodes = findNodes(newNodes, newStart, newEnd);
					nodes.forEach(n -> edits.add(new ESNodeEdit(ESNodeEdit.OP_INSERT, n, n.parent, n.posInParent)));
				}
			}
			break;
		}

		return edits;
	}

	private int getLTrim(String oldCode) {
		int trim = 0;
		for(char c : oldCode.toCharArray()){
			if(c == ' ' || c == '\n' || c == '\t')
				trim++;
			else
				break;
		}
		return trim;
	}

	private int getRTrim(String oldCode) {
		int trim = 0;
		for(int i=oldCode.length()-1; i>=0; i--){
			char c = oldCode.charAt(i);
			if(c == ' ' || c == '\n' || c == '\t')
				trim++;
			else
				break;
		}
		return trim;
	}

	private boolean checkNode(ESNode node, String treeType) {
		switch(treeType) {
		case TREE_TYPE_LAS:
			return node.label.contains(TreeNode.DELIM);
		case TREE_TYPE_GT:
			return !"".equals(node.label);
		case TREE_TYPE_IJM:
			//TODO:Check condition for IJM.
			return !"".equals(node.label);
		}
		return false;
	}

	protected ESNode findSubtreeRoot(List<ESNode> nodes, int startPos, int endPos) {
		ESNode last = null;
		for(ESNode node : nodes){
			if(node.pos >= startPos && node.pos <= endPos){
				return node;
			}else if(node.pos >= startPos){
				if(last.pos + last.length >= endPos)
					return last;
			}
			last = node;
		}
		return null;
	}

	protected List<ESNode> findNodes(List<ESNode> nodes, int startPos, int endPos) {
		//nodes must be sorted in ascending order of pos.
		//Find the smallest node contains the entire range.
		List<ESNode> nodesInRange = new ArrayList<>();
		ESNode smallest = nodes.get(0);
		for(int i=0; i<nodes.size(); i++){
			ESNode node = nodes.get(i);
			int nodeEnd = node.pos + node.length;
			if(includeRange(node, startPos, endPos)) {
				if(includeRange(smallest, node.pos, nodeEnd)) {
					smallest = node;
				}
			} else if (node.pos > endPos){
				break;
			}
		}

		//Then include descendants within range.
		//Don't add CompilationUnit change which works as the root. This is to remove comment changes.
		if(!"CompilationUnit".equals(smallest.type) && !"ImportDeclaration".equals(smallest.type))
			nodesInRange.add(smallest);
		findNodes(smallest, nodesInRange, startPos, endPos);

		return nodesInRange;
	}

	protected void findNodes(ESNode n, List<ESNode> nodesInRange, int startPos, int endPos) {
		for(ESNode c : n.children) {
			if(hasOverlap(c, startPos, endPos)) {
				if(isValid(c)) {
					nodesInRange.add(c);
					findNodes(c, nodesInRange, startPos, endPos);
				}
			}
		}
	}

	private boolean isValid(ESNode n) {
		//Remove import declaration related nodes.
		boolean isValid = true;
		if(n.type != null) {
			if(n.type.equals("ImportDeclaration")) {
				return false;
			} else if(n.parent != null && n.parent.type != null
					&& n.parent.type.equals("ImportDeclaration")) {
				return false;
			}
		}
		return isValid;
	}

	private boolean hasOverlap(ESNode n, int startPos, int endPos) {
		int nodeEnd = n.pos + n.length;
		return (n.pos >= startPos && n.pos <= endPos)
				|| (nodeEnd <= startPos && nodeEnd >= endPos);
	}

	private boolean includeRange(ESNode n, int startPos, int endPos) {
		return n.pos <= startPos && n.pos + n.length >= endPos;
	}

	public List<TreeEdit> groupSubtrees(List<ESNodeEdit> nodeEdits) {
		List<ESNodeEdit> edits = new ArrayList<>(nodeEdits);
		List<TreeEdit> roots = new ArrayList<>();
		while(edits.size() > 0) {
			ESNodeEdit e = edits.get(0);
			TreeEdit r = groupSubtree(e, edits);
			roots.add(r);
		}
		return roots;
	}

	private TreeEdit groupSubtree(ESNodeEdit r, List<ESNodeEdit> edits) {
		if(r == null)
			return null;
		TreeEdit root = new TreeEdit(r);

		//Find root.
		TreeEdit p;
		ESNode pNode;
		do {
			p = null;
			pNode = root.nodeEdit.location;
			if(pNode != null && pNode.type != null) {
				for(ESNodeEdit e : edits) {
					if(pNode.equals(e.node) && e.type.equals(r.type)) {
						p = new TreeEdit(e);
						break;
					}
				}
			}
			if(p != null)
				root = p;
		} while (p != null);

		List<TreeEdit> targets = new ArrayList<>();
		List<TreeEdit> grouped = new ArrayList<>();
		targets.add(root);
		edits.remove(root.nodeEdit);
		do {
			targets.addAll(grouped);
			grouped.clear();
			//Find children of each target.
			for(TreeEdit target : targets){
				for (ESNode child : target.nodeEdit.node.children) {
					for (ESNodeEdit e : edits) {
						if (e.node.equals(child) && target.nodeEdit.type.equals(e.type)) {
							TreeEdit t = new TreeEdit(e);
							target.children.add(t);
							grouped.add(t);
							break;
						}
					}
				}
			}
			//Remove grouped.
			for(TreeEdit t : grouped){
				edits.remove(t.nodeEdit);
			}
		} while (grouped.size() > 0 && edits.size() > 0);

		return root;
	}
}
