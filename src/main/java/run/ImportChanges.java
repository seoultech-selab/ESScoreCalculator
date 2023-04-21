package run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;

import hk.ust.cse.pishon.esgen.model.Change;
import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import model.ESNode;
import model.ESNodeEdit;
import model.TreeEdit;
import util.CodeHandler;
import util.FileHandler;
import util.ScriptConverter;

public class ImportChanges {

	public static void main(String[] args) {
		String path = "scripts";

		System.setProperty("las.enable.gumtree.ast", "false");

		List<File> files = FileHandler.findChangeFiles(new File(path));
		System.out.println("Total "+files.size()+" Change Files.");
		List<Change> changes = FileHandler.readChanges(files);
		System.out.println("Total "+changes.size()+" Changes.");
		ScriptConverter converter = new ScriptConverter();
		List<ESNode> oldNodes = null;
		List<ESNode> newNodes = null;

		for(Change change : changes){
			try {
				String oldCode = FileHandler.convertToString(change.getOldFile().getContents());
				String newCode = FileHandler.convertToString(change.getNewFile().getContents());
				String changeName = change.getName();
				System.out.println(changeName);
				oldNodes = CodeHandler.parse(oldCode);
				newNodes = CodeHandler.parse(newCode);
				EditScript editScript = change.getScript();
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
					//Import data to DB here.
					System.out.println(changeType + " " + entityType);
				}


			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

}

