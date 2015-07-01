/**
 * SearchIndex.java
 * By default, we use standard analyzer (grammar-based), please refer to http://lucene.apache.org/core/4_9_0/analyzers-common/overview-summary.html
 */
package com.samsung.scrc.wsg.k.index.search;

import java.io.File;
import java.io.IOException;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author yuxie
 * 
 * @date Jul 13, 2014
 * 
 */
public class SearchIndex {
//	private static Logger log = LogManager.getLogger(SearchIndex.class
//			.getName());
	private IndexReader reader;
	private IndexSearcher searcher;
	private String indexPath;
	private ScoreDoc tmpSortPagSearchSD = null;
	private ScoreDoc tmpPagSearchSD = null;

	public SearchIndex(String indexPath) {
		try {
			this.indexPath = indexPath;
			loadSearcher();
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
		}
	}

	private void loadSearcher() throws IOException {
		Directory directory = FSDirectory.open(new File(indexPath).toPath()); // disk
		// index storage
		reader = DirectoryReader.open(directory);
		searcher = new IndexSearcher(reader);
		directory.close();
	}

	public String[] termSearch(String[] keys, String[] condKey,
			String[] condKeyword) {
		BooleanQuery query = new BooleanQuery();
		for (int i = 0; i < condKey.length; i++) {
			Query q = new TermQuery(new Term(condKey[i], condKeyword[i]));
			query.add(q, BooleanClause.Occur.MUST);
		}
		TopDocs hits = null;
		try {
			hits = searcher.search(query, 1);
			int count = hits.scoreDocs.length;
			// log.trace("Term Search Found " + count + " out of "
			// + hits.totalHits + ".");
			if (count > 0) {
				String[] results = new String[keys.length];
				int docId = hits.scoreDocs[0].doc;
				Document d = searcher.doc(docId);
				for (int i = 0; i < keys.length; i++) {
					results[i] = d.get(keys[i]);
				}
				return results;
			} else {
				return null;
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
			return null;
		}
	}

	public String[] termSearch(String[] keys, String condKey, String condKeyword) {
		Query query = new TermQuery(new Term(condKey, condKeyword));
		TopDocs hits = null;
		try {
			hits = searcher.search(query, 1);
			int count = hits.scoreDocs.length;
			// log.trace("Term Search Found " + count + " out of "
			// + hits.totalHits + ".");
			if (count > 0) {
				String[] results = new String[keys.length];
				int docId = hits.scoreDocs[0].doc;
				Document d = searcher.doc(docId);
				for (int i = 0; i < keys.length; i++) {
					results[i] = d.get(keys[i]);
				}
				return results;
			} else {
				return null;
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
			return null;
		}
	}

	public boolean termSearchExist(String[] condKey, String[] condKeyword) {
		BooleanQuery query = new BooleanQuery();
		for (int i = 0; i < condKey.length; i++) {
			Query q = new TermQuery(new Term(condKey[i], condKeyword[i]));
			query.add(q, BooleanClause.Occur.MUST);
		}
		TopDocs hits = null;
		try {
			hits = searcher.search(query, 1);
			int count = hits.scoreDocs.length;
			// log.trace("Term Search Found " + count + " out of "
			// + hits.totalHits + ".");
			if (count > 0) {
				return true;
			} else {
				return false;
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
			return false;
		}
	}

	public String[][] pagSearch(String[] keys, int limit, boolean init) {
		if (init) {
			tmpPagSearchSD = null;
		}
		MatchAllDocsQuery query = new MatchAllDocsQuery();
		// log.trace(query.toString());
		TopDocs hits = null;
		try {
			hits = searcher.searchAfter(tmpPagSearchSD, query, limit);
			int count = hits.scoreDocs.length;
			if (count > 0) {
				String[][] searchResults = new String[count][keys.length];
//				log.trace("Full Sort Search Found " + count + " hits out of "
//						+ hits.totalHits + ".");
				
				for (int i = 0; i < count; ++i) {
					int docId = hits.scoreDocs[i].doc;
					Document d = searcher.doc(docId);
					for (int j = 0; j < keys.length; j++) {
						searchResults[i][j] = d.get(keys[j]);
					}
				}
				tmpPagSearchSD = hits.scoreDocs[count - 1];
				return searchResults;
			} else {
				return null;
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
			return null;
		}
	}

	public String[][] sortPagSearch(String[] keys, String[] sortKeys,
			int limit, boolean init) {
		if (init) {
			tmpSortPagSearchSD = null;
		}
		MatchAllDocsQuery query = new MatchAllDocsQuery();
		TopDocs topDocs = null;
		SortField[] sf = new SortField[sortKeys.length];
		for (int i = 0; i < sortKeys.length; i++) {
			sf[i] = new SortField(sortKeys[i], SortField.Type.STRING);
		}
		Sort sort = new Sort(sf);
		try {
			topDocs = searcher.searchAfter(tmpSortPagSearchSD, query, limit,
					sort);
			int count = topDocs.scoreDocs.length;
//			log.trace("Sort Pagination Search Found " + count + " hits out of "
//					+ topDocs.totalHits + ".");
			if (count > 0) {
				String[][] searchResults = new String[count][keys.length];
				for (int i = 0; i < count; ++i) {
					int docId = topDocs.scoreDocs[i].doc;
					Document d = searcher.doc(docId);
					String[] searchResultItem = new String[keys.length];
					for (int j = 0; j < keys.length; j++) {
						searchResultItem[j] = d.get(keys[j]);
					}
					searchResults[i] = searchResultItem;
				}
				tmpSortPagSearchSD = topDocs.scoreDocs[count - 1];
				return searchResults;
			} else {
				return null;
			}
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
			return null;
		}
	}

	public long countHits() {
		long totalCount = 0;
		MatchAllDocsQuery query = new MatchAllDocsQuery();
		TopDocs docs = null;
		try {
			docs = searcher.search(query, 1);
			totalCount = docs.totalHits;
		} catch (IOException ioe) {
			// TODO Auto-generated catch block
//			log.error(this, ioe);
			System.err.println(ioe);
		}
		return totalCount;
	}

	public void close() {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
//				log.error(this, ioe);
				System.err.println(ioe);
			}
		}
	}
}