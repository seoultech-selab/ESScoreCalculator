package kr.ac.seoultech.selab.esscore.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

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

}
