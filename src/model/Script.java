package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Script implements Serializable {

	private static final long serialVersionUID = 2670527991597044417L;
	public List<ESNodeEdit> editOps = new ArrayList<ESNodeEdit>();
	public String textScript;

	public Script(){
		this.textScript = "";
	}

	public Script(String textScript){
		this.textScript = textScript;
	}

	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		List<ESNodeEdit> ops = new ArrayList<>(editOps);
		ops.sort((ESNodeEdit op1, ESNodeEdit op2)->op1.node.pos-op2.node.pos);
		for(ESNodeEdit edit : ops){
			sb.append(edit);
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Script){
			List<ESNodeEdit> script1 = new ArrayList<ESNodeEdit>(((Script)obj).editOps);
			script1.sort((ESNodeEdit op1, ESNodeEdit op2)->op1.node.pos-op2.node.pos);
			List<ESNodeEdit> script2 = new ArrayList<ESNodeEdit>(editOps);
			script2.sort((ESNodeEdit op1, ESNodeEdit op2)->op1.node.pos-op2.node.pos);
			return script1.equals(script2);
		}else{
			return false;
		}
	}

	@Override
	public int hashCode(){
		List<ESNodeEdit> script = new ArrayList<ESNodeEdit>(editOps);
		script.sort((ESNodeEdit op1, ESNodeEdit op2)->op1.node.pos-op2.node.pos);
		return script.hashCode();
	}
}
