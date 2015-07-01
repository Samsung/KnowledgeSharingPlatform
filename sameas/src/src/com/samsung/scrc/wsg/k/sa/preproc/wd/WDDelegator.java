/**
 * WDDelegator.java
 */
package com.samsung.scrc.wsg.k.sa.preproc.wd;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.kb.KBFactory;
import com.samsung.scrc.wsg.k.kb.KBHandler;
import com.samsung.scrc.wsg.k.kb.exception.HandlingFailureException;
import com.samsung.scrc.wsg.k.util.Tools;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Nov 28, 2014
 * 
 */
public class WDDelegator {
//	private static Logger log = LogManager.getLogger(WDDelegator.class
//			.getName());
	public static WDDelegator INSTANCE = new WDDelegator();
	private KBHandler db = null;
	public static String TABLE_WD = "wb_items_per_site";
	public static String COL_ROW = "ips_row_id";
	public static String COL_ITEM = "ips_item_id";
	public static String COL_SITE_ID = "ips_site_id";
	public static String COL_SITE_PAGE = "ips_site_page";
	public static int DEFAULT_LIMIT = 100000;

	private WDDelegator() {

	}

	/**
	 * Initialization from fb.properties
	 * 
	 * @return false - failed / true - success
	 */
	public boolean init() {
		// read prop file
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			fis = new FileInputStream(GlobalParameters.FILE_PROP_SA);
			props.load(fis);
			String type = props.getProperty(GlobalParameters.PROP_WD_TYPE);
			String host = props.getProperty(GlobalParameters.PROP_WD_HOST);
			String port = props.getProperty(GlobalParameters.PROP_WD_PORT);
			String user = props.getProperty(GlobalParameters.PROP_WD_USER);
			String pwd = props.getProperty(GlobalParameters.PROP_WD_PWD);
			String database = props.getProperty(GlobalParameters.PROP_WD_DB);
			db = KBFactory.getHandler(type);
			if (db == null) {
				return false;
			}
			return db.init(host, port, user, pwd, database);
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

	public List<String[]> getItemWikiURL(int limit, int offset) {
		List<String[]> resultList = new ArrayList<String[]>();
		String[] fields = { COL_ITEM, COL_SITE_ID, COL_SITE_PAGE };
		List<Map<String, Object>> qResult = null;
		try {
			qResult = db.fetch(TABLE_WD, fields, String.valueOf(limit),
					String.valueOf(offset));
			if (qResult != null && !qResult.isEmpty()) {
				for (Map<String, Object> qMap : qResult) {
					String siteId = new String((byte[]) qMap.get(COL_SITE_ID));
					if (siteId.endsWith("wiki")) {
						String lang = siteId.substring(0,
								siteId.lastIndexOf("wiki"));
						String id = qMap.get(COL_ITEM).toString();
						String sitePage = new String(
								(byte[]) qMap.get(COL_SITE_PAGE)).replace(" ",
								"_");
						String[] pair = new String[4];
						pair[0] = "http://www.wikidata.org/wiki/Q" + id;
						pair[1] = "http://" + lang + ".wikipedia.org/wiki/"
								+ URLEncoder.encode(sitePage, "utf-8");
						pair[2] = lang;
						pair[3] = Tools.md52String(pair[1]);
						resultList.add(pair);
					}
				}
			}
		} catch (HandlingFailureException | UnsupportedEncodingException hfe) {
			// TODO Auto-generated catch block
//			log.error(this, hfe);
			System.err.println(hfe);
		}
		return resultList;
	}

	public List<String[]> getItemWikiMediaURL(int limit, int offset) {
		List<String[]> resultList = new ArrayList<String[]>();
		String[] fields = { COL_ITEM, COL_SITE_ID, COL_SITE_PAGE };
		List<Map<String, Object>> qResult = null;
		try {
			qResult = db.fetch(TABLE_WD, fields, String.valueOf(limit),
					String.valueOf(offset));
			if (qResult != null && !qResult.isEmpty()) {
				for (Map<String, Object> qMap : qResult) {
					String siteId = new String((byte[]) qMap.get(COL_SITE_ID));
					if (!siteId.endsWith("wiki")) {
						// String lang = siteId.substring(0,
						// siteId.lastIndexOf("wikimedia"));
						String id = qMap.get(COL_ITEM).toString();
						String sitePage = new String(
								(byte[]) qMap.get(COL_SITE_PAGE)).replace(" ",
								"_");
//						log.trace(siteId + "\t" + id + "\t" + sitePage);
						// String[] pair = new String[4];
						// pair[0] = "http://www.wikidata.org/wiki/Q" + id;
						// pair[1] = "http://" + lang + ".wikipedia.org/wiki/"
						// + URLEncoder.encode(sitePage, "utf-8");
						// pair[2] = lang;
						// pair[3] = Tools.md52String(pair[1]);
						// resultList.add(pair);
					}
				}
			}
		} catch (HandlingFailureException hfe) {
			// TODO Auto-generated catch block
//			log.error(this, hfe);
			System.err.println(hfe);
		}
		return resultList;
	}

	public void close() {
		db.release();
	}
}
