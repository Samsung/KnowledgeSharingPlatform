/**
 * SAEngine.java
 * SA Engine is dedicated to extracting sameAs pairs based on a set of approaches, including pre-processing, matching, outputing
 */
package com.samsung.scrc.wsg.k.sa.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.sa.matcher.FullMatcher;
import com.samsung.scrc.wsg.k.sa.matcher.Matcher;
import com.samsung.scrc.wsg.k.sa.output.TSVgenerator;
import com.samsung.scrc.wsg.k.sa.preproc.Preproc;
import com.samsung.scrc.wsg.k.sa.stat.Stat;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Mar 16, 2015
 * 
 */
public class SAEngine {
//	private static Logger log = LogManager.getLogger(SAWriter.class.getName());
	private List<Matcher> matchers = new ArrayList<Matcher>();

	public SAEngine() {

	}

	/**
	 * 
	 * @return false: failed in initialization / true: success
	 */
	private boolean init() {
		boolean init = true;
//		log.info("Configure matchers ....");
		System.out.println("Configure matchers ....");
		// read prop file
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			fis = new FileInputStream(GlobalParameters.FILE_PROP_SA);
			props.load(fis);
			String matcherString = props
					
					.getProperty(GlobalParameters.PROP_SA_MATCHER);
			if (matcherString == null || matcherString.equals("")) {
				matchers = GlobalParameters.MATCHER_LIST;
			} else {
				String[] matcherSplits = matcherString.split(",");
				for (String matcherSplit : matcherSplits) {
					Matcher m = GlobalParameters.MATCHER_MAPPING
							.get(matcherSplit.trim());
					if (m != null) {
						matchers.add(m);
					}
				}
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
			init = false;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException ioe) {
					// TODO Auto-generated catch block
//					log.warn(this, ioe);
					System.err.println(ioe);
				}
			}
		}
		return init;
	}

	/**
	 * Engine Run EntryPoint
	 */
	public void run() {
		this.init();
//		log.info("Start to run SameAs Extraction ....");
		System.out.println("Start to run SameAs Extraction ....");
		// indexing (extracting Wikidata Link from Freebase RDF dump & Wikidata
		// SQL database)
		Preproc.proc();
		// engine core
		// 1- statistics (count entities according to language)
		Stat.statLang();
		// 2- full matching
		FullMatcher fMatcher = new FullMatcher();
		fMatcher.init();
		fMatcher.match();
		fMatcher.close();
		// 3 - statistics (count all)
		Stat.statSA();
		// for specific matcher approaches
		// 4 - matcher
		for (Matcher matcher : matchers) {
			matcher.init();
			matcher.match();
			matcher.close();
		}
		// 5- generate tsv result files
		TSVgenerator generator = new TSVgenerator();
		generator.generate();
//		log.info("SameAs Extraction is finished!");
		System.out.println("SameAs Extraction is finished!");
	}
}
