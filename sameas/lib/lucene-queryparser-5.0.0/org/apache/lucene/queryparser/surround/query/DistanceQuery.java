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

import java.util.List;
import java.util.Iterator;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;

/** Factory for NEAR queries */
public class DistanceQuery extends ComposedQuery implements DistanceSubQuery {
  public DistanceQuery(
      List<SrndQuery> queries,
      boolean infix,
      int opDistance,
      String opName,
      boolean ordered) {
    super(queries, infix, opName);
    this.opDistance = opDistance; /* the distance indicated in the operator */
    this.ordered = ordered;
  }


  private int opDistance;
  public int getOpDistance() {return opDistance;}
  
  private boolean ordered;
  public boolean subQueriesOrdered() {return ordered;}
  
  @Override
  public String distanceSubQueryNotAllowed() {
    Iterator<?> sqi = getSubQueriesIterator();
    while (sqi.hasNext()) {
      Object leq = sqi.next();
      if (leq instanceof DistanceSubQuery) {
        DistanceSubQuery dsq = (DistanceSubQuery) leq;
        String m = dsq.distanceSubQueryNotAllowed();
        if (m != null) {
          return m; 
        }
      } else {
        return "Operator " + getOperatorName() + " does not allow subquery " + leq.toString();
      }
    }
    return null; /* subqueries acceptable */
  }
  
  @Override
  public void addSpanQueries(SpanNearClauseFactory sncf) throws IOException {
    Query snq = getSpanNearQuery(sncf.getIndexReader(),
                                  sncf.getFieldName(),
                                  getWeight(),
                                  sncf.getBasicQueryFactory());
    sncf.addSpanQuery(snq);
  }
  
  public Query getSpanNearQuery(
          IndexReader reader,
          String fieldName,
          float boost,
          BasicQueryFactory qf) throws IOException {
    SpanQuery[] spanClauses = new SpanQuery[getNrSubQueries()];
    Iterator<?> sqi = getSubQueriesIterator();
    int qi = 0;
    while (sqi.hasNext()) {
      SpanNearClauseFactory sncf = new SpanNearClauseFactory(reader, fieldName, qf);
      
      ((DistanceSubQuery)sqi.next()).addSpanQueries(sncf);
      if (sncf.size() == 0) { /* distance operator requires all sub queries */
        while (sqi.hasNext()) { /* produce evt. error messages but ignore results */
          ((DistanceSubQuery)sqi.next()).addSpanQueries(sncf);
          sncf.clear();
        }
        return SrndQuery.theEmptyLcnQuery;
      }
      
      spanClauses[qi] = sncf.makeSpanClause();
      qi++;
    }

    SpanNearQuery r = new SpanNearQuery(spanClauses, getOpDistance() - 1, subQueriesOrdered());
    r.setBoost(boost);
    return r;
  }

  @Override
  public Query makeLuceneQueryFieldNoBoost(final String fieldName, final BasicQueryFactory qf) {
    return new DistanceRewriteQuery(this, fieldName, qf);
  }
}

