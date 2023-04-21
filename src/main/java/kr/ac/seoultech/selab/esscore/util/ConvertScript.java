package kr.ac.seoultech.selab.esscore.util;

import java.util.List;

import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import kr.ac.seoultech.selab.esscore.model.ESNode;
import kr.ac.seoultech.selab.esscore.model.ESNodeEdit;

public interface ConvertScript {

	public abstract kr.ac.seoultech.selab.esscore.model.Script convert(EditScript script, List<ESNode> oldNodes, List<ESNode> newNodes);
	public abstract kr.ac.seoultech.selab.esscore.model.Script convert(EditScript script, List<ESNode> oldNodes, List<ESNode> newNodes, String treeType);
	public abstract List<ESNodeEdit> convert(EditOp op, List<ESNode> oldNodes, List<ESNode> newNodes);
	public abstract List<ESNodeEdit> convert(EditOp op, List<ESNode> oldNodes, List<ESNode> newNodes, String treeType);

}
