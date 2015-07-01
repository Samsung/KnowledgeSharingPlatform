/**
 * Indexer.java
 * Index writer 
 */
package com.samsung.scrc.wsg.k.index.core;

import java.io.File;
import java.io.IOException;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.samsung.scrc.wsg.k.var.GlobalParameters;

/**
 * @author yuxie
 * 
 * @date Jul 11, 2014
 * 
 */
public class Indexer {
//	private static Logger log = LogManager.getLogger(Indexer.class.getName());
	private String indexDic;
	private Directory directory;
	private IndexWriter writer;
	private Analyzer analyzer;

	public Indexer(String indexDirectory) throws IOException {
		if (indexDirectory != null) {
			indexDic = indexDirectory;
		} else {
			indexDic = GlobalParameters.PATH_INDEX;
		}
		// 0. Specify the analyzer for tokenizing text. The same analyzer should
		// be used for indexing and searching
		analyzer = new StandardAnalyzer();
		// 1. create index
		File file = new File(indexDic);
		directory = FSDirectory.open(file.toPath());
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		// if the index directory exists, remove the old one
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		writer = new IndexWriter(directory, indexWriterConfig);
	}

	/**
	 * @return the indexDic
	 */
	public String getIndexDic() {
		return indexDic;
	}

	/**
	 * @return the writer
	 */
	public IndexWriter getWriter() {
		return writer;
	}

	public void closeIndexer() {
		if (analyzer != null) {
			analyzer.close();
		}
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
//				log.error(this, ioe);
				System.err.println(ioe);
			}
		}
		if (directory != null) {
			try {
				directory.close();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
//				log.error(this, ioe);
				System.err.println(ioe);
			}
		}
	}
}
