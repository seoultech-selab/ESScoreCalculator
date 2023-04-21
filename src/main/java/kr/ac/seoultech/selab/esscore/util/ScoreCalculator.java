package kr.ac.seoultech.selab.esscore.util;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import kr.ac.seoultech.selab.esscore.model.Benchmark;
import kr.ac.seoultech.selab.esscore.model.ESNodeEdit;
import kr.ac.seoultech.selab.esscore.model.Score;
import kr.ac.seoultech.selab.esscore.model.Script;

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
		int count = benchmark.count(changeName, script);
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

	public Score getDominantMatchScore(String changeName, Script script, double penalty){
		Score score = null;
		//Use similarity if the given script is matched with the dominant, or there is no dominant.
		//Otherwise, use (1-penalty) * similarity.
		int totalCount = benchmark.totalCount(changeName);
		int count = benchmark.count(changeName, script);
		int maxCount = benchmark.maxCount(changeName);
		double penalizeRatio = (1.0d-penalty);
		Script dominant = benchmark.getDominantScript(changeName);
		if(count > 0){
			if(script.equals(dominant)) {
				score = new Score(1.0, dominant, count, 1.0);
			} else {
				Script s = benchmark.find(changeName, script);
				score = new Score(dominant == null ? 1.0 : penalizeRatio, s, count, 1.0);
			}
		}else{
			Multiset<Script> scripts = benchmark.getScripts(changeName);
			double highestScore = 0.0d;
			score = new Score(highestScore, null, 0, 0);
			Script bestMatch = findBestMatch(changeName, script);
			double similarity = computeSimilarity(bestMatch, script);
			count = scripts.count(bestMatch);
			if(dominant == null || bestMatch == dominant)
				score.score = similarity;
			else
				score.score = similarity*penalizeRatio;
			score.script = bestMatch;
			score.count = count;
			score.similarity = similarity;
		}
		score.maxCount = maxCount;
		score.totalCount = totalCount;
		return score;
	}

	public Score getBestMatchScore(String changeName, Script script){
		Score score = null;
		//If there is a match for a given script, use its vote as score.
		//Otherwise, find most similar script and use its vote * similarity as score.
		int totalCount = benchmark.totalCount(changeName);
		int count = benchmark.count(changeName, script);
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
		for(Script s : scripts){
			double similarity = computeSimilarity(s, script);
			if(bestMatch == null || similarity > highestSimilarity){
				bestMatch = s;
				highestSimilarity = similarity;
			}
		}
		return bestMatch;
	}

	public double computeSimilarity(Script script1, Script script2) {
		Multiset<ESNodeEdit> edit1 = HashMultiset.create(script1.editOps);
		Multiset<ESNodeEdit> edit2 = HashMultiset.create(script2.editOps);
		Multiset<ESNodeEdit> intersection = Multisets.intersection(edit1, edit2);
		int sum = edit1.size() + edit2.size();

		return (double)2*intersection.size()/sum;
	}

	public Benchmark getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(Benchmark benchmark) {
		this.benchmark = benchmark;
	}

}
