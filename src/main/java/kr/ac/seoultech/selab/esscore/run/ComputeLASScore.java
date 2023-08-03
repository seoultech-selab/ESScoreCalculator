package kr.ac.seoultech.selab.esscore.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kr.ac.seoultech.selab.esscore.model.Benchmark;
import kr.ac.seoultech.selab.esscore.model.Score;
import kr.ac.seoultech.selab.esscore.model.Script;
import kr.ac.seoultech.selab.esscore.util.LASScriptConverter;
import kr.ac.seoultech.selab.esscore.util.ScoreCalculator;
import script.ScriptGenerator;
import script.model.EditOp;
import script.model.EditScript;
import tree.Tree;
import tree.TreeBuilder;

public class ComputeLASScore {

	public static void main(String[] args) {
		System.setProperty("las.enable.exact", "true");
		System.setProperty("las.enable.gumtree.ast", "false");
		System.setProperty("las.dist.threshold", "0.5");
		System.setProperty("las.depth.threshold", "3");
		System.setProperty("las.sim.threshold", "0.65");
		String postfix = "";
		String benchmarkPath = "benchmark.obj";
		Map<String, Score> scores = new HashMap<>();
		try {
			Benchmark benchmark = readBenchmark(benchmarkPath);
			ScoreCalculator calculator = new ScoreCalculator(benchmark);
			Set<String> changeNames = benchmark.getChangeNames();
			for(String changeName : changeNames){
				System.out.println("Change - "+changeName);
				String oldCode = benchmark.getOldCode(changeName);
				String newCode = benchmark.getNewCode(changeName);
				Tree oldTree = TreeBuilder.buildTreeFromSource(oldCode);
				Tree newTree = TreeBuilder.buildTreeFromSource(newCode);
				EditScript lasScript = ScriptGenerator.generateScript(oldTree, newTree);
				int size = 0;
				for(EditOp op : lasScript.getEditOps()){
					size += op.size();
				}
				Script script = LASScriptConverter.convert(lasScript, true);
				System.out.println(lasScript);
				scores.put(changeName, calculator.getBestMatchScore(changeName, script));
				Score score = scores.get(changeName);
				System.out.println("Score - "+ score.score);
				System.out.println("Matched Count:"+score.count+" (Max:"+score.maxCount+")");
				System.out.println("Similarity:"+score.similarity);
				String content = "Script Size:"+size+"\n";
				content += "Score:"+scores.get(changeName).score+"\n";
				content += "Matched Count:"+score.count+" (Max:"+score.maxCount+")\n";
				content += "Similarity:"+score.similarity+"\n";
				content += lasScript.toString();
				saveToFile("changes/"+changeName+"/script_las"+postfix+".txt", content);
			}
			StringBuffer sb = new StringBuffer();
			sb.append("Change,Score,Max.Count,Matched Count,Similarity\n");
			for(String changeName : scores.keySet()){
				Score score = scores.get(changeName);
				sb.append(changeName+","+score.score+","+score.maxCount+","+score.count+","+score.similarity+"\n");
			}
			saveToFile("score_las"+postfix+".csv", sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
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
