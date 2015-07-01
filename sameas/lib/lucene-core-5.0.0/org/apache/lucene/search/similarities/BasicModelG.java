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

import static org.apache.lucene.search.similarities.SimilarityBase.log2;

/**
 * Geometric as limiting form of the Bose-Einstein model.  The formula used in Lucene differs
 * slightly from the one in the original paper: {@code F} is increased by {@code 1}
 * and {@code N} is increased by {@code F}.
 * @lucene.experimental
 */
public class BasicModelG extends BasicModel {
  
  /** Sole constructor: parameter-free */
  public BasicModelG() {}

  @Override
  public final float score(BasicStats stats, float tfn) {
    // just like in BE, approximation only holds true when F << N, so we use lambda = F / (N + F)
    double F = stats.getTotalTermFreq() + 1;
    double N = stats.getNumberOfDocuments();
    double lambda = F / (N + F);
    // -log(1 / (lambda + 1)) -> log(lambda + 1)
    return (float)(log2(lambda + 1) + tfn * log2((1 + lambda) / lambda));
  }

  @Override
  public String toString() {
    return "G";
  }
}
