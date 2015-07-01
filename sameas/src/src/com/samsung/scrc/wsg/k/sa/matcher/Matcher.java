/**
 * Matcher.java
 */
package com.samsung.scrc.wsg.k.sa.matcher;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.sa.core.SASearcher;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Mar 15, 2015
 * 
 */
public abstract class Matcher {
//	protected static Logger log = LogManager.getLogger(Matcher.class.getName());
	protected SASearcher freebase;
	protected SASearcher wikidata;

	public void init() {
		freebase = new SASearcher(GlobalParameters.PATH_INDEX_FB);
		wikidata = new SASearcher(GlobalParameters.PATH_INDEX_WD);
	}

	public abstract void match();

	public void close() {
		freebase.close();
		wikidata.close();
	}
}
