/**
 * Preprocessor.java
 * Pre-process Freebase and Wikidata into local index
 */
package com.samsung.scrc.wsg.k.sa.preproc;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.sa.preproc.fb.FBProcessor;
import com.samsung.scrc.wsg.k.sa.preproc.wd.WDProcessor;

/**
 * @author yuxie, yanli wang
 * 
 * @date June 16, 2015
 * 
 */
public class Preproc {
//	protected static Logger log = LogManager.getLogger(Preproc.class.getName());

	/**
	 * Index Freebase RDF dump & Wikidata SQL Attention: considering of writing
	 * into a single disk, Freebase processor and Wikidata processor are
	 * sequentially called
	 */
	public static void proc() {
//		log.trace("Start to generate indexing on Freebase...");
		System.out.println("Start to generate indexing on Freebase...");
		FBProcessor fbProcessor = new FBProcessor();
		fbProcessor.run();
//		log.trace("Finish indexing on Freebase!");
		System.out.println("Finish indexing on Freebase!");
//		log.trace("Start to generate indexing on Wikidata...");
		System.out.println("Start to generate indexing on Wikidata...");
		WDProcessor wdProcessor = new WDProcessor();
		wdProcessor.run();
//		log.trace("Finish indexing on Wikidata!");
		System.out.println("Finish indexing on Wikidata!");
	}
	
	public static void main(String[] args) {
//		log.info("Start preprocess...");
		System.out.println("Start preprocess...");
		proc();
//		log.info("End preprocess...");
		System.out.println("End preprocess...");
	}
}
