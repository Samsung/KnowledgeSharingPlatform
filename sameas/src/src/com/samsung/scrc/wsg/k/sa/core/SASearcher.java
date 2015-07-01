/**
 * LuceneSearcher.java
 */
package com.samsung.scrc.wsg.k.sa.core;

import java.util.ArrayList;
import java.util.List;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.index.search.SearchIndex;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Mar 18, 2015
 * 
 */

public class SASearcher {
	// logger
//	private static Logger log = LogManager
//			.getLogger(SASearcher.class.getName());
	private SearchIndex searcher;

	public SASearcher(String path) {
		this.searcher = new SearchIndex(path);
	}

	public List<String[]> fetchEntityCount(boolean flag) {
		List<String[]> entityCounts = new ArrayList<String[]>();
		String[] keys = { GlobalParameters.COL_ID, GlobalParameters.COL_LANG };
		String[] sortKeys = { GlobalParameters.COL_ID_SORT };
		String[][] results = searcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, flag);
		if (results != null && results.length != 0) {
			String preEntity = results[0][0];
			int currCount = 0;
			for (int i = 0; i < results.length; i++) {
				if (results[i][0].equalsIgnoreCase(preEntity)) {
					currCount++;
				} else {
					String[] entityCount = { preEntity,
							String.valueOf(currCount) };
					entityCounts.add(entityCount);
					preEntity = results[i][0];
					currCount = 1;
				}
			}
			String[] lastEntityCount = { preEntity, String.valueOf(currCount) };
			entityCounts.add(lastEntityCount);
		}
		return entityCounts;
	}

	public String fetchLangCount(String id) {
		String[] keys = { GlobalParameters.COL_LANG_COUNT };
		String condKey = GlobalParameters.COL_ID;
		String condKeyword = id;
		String[] results = searcher.termSearch(keys, condKey, condKeyword);
		if (results != null) {
			return results[0];
		} else {
//			log.trace("here " + id);
			return null;
		}
	}
	
	public String[][] fetchData(boolean flag) {
		String[] keys = { GlobalParameters.COL_ID, GlobalParameters.COL_URL,
				GlobalParameters.COL_MD5, GlobalParameters.COL_LANG };
		String[] sortKey = { GlobalParameters.COL_MD5_SORT };
		String[][] items = searcher.sortPagSearch(keys, sortKey,
				GlobalParameters.BATCH_SIZE_MAX, flag);
		return items;
	}

	/**
	 * Fetch a SASearcher total count number
	 * 
	 * @return
	 */
	public long fetchTotalCount() {
		return searcher.countHits();
	}

	/**
	 * 
	 * @param flag
	 * @return
	 */
	public String[][] fetchStatSAPairs(boolean flag) {
		String[] keys = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD, GlobalParameters.COL_LANG_COUNT,
				GlobalParameters.COL_LANG_COUNT_FB,
				GlobalParameters.COL_LANG_COUNT_WD };
		return searcher.pagSearch(keys, GlobalParameters.BATCH_SIZE_MAX, flag);
	}

	private long feetchDistinctEntityCount(String colId, String colSortId) {
		long count = 0;
		String[] keys = { colId };
		String[] sortKeys = { colSortId };
		String[][] results = searcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, true);
		String currEntityId = null;
		while (results != null && results.length != 0) {
			for (int i = 0; i < results.length; i++) {
				if (!results[i][0].equalsIgnoreCase(currEntityId)) {
					count++;
					currEntityId = results[i][0];
				}
			}
			results = searcher.sortPagSearch(keys, sortKeys,
					GlobalParameters.BATCH_SIZE_MAX, false);
		}
