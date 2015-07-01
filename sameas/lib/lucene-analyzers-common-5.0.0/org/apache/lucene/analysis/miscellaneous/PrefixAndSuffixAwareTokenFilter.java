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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;

/**
 * Links two {@link PrefixAwareTokenFilter}.
 * <p/>
 * <b>NOTE:</b> This filter might not behave correctly if used with custom Attributes, i.e. Attributes other than
 * the ones located in org.apache.lucene.analysis.tokenattributes. 
 */
public class PrefixAndSuffixAwareTokenFilter extends TokenStream {

  private PrefixAwareTokenFilter suffix;

  public PrefixAndSuffixAwareTokenFilter(TokenStream prefix, TokenStream input, TokenStream suffix) {
    super(suffix);
    prefix = new PrefixAwareTokenFilter(prefix, input) {
      @Override
      public Token updateSuffixToken(Token suffixToken, Token lastInputToken) {
        return PrefixAndSuffixAwareTokenFilter.this.updateInputToken(suffixToken, lastInputToken);
      }
    };
    this.suffix = new PrefixAwareTokenFilter(prefix, suffix) {
      @Override
      public Token updateSuffixToken(Token suffixToken, Token lastInputToken) {
        return PrefixAndSuffixAwareTokenFilter.this.updateSuffixToken(suffixToken, lastInputToken);
      }
    };
  }

  public Token updateInputToken(Token inputToken, Token lastPrefixToken) {
    inputToken.setOffset(lastPrefixToken.endOffset() + inputToken.startOffset(), 
                         lastPrefixToken.endOffset() + inputToken.endOffset());
    return inputToken;
  }

  public Token updateSuffixToken(Token suffixToken, Token lastInputToken) {
    suffixToken.setOffset(lastInputToken.endOffset() + suffixToken.startOffset(),
                          lastInputToken.endOffset() + suffixToken.endOffset());
    return suffixToken;
  }


  @Override
  public final boolean incrementToken() throws IOException {
    return suffix.incrementToken();
  }

  @Override
  public void reset() throws IOException {
    suffix.reset();
  }


  @Override
  public void close() throws IOException {
    suffix.close();
  }

  @Override
  public void end() throws IOException {
    suffix.end();
  }
}
