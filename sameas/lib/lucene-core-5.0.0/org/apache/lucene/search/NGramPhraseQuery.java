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

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

/**
 * This is a {@link PhraseQuery} which is optimized for n-gram phrase query.
 * For example, when you query "ABCD" on a 2-gram field, you may want to use
 * NGramPhraseQuery rather than {@link PhraseQuery}, because NGramPhraseQuery
 * will {@link #rewrite(IndexReader)} the query to "AB/0 CD/2", while {@link PhraseQuery}
 * will query "AB/0 BC/1 CD/2" (where term/position).
 *
 */
public class NGramPhraseQuery extends PhraseQuery {
  private final int n;
  
  /**
   * Constructor that takes gram size.
   * @param n n-gram size
   */
  public NGramPhraseQuery(int n){
    super();
    this.n = n;
  }

  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    if(getSlop() != 0) return super.rewrite(reader);
    
    // check whether optimizable or not
    if(n < 2 || // non-overlap n-gram cannot be optimized
        getTerms().length < 3)  // too short to optimize
      return super.rewrite(reader);

    // check all posIncrement is 1
    // if not, cannot optimize
    int[] positions = getPositions();
    Term[] terms = getTerms();
    int prevPosition = positions[0];
    for(int i = 1; i < positions.length; i++){
      int pos = positions[i];
      if(prevPosition + 1 != pos) return super.rewrite(reader);
      prevPosition = pos;
    }

    // now create the new optimized phrase query for n-gram
    PhraseQuery optimized = new PhraseQuery();
    optimized.setBoost(getBoost());
    int pos = 0;
    final int lastPos = terms.length - 1;
    for(int i = 0; i < terms.length; i++){
      if(pos % n == 0 || pos >= lastPos){
        optimized.add(terms[i], positions[i]);
      }
      pos++;
    }
    
    return optimized;
  }

  /** Returns true iff <code>o</code> is equal to this. */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof NGramPhraseQuery))
      return false;
    NGramPhraseQuery other = (NGramPhraseQuery)o;
    if(this.n != other.n) return false;
    return super.equals(other);
  }

  /** Returns a hash code value for this object.*/
  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost())
      ^ getSlop()
      ^ getTerms().hashCode()
      ^ getPositions().hashCode()
      ^ n;
  }
}
