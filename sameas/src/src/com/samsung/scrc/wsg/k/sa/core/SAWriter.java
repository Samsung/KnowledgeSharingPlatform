/**
 * FBLDelegator.java
 */
package com.samsung.scrc.wsg.k.sa.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.util.BytesRef;

import com.samsung.scrc.wsg.k.index.core.BasicIndexWriter;
import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Mar 16, 2015
 * 
 */
public class SAWriter {
//	private static Logger log = LogManager.getLogger(SAWriter.class.getName());
	private BasicIndexWriter writer;

	public SAWriter(String path) {
		try {
//			log.trace("Lucene Delegator:\t" + path);
			writer = new BasicIndexWriter(path);
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
		}
	}

	public void insertData(List<String[]> items) {
		// COL_ID, COL_URL, COL_LANG, COL_MD5
		List<Document> docs = new ArrayList<>();
		for (String[] item : items) {
			Document doc = new Document();
			doc.add(new StringField(GlobalParameters.COL_ID, item[0],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_SORT,
					new BytesRef(item[0])));
			doc.add(new StringField(GlobalParameters.COL_URL, item[1],
					Field.Store.YES));
			doc.add(new StringField(GlobalParameters.COL_LANG, item[2],
					Field.Store.YES));
			doc.add(new StringField(GlobalParameters.COL_MD5, item[3],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_MD5_SORT,
					new BytesRef(item[3])));
			docs.add(doc);
		}
		writer.write(docs);
	}

	public void insertSameAsRelations(String freebase, Set<String> wikidatas) {
		List<Document> docs = new ArrayList<>();
		for (String wikidata : wikidatas) {
			Document doc = new Document();
			doc.add(new StringField(GlobalParameters.COL_ID_FB, freebase,
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_FB_SORT,
					new BytesRef(freebase)));
			doc.add(new StringField(GlobalParameters.COL_ID_WD, wikidata,
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_WD_SORT,
					new BytesRef(wikidata)));
			docs.add(doc);
		}
		writer.write(docs);
	}

	public void insertSameAsRelations(List<String[]> items) {
		// COL_ID, COL_URL, COL_LANG, COL_MD5
		List<Document> docs = new ArrayList<>();
		for (String[] item : items) {
			Document doc = new Document();
			doc.add(new StringField(GlobalParameters.COL_ID_FB, item[0],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_FB_SORT,
					new BytesRef(item[0])));
			doc.add(new StringField(GlobalParameters.COL_URL_FB, item[1],
					Field.Store.YES));
			doc.add(new StringField(GlobalParameters.COL_LANG_FB, item[2],
					Field.Store.YES));
			doc.add(new StringField(GlobalParameters.COL_ID_WD, item[3],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_WD_SORT,
					new BytesRef(item[3])));
			doc.add(new StringField(GlobalParameters.COL_URL_WD, item[4],
					Field.Store.YES));
			doc.add(new StringField(GlobalParameters.COL_LANG_WD, item[5],
					Field.Store.YES));
			doc.add(new StringField(GlobalParameters.COL_MD5, item[6],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_MD5_SORT,
					new BytesRef(item[6])));
			docs.add(doc);
		}
		writer.write(docs);
	}

	public void insertLangStat(List<String[]> items) {
		List<Document> docs = new ArrayList<>();
//		log.info("Insert doc size:\t" + items.size());
		System.out.println("Insert doc size:\t" + items.size());
		for (String[] item : items) {
			Document doc = new Document();
			// log.trace(item[0] + "\t" + item[1]);
			doc.add(new StringField(GlobalParameters.COL_ID, item[0],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_SORT,
					new BytesRef(item[0])));
			doc.add(new IntField(GlobalParameters.COL_LANG_COUNT, Integer
					.valueOf(item[1]), Field.Store.YES));
			docs.add(doc);
		}
		writer.write(docs);
	}

	public void insertMCRaw(List<String[]> items) {
		List<Document> docs = new ArrayList<>();
		for (String[] item : items) {
			Document doc = new Document();
			doc.add(new StringField(GlobalParameters.COL_ID_FB, item[0],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_FB_SORT,
					new BytesRef(item[0])));
			doc.add(new StringField(GlobalParameters.COL_ID_WD, item[1],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_WD_SORT,
					new BytesRef(item[1])));
			doc.add(new IntField(GlobalParameters.COL_LANG_COUNT, Integer
					.valueOf(item[2]), Field.Store.YES));
			docs.add(doc);
		}
		writer.write(docs);
	}

	public void insertSAPairs(List<String[]> items) {
		List<Document> docs = new ArrayList<>();
		for (String[] item : items) {
			Document doc = new Document();
			doc.add(new StringField(GlobalParameters.COL_ID_FB, item[0],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_FB_SORT,
					new BytesRef(item[0])));
			doc.add(new StringField(GlobalParameters.COL_ID_WD, item[1],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_WD_SORT,
					new BytesRef(item[1])));
			docs.add(doc);
		}
		writer.write(docs);
	}

	public void insertSAStat(List<String[]> items) {
		List<Document> docs = new ArrayList<>();
		for (String[] item : items) {
			Document doc = new Document();
			doc.add(new StringField(GlobalParameters.COL_ID_FB, item[0],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_FB_SORT,
					new BytesRef(item[0])));
			doc.add(new StringField(GlobalParameters.COL_ID_WD, item[1],
					Field.Store.YES));
			doc.add(new SortedDocValuesField(GlobalParameters.COL_ID_WD_SORT,
					new BytesRef(item[1])));
			doc.add(new IntField(GlobalParameters.COL_LANG_COUNT, Integer
					.valueOf(item[2]), Field.Store.YES));
			doc.add(new IntField(GlobalParameters.COL_LANG_COUNT_FB, Integer
					.valueOf(item[3]), Field.Store.YES));
			doc.add(new IntField(GlobalParameters.COL_LANG_COUNT_WD, Integer
					.valueOf(item[4]), Field.Store.YES));
			docs.add(doc);
		}
		writer.write(docs);
	}

	public void close() {
		writer.close();
	}
}
