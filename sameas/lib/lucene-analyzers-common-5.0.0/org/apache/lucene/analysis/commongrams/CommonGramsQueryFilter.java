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
package org.apache.lucene.analysis.commongrams;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import static org.apache.lucene.analysis.commongrams.CommonGramsFilter.GRAM_TYPE;

/**
 * Wrap a CommonGramsFilter optimizing phrase queries by only returning single
 * words when they are not a member of a bigram.
 * 
 * Example:
 * <ul>
 * <li>query input to CommonGramsFilter: "the rain in spain falls mainly"
 * <li>output of CommomGramsFilter/input to CommonGramsQueryFilter:
 * |"the, "the-rain"|"rain" "rain-in"|"in, "in-spain"|"spain"|"falls"|"mainly"
 * <li>output of CommonGramsQueryFilter:"the-rain", "rain-in" ,"in-spain",
 * "falls", "mainly"
 * </ul>
 */

/*
 * See:http://hudson.zones.apache.org/hudson/job/Lucene-trunk/javadoc//all/org/apache/lucene/analysis/TokenStream.html and
 * http://svn.apache.org/viewvc/lucene/dev/trunk/lucene/src/java/org/apache/lucene/analysis/package.html?revision=718798
 */
public final class CommonGramsQueryFilter extends TokenFilter {

  private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);
  private final PositionIncrementAttribute posIncAttribute = addAttribute(PositionIncrementAttribute.class);
  
  private State previous;
  private String previousType;
  private boolean exhausted;

  /**
   * Constructs a new CommonGramsQueryFilter based on the provided CommomGramsFilter 
   * 
   * @param input CommonGramsFilter the QueryFilter will use
   */
  public CommonGramsQueryFilter(CommonGramsFilter input) {
    super(input);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset() throws IOException {
    super.reset();
    previous = null;
    previousType = null;
    exhausted = false;
  }
  
  /**
   * Output bigrams whenever possible to optimize queries. Only output unigrams
   * when they are not a member of a bigram. Example:
   * <ul>
   * <li>input: "the rain in spain falls mainly"
   * <li>output:"the-rain", "rain-in" ,"in-spain", "falls", "mainly"
   * </ul>
   */
  @Override
  public boolean incrementToken() throws IOException {
    while (!exhausted && input.incrementToken()) {
      State current = captureState();

      if (previous != null && !isGramType()) {
        restoreState(previous);
        previous = current;
        previousType = typeAttribute.type();
        
        if (isGramType()) {
          posIncAttribute.setPositionIncrement(1);
        }
        return true;
      }

      previous = current;
    }

    exhausted = true;

    if (previous == null || GRAM_TYPE.equals(previousType)) {
      return false;
    }
    
    restoreState(previous);
    previous = null;
    
    if (isGramType()) {
      posIncAttribute.setPositionIncrement(1);
    }
    return true;
  }

  // ================================================= Helper Methods ================================================

  /**
   * Convenience method to check if the current type is a gram type
   * 
   * @return {@code true} if the current type is a gram type, {@code false} otherwise
   */
  public boolean isGramType() {
    return GRAM_TYPE.equals(typeAttribute.type());
  }
}
