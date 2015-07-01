/**
 * WDProcessor.java
 * A Runnable for indexing Wikipedia URLs from Wikidata SQL database into Lucene
 */
package com.samsung.scrc.wsg.k.sa.preproc.wd;

import java.util.ArrayList;
import java.util.List;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.sa.core.SAWriter;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Dec 1, 2014
 * 
 */
public class WDProcessor implements Runnable {
//	private static Logger log = LogManager.getLogger(WDProcessor.class
//			.getName());
	public static int DEFAULT_LIMIT = 500000;

	public WDProcessor() {
	}

	@Override
	public void run() {
		// init
		WDDelegator.INSTANCE.init();
		SAWriter wdlDelegator = new SAWriter(GlobalParameters.PATH_INDEX_WD);
		// process
		List<String[]> tmpResult = new ArrayList<String[]>();
		int offset = 0;
		while (!(tmpResult = WDDelegator.INSTANCE.getItemWikiURL(DEFAULT_LIMIT,
				offset)).isEmpty()) {
			wdlDelegator.insertData(tmpResult);
			offset += DEFAULT_LIMIT;
//			log.trace("Offset:\t" + offset);
		}
		WDDelegator.INSTANCE.close();
		wdlDelegator.close();
	}
}
