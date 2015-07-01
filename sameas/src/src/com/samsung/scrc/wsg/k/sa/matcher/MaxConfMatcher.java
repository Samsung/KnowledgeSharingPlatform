/**
 * MaxConfMatcher.java
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
 * @date Mar 19, 2015
 * 
 */
public class MaxConfMatcher extends Matcher {

	protected void coreFB(String searchIndex, String writeIndex) {
		SASearcher saSearcher = new SASearcher(searchIndex);
		SAWriter mcrWriter = new SAWriter(writeIndex);
		List<String[]> mcrPairs = new ArrayList<String[]>();
		String[][] pairLangs = saSearcher.fetchPairLangFB(true);
		if (pairLangs != null && pairLangs.length != 0) {
			int i = 0;
			String currFB = pairLangs[i][0];
			List<String> currWD = new ArrayList<String>();
			currWD.add(pairLangs[i][1]);
			int currMax = Integer.valueOf(pairLangs[i][2]);
			i++;
			while (pairLangs != null && pairLangs.length != 0) {
				while (i < pairLangs.length) {
					if (pairLangs[i][0].equalsIgnoreCase(currFB)) {
						if (Integer.valueOf(pairLangs[i][2]) > currMax) {
							currMax = Integer.valueOf(pairLangs[i][2]);
							currWD.clear();
							currWD.add(pairLangs[i][1]);
						} else if (Integer.valueOf(pairLangs[i][2]) == currMax) {
							currWD.add(pairLangs[i][1]);
						}
					} else {
						for (String wd : currWD) {
							String[] mcrPair = { currFB, wd,
									String.valueOf(currMax) };
							mcrPairs.add(mcrPair);
						}
						currFB = pairLangs[i][0];
						currWD.clear();
						currWD.add(pairLangs[i][1]);
						currMax = Integer.valueOf(pairLangs[i][2]);
					}
					i++;
				}
				mcrWriter.insertMCRaw(mcrPairs);
				mcrPairs.clear();
				pairLangs = saSearcher.fetchPairLangFB(false);
				i = 0;
			}
			for (String wd : currWD) {
				String[] mcrPair = { currFB, wd, String.valueOf(currMax) };
				mcrPairs.add(mcrPair);
			}
			mcrWriter.insertMCRaw(mcrPairs);
		}
		saSearcher.close();
		mcrWriter.close();
	}

	protected void coreWD(String searchIndex, String writeIndex) {
		SASearcher saSearcher = new SASearcher(searchIndex);
		SAWriter mcrWriter = new SAWriter(writeIndex);
		List<String[]> mcrPairs = new ArrayList<String[]>();

		String[][] pairLangs = saSearcher.fetchPairLangWD(true);
		if (pairLangs != null && pairLangs.length != 0) {
			int i = 0;
			String currWD = pairLangs[i][0];
			List<String> currFB = new ArrayList<String>();
			currFB.add(pairLangs[i][1]);
			int currMax = Integer.valueOf(pairLangs[i][2]);
			i++;
			while (pairLangs != null && pairLangs.length != 0) {
				while (i < pairLangs.length) {
					if (pairLangs[i][0].equalsIgnoreCase(currWD)) {
						if (Integer.valueOf(pairLangs[i][2]) > currMax) {
							currMax = Integer.valueOf(pairLangs[i][2]);
							currFB.clear();
							currFB.add(pairLangs[i][1]);
						} else if (Integer.valueOf(pairLangs[i][2]) == currMax) {
							currFB.add(pairLangs[i][1]);
						}
					} else {
						for (String fb : currFB) {
							String[] mcrPair = { fb, currWD,
									String.valueOf(currMax) };
							mcrPairs.add(mcrPair);
						}
						currWD = pairLangs[i][0];
						currFB.clear();
						currFB.add(pairLangs[i][1]);
						currMax = Integer.valueOf(pairLangs[i][2]);
					}
					i++;
				}
				mcrWriter.insertMCRaw(mcrPairs);
				mcrPairs.clear();
				pairLangs = saSearcher.fetchPairLangWD(false);
				i = 0;
			}
			for (String fb : currFB) {
				String[] mcrPair = { fb, currWD, String.valueOf(currMax) };
				mcrPairs.add(mcrPair);
			}
			mcrWriter.insertMCRaw(mcrPairs);
		}
		saSearcher.close();
		mcrWriter.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.samsung.scrc.wsg.k.sa.core.Matcher#match()
	 */
	@Override
	public void match() {
		// TODO Auto-generated method stub
		coreFB(GlobalParameters.PATH_INDEX_STAT_SA,
				GlobalParameters.PATH_INDEX_SA_MC_RAW);
		coreWD(GlobalParameters.PATH_INDEX_SA_MC_RAW,
				GlobalParameters.PATH_INDEX_SA_MC);
	}
}
