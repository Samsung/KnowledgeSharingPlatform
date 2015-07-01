package org.apache.lucene.analysis.ga;

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

/**
 * Normalises token text to lower case, handling t-prothesis
 * and n-eclipsis (i.e., that 'nAthair' should become 'n-athair')
 */
public final class IrishLowerCaseFilter extends TokenFilter {
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  /**
   * Create an IrishLowerCaseFilter that normalises Irish token text.
   */
  public IrishLowerCaseFilter(TokenStream in) {
    super(in);
  }
  
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      char[] chArray = termAtt.buffer();
      int chLen = termAtt.length();
      int idx = 0;

      if (chLen > 1 && (chArray[0] == 'n' || chArray[0] == 't') && isUpperVowel(chArray[1])) {
        chArray = termAtt.resizeBuffer(chLen + 1);
        for (int i = chLen; i > 1; i--) {
          chArray[i] = chArray[i - 1];
        }
        chArray[1] = '-';
        termAtt.setLength(chLen + 1);
        idx = 2;
        chLen = chLen + 1;
      }

      for (int i = idx; i < chLen;) {
        i += Character.toChars(Character.toLowerCase(chArray[i]), chArray, i);
       }
      return true;
    } else {
      return false;
    }
  }
  
  private boolean isUpperVowel (int v) {
    switch (v) {
      case 'A':
      case 'E':
      case 'I':
      case 'O':
      case 'U':
      // vowels with acute accent (fada)
      case '\u00c1':
      case '\u00c9':
      case '\u00cd':
      case '\u00d3':
      case '\u00da':
        return true;
      default:
        return false;
    }
  }
}
