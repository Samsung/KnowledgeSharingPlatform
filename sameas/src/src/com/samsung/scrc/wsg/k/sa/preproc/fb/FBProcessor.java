/**
 * FBProcessor.java
 * A Runnable for indexing wikipedia URL related contents from Freebase RDF dump into Lucene
 */
package com.samsung.scrc.wsg.k.sa.preproc.fb;

import java.util.ArrayList;
import java.util.List;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.sa.core.SAWriter;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

public class FBProcessor implements Runnable {
	// logger
//	private static Logger log = LogManager.getLogger(FBProcessor.class
//			.getName());
	public static final int DEFAULT_LINE_NO = 1000000;

	public FBProcessor() {
	}

	@Override
	public void run() {
		// init
		FBDelegator.INSTANCE.init();
		SAWriter fblDelegator = new SAWriter(GlobalParameters.PATH_INDEX_FB);
		// process
		int offset = 0;
		List<String[]> tmpResult = new ArrayList<String[]>();
		while ((tmpResult = FBDelegator.INSTANCE.extractKeys(DEFAULT_LINE_NO)) != null) {
			fblDelegator.insertData(tmpResult);
			offset += DEFAULT_LINE_NO;
//			log.trace("Offset:\t" + offset);
			// ATTENTION!!!
			// break;
		}
		FBDelegator.INSTANCE.close();
		fblDelegator.close();
	}
}
