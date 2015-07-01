package org.apache.lucene.queryparser.surround.query;
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

/* Create basic queries to be used during rewrite.
 * The basic queries are TermQuery and SpanTermQuery.
 * An exception can be thrown when too many of these are used.
 * SpanTermQuery and TermQuery use IndexReader.termEnum(Term), which causes the buffer usage.
 *
 * Use this class to limit the buffer usage for reading terms from an index.
 * Default is 1024, the same as the max. number of subqueries for a BooleanQuery.
 */
 
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

/** Factory for creating basic term queries */
public class BasicQueryFactory {
  public BasicQueryFactory(int maxBasicQueries) {
    this.maxBasicQueries = maxBasicQueries;
    this.queriesMade = 0;
  }
  
  public BasicQueryFactory() {
    this(1024);
  }
  
  private int maxBasicQueries;
  private int queriesMade;
  
  public int getNrQueriesMade() {return queriesMade;}
  public int getMaxBasicQueries() {return maxBasicQueries;}
  
  @Override
  public String toString() {
    return getClass().getName()
    + "(maxBasicQueries: " + maxBasicQueries
    + ", queriesMade: " + queriesMade
    + ")";
  }

  private boolean atMax() {
    return queriesMade >= maxBasicQueries;
  }

  protected synchronized void checkMax() throws TooManyBasicQueries {
    if (atMax())
      throw new TooManyBasicQueries(getMaxBasicQueries());
    queriesMade++;
  }
  
  public TermQuery newTermQuery(Term term) throws TooManyBasicQueries {
    checkMax();
    return new TermQuery(term);
  }
  
  public SpanTermQuery newSpanTermQuery(Term term) throws TooManyBasicQueries {
    checkMax();
    return new SpanTermQuery(term);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() ^ (atMax() ? 7 : 31*32);
  }

  /** Two BasicQueryFactory's are equal when they generate
   *  the same types of basic queries, or both cannot generate queries anymore.
   */
  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof BasicQueryFactory))
      return false;
    BasicQueryFactory other = (BasicQueryFactory) obj;
    return atMax() == other.atMax();
  }
}


