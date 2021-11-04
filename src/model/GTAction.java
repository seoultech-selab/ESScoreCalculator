package model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;

public class GTAction {

	public String actionType;
	public String codeType;
	public Action action;
	public List<GTAction> children;
	public String charStr;

	public GTAction(Action action){
		this.action = action;
		this.actionType = getActionType(action);
		this.codeType = ASTNode.nodeClassForType(action.getNode().getType()).getSimpleName();;
		this.children = new ArrayList<GTAction>();
		this.charStr = "";
	}

	public GTAction(Action action, CompilationUnit srcCu, CompilationUnit dstCu){
		this.action = action;
		this.actionType = getActionType(action);
		this.codeType = ASTNode.nodeClassForType(action.getNode().getType()).getSimpleName();;
		this.children = new ArrayList<GTAction>();
		setCharStr(srcCu, dstCu);
	}

	public void setCharStr(CompilationUnit srcCu,  CompilationUnit dstCu){
		StringBuffer sb = new StringBuffer();
		sb.append(actionType);
		sb.append("\t");
		sb.append(convertNodeType(action.getNode().toShortString()));
		sb.append("[");
		if(actionType.equals("INS")){
			sb.append(dstCu.getLineNumber(action.getNode().getPos()));
		}else{
			sb.append(srcCu.getLineNumber(action.getNode().getPos()));
		}
		sb.append("]");
		if(actionType.equals("MOV")){
			Move move = (Move)action;
			sb.append("\tTO\t");
			sb.append(convertNodeType(move.getParent().toShortString()));
			sb.append("[");
			sb.append(srcCu.getLineNumber(move.getParent().getPos()));
			sb.append("]");
		}else if(actionType.equals("INS")){
			Insert insert = (Insert)action;
			sb.append("\tTO\t");
			sb.append(convertNodeType(insert.getParent().toShortString()));
			sb.append("[");
			sb.append(srcCu.getLineNumber(insert.getParent().getPos()));
			sb.append("]");
		}else if(actionType.equals("UPD")){
			Update update = (Update)action;
			sb.append("\tTO\t");
			sb.append(update.getValue());
		}
		this.charStr = sb.toString();
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(this.charStr);
		for(GTAction child : children){
			sb.append("\n\t");
			sb.append(child.toString());
		}
		return sb.toString();
	}

	public static String getActionType(Action action) {
		String actionType = "";
		if(action instanceof Insert){
			actionType = "INS";
		}else if(action instanceof Delete){
			actionType = "DEL";
		}else if(action instanceof Update){
			actionType = "UPD";
		}else if(action instanceof Move){
			actionType = "MOV";
		}
		return actionType;
	}

	public String convertNodeType(String actionString){
		Pattern nodeTypePattern = Pattern.compile("([0-9]+)(@@)");
		java.util.regex.Matcher matcher = nodeTypePattern.matcher(actionString);
		if(matcher.find()){
			int nodeType = Integer.parseInt(matcher.group(1));
			return actionString.replaceAll(nodeType+"@@", ASTNode.nodeClassForType(nodeType).getSimpleName()+"@@");
		}
		return actionString;
	}
}
