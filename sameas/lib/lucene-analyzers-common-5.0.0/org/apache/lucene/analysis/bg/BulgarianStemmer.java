package org.apache.lucene.analysis.bg;

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

import static org.apache.lucene.analysis.util.StemmerUtil.*;

/**
 * Light Stemmer for Bulgarian.
 * <p>
 * Implements the algorithm described in:  
 * <i>
 * Searching Strategies for the Bulgarian Language
 * </i>
 * http://members.unine.ch/jacques.savoy/Papers/BUIR.pdf
 */
public class BulgarianStemmer {
  
  /**
   * Stem an input buffer of Bulgarian text.
   * 
   * @param s input buffer
   * @param len length of input buffer
   * @return length of input buffer after normalization
   */
  public int stem(final char s[], int len) {
    if (len < 4) // do not stem
      return len;
    
    if (len > 5 && endsWith(s, len, "ища"))
      return len - 3;
    
    len = removeArticle(s, len);
    len = removePlural(s, len);
    
    if (len > 3) {
      if (endsWith(s, len, "я"))
        len--;
      if (endsWith(s, len, "а") ||
          endsWith(s, len, "о") ||
          endsWith(s, len, "е"))
        len--;
    }
    
    // the rule to rewrite ен -> н is duplicated in the paper.
    // in the perl implementation referenced by the paper, this is fixed.
    // (it is fixed here as well)
    if (len > 4 && endsWith(s, len, "ен")) {
      s[len - 2] = 'н'; // replace with н
      len--;
    }
    
    if (len > 5 && s[len - 2] == 'ъ') {
      s[len - 2] = s[len - 1]; // replace ъN with N
      len--;
    }

    return len;
  }
  
  /**
   * Mainly remove the definite article
   * @param s input buffer
   * @param len length of input buffer
   * @return new stemmed length
   */
  private int removeArticle(final char s[], final int len) {
    if (len > 6 && endsWith(s, len, "ият"))
      return len - 3;
    
    if (len > 5) {
      if (endsWith(s, len, "ът") ||
          endsWith(s, len, "то") ||
          endsWith(s, len, "те") ||
          endsWith(s, len, "та") ||
          endsWith(s, len, "ия"))
        return len - 2;
    }
    
    if (len > 4 && endsWith(s, len, "ят"))
      return len - 2;

    return len;
  }
  
  private int removePlural(final char s[], final int len) {
    if (len > 6) {
      if (endsWith(s, len, "овци"))
        return len - 3; // replace with о
      if (endsWith(s, len, "ове"))
        return len - 3;
      if (endsWith(s, len, "еве")) {
        s[len - 3] = 'й'; // replace with й
        return len - 2;
      }
    }
    
    if (len > 5) {
      if (endsWith(s, len, "ища"))
        return len - 3;
      if (endsWith(s, len, "та"))
        return len - 2;
      if (endsWith(s, len, "ци")) {
        s[len - 2] = 'к'; // replace with к
        return len - 1;
      }
      if (endsWith(s, len, "зи")) {
        s[len - 2] = 'г'; // replace with г
        return len - 1;
      }
      
      if (s[len - 3] == 'е' && s[len - 1] == 'и') {
        s[len - 3] = 'я'; // replace е with я, remove и
        return len - 1;
      }
    }
    
    if (len > 4) {
      if (endsWith(s, len, "си")) {
        s[len - 2] = 'х'; // replace with х
        return len - 1;
      }
      if (endsWith(s, len, "и"))
        return len - 1;
    }
    
    return len;
  }
}
