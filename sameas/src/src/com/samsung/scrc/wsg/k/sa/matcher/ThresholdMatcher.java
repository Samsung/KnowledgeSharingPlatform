/**
 * ThresholdMatcher.java
 */
package com.samsung.scrc.wsg.k.sa.matcher;

import java.util.ArrayList;
import java.util.List;

import com.samsung.scrc.wsg.k.sa.core.SASearcher;
import com.samsung.scrc.wsg.k.sa.core.SAWriter;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Mar 25, 2015
 * 
 */
public class ThresholdMatcher extends MaxConfMatcher {
	private double threshold = 0.5; // by default

	private void preproc() {
		// preprocess
		SASearcher saStat = new SASearcher(GlobalParameters.PATH_INDEX_STAT_SA);
		SAWriter rawWriter = new SAWriter(
				GlobalParameters.PATH_INDEX_SA_THRESHOLD_RAW);
		String[][] statSAPairs = saStat.fetchStatSAPairs(true);
		List<String[]> items = new ArrayList<String[]>();
		while (statSAPairs != null && statSAPairs.length != 0) {
			for (int i = 0; i < statSAPairs.length; i++) {
				double fbRatio = Double.valueOf(statSAPairs[i][2])
						/ Double.valueOf(statSAPairs[i][3]);
				double wdRatio = Double.valueOf(statSAPairs[i][2])
						/ Double.valueOf(statSAPairs[i][4]);
				if (fbRatio > threshold && wdRatio > threshold) {
					items.add(statSAPairs[i]);
				}
			}
			rawWriter.insertSAStat(items);
			items.clear();
			statSAPairs = saStat.fetchStatSAPairs(false);
		}
		rawWriter.close();
		saStat.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.samsung.scrc.wsg.k.sa.matcher.Matcher#match()
	 */
	@Override
	public void match() {
		// TODO Auto-generated method stub
		preproc();
		coreFB(GlobalParameters.PATH_INDEX_SA_THRESHOLD_RAW,
				GlobalParameters.PATH_INDEX_SA_THRESHOLD_RAW_1);
		coreWD(GlobalParameters.PATH_INDEX_SA_THRESHOLD_RAW_1,
				GlobalParameters.PATH_INDEX_SA_THRESHOLD);
	}
}
