/**
 * TSVgenerator.java
 */
package com.samsung.scrc.wsg.k.sa.output;

import java.util.HashMap;
import java.util.Map;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Mar 30, 2015
 * 
 */
public class TSVgenerator {
//	private static Logger log = LogManager.getLogger(TSVgenerator.class
//			.getName());
	private Map<String, String> indexTSVMap = new HashMap<String, String>();

	/**
	 * 
	 */
	public TSVgenerator() {
		// TODO Auto-generated constructor stub
		indexTSVMap.put(GlobalParameters.PATH_INDEX_SA_ORIGIN,
				GlobalParameters.FILE_RESULT_ORIGIN);
		indexTSVMap.put(GlobalParameters.PATH_INDEX_SA_MC,
				GlobalParameters.FILE_RESULT_MAX_CONF);
		indexTSVMap.put(GlobalParameters.PATH_INDEX_SA_THRESHOLD,
				GlobalParameters.FILE_RESULT_THRESHOLD);
		indexTSVMap.put(GlobalParameters.PATH_INDEX_SA_ONE_ONLY,
				GlobalParameters.FILE_RESULT_1_1);
		indexTSVMap.put(GlobalParameters.PATH_INDEX_SA_BELIEF,
				GlobalParameters.FILE_RESULT_BELIEF_BASED);
	}

	public void generate() {
//		log.trace("Start to generate TSV files...");
		System.out.println("Start to generate TSV files...");
		for (String index : indexTSVMap.keySet()) {
			Thread thread = new Thread(new ResOutput(index,
					indexTSVMap.get(index)));
			thread.start();
		}
	}

}
