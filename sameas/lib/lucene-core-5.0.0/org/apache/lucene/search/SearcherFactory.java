package org.apache.lucene.search;

/*
 *
 * Copyright(c) 2015, Samsung Electronics Co., Ltd.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.
    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.io.IOException;
import java.util.concurrent.ExecutorService; // javadocs

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter; // javadocs
import org.apache.lucene.index.IndexWriterConfig; // javadocs
import org.apache.lucene.search.similarities.Similarity; // javadocs

/**
 * Factory class used by {@link SearcherManager} to
 * create new IndexSearchers. The default implementation just creates 
 * an IndexSearcher with no custom behavior:
 * 
 * <pre class="prettyprint">
 *   public IndexSearcher newSearcher(IndexReader r) throws IOException {
 *     return new IndexSearcher(r);
 *   }
 * </pre>
 * 
 * You can pass your own factory instead if you want custom behavior, such as:
 * <ul>
 *   <li>Setting a custom scoring model: {@link IndexSearcher#setSimilarity(Similarity)}
 *   <li>Parallel per-segment search: {@link IndexSearcher#IndexSearcher(IndexReader, ExecutorService)}
 *   <li>Return custom subclasses of IndexSearcher (for example that implement distributed scoring)
 *   <li>Run queries to warm your IndexSearcher before it is used. Note: when using near-realtime search
 *       you may want to also {@link IndexWriterConfig#setMergedSegmentWarmer(IndexWriter.IndexReaderWarmer)} to warm
 *       newly merged segments in the background, outside of the reopen path.
 * </ul>
 * @lucene.experimental
 */
public class SearcherFactory {
  /** 
   * Returns a new IndexSearcher over the given reader. 
   */
  public IndexSearcher newSearcher(IndexReader reader) throws IOException {
    return new IndexSearcher(reader);
  }
}
