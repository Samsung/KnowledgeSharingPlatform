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

/**
 * <p>Collector decouples the score from the collected doc:
 * the score computation is skipped entirely if it's not
 * needed.  Collectors that do need the score should
 * implement the {@link #setScorer} method, to hold onto the
 * passed {@link Scorer} instance, and call {@link
 * Scorer#score()} within the collect method to compute the
 * current hit's score.  If your collector may request the
 * score for a single hit multiple times, you should use
 * {@link ScoreCachingWrappingScorer}. </p>
 * 
 * <p><b>NOTE:</b> The doc that is passed to the collect
 * method is relative to the current reader. If your
 * collector needs to resolve this to the docID space of the
 * Multi*Reader, you must re-base it by recording the
 * docBase from the most recent setNextReader call.  Here's
 * a simple example showing how to collect docIDs into a
 * BitSet:</p>
 * 
 * <pre class="prettyprint">
 * IndexSearcher searcher = new IndexSearcher(indexReader);
 * final BitSet bits = new BitSet(indexReader.maxDoc());
 * searcher.search(query, new Collector() {
 *
 *   public LeafCollector getLeafCollector(LeafReaderContext context)
 *       throws IOException {
 *     final int docBase = context.docBase;
 *     return new LeafCollector() {
 *
 *       <em>// ignore scorer</em>
 *       public void setScorer(Scorer scorer) throws IOException {
 *       }
 *
 *       public void collect(int doc) throws IOException {
 *         bits.set(docBase + doc);
 *       }
 *
 *     };
 *   }
 *
 * });
 * </pre>
 *
 * <p>Not all collectors will need to rebase the docID.  For
 * example, a collector that simply counts the total number
 * of hits would skip it.</p>
 *
 * @lucene.experimental
 */
public interface LeafCollector {

  /**
   * Called before successive calls to {@link #collect(int)}. Implementations
   * that need the score of the current document (passed-in to
   * {@link #collect(int)}), should save the passed-in Scorer and call
   * scorer.score() when needed.
   */
  void setScorer(Scorer scorer) throws IOException;
  
  /**
   * Called once for every document matching a query, with the unbased document
   * number.
   * <p>Note: The collection of the current segment can be terminated by throwing
   * a {@link CollectionTerminatedException}. In this case, the last docs of the
   * current {@link org.apache.lucene.index.LeafReaderContext} will be skipped and {@link IndexSearcher}
   * will swallow the exception and continue collection with the next leaf.
   * <p>
   * Note: This is called in an inner search loop. For good search performance,
   * implementations of this method should not call {@link IndexSearcher#doc(int)} or
   * {@link org.apache.lucene.index.IndexReader#document(int)} on every hit.
   * Doing so can slow searches by an order of magnitude or more.
   */
  void collect(int doc) throws IOException;

}
