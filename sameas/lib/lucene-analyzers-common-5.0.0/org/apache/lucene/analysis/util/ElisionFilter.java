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
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * Removes elisions from a {@link TokenStream}. For example, "l'avion" (the plane) will be
 * tokenized as "avion" (plane).
 * 
 * @see <a href="http://fr.wikipedia.org/wiki/%C3%89lision">Elision in Wikipedia</a>
 */
public final class ElisionFilter extends TokenFilter {
  private final CharArraySet articles;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  
  /**
   * Constructs an elision filter with a Set of stop words
   * @param input the source {@link TokenStream}
   * @param articles a set of stopword articles
   */
  public ElisionFilter(TokenStream input, CharArraySet articles) {
    super(input);
    this.articles = articles;
  }

  /**
   * Increments the {@link TokenStream} with a {@link CharTermAttribute} without elisioned start
   */
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      char[] termBuffer = termAtt.buffer();
      int termLength = termAtt.length();

      int index = -1;
      for (int i = 0; i < termLength; i++) {
        char ch = termBuffer[i];
        if (ch == '\'' || ch == '\u2019') {
          index = i;
          break;
        }
      }

      // An apostrophe has been found. If the prefix is an article strip it off.
      if (index >= 0 && articles.contains(termBuffer, 0, index)) {
        termAtt.copyBuffer(termBuffer, index + 1, termLength - (index + 1));
      }

      return true;
    } else {
      return false;
    }
  }
}
