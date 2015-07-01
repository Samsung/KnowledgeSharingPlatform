/**
 * One2OneMatcher.java
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
 * @date Mar 30, 2015
 * 
 */
public class One2OneMatcher extends Matcher {

	private void coreFB() {
		SASearcher searcher = new SASearcher(
				GlobalParameters.PATH_INDEX_SA_THRESHOLD);
		SAWriter writer = new SAWriter(
				GlobalParameters.PATH_INDEX_SA_ONE_ONLY_RAW);
		String[][] pairs = searcher.fetchPairsFBSorted(true);
		List<String[]> items = new ArrayList<String[]>();
		String lastFB = null;
		String lastWD = null;
		int currCount = 0;
		while (pairs != null && pairs.length != 0) {
			for (int i = 0; i < pairs.length; i++) {
				if (pairs[i][0].equalsIgnoreCase(lastFB)) {
					currCount++;
				} else {
					if (currCount == 1) {
						String[] currPair = { lastFB, lastWD };
						items.add(currPair);
					}
					lastFB = pairs[i][0];
					lastWD = pairs[i][1];
					currCount = 1;
				}
			}
			writer.insertSAPairs(items);
			items.clear();
			pairs = searcher.fetchPairsFBSorted(false);
		}
		if (currCount == 1) {
			String[] currPair = { lastFB, lastWD };
			items.add(currPair);
		}
		writer.insertSAPairs(items);
		searcher.close();
		writer.close();
	}

	private void coreWD() {
		SASearcher searcher = new SASearcher(
				GlobalParameters.PATH_INDEX_SA_ONE_ONLY_RAW);
		SAWriter writer = new SAWriter(GlobalParameters.PATH_INDEX_SA_ONE_ONLY);
		String[][] pairs = searcher.fetchPairsWDSorted(true);
		List<String[]> items = new ArrayList<String[]>();

		String lastFB = null;
		String lastWD = null;
		int currCount = 0;
		while (pairs != null && pairs.length != 0) {
			for (int i = 0; i < pairs.length; i++) {
				if (pairs[i][1].equalsIgnoreCase(lastWD)) {
					currCount++;
				} else {
					if (currCount == 1) {
						String[] currPair = { lastFB, lastWD };
						items.add(currPair);
					}
					lastFB = pairs[i][0];
					lastWD = pairs[i][1];
					currCount = 1;
				}
			}
			writer.insertSAPairs(items);
			items.clear();
			pairs = searcher.fetchPairsWDSorted(false);
		}
		if (currCount == 1) {
			String[] currPair = { lastFB, lastWD };
			items.add(currPair);
		}
		writer.insertSAPairs(items);
		searcher.close();
		writer.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.samsung.scrc.wsg.k.sa.matcher.Matcher#match()
	 */
	@Override
	public void match() {
		// TODO Auto-generated method stub
		coreFB();
		coreWD();
	}

}
