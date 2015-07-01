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

import org.apache.lucene.index.LeafReaderContext;

/**
 * <p>Expert: Collectors are primarily meant to be used to
 * gather raw results from a search, and implement sorting
 * or custom result filtering, collation, etc. </p>
 *
 * <p>Lucene's core collectors are derived from {@link Collector}
 * and {@link SimpleCollector}. Likely your application can
 * use one of these classes, or subclass {@link TopDocsCollector},
 * instead of implementing Collector directly:
 *
 * <ul>
 *
 *   <li>{@link TopDocsCollector} is an abstract base class
 *   that assumes you will retrieve the top N docs,
 *   according to some criteria, after collection is
 *   done.  </li>
 *
 *   <li>{@link TopScoreDocCollector} is a concrete subclass
 *   {@link TopDocsCollector} and sorts according to score +
 *   docID.  This is used internally by the {@link
 *   IndexSearcher} search methods that do not take an
 *   explicit {@link Sort}. It is likely the most frequently
 *   used collector.</li>
 *
 *   <li>{@link TopFieldCollector} subclasses {@link
 *   TopDocsCollector} and sorts according to a specified
 *   {@link Sort} object (sort by field).  This is used
 *   internally by the {@link IndexSearcher} search methods
 *   that take an explicit {@link Sort}.
 *
 *   <li>{@link TimeLimitingCollector}, which wraps any other
 *   Collector and aborts the search if it's taken too much
 *   time.</li>
 *
 *   <li>{@link PositiveScoresOnlyCollector} wraps any other
 *   Collector and prevents collection of hits whose score
 *   is &lt;= 0.0</li>
 *
 * </ul>
 *
 * @lucene.experimental
 */
public interface Collector {

  /**
   * Create a new {@link LeafCollector collector} to collect the given context.
   *
   * @param context
   *          next atomic reader context
   */
  LeafCollector getLeafCollector(LeafReaderContext context) throws IOException;

}
