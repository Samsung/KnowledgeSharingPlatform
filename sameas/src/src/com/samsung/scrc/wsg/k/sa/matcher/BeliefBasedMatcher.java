/**
 * BeliefBasedMatcher.java
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
public class BeliefBasedMatcher extends Matcher {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.samsung.scrc.wsg.k.sa.matcher.Matcher#match()
	 */
	@Override
	public void match() {
		// TODO Auto-generated method stub
		SASearcher searcher = new SASearcher(
				GlobalParameters.PATH_INDEX_STAT_SA);
		SAWriter writer = new SAWriter(GlobalParameters.PATH_INDEX_SA_BELIEF);
		String[][] pairs = searcher.fetchStatSAPairs(true);
		List<String[]> matchPairs = new ArrayList<String[]>();
		while (pairs != null && pairs.length != 0) {
			for (int i = 0; i < pairs.length; i++) {
				if (Integer.valueOf(pairs[i][2]) >= Integer
						.valueOf(pairs[i][4]) - Integer.valueOf(pairs[i][2])
						&& Integer.valueOf(pairs[i][2]) >= Integer
								.valueOf(pairs[i][3])
								- Integer.valueOf(pairs[i][2])) {
					String[] mp = { pairs[i][0], pairs[i][1] };
					matchPairs.add(mp);
				}
			}
			writer.insertSAPairs(matchPairs);
			matchPairs.clear();
			pairs = searcher.fetchStatSAPairs(false);
		}
		searcher.close();
		writer.close();
	}

}
