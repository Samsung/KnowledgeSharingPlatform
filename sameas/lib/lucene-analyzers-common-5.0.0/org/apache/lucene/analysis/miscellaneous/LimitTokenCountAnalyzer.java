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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.AnalyzerWrapper;

/**
 * This Analyzer limits the number of tokens while indexing. It is
 * a replacement for the maximum field length setting inside {@link org.apache.lucene.index.IndexWriter}.
 * @see LimitTokenCountFilter
 */
public final class LimitTokenCountAnalyzer extends AnalyzerWrapper {
  private final Analyzer delegate;
  private final int maxTokenCount;
  private final boolean consumeAllTokens;

  /**
   * Build an analyzer that limits the maximum number of tokens per field.
   * This analyzer will not consume any tokens beyond the maxTokenCount limit
   *
   * @see #LimitTokenCountAnalyzer(Analyzer,int,boolean)
   */
  public LimitTokenCountAnalyzer(Analyzer delegate, int maxTokenCount) {
    this(delegate, maxTokenCount, false);
  }
  /**
   * Build an analyzer that limits the maximum number of tokens per field.
   * @param delegate the analyzer to wrap
   * @param maxTokenCount max number of tokens to produce
   * @param consumeAllTokens whether all tokens from the delegate should be consumed even if maxTokenCount is reached.
   */
  public LimitTokenCountAnalyzer(Analyzer delegate, int maxTokenCount, boolean consumeAllTokens) {
    super(delegate.getReuseStrategy());
    this.delegate = delegate;
    this.maxTokenCount = maxTokenCount;
    this.consumeAllTokens = consumeAllTokens;
  }

  @Override
  protected Analyzer getWrappedAnalyzer(String fieldName) {
    return delegate;
  }

  @Override
  protected TokenStreamComponents wrapComponents(String fieldName, TokenStreamComponents components) {
    return new TokenStreamComponents(components.getTokenizer(),
      new LimitTokenCountFilter(components.getTokenStream(), maxTokenCount, consumeAllTokens));
  }
  
  @Override
  public String toString() {
    return "LimitTokenCountAnalyzer(" + delegate.toString() + ", maxTokenCount=" + maxTokenCount + ", consumeAllTokens=" + consumeAllTokens + ")";
  }
}
