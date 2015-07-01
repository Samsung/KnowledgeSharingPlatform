/**
 * Stator.java
 * This class is used to make some statistics information about Freebase, Wikidata & sameAs pairs information
 */
package com.samsung.scrc.wsg.k.sa.stat;

import java.util.ArrayList;
import java.util.List;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.sa.core.SASearcher;
import com.samsung.scrc.wsg.k.sa.core.SAWriter;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Mar 19, 2015
 * 
 */
public class Stat {
//	private static Logger log = LogManager.getLogger(Stat.class.getName());

	public Stat() {

	}

	/**
	 * Count <Entity, Available Language Count> into a Lucene indexing directory
	 * for follow-up matchers For example, the Freebase entity.
	 * http://rdf.freebase.com/ns/m.01k9rg has 3-language Wikipedia links, noted
	 * as <http://rdf.freebase.com/ns/m.01k9rg, 3>
	 * 
	 * @param indexWriter
	 * @param indexReader
	 */
	private static void statEntityLang(String indexWriter, String indexReader) {
		SAWriter saWriter = new SAWriter(indexWriter);
		SASearcher saSearcher = new SASearcher(indexReader);
		List<String[]> entityCounts = saSearcher.fetchEntityCount(true);
		String lastEntity = entityCounts.get(0)[0];
		int lastCount = 0;
		boolean removeFirst = false;
		while (entityCounts != null && !entityCounts.isEmpty()) {
			String[] adjustEntityCount = new String[2];
			adjustEntityCount[0] = lastEntity;
			if (!entityCounts.get(0)[0].equalsIgnoreCase(lastEntity)) {
				adjustEntityCount[1] = String.valueOf(lastCount);
				removeFirst = false;
			} else {
				adjustEntityCount[1] = String.valueOf(lastCount
						+ Integer.valueOf(entityCounts.get(0)[1]));
				removeFirst = true;
			}
			lastEntity = entityCounts.get(entityCounts.size() - 1)[0];
			lastCount = Integer
					.valueOf(entityCounts.get(entityCounts.size() - 1)[1]);
			entityCounts.remove(entityCounts.get(entityCounts.size() - 1));
			if (removeFirst) {
				entityCounts.remove(0);
			}
			entityCounts.add(adjustEntityCount);
			saWriter.insertLangStat(entityCounts);
			entityCounts = saSearcher.fetchEntityCount(false);
		}
		String[] remainEntityCount = { lastEntity, String.valueOf(lastCount) };
		entityCounts.add(remainEntityCount);
		saWriter.insertLangStat(entityCounts);
		saWriter.close();
		saSearcher.close();
	}

	/**
	 * The STAT_SA indexing doc format is: ID_FB, ID_FB_SORT, ID_WD, ID_WD_SORT,
	 * LANG_COUNT, LANG_COUNT_FB, LANG_COUNT_WD
	 */
	public static void statSA() {
		SASearcher fbStat = new SASearcher(GlobalParameters.PATH_INDEX_STAT_FB);
		SASearcher wdStat = new SASearcher(GlobalParameters.PATH_INDEX_STAT_WD);
		SASearcher searcher = new SASearcher(GlobalParameters.PATH_INDEX_SA_RAW);
		SAWriter statWriter = new SAWriter(GlobalParameters.PATH_INDEX_STAT_SA);
		List<String[]> pairCounts = searcher.fetchPairsCount(true);
		List<String[]> pairCountFulls = new ArrayList<String[]>();
		String lastFB = pairCounts.get(0)[0];
		String lastWD = pairCounts.get(0)[1];
		int lastCount = 0;
		boolean removeFirst = false;
		while (pairCounts != null && !pairCounts.isEmpty()) {
			String[] adjustPC = new String[3];
			adjustPC[0] = lastFB;
			adjustPC[1] = lastWD;
			if (pairCounts.get(0)[0].equalsIgnoreCase(lastFB)

			&& pairCounts.get(0)[1].equalsIgnoreCase(lastWD)) {
				adjustPC[2] = String.valueOf(lastCount + pairCounts.get(0)[2]);
				removeFirst = true;
			} else {
				adjustPC[2] = String.valueOf(lastCount);
				removeFirst = false;
			}
			lastFB = pairCounts.get(pairCounts.size() - 1)[0];
			lastWD = pairCounts.get(pairCounts.size() - 1)[1];
			lastCount = Integer
					.valueOf(pairCounts.get(pairCounts.size() - 1)[2]);
			pairCounts.remove(pairCounts.get(pairCounts.size() - 1));
			if (removeFirst) {
				pairCounts.remove(0);
			}
			pairCounts.add(adjustPC);

			String currFB = null;
			String currWD = null;
			String currFBCount = null;
			String currWDCount = null;
			for (String[] pairCount : pairCounts) {
				String[] pairCountFull = new String[pairCount.length + 2];
				int i = 0;
				while (i < pairCount.length) {
					pairCountFull[i] = pairCount[i];
					i++;
				}
				if (!pairCountFull[0].equalsIgnoreCase(currFB)) {
					currFB = pairCount[0];
					currFBCount = fbStat.fetchLangCount(currFB);
				}
				if (!pairCountFull[1].equalsIgnoreCase(currWD)) {
					currWD = pairCount[1];
					currWDCount = wdStat.fetchLangCount(currWD);
				}
				pairCountFull[i] = currFBCount;
				pairCountFull[++i] = currWDCount;
				pairCountFulls.add(pairCountFull);
			}
			statWriter.insertSAStat(pairCountFulls);
			pairCountFulls.clear();
			pairCounts = searcher.fetchPairsCount(false);
		}
		// add remain
		String[] remainPair = { lastFB, lastWD, String.valueOf(lastCount),
				fbStat.fetchLangCount(lastFB), wdStat.fetchLangCount(lastWD) };
		pairCountFulls.add(remainPair);
		statWriter.insertSAStat(pairCountFulls);
		// close all
		searcher.close();
		statWriter.close();
		fbStat.close();
		wdStat.close();
	}

	/**
	 * Indexing Freebase & Wikidata <entity, langue count>
	 */
	public static void statLang() {
		// freebase
		statEntityLang(GlobalParameters.PATH_INDEX_STAT_FB,
				GlobalParameters.PATH_INDEX_FB);
		// wikidata
		statEntityLang(GlobalParameters.PATH_INDEX_STAT_WD,
				GlobalParameters.PATH_INDEX_WD);
	}

}
