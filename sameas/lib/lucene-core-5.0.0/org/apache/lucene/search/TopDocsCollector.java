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


import org.apache.lucene.util.PriorityQueue;

/**
 * A base class for all collectors that return a {@link TopDocs} output. This
 * collector allows easy extension by providing a single constructor which
 * accepts a {@link PriorityQueue} as well as protected members for that
 * priority queue and a counter of the number of total hits.<br>
 * Extending classes can override any of the methods to provide their own
 * implementation, as well as avoid the use of the priority queue entirely by
 * passing null to {@link #TopDocsCollector(PriorityQueue)}. In that case
 * however, you might want to consider overriding all methods, in order to avoid
 * a NullPointerException.
 */
public abstract class TopDocsCollector<T extends ScoreDoc> implements Collector {

  /** This is used in case topDocs() is called with illegal parameters, or there
   *  simply aren't (enough) results. */
  protected static final TopDocs EMPTY_TOPDOCS = new TopDocs(0, new ScoreDoc[0], Float.NaN);
  
  /**
   * The priority queue which holds the top documents. Note that different
   * implementations of PriorityQueue give different meaning to 'top documents'.
   * HitQueue for example aggregates the top scoring documents, while other PQ
   * implementations may hold documents sorted by other criteria.
   */
  protected PriorityQueue<T> pq;

  /** The total number of documents that the collector encountered. */
  protected int totalHits;
  
  protected TopDocsCollector(PriorityQueue<T> pq) {
    this.pq = pq;
  }
  
  /**
   * Populates the results array with the ScoreDoc instances. This can be
   * overridden in case a different ScoreDoc type should be returned.
   */
  protected void populateResults(ScoreDoc[] results, int howMany) {
    for (int i = howMany - 1; i >= 0; i--) { 
      results[i] = pq.pop();
    }
  }

  /**
   * Returns a {@link TopDocs} instance containing the given results. If
   * <code>results</code> is null it means there are no results to return,
   * either because there were 0 calls to collect() or because the arguments to
   * topDocs were invalid.
   */
  protected TopDocs newTopDocs(ScoreDoc[] results, int start) {
    return results == null ? EMPTY_TOPDOCS : new TopDocs(totalHits, results);
  }
  
  /** The total number of documents that matched this query. */
  public int getTotalHits() {
    return totalHits;
  }
  
  /** The number of valid PQ entries */
  protected int topDocsSize() {
    // In case pq was populated with sentinel values, there might be less
    // results than pq.size(). Therefore return all results until either
    // pq.size() or totalHits.
    return totalHits < pq.size() ? totalHits : pq.size();
  }
  
  /** Returns the top docs that were collected by this collector. */
  public TopDocs topDocs() {
    // In case pq was populated with sentinel values, there might be less
    // results than pq.size(). Therefore return all results until either
    // pq.size() or totalHits.
    return topDocs(0, topDocsSize());
  }

  /**
   * Returns the documents in the range [start .. pq.size()) that were collected
   * by this collector. Note that if {@code start >= pq.size()}, an empty TopDocs is
   * returned.<br>
   * This method is convenient to call if the application always asks for the
   * last results, starting from the last 'page'.<br>
   * <b>NOTE:</b> you cannot call this method more than once for each search
   * execution. If you need to call it more than once, passing each time a
   * different <code>start</code>, you should call {@link #topDocs()} and work
   * with the returned {@link TopDocs} object, which will contain all the
   * results this search execution collected.
   */
  public TopDocs topDocs(int start) {
    // In case pq was populated with sentinel values, there might be less
    // results than pq.size(). Therefore return all results until either
    // pq.size() or totalHits.
    return topDocs(start, topDocsSize());
  }

  /**
   * Returns the documents in the range [start .. start+howMany) that were
   * collected by this collector. Note that if {@code start >= pq.size()}, an empty
   * TopDocs is returned, and if pq.size() - start &lt; howMany, then only the
   * available documents in [start .. pq.size()) are returned.<br>
   * This method is useful to call in case pagination of search results is
   * allowed by the search application, as well as it attempts to optimize the
   * memory used by allocating only as much as requested by howMany.<br>
   * <b>NOTE:</b> you cannot call this method more than once for each search
   * execution. If you need to call it more than once, passing each time a
   * different range, you should call {@link #topDocs()} and work with the
   * returned {@link TopDocs} object, which will contain all the results this
   * search execution collected.
   */
  public TopDocs topDocs(int start, int howMany) {
    
    // In case pq was populated with sentinel values, there might be less
    // results than pq.size(). Therefore return all results until either
    // pq.size() or totalHits.
    int size = topDocsSize();

    // Don't bother to throw an exception, just return an empty TopDocs in case
    // the parameters are invalid or out of range.
    // TODO: shouldn't we throw IAE if apps give bad params here so they dont
    // have sneaky silent bugs?
    if (start < 0 || start >= size || howMany <= 0) {
      return newTopDocs(null, start);
    }

    // We know that start < pqsize, so just fix howMany. 
    howMany = Math.min(size - start, howMany);
    ScoreDoc[] results = new ScoreDoc[howMany];

    // pq's pop() returns the 'least' element in the queue, therefore need
    // to discard the first ones, until we reach the requested range.
    // Note that this loop will usually not be executed, since the common usage
    // should be that the caller asks for the last howMany results. However it's
    // needed here for completeness.
    for (int i = pq.size() - start - howMany; i > 0; i--) { pq.pop(); }
    
    // Get the requested results from pq.
    populateResults(results, howMany);
    
    return newTopDocs(results, start);
  }

}
