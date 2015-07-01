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

import java.io.IOException;

/**
 * This TokenFilter limits the number of tokens while indexing. It is
 * a replacement for the maximum field length setting inside {@link org.apache.lucene.index.IndexWriter}.
 * <p>
 * By default, this filter ignores any tokens in the wrapped {@code TokenStream}
 * once the limit has been reached, which can result in {@code reset()} being 
 * called prior to {@code incrementToken()} returning {@code false}.  For most 
 * {@code TokenStream} implementations this should be acceptable, and faster 
 * then consuming the full stream. If you are wrapping a {@code TokenStream} 
 * which requires that the full stream of tokens be exhausted in order to 
 * function properly, use the 
 * {@link #LimitTokenCountFilter(TokenStream,int,boolean) consumeAllTokens} 
 * option.
 */
public final class LimitTokenCountFilter extends TokenFilter {

  private final int maxTokenCount;
  private final boolean consumeAllTokens;
  private int tokenCount = 0;
  private boolean exhausted = false;

  /**
   * Build a filter that only accepts tokens up to a maximum number.
   * This filter will not consume any tokens beyond the maxTokenCount limit
   *
   * @see #LimitTokenCountFilter(TokenStream,int,boolean)
   */
  public LimitTokenCountFilter(TokenStream in, int maxTokenCount) {
    this(in, maxTokenCount, false);
  }

  /**
   * Build an filter that limits the maximum number of tokens per field.
   * @param in the stream to wrap
   * @param maxTokenCount max number of tokens to produce
   * @param consumeAllTokens whether all tokens from the input must be consumed even if maxTokenCount is reached.
   */
  public LimitTokenCountFilter(TokenStream in, int maxTokenCount, boolean consumeAllTokens) {
    super(in);
    if (maxTokenCount < 1) {
      throw new IllegalArgumentException("maxTokenCount must be greater than zero");
    }
    this.maxTokenCount = maxTokenCount;
    this.consumeAllTokens = consumeAllTokens;
  }
  
  @Override
  public boolean incrementToken() throws IOException {
    if (exhausted) {
      return false;
    } else if (tokenCount < maxTokenCount) {
      if (input.incrementToken()) {
        tokenCount++;
        return true;
      } else {
        exhausted = true;
        return false;
      }
    } else {
      while (consumeAllTokens && input.incrementToken()) { /* NOOP */ }
      return false;
    }
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    tokenCount = 0;
    exhausted = false;
  }
}
