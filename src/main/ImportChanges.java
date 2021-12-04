package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import hk.ust.cse.pishon.esgen.model.Change;
import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import model.Node;
import model.NodeEdit;
import model.TreeEdit;
import tree.NodeVisitor;
import util.ScriptConverter;

public class ImportChanges {

	public static void main(String[] args) {
		String path = "scripts";

		System.setProperty("las.enable.gumtree.ast", "false");

		List<File> files = findChangeFiles(new File(path));
		System.out.println("Total "+files.size()+" Change Files.");
		List<Change> changes = readChanges(files);
		System.out.println("Total "+changes.size()+" Changes.");
		ScriptConverter converter = new ScriptConverter();
		List<Node> oldNodes = null;
		List<Node> newNodes = null;

		for(Change change : changes){
			try {
				String oldCode = convertToString(change.getOldFile().getContents());
				String newCode = convertToString(change.getNewFile().getContents());
				String changeName = change.getName();
				System.out.println(changeName);
				oldNodes = parse(oldCode);
				newNodes = parse(newCode);
				EditScript editScript = change.getScript();
				List<NodeEdit> nodeEdits = new ArrayList<>();
				for(EditOp op : editScript.getEditOps()) {
					nodeEdits.addAll(converter.convert(op, oldNodes, newNodes));
				}

				//Group node-level edits and leave only the roots of subtrees.
				List<TreeEdit> rootEdits = converter.groupSubtrees(nodeEdits);

				for(TreeEdit e : rootEdits) {
					String changeType = e.nodeEdit.type;
					String entityType = getEntityType(e.nodeEdit.node);
					int startPos = e.nodeEdit.node.pos;
					//Import data to DB here.
					System.out.println(changeType + " " + entityType);
				}


			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private static String getEntityType(Node node) {
		String nodeType = "";
		try {
			nodeType = ASTNode.nodeClassForType(node.type).getSimpleName();
		}catch(Exception e) {
			nodeType = node.label.substring(0, node.label.indexOf('#'));
		}
		return nodeType;
	}

	private static String convertToString(InputStream is){
		String content = null;
		try {
			content = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	private static List<Node> parse(String content) {
		List<Node> nodes = new ArrayList<>();
		CompilationUnit cu = getCompilationUnit(content);
		NodeVisitor visitor = new NodeVisitor();
		cu.accept(visitor);
		nodes.addAll(visitor.nodes);
		return nodes;
	}

	private static CompilationUnit getCompilationUnit(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		CompilationUnit cu = (CompilationUnit)parser.createAST(null);

		return cu;
	}

	private static List<Change> readChanges(List<File> files) {
		List<Change> changes = new ArrayList<>();
		FileInputStream fis = null;
		ObjectInputStream in = null;
		for(File f : files){
			try {
				fis = new FileInputStream(f);
				in = new ObjectInputStream(fis);
				Object obj = in.readObject();
				if(obj instanceof List){
					changes.addAll((List<Change>)obj);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} finally {
				try {
					if(fis != null)
						fis.close();
					if(in != null)
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return changes;
	}

	private static List<File> findChangeFiles(File path) {
		List<File> files = new ArrayList<>();
		if(path.isDirectory()){
			File[] targets = path.listFiles();
			Arrays.sort(targets, (f1, f2)->(f1.getName().compareTo(f2.getName())));
			for(File f : targets){
				if(f.isDirectory()){
					files.addAll(findChangeFiles(f));
				}else if(f.getName().equals("changes.obj")){
					files.add(f);
				}
			}
		}else{
			if(path.getName().equals("changes.obj")){
				files.add(path);
			}
		}
		return files;
	}

}

