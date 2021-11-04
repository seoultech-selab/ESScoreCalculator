package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Script implements Serializable {

	private static final long serialVersionUID = 2670527991597044417L;
	public List<NodeEdit> editOps = new ArrayList<NodeEdit>();
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
		List<NodeEdit> ops = new ArrayList<>(editOps);
		ops.sort((NodeEdit op1, NodeEdit op2)->op1.node.pos-op2.node.pos);
		for(NodeEdit edit : ops){
			sb.append(edit);
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof Script){
			List<NodeEdit> script1 = new ArrayList<NodeEdit>(((Script)obj).editOps);
			script1.sort((NodeEdit op1, NodeEdit op2)->op1.node.pos-op2.node.pos);
			List<NodeEdit> script2 = new ArrayList<NodeEdit>(editOps);
			script2.sort((NodeEdit op1, NodeEdit op2)->op1.node.pos-op2.node.pos);
			return script1.equals(script2);
		}else{
			return false;
		}
	}

	@Override
	public int hashCode(){
		List<NodeEdit> script = new ArrayList<NodeEdit>(editOps);
		script.sort((NodeEdit op1, NodeEdit op2)->op1.node.pos-op2.node.pos);
		return script.hashCode();
	}
}
