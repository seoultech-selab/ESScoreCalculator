package model;

public class Score {
	public double score;
	public Script script;
	public int count;
	public int maxCount;
	public int totalCount;
	public double similarity;

	public Score(double score, Script script, int count, double similarity) {
		super();
		this.score = score;
		this.script = script;
		this.count = count;
		this.similarity = similarity;
		this.maxCount = 0;
		this.totalCount = 0;
	}
}