//		log.debug("Distinct Entity Count:\t" + count);
		return count;
	}

	public long fetchDistinctIdCount() {
		return feetchDistinctEntityCount(GlobalParameters.COL_ID,
				GlobalParameters.COL_ID_SORT);
	}

	public long fetchDistinctFBCount() {
		return feetchDistinctEntityCount(GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_FB_SORT);
	}

	public long fetchDistinctWDCount() {
		return feetchDistinctEntityCount(GlobalParameters.COL_ID_WD,
				GlobalParameters.COL_ID_WD_SORT);
	}

	public String[][] fetchPairs(boolean flag) {
		String[] keys = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD };
		return searcher.pagSearch(keys, GlobalParameters.BATCH_SIZE_MAX, flag);
	}

	public String[][] fetchPairsFBSorted(boolean flag) {
		String[] keys = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD };
		String[] sortKeys = { GlobalParameters.COL_ID_FB_SORT };
		return searcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, flag);
	}

	public String[][] fetchPairsWDSorted(boolean flag) {
		String[] keys = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD };
		String[] sortKeys = { GlobalParameters.COL_ID_WD_SORT };
		return searcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, flag);
	}

	public String[][] fetchSAPairsFullSorted(boolean flag) {
		String[] keys = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD, GlobalParameters.COL_LANG };
		String[] sortKeys = { GlobalParameters.COL_ID_FB_SORT,
				GlobalParameters.COL_ID_WD_SORT };
		return searcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, flag);
	}

	public String[][] fetchPairLangFB(boolean flag) {
		String[] keys = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD, GlobalParameters.COL_LANG_COUNT,
				GlobalParameters.COL_LANG_COUNT_FB,
				GlobalParameters.COL_LANG_COUNT_WD };
		String[] sortKeys = { GlobalParameters.COL_ID_FB_SORT };
		String[][] results = searcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, flag);
		return results;
	}

	public String[][] fetchPairLangWD(boolean flag) {
		String[] keys = { GlobalParameters.COL_ID_WD,
				GlobalParameters.COL_ID_FB, GlobalParameters.COL_LANG_COUNT,
				GlobalParameters.COL_LANG_COUNT_WD,
				GlobalParameters.COL_LANG_COUNT_FB };
		String[] sortKeys = { GlobalParameters.COL_ID_WD_SORT };
		String[][] results = searcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, flag);
		return results;
	}

	public List<String[]> fetchPairsCount(boolean flag) {
		List<String[]> pairCounts = new ArrayList<String[]>();
		String[] keys = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD };
		String[] sortKeys = { GlobalParameters.COL_ID_FB_SORT,
				GlobalParameters.COL_ID_WD_SORT };
		String[][] results = searcher.sortPagSearch(keys, sortKeys,
				GlobalParameters.BATCH_SIZE_MAX, flag);
		if (results != null && results.length != 0) {
			int i = 0;
			String preFB = results[i][0];
			String preWD = results[i][1];
			int pc = 1;
			i++;
			while (i < results.length) {
				if (results[i][0].equalsIgnoreCase(preFB)
						&& results[i][1].equalsIgnoreCase(preWD)) {
					pc++;
				} else {
					String[] pairCount = { preFB, preWD, String.valueOf(pc) };
					pairCounts.add(pairCount);
					preFB = results[i][0];
					preWD = results[i][1];
					pc = 1;
				}
				i++;
			}
			String[] pairCount = { preFB, preWD, String.valueOf(pc) };
			pairCounts.add(pairCount);
		}
		return pairCounts;
	}

	public boolean checkPairExist(String freebase, String wikidata) {
		String[] condKey = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD };
		String[] condKeyword = { freebase, wikidata };
		return searcher.termSearchExist(condKey, condKeyword);
	}

	public String[] fetchSALangAll(String freebase, String wikidata) {
		String[] condKey = { GlobalParameters.COL_ID_FB,
				GlobalParameters.COL_ID_WD };
		String[] condKeyword = { freebase, wikidata };
		String[] keys = { GlobalParameters.COL_LANG_COUNT };
		return searcher.termSearch(keys, condKey, condKeyword);
	}

	public void close() {
		searcher.close();
	}
}
