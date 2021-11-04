package util;

import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;

import model.Node;
import model.NodeEdit;
import model.Script;

public class GTScriptConverter {

	public static Script convert(List<Action> script){
		Script convertedScript = new Script();
		for(Action op : script){
			convertedScript.editOps.add(convert(op));
		}
		return convertedScript;
	}

	private static NodeEdit convert(Action op) {
		Node node = convertNode(op.getNode());
		NodeEdit edit = null;
		if(op instanceof Insert){
			Insert insert = (Insert)op;
			Node location = convertNode(insert.getParent());
			edit = new NodeEdit(NodeEdit.OP_INSERT, node, location, insert.getPosition());
		}else if(op instanceof Delete){
			Node location = convertNode(op.getNode().getParent());
			edit = new NodeEdit(NodeEdit.OP_DELETE, node, location, op.getNode().positionInParent());
		}else if(op instanceof Update){
			Update update = (Update)op;
			//Make a fake node with the updated value of this operation.
			Node location = convertNode(op.getNode());
			location.label = update.getValue();
			location.pos = -1;
			location.length = -1;
			edit = new NodeEdit(NodeEdit.OP_UPDATE, node, location, -1);
		}else if(op instanceof Move){
			Move move = (Move)op;
			Node location = convertNode(move.getParent());
			edit = new NodeEdit(NodeEdit.OP_MOVE, node, location, move.getPosition());
		}
		return edit;
	}

	private static Node convertNode(ITree node) {
		return new Node(node.getLabel(), node.getType(), node.getPos(), node.getLength());
	}
}