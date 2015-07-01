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
 * Pareto-Zipf Normalization
 * @lucene.experimental
 */
public class NormalizationZ extends Normalization {
  final float z;

  /**
   * Calls {@link #NormalizationZ(float) NormalizationZ(0.3)}
   */
  public NormalizationZ() {
    this(0.30F);
  }

  /**
   * Creates NormalizationZ with the supplied parameter <code>z</code>.
   * @param z represents <code>A/(A+1)</code> where <code>A</code> 
   *          measures the specificity of the language.
   */
  public NormalizationZ(float z) {
    this.z = z;
  }
  
  @Override
  public float tfn(BasicStats stats, float tf, float len) {
    return (float)(tf * Math.pow(stats.avgFieldLength / len, z));
  }

  @Override
  public String toString() {
    return "Z(" + z + ")";
  }
  
  /**
   * Returns the parameter <code>z</code>
   * @see #NormalizationZ(float)
   */
  public float getZ() {
    return z;
  }
}
