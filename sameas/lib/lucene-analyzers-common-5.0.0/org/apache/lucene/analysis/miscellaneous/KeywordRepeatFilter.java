package org.apache.lucene.analysis.miscellaneous;

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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;


/**
 * This TokenFilter emits each incoming token twice once as keyword and once non-keyword, in other words once with
 * {@link KeywordAttribute#setKeyword(boolean)} set to <code>true</code> and once set to <code>false</code>.
 * This is useful if used with a stem filter that respects the {@link KeywordAttribute} to index the stemmed and the
 * un-stemmed version of a term into the same field.
 */
public final class KeywordRepeatFilter extends TokenFilter {

  private final KeywordAttribute keywordAttribute = addAttribute(KeywordAttribute.class);
  private final PositionIncrementAttribute posIncAttr = addAttribute(PositionIncrementAttribute.class);
  private State state;

  /**
   * Construct a token stream filtering the given input.
   */
  public KeywordRepeatFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (state != null) {
      restoreState(state);
      posIncAttr.setPositionIncrement(0);
      keywordAttribute.setKeyword(false);
      state = null;
      return true;
    }
    if (input.incrementToken()) {
      state = captureState();
      keywordAttribute.setKeyword(true);
      return true;
    }
    return false;
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    state = null;
  }
}
