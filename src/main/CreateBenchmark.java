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
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import hk.ust.cse.pishon.esgen.model.Change;
import model.Benchmark;
import model.Node;
import model.Script;
import tree.NodeVisitor;
import util.ScriptConverter;

public class CreateBenchmark {

	public static void main(String[] args) {
		String path = null;
		if(args.length >= 1){
			path = args[0];
		}else{
			System.out.println("CreateBenchmark [path to files]");
			path = "Scripts";
		}
		System.setProperty("las.enable.gumtree.ast", "false");
		Benchmark benchmark = new Benchmark();
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
				benchmark.addChange(changeName, oldCode, newCode);
				oldNodes = parse(oldCode);
				newNodes = parse(newCode);
				Script script = converter.convert(change.getScript(), oldNodes, newNodes);
				benchmark.addItem(changeName, script);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		Set<String> changeNames = benchmark.getChangeNames();
		for(String changeName : changeNames){
			System.out.println("Change:"+changeName);
			System.out.println("Uniq. Script Count:"+benchmark.uniqueCount(changeName));
			System.out.println("Total Script Count:"+benchmark.totalCount(changeName));
			StringBuffer sb = new StringBuffer();
			for(Script s : benchmark.getScripts(changeName).elementSet()){
				sb.append("Script\n");
				sb.append(s.textScript+"\n");
				sb.append("Vote:"+benchmark.cardinality(changeName, s)+"\n");
				sb.append("Size:"+s.editOps.size()+"\n");
				sb.append(s.toString()+"\n");
			}
			saveToFile("changes/"+changeName+"/benchmark_las.txt", sb.toString());
		}
		storeBenchmark("benchmark.obj", benchmark);
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
