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
import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.index.Term;

class SimpleTermRewriteQuery extends RewriteQuery<SimpleTerm> {

  SimpleTermRewriteQuery(
      SimpleTerm srndQuery,
      String fieldName,
      BasicQueryFactory qf) {
    super(srndQuery, fieldName, qf);
  }

  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    final List<Query> luceneSubQueries = new ArrayList<>();
    srndQuery.visitMatchingTerms(reader, fieldName,
    new SimpleTerm.MatchingTermVisitor() {
      @Override
      public void visitMatchingTerm(Term term) throws IOException {
        luceneSubQueries.add(qf.newTermQuery(term));
      }
    });
    return  (luceneSubQueries.size() == 0) ? SrndQuery.theEmptyLcnQuery
    : (luceneSubQueries.size() == 1) ? luceneSubQueries.get(0)
    : SrndBooleanQuery.makeBooleanQuery(
      /* luceneSubQueries all have default weight */
      luceneSubQueries, BooleanClause.Occur.SHOULD); /* OR the subquery terms */
  }
}

