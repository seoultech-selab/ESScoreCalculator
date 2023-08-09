package kr.ac.seoultech.selab.esscore.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import at.aau.softwaredynamics.gen.OptimizedJdtTreeGenerator;
import kr.ac.seoultech.selab.esscore.model.ESNode;
import kr.ac.seoultech.selab.esscore.tree.NodeVisitor;

public class CodeHandler {

	public static List<ESNode> parse(String content) {
		List<ESNode> nodes = new ArrayList<>();
		CompilationUnit cu = CodeHandler.getCompilationUnit(content);
		NodeVisitor visitor = new NodeVisitor();
		cu.accept(visitor);
		nodes.addAll(visitor.nodes);
		return nodes;
	}

	public static CompilationUnit getCompilationUnit(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		CompilationUnit cu = (CompilationUnit)parser.createAST(null);

		return cu;
	}

	public static String getEntityType(ESNode node) {
		return node.type != null ? node.type : node.label.substring(0, node.label.indexOf('#'));
	}

	public static String getTypeName(int type) {
		String typeName = null;
		try {
			typeName = ASTNode.nodeClassForType(type).getSimpleName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return typeName;
	}

	/**
	 * @param code
	 * @return a list of converted nodes visited in DFS.
	 * @throws IOException
	 */
	public static List<ESNode> parseIJM(String code) throws Exception {
		List<ESNode> nodes = new ArrayList<>();
		TreeGenerator generator = new OptimizedJdtTreeGenerator();
		TreeContext tree = generator.generateFromString(code);
		ITree root = tree.getRoot();
		//		TreeMap<Integer, Integer> posLineMap = IJMScriptConverter.computePosLineMap(code);
		//For simple parsing, no need to adjust nodes' positions.
		IJMScriptConverter.convert(root, nodes);

		return nodes;
	}

}
