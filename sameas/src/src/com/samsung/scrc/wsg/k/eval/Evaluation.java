/**
 * Evaluation.java
 * It is to evaluate the elapsed time of each sameAs approach
 */
package com.samsung.scrc.wsg.k.eval;


import com.samsung.scrc.wsg.k.sa.matcher.BeliefBasedMatcher;
import com.samsung.scrc.wsg.k.sa.matcher.FullMatcher;
import com.samsung.scrc.wsg.k.sa.matcher.MaxConfMatcher;
import com.samsung.scrc.wsg.k.sa.matcher.One2OneMatcher;
import com.samsung.scrc.wsg.k.sa.matcher.ThresholdMatcher;
import com.samsung.scrc.wsg.k.sa.stat.Stat;

/**
 * @author yuxie
 * 
 * @date Apr 6, 2015
 * 
 */
public class Evaluation {

	/**
	 * Evaluate pre-processing (<Entity, Language Count>)
	 */
	public void evalPreprocess() {
		long startTime = System.currentTimeMillis();
		System.out.println("Preprocess Evaluation starts at: "+startTime);
		Stat.statLang();
		long endTime = System.currentTimeMillis();
		System.out.println("Preprocess Evaluation finishes at: "+endTime);
		System.out.println("Preprocess Elapsed Time: "+(endTime - startTime) / 1000.0+"s.");
	}

	/**
	 * Evaluate full matcher approach
	 */
	public void evalFullMatcher() {
		long startTime = System.currentTimeMillis();
		System.out.println("Full Matcher Evaluation starts at: "+startTime);
		FullMatcher matcher = new FullMatcher();
		matcher.init();
		matcher.match();
		matcher.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Full Matcher Evaluation finishes at: "+endTime);
		
		System.out.println("Full Matcher Elapsed Time: " + (endTime - startTime)
				/ 1000.0 + "s.");
	}

	/**
	 * Evaluate max confidence approach
	 */
	public void evalMaxConfMatcher() {
		long startTime = System.currentTimeMillis();
		System.out.println("Max Confidence Matcher Evaluation starts at: " + startTime);
		Stat.statSA();
		long interTime = System.currentTimeMillis();
		System.out.println("Max Confidence Matcher Evaluation intermediates at: "
				+ interTime);
		MaxConfMatcher matcher = new MaxConfMatcher();
		matcher.init();
		matcher.match();
		matcher.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Max Confidence Evaluation finishes at: " + endTime);
		System.out.println("Max Confidence Elapsed Time: " + (endTime - startTime)
				/ 1000.0 + "s.");
	}

	/**
	 * Evaluate threshold filtering approach
	 */
	public void evalThresholdMatcher() {
		long startTime = System.currentTimeMillis();
		System.out.println("Threshold Matcher Evaluation starts at:\t" + startTime);
		ThresholdMatcher matcher = new ThresholdMatcher();
		matcher.init();
		matcher.match();
		matcher.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Threshold Evaluation finishes at:\t" + endTime);
		System.out.println("Threshold Elapsed Time:\t" + (endTime - startTime) / 1000.0
				+ "s.");
	}

	/**
	 * Evaluate one-to-one mapping approach
	 */
	public void evalOne2OneMatcher() {
		long startTime = System.currentTimeMillis();
		System.out.println("1-1 Only Matcher Evaluation starts at:\t" + startTime);
		One2OneMatcher matcher = new One2OneMatcher();
		matcher.init();
		matcher.match();
		matcher.close();
		long endTime = System.currentTimeMillis();
		System.out.println("1-1 Only Evaluation finishes at:\t" + endTime);
		System.out.println("1-1 Only Elapsed Time:\t" + (endTime - startTime) / 1000.0
				+ "s.");
	}

	/**
	 * Evaluate belief-base approach
	 */
	public void evalBeliefBasedMatcher() {
		long startTime = System.currentTimeMillis();
		System.out.println("Belief-based Evaluation starts at:\t" + startTime);
		BeliefBasedMatcher matcher = new BeliefBasedMatcher();
		matcher.init();
		matcher.match();
		matcher.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Belief-based Evaluation finishes at:\t" + endTime);
		System.out.println("Belief-based Elapsed Time:\t" + (endTime - startTime)
				/ 1000.0 + "s.");
	}

	/**
	 * Control whole evaluation process
	 */
	public void eval() {
		evalPreprocess();
		evalFullMatcher();
		evalMaxConfMatcher();
		evalThresholdMatcher();
		evalOne2OneMatcher();
		evalBeliefBasedMatcher();
	}
}
