/**
 * GlobalParameters.java
 */
package com.samsung.scrc.wsg.k.var;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.samsung.scrc.wsg.k.sa.matcher.BeliefBasedMatcher;
import com.samsung.scrc.wsg.k.sa.matcher.Matcher;
import com.samsung.scrc.wsg.k.sa.matcher.MaxConfMatcher;
import com.samsung.scrc.wsg.k.sa.matcher.One2OneMatcher;
import com.samsung.scrc.wsg.k.sa.matcher.ThresholdMatcher;

/**
 * @author yuxie
 * 
 * @date Nov 28, 2014
 * 
 */
public class GlobalParameters {
//	public static Logger log = LogManager.getLogger(GlobalParameters.class
//			.getName());

	// get the running root path
	public static String getRootPath() {
		String path = System.getProperty("user.dir");
		return path;
	}

	// ******************** CONFIGURATION *********************************//
	// config files
	public static String PATH_CONF = getRootPath() + "/conf/";
	public static String FILE_PROP_SA = PATH_CONF + "sa.properties";
	// config prop
	public static final String PROP_WD_TYPE = "wd.type";
	public static final String PROP_WD_HOST = "wd.host";
	public static final String PROP_WD_PORT = "wd.port";
	public static final String PROP_WD_USER = "wd.user";
	public static final String PROP_WD_PWD = "wd.pwd";
	public static final String PROP_WD_DB = "wd.database";
	public static final String PROP_FB_FILE = "fb.file";
	public static final String PROP_SA_MATCHER = "sa.matcher";

	// matchers
	public static final String MATCHER_MC = "maxconf";
	public static final String MATCHER_THRESHOLD = "threshold";
	public static final String MATCHER_ONE2ONE = "oneonly";
	public static final String MATCHER_BELIEF = "belief";

	public static Map<String, Matcher> MATCHER_MAPPING = new HashMap<String, Matcher>();
	static {
		MATCHER_MAPPING.put(MATCHER_MC, new MaxConfMatcher());
		MATCHER_MAPPING.put(MATCHER_THRESHOLD, new ThresholdMatcher());
		MATCHER_MAPPING.put(MATCHER_ONE2ONE, new One2OneMatcher());
		MATCHER_MAPPING.put(MATCHER_BELIEF, new BeliefBasedMatcher());
	}

	// by default, contains four matchers: max confidence, threshold filtering, one-to-one only, belief based
	public static List<Matcher> MATCHER_LIST = new ArrayList<Matcher>();
	static {
		MATCHER_LIST.add(new MaxConfMatcher());
		MATCHER_LIST.add(new ThresholdMatcher());
		MATCHER_LIST.add(new One2OneMatcher());
		MATCHER_LIST.add(new BeliefBasedMatcher());
	}

	// index files
	public static String PATH_INDEX = getRootPath() + "/index/";
	// schema based on system current time
	public static Calendar calendar = Calendar.getInstance();
	public static String SCHEMA = calendar.get(Calendar.YEAR) + "-"
			+ (calendar.get(Calendar.MONTH) + 1) + "-"
			+ calendar.get(Calendar.DAY_OF_MONTH);
	public static String PATH_INDEX_FB = PATH_INDEX + SCHEMA + "/freebase/";
	public static String PATH_INDEX_WD = PATH_INDEX + SCHEMA + "/wikidata/";
	public static String PATH_INDEX_STAT = PATH_INDEX + SCHEMA + "/stat/";
	public static String PATH_INDEX_STAT_FB = PATH_INDEX_STAT + "/freebase/";
	public static String PATH_INDEX_STAT_WD = PATH_INDEX_STAT + "/wikidata/";
	public static String PATH_INDEX_STAT_SA = PATH_INDEX_STAT + "/sa/";
	public static String PATH_INDEX_STAT_SA_FULL = PATH_INDEX_STAT
			+ "/sa-full/";
	public static String PATH_INDEX_SA_RAW = PATH_INDEX + SCHEMA
			+ "/result/raw/";
	public static String PATH_INDEX_SA_ORIGIN = PATH_INDEX + SCHEMA
			+ "/result/origin/";
	public static String PATH_INDEX_SA_MC_RAW = PATH_INDEX + SCHEMA
			+ "/result/mc_raw/";
	public static String PATH_INDEX_SA_MC = PATH_INDEX + SCHEMA + "/result/mc/";
	public static String PATH_INDEX_SA_THRESHOLD_RAW = PATH_INDEX + SCHEMA
			+ "/result/threshold_raw/";
	public static String PATH_INDEX_SA_THRESHOLD_RAW_1 = PATH_INDEX + SCHEMA
			+ "/result/threshold_raw1/";
	public static String PATH_INDEX_SA_THRESHOLD = PATH_INDEX + SCHEMA
			+ "/result/threshold/";
	public static String PATH_INDEX_SA_ONE_ONLY_RAW = PATH_INDEX + SCHEMA
			+ "/result/oneonly_raw/";
	public static String PATH_INDEX_SA_ONE_ONLY = PATH_INDEX + SCHEMA
			+ "/result/oneonly/";
	public static String PATH_INDEX_SA_BELIEF = PATH_INDEX + SCHEMA
			+ "/result/belief/";
	// all the temporary index files
	public static String PATH_INDEX_TMP = PATH_INDEX + SCHEMA + "/tmp/";

	// output file
	public static String PATH_RESULT = getRootPath() + "/result/";
	public static String FILE_RESULT_ORIGIN = PATH_RESULT + SCHEMA
			+ "/origin.tsv";
	public static String FILE_RESULT_MAX_CONF = PATH_RESULT + SCHEMA
			+ "/max_conf.tsv";
	public static String FILE_RESULT_THRESHOLD = PATH_RESULT + SCHEMA
			+ "/threshold.tsv";
	public static String FILE_RESULT_1_1 = PATH_RESULT + SCHEMA
			+ "/one2one.tsv";
	public static String FILE_RESULT_BELIEF_BASED = PATH_RESULT + SCHEMA
			+ "/belief.tsv";

	// lucene key
	public static final String COL_ID = "id";
	public static final String COL_ID_SORT = "id_sort";
	public static final String COL_ID_FB = "id_fb";
	public static final String COL_ID_FB_SORT = "id_fb_sort";
	public static final String COL_ID_WD = "id_wd";
	public static final String COL_ID_WD_SORT = "id_wd_sort";
	public static final String COL_URL = "url";
	public static final String COL_URL_FB = "url_fb";
	public static final String COL_URL_WD = "url_wd";
	public static final String COL_LANG = "lang";
	public static final String COL_LANG_FB = "lang_fb";
	public static final String COL_LANG_WD = "lang_wd";
	public static final String COL_MD5 = "md5";
	public static final String COL_MD5_SORT = "md5_sort";
	public static final String COL_WIKIPEDIA = "wikipedia";
	public static final String COL_MATCHED_COUNT = "matched_count";
	public static final String COL_LANG_COUNT = "lang_count";
	public static final String COL_LANG_COUNT_FB = "lang_count_fb";
	public static final String COL_LANG_COUNT_WD = "lang_count_wd";
	public static final String COL_LANG_RATIO_FB = "lang_ratio_fb";
	public static final String COL_LANG_RATIO_WD = "lang_ratio_wd";

	// max batch size
	public static int BATCH_SIZE_MAX = 500000;
}
