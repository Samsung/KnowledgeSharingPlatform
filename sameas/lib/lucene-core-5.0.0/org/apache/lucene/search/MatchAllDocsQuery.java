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

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.ToStringUtils;
import org.apache.lucene.util.Bits;

import java.util.Set;
import java.io.IOException;

/**
 * A query that matches all documents.
 *
 */
public class MatchAllDocsQuery extends Query {

  private class MatchAllScorer extends Scorer {
    final float score;
    private int doc = -1;
    private final int maxDoc;
    private final Bits liveDocs;

    MatchAllScorer(IndexReader reader, Bits liveDocs, Weight w, float score) {
      super(w);
      this.liveDocs = liveDocs;
      this.score = score;
      maxDoc = reader.maxDoc();
    }

    @Override
    public int docID() {
      return doc;
    }

    @Override
    public int nextDoc() throws IOException {
      doc++;
      while(liveDocs != null && doc < maxDoc && !liveDocs.get(doc)) {
        doc++;
      }
      if (doc == maxDoc) {
        doc = NO_MORE_DOCS;
      }
      return doc;
    }
    
    @Override
    public float score() {
      return score;
    }

    @Override
    public int freq() {
      return 1;
    }

    @Override
    public int advance(int target) throws IOException {
      doc = target-1;
      return nextDoc();
    }

    @Override
    public long cost() {
      return maxDoc;
    }
  }

  private class MatchAllDocsWeight extends Weight {
    private float queryWeight;
    private float queryNorm;

    public MatchAllDocsWeight(IndexSearcher searcher) {
    }

    @Override
    public String toString() {
      return "weight(" + MatchAllDocsQuery.this + ")";
    }

    @Override
    public Query getQuery() {
      return MatchAllDocsQuery.this;
    }

    @Override
    public float getValueForNormalization() {
      queryWeight = getBoost();
      return queryWeight * queryWeight;
    }

    @Override
    public void normalize(float queryNorm, float topLevelBoost) {
      this.queryNorm = queryNorm * topLevelBoost;
      queryWeight *= this.queryNorm;
    }

    @Override
    public Scorer scorer(LeafReaderContext context, Bits acceptDocs) throws IOException {
      return new MatchAllScorer(context.reader(), acceptDocs, this, queryWeight);
    }

    @Override
    public Explanation explain(LeafReaderContext context, int doc) {
      // explain query weight
      Explanation queryExpl = new ComplexExplanation
        (true, queryWeight, "MatchAllDocsQuery, product of:");
      if (getBoost() != 1.0f) {
        queryExpl.addDetail(new Explanation(getBoost(),"boost"));
      }
      queryExpl.addDetail(new Explanation(queryNorm,"queryNorm"));

      return queryExpl;
    }
  }

  @Override
  public Weight createWeight(IndexSearcher searcher) {
    return new MatchAllDocsWeight(searcher);
  }

  @Override
  public void extractTerms(Set<Term> terms) {
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("*:*");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof MatchAllDocsQuery))
      return false;
    MatchAllDocsQuery other = (MatchAllDocsQuery) o;
    return this.getBoost() == other.getBoost();
  }

  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost()) ^ 0x1AA71190;
  }
}
