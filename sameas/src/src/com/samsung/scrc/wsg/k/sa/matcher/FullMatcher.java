/**
 * Origin.java
 */
package com.samsung.scrc.wsg.k.sa.matcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.samsung.scrc.wsg.k.index.search.SearchIndex;
import com.samsung.scrc.wsg.k.sa.core.SAWriter;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Mar 15, 2015
 * 
 */
public class FullMatcher extends Matcher {

	public FullMatcher() {
		super();
	}

	private void pickUpMappingPairs() {
		SAWriter rawWriter = new SAWriter(GlobalParameters.PATH_INDEX_SA_RAW);
		int iFB = 0;
		int iWD = 0;
		long offsetFB = 0;
		long offsetWD = 0;
		List<String[]> saPairs = new ArrayList<String[]>();
		try {
			String[][] fbItems = freebase.fetchData(true);
//			log.trace("Freebase : " + offsetFB);
			String[][] wdItems = wikidata.fetchData(true);
//			log.trace("Wikidata : " + offsetWD);
			String[] currFBItem = fbItems[iFB];
			String currFBItemMD5 = currFBItem[2];
			String[] currWDItem = wdItems[iWD];
			String currWDItemMD5 = currWDItem[2];
			WHILE_LOOP: while (iFB < fbItems.length && iWD < wdItems.length) {
				int compValue = currFBItemMD5
						.compareToIgnoreCase(currWDItemMD5);
				if (compValue == 0) {
					String[] saPair = new String[7];
					// traceResult(currFBItem, currWDItem);
					saPair[0] = currFBItem[0];
					saPair[1] = currFBItem[1];
					saPair[2] = currFBItem[3];
					saPair[3] = currWDItem[0];
					saPair[4] = currWDItem[1];
					saPair[5] = currWDItem[3];
					saPair[6] = currFBItemMD5;
					saPairs.add(saPair);
					iFB++;
					if (iFB >= fbItems.length) {
						rawWriter.insertSameAsRelations(saPairs);
						saPairs.clear();
						iFB = 0;
						fbItems = freebase.fetchData(false);
						offsetFB += GlobalParameters.BATCH_SIZE_MAX;
//						log.trace("Freebase : " + offsetFB);
						if (fbItems == null || fbItems.length == 0) {
							break WHILE_LOOP;
						}
					}
					currFBItem = fbItems[iFB];
					currFBItemMD5 = currFBItem[2];
				} else if (compValue > 0) {
					iWD++;
					if (iWD >= wdItems.length) {
						rawWriter.insertSameAsRelations(saPairs);
						saPairs.clear();
						iWD = 0;
						wdItems = wikidata.fetchData(false);
						offsetWD += GlobalParameters.BATCH_SIZE_MAX;
//						log.trace("Wikidata : " + offsetWD);
						if (wdItems == null || wdItems.length == 0) {
							break WHILE_LOOP;
						}
					}
					currWDItem = wdItems[iWD];
					currWDItemMD5 = currWDItem[2];
				} else {
					iFB++;
					if (iFB >= fbItems.length) {
						rawWriter.insertSameAsRelations(saPairs);
						saPairs.clear();
						iFB = 0;
						fbItems = freebase.fetchData(false);
						offsetFB += GlobalParameters.BATCH_SIZE_MAX;
//						log.trace("Freebase : " + offsetFB);
						if (fbItems == null || fbItems.length == 0) {
							break WHILE_LOOP;
						}
					}
					currFBItem = fbItems[iFB];
					currFBItemMD5 = currFBItem[2];
				}
			}
			if (saPairs != null && !saPairs.isEmpty()) {
				rawWriter.insertSameAsRelations(saPairs);
			}
		} catch (Exception e) {
//			log.error(this, e);
			System.err.println(e);
		} finally {
			rawWriter.close();
		}
	}

	private void cleansing() {
		SearchIndex rawSearcher = new SearchIndex(
				GlobalParameters.PATH_INDEX_SA_RAW);
		SAWriter saWriter = new SAWriter(GlobalParameters.PATH_INDEX_SA_ORIGIN);
		long count = 0;
		String[] keys = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD };
		String[] sortKeys = { GlobalParameters.COL_ID_FB_SORT };
		String[][] results = rawSearcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, true);
		String currFB = results[0][0];
		Set<String> wds = new HashSet<String>();
		while (results != null) {
			for (int i = 0; i < results.length; i++) {
				String fb = results[i][0];
				String wd = results[i][1];
				if (fb.equalsIgnoreCase(currFB)) {
					wds.add(wd);
				} else {
					// store
					saWriter.insertSameAsRelations(currFB, wds);
					count += wds.size();
					// log.info("current:\t" + count);
					currFB = fb;
					wds.clear();
					wds.add(wd);
				}
			}
//			log.trace("current:\t" + count);
			results = rawSearcher.sortPagSearch(keys, sortKeys,
					GlobalParameters.BATCH_SIZE_MAX, false);
		}
		saWriter.insertSameAsRelations(currFB, wds);
		count += wds.size();
//		log.trace("Ultimate SA pairs count:\t" + count);
		System.out.println("Ultimate SA pairs count:\t" + count);
		saWriter.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.samsung.scrc.wsg.k.sa.core.Matcher#match()
	 */
	@Override
	public void match() {
		// TODO Auto-generated method stub
		// pick up mapping pairs
		this.pickUpMappingPairs();
		// eliminate duplicated
		this.cleansing();
	}

}
