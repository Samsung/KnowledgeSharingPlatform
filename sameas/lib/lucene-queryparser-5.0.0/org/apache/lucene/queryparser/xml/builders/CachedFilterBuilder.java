/*
 * Created on 25-Jan-2006
 */
package org.apache.lucene.queryparser.xml.builders;

import org.apache.lucene.queryparser.xml.*;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.w3c.dom.Element;

import java.util.Map;
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

/**
 * Filters are cached in an LRU Cache keyed on the contained query or filter object. Using this will
 * speed up overall performance for repeated uses of the same expensive query/filter. The sorts of
 * queries/filters likely to benefit from caching need not necessarily be complex - e.g. simple
 * TermQuerys with a large DF (document frequency) can be expensive  on large indexes.
 * A good example of this might be a term query on a field with only 2 possible  values -
 * "true" or "false". In a large index, querying or filtering on this field requires reading
 * millions  of document ids from disk which can more usefully be cached as a filter bitset.
 * <p/>
 * For Queries/Filters to be cached and reused the object must implement hashcode and
 * equals methods correctly so that duplicate queries/filters can be detected in the cache.
 * <p/>
 * The CoreParser.maxNumCachedFilters property can be used to control the size of the LRU
 * Cache established during the construction of CoreParser instances.
 */
public class CachedFilterBuilder implements FilterBuilder {

  private final QueryBuilderFactory queryFactory;
  private final FilterBuilderFactory filterFactory;

  private LRUCache<Object, Filter> filterCache;

  private final int cacheSize;

  public CachedFilterBuilder(QueryBuilderFactory queryFactory,
                             FilterBuilderFactory filterFactory,
                             int cacheSize) {
    this.queryFactory = queryFactory;
    this.filterFactory = filterFactory;
    this.cacheSize = cacheSize;
  }

  @Override
  public synchronized Filter getFilter(Element e) throws ParserException {
    Element childElement = DOMUtils.getFirstChildOrFail(e);

    if (filterCache == null) {
      filterCache = new LRUCache<>(cacheSize);
    }

    // Test to see if child Element is a query or filter that needs to be
    // cached
    QueryBuilder qb = queryFactory.getQueryBuilder(childElement.getNodeName());
    Object cacheKey = null;
    Query q = null;
    Filter f = null;
    if (qb != null) {
      q = qb.getQuery(childElement);
      cacheKey = q;
    } else {
      f = filterFactory.getFilter(childElement);
      cacheKey = f;
    }
    Filter cachedFilter = filterCache.get(cacheKey);
    if (cachedFilter != null) {
      return cachedFilter; // cache hit
    }

    //cache miss
    if (qb != null) {
      cachedFilter = new QueryWrapperFilter(q);
    } else {
      cachedFilter = new CachingWrapperFilter(f);
    }

    filterCache.put(cacheKey, cachedFilter);
    return cachedFilter;
  }

  static class LRUCache<K, V> extends java.util.LinkedHashMap<K, V> {

    public LRUCache(int maxsize) {
      super(maxsize * 4 / 3 + 1, 0.75f, true);
      this.maxsize = maxsize;
    }

    protected int maxsize;

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
      return size() > maxsize;
    }

  }

}
