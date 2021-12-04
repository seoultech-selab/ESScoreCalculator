package util;

import java.util.ArrayList;
import java.util.List;

import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import model.Node;
import model.NodeEdit;
import model.TreeEdit;
import tree.TreeNode;

public class ScriptConverter implements ConvertScript {

	@Override
	public model.Script convert(EditScript script, List<Node> oldNodes, List<Node> newNodes){
		model.Script converted = new model.Script(script.toString());
		for(EditOp op : script.getEditOps()){
			converted.editOps.addAll(convert(op, oldNodes, newNodes));
		}
		return converted;
	}

	@Override
	public List<NodeEdit> convert(EditOp op, List<Node> oldNodes, List<Node> newNodes) {
		List<NodeEdit> edits = new ArrayList<>();
		List<Node> nodes = null;
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
			nodes.forEach(n -> edits.add(new NodeEdit(NodeEdit.OP_INSERT, n, n.parent, n.posInParent)));
			break;
		case EditOp.OP_DELETE:
			nodes = findNodes(oldNodes, oldStart, oldEnd);
			nodes.forEach(n -> edits.add(new NodeEdit(NodeEdit.OP_DELETE, n, n.parent, n.posInParent)));
			break;
		case EditOp.OP_MOVE:
			Node node = findSubtreeRoot(oldNodes, oldStart, oldEnd);
			Node newNode = findSubtreeRoot(newNodes, newStart, newEnd);
			if(node != null && newNode != null){
				edits.add(new NodeEdit(NodeEdit.OP_MOVE, node, newNode.parent, newNode.posInParent));
			}
			break;
		case EditOp.OP_UPDATE:
			node = findSubtreeRoot(oldNodes, oldStart, oldEnd);
			newNode = findSubtreeRoot(newNodes, newStart, newEnd);
			if(node != null && newNode != null){
				//For Update operation, all nodes should have values.
				if(node.type == newNode.type &&
						checkNode(node) && checkNode(newNode)){
					edits.add(new NodeEdit(NodeEdit.OP_UPDATE, node, newNode, -1));
				}else{
					//If not, separate this update to one deletion and one insertion.
					nodes = findNodes(oldNodes, oldStart, oldEnd);
					nodes.forEach(n -> edits.add(new NodeEdit(NodeEdit.OP_DELETE, n, n.parent, n.posInParent)));
					nodes = findNodes(newNodes, newStart, newEnd);
					nodes.forEach(n -> edits.add(new NodeEdit(NodeEdit.OP_INSERT, n, n.parent, n.posInParent)));
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

	private boolean checkNode(Node node) {
		return node.label.contains(TreeNode.DELIM);
	}

	protected Node findSubtreeRoot(List<Node> nodes, int startPos, int endPos) {
		Node last = null;
		for(Node node : nodes){
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

	protected List<Node> findNodes(List<Node> nodes, int startPos, int endPos) {
		List<Node> nodesInRange = new ArrayList<>();
		for(int i=0; i<nodes.size(); i++){
			Node node = nodes.get(i);
			int nodeEnd = node.pos + node.length;
			if((node.pos >= startPos && node.pos <= endPos) || (nodeEnd >= startPos && nodeEnd <= endPos)){
				nodesInRange.add(node);
			}else if(node.pos > endPos){
				break;
			}
		}
		return nodesInRange;
	}

	public List<TreeEdit> groupSubtrees(List<NodeEdit> nodeEdits) {
		List<NodeEdit> edits = new ArrayList<>(nodeEdits);
		List<TreeEdit> roots = new ArrayList<>();
		while(edits.size() > 0) {
			NodeEdit e = edits.get(0);
			TreeEdit r = groupSubtree(e, edits);
			roots.add(r);
		}
		return roots;
	}

	private TreeEdit groupSubtree(NodeEdit r, List<NodeEdit> edits) {
		if(r == null)
			return null;
		TreeEdit root = new TreeEdit(r);

		//Find root.
		TreeEdit p;
		Node pNode;
		do {
			p = null;
			pNode = root.nodeEdit.location;
			if(pNode != null && pNode.type != -1) {
				for(NodeEdit e : edits) {
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
				for (Node child : target.nodeEdit.node.children) {
					for (NodeEdit e : edits) {
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
