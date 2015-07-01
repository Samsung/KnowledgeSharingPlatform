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
import org.apache.lucene.search.TermStatistics;

/**
 * Provides the ability to use a different {@link Similarity} for different fields.
 * <p>
 * Subclasses should implement {@link #get(String)} to return an appropriate
 * Similarity (for example, using field-specific parameter values) for the field.
 * 
 * @lucene.experimental
 */
public abstract class PerFieldSimilarityWrapper extends Similarity {
  
  /**
   * Sole constructor. (For invocation by subclass 
   * constructors, typically implicit.)
   */
  public PerFieldSimilarityWrapper() {}

  @Override
  public final long computeNorm(FieldInvertState state) {
    return get(state.getName()).computeNorm(state);
  }

  @Override
  public final SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics... termStats) {
    PerFieldSimWeight weight = new PerFieldSimWeight();
    weight.delegate = get(collectionStats.field());
    weight.delegateWeight = weight.delegate.computeWeight(queryBoost, collectionStats, termStats);
    return weight;
  }

  @Override
  public final SimScorer simScorer(SimWeight weight, LeafReaderContext context) throws IOException {
    PerFieldSimWeight perFieldWeight = (PerFieldSimWeight) weight;
    return perFieldWeight.delegate.simScorer(perFieldWeight.delegateWeight, context);
  }
  
  /** 
   * Returns a {@link Similarity} for scoring a field.
   */
  public abstract Similarity get(String name);
  
  static class PerFieldSimWeight extends SimWeight {
    Similarity delegate;
    SimWeight delegateWeight;
    
    @Override
    public float getValueForNormalization() {
      return delegateWeight.getValueForNormalization();
    }
    
    @Override
    public void normalize(float queryNorm, float topLevelBoost) {
      delegateWeight.normalize(queryNorm, topLevelBoost);
    }
  }
}
