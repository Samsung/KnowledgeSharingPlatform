/**
 * BaiscIndexWriter.java
 * Be charge of writing list of documents into Lucene index directory
 */
package com.samsung.scrc.wsg.k.index.core;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;

/**
 * @author yuxie
 * 
 * @date Aug 18, 2014
 * 
 */
public class BasicIndexWriter {
//	protected static Logger log = LogManager.getLogger(BasicIndexWriter.class
//			.getName());
	protected Indexer indexer;

	/**
	 * Pass the Lucene index directory to constructor
	 * 
	 * @param path
	 * @throws IOException
	 */
	public BasicIndexWriter(String path) throws IOException {
		indexer = new Indexer(path);
	}

	/**
	 * Write List<Document> into Lucene directory
	 * 
	 * @param docs
	 */
	public void write(List<Document> docs) {
		Iterator<Document> iterator = docs.iterator();
//		log.debug(docs.size());
		long count = 0;
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			try {
				indexer.getWriter().addDocument(doc);
				count++;
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
//				log.error(ioe);
				System.err.println(ioe);
			}
		}
//		log.debug("Doc number into Lucene:\t" + count);
	}

	/**
	 * Close indexer
	 */
	public void close() {
		indexer.closeIndexer();
	}
}
