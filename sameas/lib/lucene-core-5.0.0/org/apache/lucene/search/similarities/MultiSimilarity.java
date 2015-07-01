package org.apache.lucene.search.similarities;

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
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.util.BytesRef;

/**
 * Implements the CombSUM method for combining evidence from multiple
 * similarity values described in: Joseph A. Shaw, Edward A. Fox. 
 * In Text REtrieval Conference (1993), pp. 243-252
 * @lucene.experimental
 */
public class MultiSimilarity extends Similarity {
  /** the sub-similarities used to create the combined score */
  protected final Similarity sims[];
  
  /** Creates a MultiSimilarity which will sum the scores
   * of the provided <code>sims</code>. */
  public MultiSimilarity(Similarity sims[]) {
    this.sims = sims;
  }
  
  @Override
  public long computeNorm(FieldInvertState state) {
    return sims[0].computeNorm(state);
  }

  @Override
  public SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics... termStats) {
    SimWeight subStats[] = new SimWeight[sims.length];
    for (int i = 0; i < subStats.length; i++) {
      subStats[i] = sims[i].computeWeight(queryBoost, collectionStats, termStats);
    }
    return new MultiStats(subStats);
  }

  @Override
  public SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
    SimScorer subScorers[] = new SimScorer[sims.length];
    for (int i = 0; i < subScorers.length; i++) {
      subScorers[i] = sims[i].simScorer(((MultiStats)stats).subStats[i], context);
    }
    return new MultiSimScorer(subScorers);
  }
  
  static class MultiSimScorer extends SimScorer {
    private final SimScorer subScorers[];
    
    MultiSimScorer(SimScorer subScorers[]) {
      this.subScorers = subScorers;
    }
    
    @Override
    public float score(int doc, float freq) {
      float sum = 0.0f;
      for (SimScorer subScorer : subScorers) {
        sum += subScorer.score(doc, freq);
      }
      return sum;
    }

    @Override
    public Explanation explain(int doc, Explanation freq) {
      Explanation expl = new Explanation(score(doc, freq.getValue()), "sum of:");
      for (SimScorer subScorer : subScorers) {
        expl.addDetail(subScorer.explain(doc, freq));
      }
      return expl;
    }

    @Override
    public float computeSlopFactor(int distance) {
      return subScorers[0].computeSlopFactor(distance);
    }

    @Override
    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
      return subScorers[0].computePayloadFactor(doc, start, end, payload);
    }
  }

  static class MultiStats extends SimWeight {
    final SimWeight subStats[];
    
    MultiStats(SimWeight subStats[]) {
      this.subStats = subStats;
    }
    
    @Override
    public float getValueForNormalization() {
      float sum = 0.0f;
      for (SimWeight stat : subStats) {
        sum += stat.getValueForNormalization();
      }
      return sum / subStats.length;
    }

    @Override
    public void normalize(float queryNorm, float topLevelBoost) {
      for (SimWeight stat : subStats) {
        stat.normalize(queryNorm, topLevelBoost);
      }
    }
  }
}
