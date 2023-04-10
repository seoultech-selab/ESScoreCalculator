package util;

import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import model.ESNode;
import model.ESNodeEdit;
import model.TreeEdit;
import tree.TreeNode;

public class ScriptConverter implements ConvertScript {

	@Override
	public model.Script convert(EditScript script, List<ESNode> oldNodes, List<ESNode> newNodes){
		model.Script converted = new model.Script(script.toString());
		for(EditOp op : script.getEditOps()){
			converted.editOps.addAll(convert(op, oldNodes, newNodes));
		}
		return converted;
	}

	@Override
	public List<ESNodeEdit> convert(EditOp op, List<ESNode> oldNodes, List<ESNode> newNodes) {
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
			if(node != null && newNode != null){
				edits.add(new ESNodeEdit(ESNodeEdit.OP_MOVE, node, newNode.parent, newNode.posInParent));
			}
			break;
		case EditOp.OP_UPDATE:
			node = findSubtreeRoot(oldNodes, oldStart, oldEnd);
			newNode = findSubtreeRoot(newNodes, newStart, newEnd);
			if(node != null && newNode != null){
				//For Update operation, all nodes should have values.
				if(node.type == newNode.type &&
						checkNode(node) && checkNode(newNode)){
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

	private boolean checkNode(ESNode node) {
		return node.label.contains(TreeNode.DELIM);
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
		List<ESNode> nodesInRange = new ArrayList<>();
		for(int i=0; i<nodes.size(); i++){
			ESNode node = nodes.get(i);
			int nodeEnd = node.pos + node.length;
			if((node.pos >= startPos && node.pos <= endPos) || (nodeEnd >= startPos && nodeEnd <= endPos)){
				nodesInRange.add(node);
			}else if(node.pos > endPos){
				break;
			}
		}
		return nodesInRange;
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
