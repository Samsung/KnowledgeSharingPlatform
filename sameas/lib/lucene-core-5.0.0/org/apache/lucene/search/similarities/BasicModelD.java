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
 * Implements the approximation of the binomial model with the divergence
 * for DFR. The formula used in Lucene differs slightly from the one in the
 * original paper: to avoid underflow for small values of {@code N} and
 * {@code F}, {@code N} is increased by {@code 1} and
 * {@code F} is always increased by {@code tfn+1}.
 * <p>
 * WARNING: for terms that do not meet the expected random distribution
 * (e.g. stopwords), this model may give poor performance, such as
 * abnormally high scores for low tf values.
 * @lucene.experimental
 */
public class BasicModelD extends BasicModel {
  
  /** Sole constructor: parameter-free */
  public BasicModelD() {}
  
  @Override
  public final float score(BasicStats stats, float tfn) {
    // we have to ensure phi is always < 1 for tiny TTF values, otherwise nphi can go negative,
    // resulting in NaN. cleanest way is to unconditionally always add tfn to totalTermFreq
    // to create a 'normalized' F.
    double F = stats.getTotalTermFreq() + 1 + tfn;
    double phi = (double)tfn / F;
    double nphi = 1 - phi;
    double p = 1.0 / (stats.getNumberOfDocuments() + 1);
    double D = phi * log2(phi / p) + nphi * log2(nphi / (1 - p));
    return (float)(D * F + 0.5 * log2(1 + 2 * Math.PI * tfn * nphi));
  }
  
  @Override
  public String toString() {
    return "D";
  }
}
