package kr.ac.seoultech.selab.esscore.run;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.Matcher;

import at.aau.softwaredynamics.classifier.AbstractJavaChangeClassifier;
import at.aau.softwaredynamics.classifier.JChangeClassifier;
import at.aau.softwaredynamics.classifier.NonClassifyingClassifier;
import at.aau.softwaredynamics.classifier.entities.FileChangeSummary;
import at.aau.softwaredynamics.classifier.entities.SourceCodeChange;
import at.aau.softwaredynamics.gen.DocIgnoringTreeGenerator;
import at.aau.softwaredynamics.gen.OptimizedJdtTreeGenerator;
import at.aau.softwaredynamics.gen.SpoonTreeGenerator;
import at.aau.softwaredynamics.matchers.JavaMatchers;
import at.aau.softwaredynamics.runner.util.ClassifierFactory;
import kr.ac.seoultech.selab.esscore.model.Benchmark;
import kr.ac.seoultech.selab.esscore.model.Score;
import kr.ac.seoultech.selab.esscore.model.Script;
import kr.ac.seoultech.selab.esscore.util.FileHandler;
import kr.ac.seoultech.selab.esscore.util.IJMScriptConverter;
import kr.ac.seoultech.selab.esscore.util.ScoreCalculator;

public class ComputeIJMScore {

	public static void main(String[] args) {
		String tool = "ijm";
		String postfix = "";
		String benchmarkPath = "benchmark_"+tool+".obj";
		Map<String, Score> scores = new HashMap<>();
		try {
			Benchmark benchmark = FileHandler.readBenchmark(benchmarkPath);
			ScoreCalculator calculator = new ScoreCalculator(benchmark);
			Set<String> changeNames = benchmark.getChangeNames();
			for(String changeName : changeNames){
				System.out.println("Change - "+changeName);
				String oldCode = benchmark.getOldCode(changeName);
				String newCode = benchmark.getNewCode(changeName);
				List<SourceCodeChange> changes = getIJMScript(oldCode, newCode);
				IJMScriptConverter.computePosLineMap(oldCode, newCode);
				Script script = IJMScriptConverter.convert(changes, true, false);
				scores.put(changeName, calculator.getBestMatchScore(changeName, script));
				Score score = scores.get(changeName);
				System.out.println("Score - "+ score.score);
				System.out.println("Matched Count:"+score.count+" (Max:"+score.maxCount+")");
				System.out.println("Similarity:"+score.similarity);
				StringBuffer sb = new StringBuffer();
				sb.append("Script Size:"+changes.size()+"\n");
				sb.append("Score:"+scores.get(changeName).score+"\n");
				sb.append("Matched Count:"+score.count+" (Max:"+score.maxCount+")\n");
				sb.append("Similarity:"+score.similarity+"\n");
				for(SourceCodeChange c : changes) {
					sb.append(c.toStringCompact());
				}
				FileHandler.storeContent("changes/"+changeName+"/script_"+tool+postfix+".txt", sb.toString());
			}
			StringBuffer sb = new StringBuffer();
			sb.append("Change,Score,Max.Count,Matched Count,Similarity\n");
			for(String changeName : scores.keySet()){
				Score score = scores.get(changeName);
				sb.append(changeName+","+score.score+","+score.maxCount+","+score.count+","+score.similarity+"\n");
			}
			FileHandler.storeContent("score_"+tool+postfix+".csv", sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<SourceCodeChange> getIJMScript(String oldCode, String newCode) {
		//Default options from the example: -c None -m IJM -w FS -g OTG
		return getIJMScript(oldCode, newCode, "None", "IJM", "OTG");
	}

	private static List<SourceCodeChange> getIJMScript(String oldCode, String newCode, String optClassifier, String optMatcher, String optGenerator) {
		Class<? extends AbstractJavaChangeClassifier> classifierType = getClassifierType(optClassifier);
		Class<? extends Matcher> matcher = getMatcherTypes(optMatcher);
		TreeGenerator generator = getTreeGenerator(optGenerator);

		ClassifierFactory factory = new ClassifierFactory(classifierType, matcher, generator);
		FileChangeSummary summary = new FileChangeSummary("", "", "Old", "New");

		AbstractJavaChangeClassifier classifier = factory.createClassifier();
		try {
			classifier.classify(oldCode, newCode);
			summary.setChanges(classifier.getCodeChanges());
			summary.setMetrics(classifier.getMetrics());
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		List<at.aau.softwaredynamics.classifier.entities.SourceCodeChange> changes = classifier.getCodeChanges();

		return changes;
	}

	private static Class<? extends Matcher> getMatcherTypes(String option) {
		switch(option) {
		case "GT":
			return CompositeMatchers.ClassicGumtree.class;
		case "IJM":
			return JavaMatchers.IterativeJavaMatcher_V2.class;
		case "IJM_Spoon":
			return JavaMatchers.IterativeJavaMatcher_Spoon.class;
		}

		return null;
	}

	private static Class<? extends AbstractJavaChangeClassifier> getClassifierType(String option) {
		switch (option) {
		case "Java": return JChangeClassifier.class;
		case "None": return NonClassifyingClassifier.class;
		default: return JChangeClassifier.class;
		}
	}

	private static TreeGenerator getTreeGenerator(String option) {
		switch (option)
		{
		case "OTG": return new OptimizedJdtTreeGenerator();
		case "JTG": return new JdtTreeGenerator();
		case "JTG1": return new DocIgnoringTreeGenerator();
		case "SPOON":
			return new SpoonTreeGenerator();
		}
		return null;
	}

}
