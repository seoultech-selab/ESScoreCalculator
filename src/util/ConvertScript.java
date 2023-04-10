package util;

import java.util.List;

import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import model.ESNode;
import model.ESNodeEdit;

public interface ConvertScript {

	public abstract model.Script convert(EditScript script, List<ESNode> oldNodes, List<ESNode> newNodes);
	public abstract model.Script convert(EditScript script, List<ESNode> oldNodes, List<ESNode> newNodes, String treeType);
	public abstract List<ESNodeEdit> convert(EditOp op, List<ESNode> oldNodes, List<ESNode> newNodes);
	public abstract List<ESNodeEdit> convert(EditOp op, List<ESNode> oldNodes, List<ESNode> newNodes, String treeType);

}
