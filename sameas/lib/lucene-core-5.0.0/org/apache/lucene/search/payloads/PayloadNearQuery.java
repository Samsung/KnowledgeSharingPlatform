package org.apache.lucene.search.payloads;

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
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.search.spans.NearSpansOrdered;
import org.apache.lucene.search.spans.NearSpansUnordered;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanScorer;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.ToStringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * This class is very similar to
 * {@link org.apache.lucene.search.spans.SpanNearQuery} except that it factors
 * in the value of the payloads located at each of the positions where the
 * {@link org.apache.lucene.search.spans.TermSpans} occurs.
 * <p/>
 * NOTE: In order to take advantage of this with the default scoring implementation
 * ({@link DefaultSimilarity}), you must override {@link DefaultSimilarity#scorePayload(int, int, int, BytesRef)},
 * which returns 1 by default.
 * <p/>
 * Payload scores are aggregated using a pluggable {@link PayloadFunction}.
 * 
 * @see org.apache.lucene.search.similarities.Similarity.SimScorer#computePayloadFactor(int, int, int, BytesRef)
 */
public class PayloadNearQuery extends SpanNearQuery {
  protected String fieldName;
  protected PayloadFunction function;

  public PayloadNearQuery(SpanQuery[] clauses, int slop, boolean inOrder) {
    this(clauses, slop, inOrder, new AveragePayloadFunction());
  }

  public PayloadNearQuery(SpanQuery[] clauses, int slop, boolean inOrder,
      PayloadFunction function) {
    super(clauses, slop, inOrder);
    fieldName = clauses[0].getField(); // all clauses must have same field
    this.function = function;
  }

  @Override
  public Weight createWeight(IndexSearcher searcher) throws IOException {
    return new PayloadNearSpanWeight(this, searcher);
  }

  @Override
  public PayloadNearQuery clone() {
    int sz = clauses.size();
    SpanQuery[] newClauses = new SpanQuery[sz];

    for (int i = 0; i < sz; i++) {
      newClauses[i] = (SpanQuery) clauses.get(i).clone();
    }
    PayloadNearQuery boostingNearQuery = new PayloadNearQuery(newClauses, slop,
        inOrder, function);
    boostingNearQuery.setBoost(getBoost());
    return boostingNearQuery;
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("payloadNear([");
    Iterator<SpanQuery> i = clauses.iterator();
    while (i.hasNext()) {
      SpanQuery clause = i.next();
      buffer.append(clause.toString(field));
      if (i.hasNext()) {
        buffer.append(", ");
      }
    }
    buffer.append("], ");
    buffer.append(slop);
    buffer.append(", ");
    buffer.append(inOrder);
    buffer.append(")");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
    result = prime * result + ((function == null) ? 0 : function.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    PayloadNearQuery other = (PayloadNearQuery) obj;
    if (fieldName == null) {
      if (other.fieldName != null)
        return false;
    } else if (!fieldName.equals(other.fieldName))
      return false;
    if (function == null) {
      if (other.function != null)
        return false;
    } else if (!function.equals(other.function))
      return false;
    return true;
  }

  public class PayloadNearSpanWeight extends SpanWeight {
    public PayloadNearSpanWeight(SpanQuery query, IndexSearcher searcher)
        throws IOException {
      super(query, searcher);
    }

    @Override
    public Scorer scorer(LeafReaderContext context, Bits acceptDocs) throws IOException {
      return new PayloadNearSpanScorer(query.getSpans(context, acceptDocs, termContexts), this,
          similarity, similarity.simScorer(stats, context));
    }
    
    @Override
    public Explanation explain(LeafReaderContext context, int doc) throws IOException {
      PayloadNearSpanScorer scorer = (PayloadNearSpanScorer) scorer(context, context.reader().getLiveDocs());
      if (scorer != null) {
        int newDoc = scorer.advance(doc);
        if (newDoc == doc) {
          float freq = scorer.freq();
          SimScorer docScorer = similarity.simScorer(stats, context);
          Explanation expl = new Explanation();
          expl.setDescription("weight("+getQuery()+" in "+doc+") [" + similarity.getClass().getSimpleName() + "], result of:");
          Explanation scoreExplanation = docScorer.explain(doc, new Explanation(freq, "phraseFreq=" + freq));
          expl.addDetail(scoreExplanation);
          expl.setValue(scoreExplanation.getValue());
          String field = ((SpanQuery)getQuery()).getField();
          // now the payloads part
          Explanation payloadExpl = function.explain(doc, field, scorer.payloadsSeen, scorer.payloadScore);
          // combined
          ComplexExplanation result = new ComplexExplanation();
          result.addDetail(expl);
          result.addDetail(payloadExpl);
          result.setValue(expl.getValue() * payloadExpl.getValue());
          result.setDescription("PayloadNearQuery, product of:");
          return result;
        }
      }
      
      return new ComplexExplanation(false, 0.0f, "no matching term");
    }
  }

  public class PayloadNearSpanScorer extends SpanScorer {
    Spans spans;
    protected float payloadScore;
    private int payloadsSeen;

    protected PayloadNearSpanScorer(Spans spans, Weight weight,
        Similarity similarity, Similarity.SimScorer docScorer) throws IOException {
      super(spans, weight, docScorer);
      this.spans = spans;
    }

    // Get the payloads associated with all underlying subspans
    public void getPayloads(Spans[] subSpans) throws IOException {
      for (int i = 0; i < subSpans.length; i++) {
        if (subSpans[i] instanceof NearSpansOrdered) {
          if (((NearSpansOrdered) subSpans[i]).isPayloadAvailable()) {
            processPayloads(((NearSpansOrdered) subSpans[i]).getPayload(),
                subSpans[i].start(), subSpans[i].end());
          }
          getPayloads(((NearSpansOrdered) subSpans[i]).getSubSpans());
        } else if (subSpans[i] instanceof NearSpansUnordered) {
          if (((NearSpansUnordered) subSpans[i]).isPayloadAvailable()) {
            processPayloads(((NearSpansUnordered) subSpans[i]).getPayload(),
                subSpans[i].start(), subSpans[i].end());
          }
          getPayloads(((NearSpansUnordered) subSpans[i]).getSubSpans());
        }
      }
    }

    // TODO change the whole spans api to use bytesRef, or nuke spans
    BytesRef scratch = new BytesRef();

    /**
     * By default, uses the {@link PayloadFunction} to score the payloads, but
     * can be overridden to do other things.
     * 
     * @param payLoads The payloads
     * @param start The start position of the span being scored
     * @param end The end position of the span being scored
     * 
     * @see Spans
     */
    protected void processPayloads(Collection<byte[]> payLoads, int start, int end) {
      for (final byte[] thePayload : payLoads) {
        scratch.bytes = thePayload;
        scratch.offset = 0;
        scratch.length = thePayload.length;
        payloadScore = function.currentScore(doc, fieldName, start, end,
            payloadsSeen, payloadScore, docScorer.computePayloadFactor(doc,
                spans.start(), spans.end(), scratch));
        ++payloadsSeen;
      }
    }

    //
    @Override
    protected boolean setFreqCurrentDoc() throws IOException {
        if (!more) {
            return false;
          }
          doc = spans.doc();
          freq = 0.0f;
          payloadScore = 0;
          payloadsSeen = 0;
          do {
            int matchLength = spans.end() - spans.start();
            freq += docScorer.computeSlopFactor(matchLength);
            Spans[] spansArr = new Spans[1];
            spansArr[0] = spans;
            getPayloads(spansArr);            
            more = spans.next();
          } while (more && (doc == spans.doc()));
          return true;
    }

    @Override
    public float score() throws IOException {

      return super.score()
          * function.docScore(doc, fieldName, payloadsSeen, payloadScore);
    }
  }

}
