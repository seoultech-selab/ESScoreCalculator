package util;

import java.util.List;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.Tree;

import model.ESNode;
import model.ESNodeEdit;
import model.Script;

public class GTScriptConverter {

	public static Script convert(List<Action> script){
		Script convertedScript = new Script();
		for(Action op : script){
			convertedScript.editOps.add(convert(op));
		}
		return convertedScript;
	}

	private static ESNodeEdit convert(Action op) {
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
			Update update = (Update)op;
			//Make a fake node with the updated value of this operation.
			ESNode location = convertNode(op.getNode());
			location.label = update.getValue();
			location.pos = -1;
			location.length = -1;
			edit = new ESNodeEdit(ESNodeEdit.OP_UPDATE, node, location, -1);
		}else if(op instanceof Move){
			Move move = (Move)op;
			ESNode location = convertNode(move.getParent());
			edit = new ESNodeEdit(ESNodeEdit.OP_MOVE, node, location, move.getPosition());
		}
		return edit;
	}

	private static ESNode convertNode(Tree node) {
		return new ESNode(node.getLabel(), node.getType().name, node.getPos(), node.getLength());
	}
}