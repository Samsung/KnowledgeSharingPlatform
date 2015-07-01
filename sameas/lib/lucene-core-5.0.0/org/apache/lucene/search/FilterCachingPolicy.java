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

import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.TieredMergePolicy;

/**
 * A policy defining which filters should be cached.
 *
 * Implementations of this class must be thread-safe.
 *
 * @see UsageTrackingFilterCachingPolicy
 * @see LRUFilterCache
 * @lucene.experimental
 */
// TODO: add APIs for integration with IndexWriter.IndexReaderWarmer
public interface FilterCachingPolicy {

  /** A simple policy that caches all the provided filters on all segments. */
  public static final FilterCachingPolicy ALWAYS_CACHE = new FilterCachingPolicy() {

    @Override
    public void onUse(Filter filter) {}

    @Override
    public boolean shouldCache(Filter filter, LeafReaderContext context, DocIdSet set) throws IOException {
      return true;
    }

  };

  /** A simple policy that only caches on the largest segments of an index.
   *  The reasoning is that these segments likely account for most of the
   *  execution time of queries and are also more likely to stay around longer
   *  than small segments, which makes them more interesting for caching.
   */
  public static class CacheOnLargeSegments implements FilterCachingPolicy {

    /** {@link CacheOnLargeSegments} instance that only caches on segments that
     *  account for more than 3% of the total index size. This should guarantee
     *  that all segments from the upper {@link TieredMergePolicy tier} will be
     *  cached while ensuring that at most <tt>33</tt> segments can make it to
     *  the cache (given that some implementations such as {@link LRUFilterCache}
     *  perform better when the number of cached segments is low). */
    public static final CacheOnLargeSegments DEFAULT = new CacheOnLargeSegments(0.03f);

    private final float minSizeRatio;

    /**
     * Create a {@link CacheOnLargeSegments} instance that only caches on a
     * given segment if its number of documents divided by the total number of
     * documents in the index is greater than or equal to
     * <code>minSizeRatio</code>.
     */
    public CacheOnLargeSegments(float minSizeRatio) {
      if (minSizeRatio <= 0 || minSizeRatio >= 1) {
        throw new IllegalArgumentException("minSizeRatio must be in ]0, 1[, got " + minSizeRatio);
      }
      this.minSizeRatio = minSizeRatio;
    }

    @Override
    public void onUse(Filter filter) {}

    @Override
    public boolean shouldCache(Filter filter, LeafReaderContext context, DocIdSet set) throws IOException {
      final IndexReaderContext topLevelContext = ReaderUtil.getTopLevelContext(context);
      final float sizeRatio = (float) context.reader().maxDoc() / topLevelContext.reader().maxDoc();
      return sizeRatio >= minSizeRatio;
    }

  };

  /** Callback that is called every time that a cached filter is used.
   *  This is typically useful if the policy wants to track usage statistics
   *  in order to make decisions. */
  void onUse(Filter filter);

  /** Whether the given {@link DocIdSet} should be cached on a given segment.
   *  This method will be called on each leaf context to know if the filter
   *  should be cached on this particular leaf. The filter cache will first
   *  attempt to load a {@link DocIdSet} from the cache. If it is not cached
   *  yet and this method returns <tt>true</tt> then a cache entry will be
   *  generated. Otherwise an uncached set will be returned. */
  boolean shouldCache(Filter filter, LeafReaderContext context, DocIdSet set) throws IOException;

}
