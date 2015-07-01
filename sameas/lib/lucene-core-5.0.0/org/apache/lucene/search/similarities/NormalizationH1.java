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

/**
 * Normalization model that assumes a uniform distribution of the term frequency.
 * <p>While this model is parameterless in the
 * <a href="http://citeseer.ist.psu.edu/viewdoc/summary?doi=10.1.1.101.742">
 * original article</a>, <a href="http://dl.acm.org/citation.cfm?id=1835490">
 * information-based models</a> (see {@link IBSimilarity}) introduced a
 * multiplying factor.
 * The default value for the {@code c} parameter is {@code 1}.</p>
 * @lucene.experimental
 */
public class NormalizationH1 extends Normalization {
  private final float c;
  
  /**
   * Creates NormalizationH1 with the supplied parameter <code>c</code>.
   * @param c hyper-parameter that controls the term frequency 
   * normalization with respect to the document length.
   */
  public NormalizationH1(float c) {
    this.c = c;
  }
  
  /**
   * Calls {@link #NormalizationH1(float) NormalizationH1(1)}
   */
  public NormalizationH1() {
    this(1);
  }
  
  @Override
  public final float tfn(BasicStats stats, float tf, float len) {
    return tf * stats.getAvgFieldLength() / len;
  }

  @Override
  public String toString() {
    return "1";
  }
  
  /**
   * Returns the <code>c</code> parameter.
   * @see #NormalizationH1(float)
   */
  public float getC() {
    return c;
  }
}
