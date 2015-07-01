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
import java.util.Arrays;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.SortingMergePolicy;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.CollectionTerminatedException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FilterLeafCollector;
import org.apache.lucene.search.FilterCollector;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TotalHitCountCollector;

/**
 * A {@link Collector} that early terminates collection of documents on a
 * per-segment basis, if the segment was sorted according to the given
 * {@link Sort}.
 *
 * <p>
 * <b>NOTE:</b> the {@code Collector} detects sorted segments according to
 * {@link SortingMergePolicy}, so it's best used in conjunction with it. Also,
 * it collects up to a specified {@code numDocsToCollect} from each segment,
 * and therefore is mostly suitable for use in conjunction with collectors such as
 * {@link TopDocsCollector}, and not e.g. {@link TotalHitCountCollector}.
 * <p>
 * <b>NOTE</b>: If you wrap a {@code TopDocsCollector} that sorts in the same
 * order as the index order, the returned {@link TopDocsCollector#topDocs() TopDocs}
 * will be correct. However the total of {@link TopDocsCollector#getTotalHits()
 * hit count} will be underestimated since not all matching documents will have
 * been collected.
 * <p>
 * <b>NOTE</b>: This {@code Collector} uses {@link Sort#toString()} to detect
 * whether a segment was sorted with the same {@code Sort}. This has
 * two implications:
 * <ul>
 * <li>if a custom comparator is not implemented correctly and returns
 * different identifiers for equivalent instances, this collector will not
 * detect sorted segments,</li>
 * <li>if you suddenly change the {@link IndexWriter}'s
 * {@code SortingMergePolicy} to sort according to another criterion and if both
 * the old and the new {@code Sort}s have the same identifier, this
 * {@code Collector} will incorrectly detect sorted segments.</li>
 * </ul>
 *
 * @lucene.experimental
 */
public class EarlyTerminatingSortingCollector extends FilterCollector {

  /** Returns whether collection can be early-terminated if it sorts with the
   *  provided {@link Sort} and if segments are merged with the provided
   *  {@link SortingMergePolicy}. */
  public static boolean canEarlyTerminate(Sort sort, SortingMergePolicy mergePolicy) {
    final SortField[] fields1 = sort.getSort();
    final SortField[] fields2 = mergePolicy.getSort().getSort();
    // early termination is possible if fields1 is a prefix of fields2
    if (fields1.length > fields2.length) {
      return false;
    }
    return Arrays.asList(fields1).equals(Arrays.asList(fields2).subList(0, fields1.length));
  }

  /** Sort used to sort the search results */
  protected final Sort sort;
  /** Number of documents to collect in each segment */
  protected final int numDocsToCollect;
  private final SortingMergePolicy mergePolicy;

  /**
   * Create a new {@link EarlyTerminatingSortingCollector} instance.
   *
   * @param in
   *          the collector to wrap
   * @param sort
   *          the sort you are sorting the search results on
   * @param numDocsToCollect
   *          the number of documents to collect on each segment. When wrapping
   *          a {@link TopDocsCollector}, this number should be the number of
   *          hits.
   * @throws IllegalArgumentException if the sort order doesn't allow for early
   *          termination with the given merge policy.
   */
  public EarlyTerminatingSortingCollector(Collector in, Sort sort, int numDocsToCollect, SortingMergePolicy mergePolicy) {
    super(in);
    if (numDocsToCollect <= 0) {
      throw new IllegalArgumentException("numDocsToCollect must always be > 0, got " + numDocsToCollect);
    }
    if (canEarlyTerminate(sort, mergePolicy) == false) {
      throw new IllegalStateException("Cannot early terminate with sort order " + sort + " if segments are sorted with " + mergePolicy.getSort());
    }
    this.sort = sort;
    this.numDocsToCollect = numDocsToCollect;
    this.mergePolicy = mergePolicy;
  }

  @Override
  public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
    if (mergePolicy.isSorted(context.reader())) {
      // segment is sorted, can early-terminate
      return new FilterLeafCollector(super.getLeafCollector(context)) {
        private int numCollected;

        @Override
        public void collect(int doc) throws IOException {
          super.collect(doc);
          if (++numCollected >= numDocsToCollect) {
            throw new CollectionTerminatedException();
          }
        }

      };
    } else {
      return super.getLeafCollector(context);
    }
  }

}
