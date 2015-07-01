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

import org.apache.lucene.search.Explanation;

/**
 * This class acts as the base class for the implementations of the term
 * frequency normalization methods in the DFR framework.
 * 
 * @see DFRSimilarity
 * @lucene.experimental
 */
public abstract class Normalization {
  
  /**
   * Sole constructor. (For invocation by subclass 
   * constructors, typically implicit.)
   */
  public Normalization() {}

  /** Returns the normalized term frequency.
   * @param len the field length. */
  public abstract float tfn(BasicStats stats, float tf, float len);
  
  /** Returns an explanation for the normalized term frequency.
   * <p>The default normalization methods use the field length of the document
   * and the average field length to compute the normalized term frequency.
   * This method provides a generic explanation for such methods.
   * Subclasses that use other statistics must override this method.</p>
   */
  public Explanation explain(BasicStats stats, float tf, float len) {
    Explanation result = new Explanation();
    result.setDescription(getClass().getSimpleName() + ", computed from: ");
    result.setValue(tfn(stats, tf, len));
    result.addDetail(new Explanation(tf, "tf"));
    result.addDetail(
        new Explanation(stats.getAvgFieldLength(), "avgFieldLength"));
    result.addDetail(new Explanation(len, "len"));
    return result;
  }

  /** Implementation used when there is no normalization. */
  public static final class NoNormalization extends Normalization {
    
    /** Sole constructor: parameter-free */
    public NoNormalization() {}
    
    @Override
    public final float tfn(BasicStats stats, float tf, float len) {
      return tf;
    }

    @Override
    public final Explanation explain(BasicStats stats, float tf, float len) {
      return new Explanation(1, "no normalization");
    }
    
    @Override
    public String toString() {
      return "";
    }
  }
  
  /**
   * Subclasses must override this method to return the code of the
   * normalization formula. Refer to the original paper for the list. 
   */
  @Override
  public abstract String toString();
}
