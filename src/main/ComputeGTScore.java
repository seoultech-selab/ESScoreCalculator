package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;

import com.github.gumtreediff.actions.ActionGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.Generators;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;

import model.Benchmark;
import model.Score;
import model.Script;
import util.GTScriptConverter;
import util.ScoreCalculator;

public class ComputeGTScore {

	public static void main(String[] args) {
		String benchmarkPath = "benchmark_gt.obj";
		Map<String, Score> scores = new HashMap<>();
		try {
			Benchmark benchmark = readBenchmark(benchmarkPath);
			ScoreCalculator calculator = new ScoreCalculator(benchmark);
			Set<String> changeNames = benchmark.getChangeNames();
			for(String changeName : changeNames){
				File srcFile = getFile(changeName, "old");
				File dstFile = getFile(changeName, "new");
				List<Action> actions = null;
				Run.initGenerators();
				ITree src = Generators.getInstance().getTree(srcFile.getAbsolutePath()).getRoot();
				ITree dst = Generators.getInstance().getTree(dstFile.getAbsolutePath()).getRoot();
				Matcher m = Matchers.getInstance().getMatcher(src, dst); // retrieve the default matcher
				m.match();
				ActionGenerator g = new ActionGenerator(src, dst, m.getMappings());
				g.generate();
				actions = g.getActions();
				List<Action> convertedActions = new ArrayList<>();
				for(Action action : actions){
					if(action instanceof Insert){
						Insert insert = (Insert)action;
						ITree newParent = insert.getNode().getParent();
						Insert newInsert = new Insert(insert.getNode(), newParent, insert.getNode().positionInParent());
						convertedActions.add(newInsert);
					}else{
						convertedActions.add(action);
					}
				}
				Script script = GTScriptConverter.convert(convertedActions);
				scores.put(changeName, calculator.getBestMatchScore(changeName, script));
				Score score = scores.get(changeName);
				System.out.println("Change - "+changeName);
				System.out.println("Score - "+ score.score);
				System.out.println("Matched Count:"+score.count+" (Max:"+score.maxCount+")");
				System.out.println("Similarity:"+score.similarity);
				StringBuffer sb = new StringBuffer();
				sb.append("Script Size:"+actions.size()+"\n");
				sb.append("Score:"+score.score+"\n");
				sb.append("Matched Count:"+score.count+" (Max:"+score.maxCount+")\n");
				sb.append("Similarity:"+score.similarity+"\n");
				for(Action action : convertedActions){
					sb.append(convertNodeType(action.toString())+"\n");
				}
				saveToFile("changes/"+changeName+"/script_gt.txt", sb.toString());
			}
			StringBuffer sb = new StringBuffer();
			sb.append("Change,Score,Max.Count,Matched Count,Similarity\n");
			for(String changeName : scores.keySet()){
				Score score = scores.get(changeName);
				sb.append(changeName+","+score.score+","+score.maxCount+","+score.count+","+score.similarity+"\n");
			}
			saveToFile("score_gt.csv", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String convertNodeType(String actionString){
		Pattern nodeTypePattern = Pattern.compile("([0-9]+)(@@)");
		java.util.regex.Matcher matcher = nodeTypePattern.matcher(actionString);
		while(matcher.find()){
			int nodeType = Integer.parseInt(matcher.group(1));
			actionString = actionString.replaceAll(nodeType+"@@", ASTNode.nodeClassForType(nodeType).getSimpleName()+"@@");
			matcher = nodeTypePattern.matcher(actionString);
		}
		return actionString;
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

	private static File getFile(String changeName, String dirName) {
		File dir = new File("changes/"+changeName+"/"+dirName);
		for(File f : dir.listFiles()){
			if(f.getName().endsWith(".java"))
				return f;
		}
		return null;
	}

	private static Benchmark readBenchmark(String benchmarkPath) {
		File f = new File(benchmarkPath);
		if(f.exists()){
			FileInputStream fis = null;
			ObjectInputStream in = null;
			try {
				fis = new FileInputStream(f);
				in = new ObjectInputStream(fis);
				Object obj = in.readObject();
				if(obj instanceof Benchmark){
					return (Benchmark)obj;
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
		return null;
	}

}
