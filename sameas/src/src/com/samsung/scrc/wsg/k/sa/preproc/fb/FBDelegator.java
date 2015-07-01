/**
 * FBDelegator.java
 * Handling operations on Freebase RDF Dump configured in fb.properties
 * Main objective:
 * Extract entities with Wikipedia type.object.key and decode key into normal Wikipedia URL 
 */
package com.samsung.scrc.wsg.k.sa.preproc.fb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.util.Tools;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Dec 2, 2014
 * 
 */
public class FBDelegator {
//	private Logger log = LogManager.getLogger(FBDelegator.class.getName());
	public static FBDelegator INSTANCE = new FBDelegator();
	private BufferedReader br = null;
	public static final String FB_TYPE_OBJECT_KEY = "http://rdf.freebase.com/ns/type.object.key";
	public static final String TITLE = "_title/";
	public static final String HTTP = "http://";
	public static final String WIKIPEDIA = "/wikipedia/";
	public static final String WIKPEDIA_URL = ".wikipedia.org/wiki/";

	private FBDelegator() {

	}

	/**
	 * Initialization from fb.properties
	 * 
	 * @return: false - failed in initialization / true - success
	 */
	public boolean init() {
		// read prop file
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			fis = new FileInputStream(GlobalParameters.FILE_PROP_SA);
			props.load(fis);
			String ntFile = props.getProperty(GlobalParameters.PROP_FB_FILE);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					new File(ntFile)), "UTF-8"));
			return true;
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
			return false;
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
	}

	/**
	 * Extract Wikipedia type.object,type
	 * 
	 * @param limit
	 * @return
	 */
	public List<String[]> extractKeys(int limit) {
		List<String[]> keyList = new ArrayList<String[]>();
		String content = null;
		int offset = 0;
		try {
			while (offset < limit && (content = br.readLine()) != null) {
				String[] frags = content.split("\t");
				if (frags[1].equalsIgnoreCase("<" + FB_TYPE_OBJECT_KEY + ">")
						&& frags[2].contains(WIKIPEDIA)
						&& frags[2].contains(TITLE)) {
					String[] item = new String[4];
					// id
					item[0] = frags[0].substring(1, frags[0].length() - 1);
					String[] keys = frags[2].split("/");
					String key = keys[3].substring(0, keys[3].length() - 1);
					// lang
					item[2] = keys[2].substring(0, keys[2].length() - 6);
					// url
					item[1] = HTTP + item[2] + WIKPEDIA_URL + Tools.f2wurl(key);
					// md5
					item[3] = Tools.md52String(item[1]);
					keyList.add(item);
					offset++;
					// log.trace(item[0] + "\t" + item[1] + "\t" + item[2] +
					// "\t"
					// + item[3]);
				}
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
		}
		if (content == null && keyList.isEmpty()) {
			return null;
		}
//		log.trace("Length: " + keyList.size());
		return keyList;
	}

	/**
	 * Close file buffer reader
	 */
	public void close() {
		if (br != null) {
			try {
				br.close();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
//				log.error(this, ioe);
				System.err.println(ioe);
			}
		}
	}
}
