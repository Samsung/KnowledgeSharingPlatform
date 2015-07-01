package org.apache.lucene.analysis.de;

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
import org.apache.lucene.analysis.util.StemmerUtil;

/**
 * Normalizes German characters according to the heuristics
 * of the <a href="http://snowball.tartarus.org/algorithms/german2/stemmer.html">
 * German2 snowball algorithm</a>.
 * It allows for the fact that ä, ö and ü are sometimes written as ae, oe and ue.
 * <p>
 * <ul>
 *   <li> 'ß' is replaced by 'ss'
 *   <li> 'ä', 'ö', 'ü' are replaced by 'a', 'o', 'u', respectively.
 *   <li> 'ae' and 'oe' are replaced by 'a', and 'o', respectively.
 *   <li> 'ue' is replaced by 'u', when not following a vowel or q.
 * </ul>
 * <p>
 * This is useful if you want this normalization without using
 * the German2 stemmer, or perhaps no stemming at all.
 */
public final class GermanNormalizationFilter extends TokenFilter {
  // FSM with 3 states:
  private static final int N = 0; /* ordinary state */
  private static final int V = 1; /* stops 'u' from entering umlaut state */
  private static final int U = 2; /* umlaut state, allows e-deletion */

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  
  public GermanNormalizationFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      int state = N;
      char buffer[] = termAtt.buffer();
      int length = termAtt.length();
      for (int i = 0; i < length; i++) {
        final char c = buffer[i];
        switch(c) {
          case 'a':
          case 'o':
            state = U;
            break;
          case 'u':
            state = (state == N) ? U : V;
            break;
          case 'e':
            if (state == U)
              length = StemmerUtil.delete(buffer, i--, length);
            state = V;
            break;
          case 'i':
          case 'q':
          case 'y':
            state = V;
            break;
          case 'ä':
            buffer[i] = 'a';
            state = V;
            break;
          case 'ö':
            buffer[i] = 'o';
            state = V;
            break;
          case 'ü': 
            buffer[i] = 'u';
            state = V;
            break;
          case 'ß':
            buffer[i++] = 's';
            buffer = termAtt.resizeBuffer(1+length);
            if (i < length)
              System.arraycopy(buffer, i, buffer, i+1, (length-i));
            buffer[i] = 's';
            length++;
            state = N;
            break;
          default:
            state = N;
        }
      }
      termAtt.setLength(length);
      return true;
    } else {
      return false;
    }
  }
}
