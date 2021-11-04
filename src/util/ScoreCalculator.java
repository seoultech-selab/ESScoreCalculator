package util;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import model.Benchmark;
import model.NodeEdit;
import model.Score;
import model.Script;

public class ScoreCalculator {

	private Benchmark benchmark;

	public ScoreCalculator(Benchmark benchmark) {
		super();
		this.benchmark = benchmark;
	}

	public Score getScore(String changeName, Script script){
		Score score = null;
		//If there is a match for a given script, use its vote as score.
		//Otherwise, find highest score script.
		int totalCount = benchmark.totalCount(changeName);
		int count = benchmark.find(changeName, script);
		int maxCount = benchmark.maxCount(changeName);
		if(count > 0){
			score = new Score((double)count/totalCount, script, count, 1.0);
		}else{
			Multiset<Script> scripts = benchmark.getScripts(changeName);
			double highestScore = 0.0d;
			score = new Score(highestScore, null, 0, 0);
			for(Script s : scripts.elementSet()){
				double similarity = computeSimilarity(s, script);
				count = scripts.count(s);
				if(similarity*count >= highestScore){
					highestScore = similarity*count;
					score.score =  (highestScore/totalCount);
					score.script = s;
					score.count = count;
					score.similarity = similarity;
				}
			}
		}
		score.maxCount = maxCount;
		score.totalCount = totalCount;
		score.score = score.score/((double)maxCount/totalCount);
		return score;
	}

	public Score getBestMatchScore(String changeName, Script script){
		Score score = null;
		//If there is a match for a given script, use its vote as score.
		//Otherwise, find most similar script and use its vote * similarity as score.
		int totalCount = benchmark.totalCount(changeName);
		int count = benchmark.find(changeName, script);
		int maxCount = benchmark.maxCount(changeName);
		if(count > 0){
			score = new Score((double)count/totalCount, script, count, 1.0);
		}else{
			Multiset<Script> scripts = benchmark.getScripts(changeName);
			double highestScore = 0.0d;
			score = new Score(highestScore, null, 0, 0);
			Script bestMatch = findBestMatch(changeName, script);
			double similarity = computeSimilarity(bestMatch, script);
			count = scripts.count(bestMatch);
			score.score =  (similarity*count/totalCount);
			score.script = bestMatch;
			score.count = count;
			score.similarity = similarity;
		}
		score.maxCount = maxCount;
		score.totalCount = totalCount;
		score.score = score.score/((double)maxCount/totalCount);
		return score;
	}

	public Script findBestMatch(String changeName, Script script){
		Multiset<Script> scripts = benchmark.getScripts(changeName);
		Script bestMatch = null;
		double highestSimilarity = 0.0d;
		if(changeName.equals("lucene2")){
			System.out.println(script);
			System.out.println("-------------------------");
		}
		for(Script s : scripts){
			double similarity = computeSimilarity(s, script);
			if(changeName.equals("lucene2")){
				System.out.println(s);
				System.out.println("**************************");
			}
			if(similarity > highestSimilarity){
				bestMatch = s;
				highestSimilarity = similarity;
			}
		}
		return bestMatch;
	}

	public double computeSimilarity(Script script1, Script script2) {
		Multiset<NodeEdit> edit1 = HashMultiset.create(script1.editOps);
		Multiset<NodeEdit> edit2 = HashMultiset.create(script2.editOps);
		Multiset<NodeEdit> intersection = Multisets.intersection(edit1, edit2);
		int sum = edit1.size() + edit2.size();
		System.out.println("Inter:"+intersection.size());

		return (double)2*intersection.size()/sum;
	}

	public Benchmark getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(Benchmark benchmark) {
		this.benchmark = benchmark;
	}

}
