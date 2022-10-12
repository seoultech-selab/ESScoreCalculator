package main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;

import hk.ust.cse.pishon.esgen.model.Change;
import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import hk.ust.cse.pishon.esgen.model.Node;
import model.ESNode;
import model.ESNodeEdit;
import model.TreeEdit;
import util.CodeHandler;
import util.FileHandler;
import util.ScriptConverter;

/*
 * Export information from multi-scripts object files of Map<String, Node> instances.
 */
public class ExportScriptInfo {

	public static void main(String[] args) {
		String basePath = "scripts_grouped";
		String changeListRefFile = "scripts/scripts1/changes.obj";
		String groupListRefFile = basePath + "/groups.csv";

		System.setProperty("las.enable.gumtree.ast", "false");

		//Load changes.
		System.out.println("Loading changes...");
		List<Change> changes = new ArrayList<>();
		FileHandler.readChanges(new File(changeListRefFile), changes);
		Map<String, String> oldCodeMap = new HashMap<>();
		Map<String, String> newCodeMap = new HashMap<>();
		try {
			for(Change c : changes) {
				oldCodeMap.put(c.getName(), FileHandler.convertToString(c.getOldFile().getContents()));
				newCodeMap.put(c.getName(), FileHandler.convertToString(c.getNewFile().getContents()));
			}
		} catch (CoreException e1) {
			e1.printStackTrace();
		}

		Map<String, String> changeGroups = new HashMap<>();
		String str = FileHandler.readFile(groupListRefFile);
		String[] lines = str.split("\n");
		for(String line : lines) {
			changeGroups.put(line.substring(0, line.indexOf(',')), line.substring(line.indexOf(',')+1));
		}

		TreeMap<String, String> groups = new TreeMap<>();
		//Processing grouped edit scripts.
		StringBuffer sb = new StringBuffer("change_id,script_id,change_type,entity_type,start_position");
		StringBuffer sbText = new StringBuffer("change_id,script_id,change_type,old_start_pos,old_length,new_start_pos,new_length");
		for(int i=1; i<=6; i++) {
			String groupName = "group"+i;
			String fileName = String.join("", basePath, File.separator, "scripts-", groupName, ".obj");
			System.out.println("Reading file - "+fileName);
			File f = new File(fileName);
			changes = new ArrayList<>();
			HashMap<String, Node> item = FileHandler.readScripts(f);
			System.out.println("Item:"+item.size());
			HashMap<String, TreeMap<String, EditScript>> scripts = new HashMap<>();
			//Changes.
			TreeSet<String> changeNames = new TreeSet<>(item.keySet());
			for(String changeName : changeNames) {
				if(!groupName.equals(changeGroups.get(changeName))) {
					continue;
				}
				TreeMap<String, EditScript> map = new TreeMap<>();
				scripts.put(changeName, map);
				Node n = item.get(changeName);
				HashMap<List<EditOp>, Integer> uniqScripts = new HashMap<>();

				//Scripts.
				for(Node c : n.children) {
					EditScript script = new EditScript();
					List<EditOp> ops = new ArrayList<>();
					String scriptName = (String)c.value;
					map.put(scriptName, script);

					//EditOps.
					for(Node d : c.children) {
						if(d.value instanceof EditOp) {
							EditOp op = (EditOp)d.value;
							script.add(op);
							ops.add(op);
							sbText.append("\n");
							sbText.append(changeName);
							sbText.append(",");
							sbText.append(scriptName);
							sbText.append(",");
							sbText.append(op.getType());
							sbText.append(",");
							sbText.append(op.getOldStartPos());
							sbText.append(",");
							sbText.append(op.getOldLength());
							sbText.append(",");
							sbText.append(op.getNewStartPos());
							sbText.append(",");
							sbText.append(op.getNewLength());
						}
					}
					Collections.sort(ops);
					uniqScripts.compute(ops, ((k, v) -> v == null ? 1 : v+1));
				}
				boolean hasMajority = false;
				int max = 0;
				int total = 0;
				for(Integer count : uniqScripts.values()) {
					if(count > max)
						max = count;
					total += count;
				}
				if(Double.compare((double)max/total, 0.5d) > 0) {
					hasMajority = true;
				}
				groups.put(changeName, "group"+uniqScripts.size()+(hasMajority ? "-major" : ""));

				ScriptConverter converter = new ScriptConverter();

				String oldCode = oldCodeMap.get(changeName);
				String newCode = newCodeMap.get(changeName);
				List<ESNode> oldNodes = CodeHandler.parse(oldCode);
				List<ESNode> newNodes = CodeHandler.parse(newCode);

				for(String scriptName : map.keySet()) {
					EditScript editScript = map.get(scriptName);
					List<ESNodeEdit> nodeEdits = new ArrayList<>();
					for(EditOp op : editScript.getEditOps()) {
						nodeEdits.addAll(converter.convert(op, oldNodes, newNodes));
					}

					//Group node-level edits and leave only the roots of subtrees.
					List<TreeEdit> rootEdits = converter.groupSubtrees(nodeEdits);

					for(TreeEdit e : rootEdits) {
						String changeType = e.nodeEdit.type;
						String entityType = CodeHandler.getEntityType(e.nodeEdit.node);
						int startPos = e.nodeEdit.node.pos;

						sb.append("\n");
						sb.append(changeName);
						sb.append(",");
						sb.append(scriptName);
						sb.append(",");
						sb.append(changeType);
						sb.append(",");
						sb.append(entityType);
						sb.append(",");
						sb.append(startPos);
					}
				}
			}
		}
		FileHandler.storeContent("scripts_grouped/edit_scripts_text.csv", sbText.toString());
		FileHandler.storeContent("scripts_grouped/edit_scripts_ctet.csv", sb.toString());
		sb = new StringBuffer("change,group");
		System.out.println("changes:"+groups.size());
		int majority = 0;
		for(String cName : groups.keySet()) {
			sb.append("\n");
			sb.append(cName);
			sb.append(",");
			sb.append(groups.get(cName));
			if(groups.get(cName).contains("major"))
				majority++;
		}
		System.out.println("Changes w/ Majority Scripts:"+majority);
		FileHandler.storeContent("change_groups.csv", sb.toString());
	}

}