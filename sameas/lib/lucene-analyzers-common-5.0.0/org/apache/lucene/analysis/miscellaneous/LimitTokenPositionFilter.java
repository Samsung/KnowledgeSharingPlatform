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
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

/**
 * This TokenFilter limits its emitted tokens to those with positions that
 * are not greater than the configured limit.
 * <p>
 * By default, this filter ignores any tokens in the wrapped {@code TokenStream}
 * once the limit has been exceeded, which can result in {@code reset()} being 
 * called prior to {@code incrementToken()} returning {@code false}.  For most 
 * {@code TokenStream} implementations this should be acceptable, and faster 
 * then consuming the full stream. If you are wrapping a {@code TokenStream}
 * which requires that the full stream of tokens be exhausted in order to 
 * function properly, use the 
 * {@link #LimitTokenPositionFilter(TokenStream,int,boolean) consumeAllTokens}
 * option.
 */
public final class LimitTokenPositionFilter extends TokenFilter {

  private final int maxTokenPosition;
  private final boolean consumeAllTokens;
  private int tokenPosition = 0;
  private boolean exhausted = false;
  private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

  /**
   * Build a filter that only accepts tokens up to and including the given maximum position.
   * This filter will not consume any tokens with position greater than the maxTokenPosition limit.

   * @param in the stream to wrap
   * @param maxTokenPosition max position of tokens to produce (1st token always has position 1)
   *                         
   * @see #LimitTokenPositionFilter(TokenStream,int,boolean)
   */
  public LimitTokenPositionFilter(TokenStream in, int maxTokenPosition) {
    this(in, maxTokenPosition, false);
  }

  /**
   * Build a filter that limits the maximum position of tokens to emit.
   * 
   * @param in the stream to wrap
   * @param maxTokenPosition max position of tokens to produce (1st token always has position 1)
   * @param consumeAllTokens whether all tokens from the wrapped input stream must be consumed
   *                         even if maxTokenPosition is exceeded.
   */
  public LimitTokenPositionFilter(TokenStream in, int maxTokenPosition, boolean consumeAllTokens) {
    super(in);
    if (maxTokenPosition < 1) {
      throw new IllegalArgumentException("maxTokenPosition must be greater than zero");
    }
    this.maxTokenPosition = maxTokenPosition;
    this.consumeAllTokens = consumeAllTokens;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (exhausted) {
      return false;
    }
    if (input.incrementToken()) {
      tokenPosition += posIncAtt.getPositionIncrement();
      if (tokenPosition <= maxTokenPosition) {
        return true;
      } else {
        while (consumeAllTokens && input.incrementToken()) { /* NOOP */ }
        exhausted = true;
        return false;
      }
    } else {
      exhausted = true;
      return false;
    }
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    tokenPosition = 0;
    exhausted = false;
  }
}
