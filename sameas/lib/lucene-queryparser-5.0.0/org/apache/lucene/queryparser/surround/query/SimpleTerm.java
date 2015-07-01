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

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

/**
 * Base class for queries that expand to sets of simple terms.
 */
public abstract class SimpleTerm
  extends SrndQuery
  implements DistanceSubQuery, Comparable<SimpleTerm>
{
  public SimpleTerm(boolean q) {quoted = q;}
  
  private boolean quoted;
  boolean isQuoted() {return quoted;}
  
  public String getQuote() {return "\"";}
  public String getFieldOperator() {return "/";}
  
  public abstract String toStringUnquoted();

  /** @deprecated (March 2011) Not normally used, to be removed from Lucene 4.0.
   *   This class implementing Comparable is to be removed at the same time.
   */
  @Override
  @Deprecated
  public int compareTo(SimpleTerm ost) {
    /* for ordering terms and prefixes before using an index, not used */
    return this.toStringUnquoted().compareTo( ost.toStringUnquoted());
  }
  
  protected void suffixToString(StringBuilder r) {} /* override for prefix query */
  
  @Override
  public String toString() {
    StringBuilder r = new StringBuilder();
    if (isQuoted()) {
      r.append(getQuote());
    }
    r.append(toStringUnquoted());
    if (isQuoted()) {
      r.append(getQuote());
    }
    suffixToString(r);
    weightToString(r);
    return r.toString();
  }
  
  public abstract void visitMatchingTerms(
                            IndexReader reader,
                            String fieldName,
                            MatchingTermVisitor mtv) throws IOException;
  
  /**
   * Callback to visit each matching term during "rewrite"
   * in {@link #visitMatchingTerm(Term)}
   */
  public interface MatchingTermVisitor {
    void visitMatchingTerm(Term t)throws IOException;
  }

  @Override
  public String distanceSubQueryNotAllowed() {return null;}
  
  @Override
  public void addSpanQueries(final SpanNearClauseFactory sncf) throws IOException {
    visitMatchingTerms(
          sncf.getIndexReader(),
          sncf.getFieldName(),
          new MatchingTermVisitor() {
            @Override
            public void visitMatchingTerm(Term term) throws IOException {
              sncf.addTermWeighted(term, getWeight());
            }
          });
  }

  @Override
  public Query makeLuceneQueryFieldNoBoost(final String fieldName, final BasicQueryFactory qf) {
    return new SimpleTermRewriteQuery(this, fieldName, qf);
  }
}



