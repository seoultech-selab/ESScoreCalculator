package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import hk.ust.cse.pishon.esgen.model.Change;
import hk.ust.cse.pishon.esgen.model.EditOp;
import hk.ust.cse.pishon.esgen.model.EditScript;
import model.Benchmark;
import model.Node;
import tree.NodeVisitor;

public class CheckScript {

	public static void main(String[] args) {
		String path = null;
		if(args.length >= 1){
			path = args[0];
		}else{
			System.out.println("CheckScript [path to files]");
			path = "Scripts";
		}
		System.setProperty("las.enable.gumtree.ast", "false");
		List<File> files = findChangeFiles(new File(path));
		System.out.println("Total "+files.size()+" Change Files.");
		List<Change> changes = readChanges(files);
		System.out.println("Total "+changes.size()+" Changes.");
		int count = 0;
		for(Change change : changes){
			try {
				String oldCode = convertToString(change.getOldFile().getContents());
				String newCode = convertToString(change.getNewFile().getContents());
				String changeName = change.getName();
				System.out.println(changeName);
				EditScript script = change.getScript();
				apply(script,oldCode);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			if(count++ > 2)
				break;
		}
	}

	private static String apply(EditScript script, String oldCode) {
		List<EditOp> ops = script.getEditOps();
		String newCode = null;
		StringBuffer sb = new StringBuffer();
		for(EditOp op : ops){
			System.out.println(op.getType());
			System.out.println(op.getOldCode());
			System.out.println("---------------------");
			System.out.println(op.getNewCode());
			switch(op.getType()){
			case EditOp.OP_DELETE:
				break;
			case EditOp.OP_INSERT:
			case EditOp.OP_MOVE:
			case EditOp.OP_UPDATE:
			}
		}
		return sb.toString();
	}

	private static void saveToFile(String fileName, String content) {
		FileOutputStream out = null;
		PrintWriter pw = null;
		try {
			out = new FileOutputStream(fileName);
			pw = new PrintWriter(out);
			pw.print(content);
			pw.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if(out != null)
					out.close();
				if(pw != null)
					pw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void storeBenchmark(String fileName, Benchmark benchmark) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(new File(fileName));
			oos = new ObjectOutputStream(fos);
			oos.writeObject(benchmark);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(fos != null)
					fos.close();
				if(oos != null)
					oos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
		List<Change> changes = new ArrayList<Change>();
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
			for(File f : path.listFiles()){
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
