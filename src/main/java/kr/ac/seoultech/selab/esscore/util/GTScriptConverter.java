package kr.ac.seoultech.selab.esscore.util;

import java.util.ArrayList;
import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.TreeAction;
import com.github.gumtreediff.actions.model.TreeDelete;
import com.github.gumtreediff.actions.model.TreeInsert;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;

import kr.ac.seoultech.selab.esscore.model.ESNode;
import kr.ac.seoultech.selab.esscore.model.ESNodeEdit;
import kr.ac.seoultech.selab.esscore.model.Script;

public class GTScriptConverter {

	public static Script convert(List<Action> script, MappingStore mappings, boolean ignoreImport, boolean combineTree){
		Script convertedScript = new Script();
		for(Action op : script){
			//Ignore import declaration related changes.
			if(ignoreImport && ("ImportDeclaration".equals(op.getNode().getType().name)
					|| "ImportDeclaration".equals(op.getNode().getParent().getType().name)))
				continue;
			if(op instanceof TreeAction) {
				List<ESNodeEdit> edits = convertTreeOp((TreeAction)op, mappings, combineTree);
				convertedScript.editOps.addAll(edits);
			} else {
				ESNodeEdit edit = convert(op, mappings);
				convertedScript.editOps.add(edit);
			}
		}
		return convertedScript;
	}

	public static Script convert(List<Action> script, MappingStore mappings){
		return convert(script, mappings, true, false);
	}

	private static List<ESNodeEdit> convertTreeOp(TreeAction op, MappingStore mappings, boolean combineTree) {
		List<ESNodeEdit> edits = new ArrayList<>();
		List<ESNode> nodes = new ArrayList<>();
		Tree n = op.getNode();
		ESNode node = convert(n, nodes);
		if(op instanceof TreeInsert) {
			TreeInsert insert = ((TreeInsert) op);
			Tree p = insert.getParent();
			ESNode location = convertNode(p);
			ESNodeEdit edit = new ESNodeEdit(ESNodeEdit.OP_INSERT, node, location, insert.getPosition());
			edits.add(edit);
			//Add separate node edits only if combineTree is false.
			if(!combineTree) {
				for(ESNode c : nodes.subList(1, nodes.size())) {
					edit = new ESNodeEdit(ESNodeEdit.OP_INSERT, c, c.parent, c.posInParent);
					edits.add(edit);
				}
			}
		} else if (op instanceof TreeDelete) {
			ESNode location = convertNode(n.getParent());
			ESNodeEdit edit = new ESNodeEdit(ESNodeEdit.OP_DELETE, node, location, node.posInParent);
			edits.add(edit);
			if(!combineTree) {
				for(ESNode c : nodes.subList(1, nodes.size())) {
					edit = new ESNodeEdit(ESNodeEdit.OP_DELETE, c, c.parent, c.posInParent);
					edits.add(edit);
				}
			}
		} else if (op instanceof Move) {
			Move move = (Move)op;
			ESNode location = convertNode(move.getParent());
			ESNodeEdit edit = new ESNodeEdit(ESNodeEdit.OP_MOVE, node, location, move.getPosition());
			edits.add(edit);
			if(!combineTree) {
				for(ESNode c : nodes.subList(1, nodes.size())) {
					edit = new ESNodeEdit(ESNodeEdit.OP_MOVE, c, c.parent, c.posInParent);
					edits.add(edit);
				}
			}
		}
		return edits;
	}

	public static ESNode convert(Tree n, List<ESNode> nodes) {
		ESNode node = GTScriptConverter.convertNode(n);
		nodes.add(node);
		for(Tree c : n.getChildren()) {
			node.addChild(convert(c, nodes));
		}
		return node;
	}

	private static ESNodeEdit convert(Action op, MappingStore mappings) {
		ESNode node = convertNode(op.getNode());
		ESNodeEdit edit = null;
		if(op instanceof Insert){
			Insert insert = (Insert)op;
			ESNode location = convertNode(insert.getParent());
			edit = new ESNodeEdit(ESNodeEdit.OP_INSERT, node, location, insert.getPosition());
		}else if(op instanceof Delete){
			ESNode location = convertNode(op.getNode().getParent());
			edit = new ESNodeEdit(ESNodeEdit.OP_DELETE, node, location, op.getNode().positionInParent());
		}else if(op instanceof Update){
			//Make a fake node with the updated value of this operation.
			ESNode location = convertNode(mappings.getDstForSrc(op.getNode()));
			edit = new ESNodeEdit(ESNodeEdit.OP_UPDATE, node, location, -1);
		}
		return edit;
	}

	public static ESNode convertNode(Tree node) {
		return new ESNode(node.getLabel(), node.getType().name, node.getPos(), node.getLength());
	}
}