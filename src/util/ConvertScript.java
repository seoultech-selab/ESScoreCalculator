package util;

import java.util.List;

import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import model.Node;
import model.NodeEdit;

public interface ConvertScript {

	public abstract model.Script convert(EditScript script, List<Node> oldNodes, List<Node> newNodes);
	public abstract List<NodeEdit> convert(EditOp op, List<Node> oldNodes, List<Node> newNodes);

}
