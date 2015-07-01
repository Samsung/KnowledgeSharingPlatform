package org.apache.lucene.analysis.util;

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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

/**
 * Backcompat FilteringTokenFilter for versions 4.3 and before.
 * @deprecated Use {@link org.apache.lucene.analysis.util.FilteringTokenFilter}
 */
@Deprecated
public abstract class Lucene43FilteringTokenFilter extends TokenFilter {

  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  private boolean enablePositionIncrements; // no init needed, as ctor enforces setting value!
  private boolean first = true; // only used when not preserving gaps

  public Lucene43FilteringTokenFilter(boolean enablePositionIncrements, TokenStream input){
    super(input);
    this.enablePositionIncrements = enablePositionIncrements;
  }

  /** Override this method and return if the current input token should be returned by {@link #incrementToken}. */
  protected abstract boolean accept() throws IOException;

  @Override
  public final boolean incrementToken() throws IOException {
    if (enablePositionIncrements) {
      int skippedPositions = 0;
      while (input.incrementToken()) {
        if (accept()) {
          if (skippedPositions != 0) {
            posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
          }
          return true;
        }
        skippedPositions += posIncrAtt.getPositionIncrement();
      }
    } else {
      while (input.incrementToken()) {
        if (accept()) {
          if (first) {
            // first token having posinc=0 is illegal.
            if (posIncrAtt.getPositionIncrement() == 0) {
              posIncrAtt.setPositionIncrement(1);
            }
            first = false;
          }
          return true;
        }
      }
    }
    // reached EOS -- return false
    return false;
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    first = true;
  }
}
