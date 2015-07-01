package org.apache.lucene.search.spans;

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
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Expert-only.  Public for use by other weight implementations
 */
public class SpanWeight extends Weight {
  protected Similarity similarity;
  protected Map<Term,TermContext> termContexts;
  protected SpanQuery query;
  protected Similarity.SimWeight stats;

  public SpanWeight(SpanQuery query, IndexSearcher searcher)
    throws IOException {
    this.similarity = searcher.getSimilarity();
    this.query = query;
    
    termContexts = new HashMap<>();
    TreeSet<Term> terms = new TreeSet<>();
    query.extractTerms(terms);
    final IndexReaderContext context = searcher.getTopReaderContext();
    final TermStatistics termStats[] = new TermStatistics[terms.size()];
    int i = 0;
    for (Term term : terms) {
      TermContext state = TermContext.build(context, term);
      termStats[i] = searcher.termStatistics(term, state);
      termContexts.put(term, state);
      i++;
    }
    final String field = query.getField();
    if (field != null) {
      stats = similarity.computeWeight(query.getBoost(), 
                                       searcher.collectionStatistics(query.getField()), 
                                       termStats);
    }
  }

  @Override
  public Query getQuery() { return query; }

  @Override
  public float getValueForNormalization() throws IOException {
    return stats == null ? 1.0f : stats.getValueForNormalization();
  }

  @Override
  public void normalize(float queryNorm, float topLevelBoost) {
    if (stats != null) {
      stats.normalize(queryNorm, topLevelBoost);
    }
  }

  @Override
  public Scorer scorer(LeafReaderContext context, Bits acceptDocs) throws IOException {
    if (stats == null) {
      return null;
    } else {
      return new SpanScorer(query.getSpans(context, acceptDocs, termContexts), this, similarity.simScorer(stats, context));
    }
  }

  @Override
  public Explanation explain(LeafReaderContext context, int doc) throws IOException {
    SpanScorer scorer = (SpanScorer) scorer(context, context.reader().getLiveDocs());
    if (scorer != null) {
      int newDoc = scorer.advance(doc);
      if (newDoc == doc) {
        float freq = scorer.sloppyFreq();
        SimScorer docScorer = similarity.simScorer(stats, context);
        ComplexExplanation result = new ComplexExplanation();
        result.setDescription("weight("+getQuery()+" in "+doc+") [" + similarity.getClass().getSimpleName() + "], result of:");
        Explanation scoreExplanation = docScorer.explain(doc, new Explanation(freq, "phraseFreq=" + freq));
        result.addDetail(scoreExplanation);
        result.setValue(scoreExplanation.getValue());
        result.setMatch(true);          
        return result;
      }
    }
    
    return new ComplexExplanation(false, 0.0f, "no matching term");
  }
}
