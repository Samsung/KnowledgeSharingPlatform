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

/*
SpanNearClauseFactory:

Operations:

- create for a field name and an indexreader.

- add a weighted Term
  this should add a corresponding SpanTermQuery, or
  increase the weight of an existing one.
  
- add a weighted subquery SpanNearQuery 

- create a clause for SpanNearQuery from the things added above.
  For this, create an array of SpanQuery's from the added ones.
  The clause normally is a SpanOrQuery over the added subquery SpanNearQuery
  the SpanTermQuery's for the added Term's
*/

/* When  it is necessary to suppress double subqueries as much as possible:
   hashCode() and equals() on unweighted SpanQuery are needed (possibly via getTerms(),
   the terms are individually hashable).
   Idem SpanNearQuery: hash on the subqueries and the slop.
   Evt. merge SpanNearQuery's by adding the weights of the corresponding subqueries.
 */
 
/* To be determined:
   Are SpanQuery weights handled correctly during search by Lucene?
   Should the resulting SpanOrQuery be sorted?
   Could other SpanQueries be added for use in this factory:
   - SpanOrQuery: in principle yes, but it only has access to its terms
                  via getTerms(); are the corresponding weights available?
   - SpanFirstQuery: treat similar to subquery SpanNearQuery. (ok?)
   - SpanNotQuery: treat similar to subquery SpanNearQuery. (ok?)
 */

import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;


/**
 * Factory for {@link SpanOrQuery}
 */
public class SpanNearClauseFactory { // FIXME: rename to SpanClauseFactory
  public SpanNearClauseFactory(IndexReader reader, String fieldName, BasicQueryFactory qf) {
    this.reader = reader;
    this.fieldName = fieldName;
    this.weightBySpanQuery = new HashMap<>();
    this.qf = qf;
  }
  private IndexReader reader;
  private String fieldName;
  private HashMap<SpanQuery, Float> weightBySpanQuery;
  private BasicQueryFactory qf;
  
  public IndexReader getIndexReader() {return reader;}
  
  public String getFieldName() {return fieldName;}

  public BasicQueryFactory getBasicQueryFactory() {return qf;}
  
  public int size() {return weightBySpanQuery.size();}
  
  public void clear() {weightBySpanQuery.clear();}

  protected void addSpanQueryWeighted(SpanQuery sq, float weight) {
    Float w = weightBySpanQuery.get(sq);
    if (w != null)
      w = Float.valueOf(w.floatValue() + weight);
    else
      w = Float.valueOf(weight);
    weightBySpanQuery.put(sq, w); 
  }
  
  public void addTermWeighted(Term t, float weight) throws IOException {   
    SpanTermQuery stq = qf.newSpanTermQuery(t);
    /* CHECKME: wrap in Hashable...? */
    addSpanQueryWeighted(stq, weight);
  }

  public void addSpanQuery(Query q) {
    if (q == SrndQuery.theEmptyLcnQuery)
      return;
    if (! (q instanceof SpanQuery))
      throw new AssertionError("Expected SpanQuery: " + q.toString(getFieldName()));
    addSpanQueryWeighted((SpanQuery)q, q.getBoost());
  }

  public SpanQuery makeSpanClause() {
    SpanQuery [] spanQueries = new SpanQuery[size()];
    Iterator<SpanQuery> sqi = weightBySpanQuery.keySet().iterator();
    int i = 0;
    while (sqi.hasNext()) {
      SpanQuery sq = sqi.next();
      sq.setBoost(weightBySpanQuery.get(sq).floatValue());
      spanQueries[i++] = sq;
    }
    
    if (spanQueries.length == 1)
      return spanQueries[0];
    else
      return new SpanOrQuery(spanQueries);
  }
}

