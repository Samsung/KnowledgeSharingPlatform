package org.apache.lucene.analysis.cjk;

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
 * A {@link TokenFilter} that normalizes CJK width differences:
 * <ul>
 *   <li>Folds fullwidth ASCII variants into the equivalent basic latin
 *   <li>Folds halfwidth Katakana variants into the equivalent kana
 * </ul>
 * <p>
 * NOTE: this filter can be viewed as a (practical) subset of NFKC/NFKD
 * Unicode normalization. See the normalization support in the ICU package
 * for full normalization.
 */
public final class CJKWidthFilter extends TokenFilter {
  private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  
  /* halfwidth kana mappings: 0xFF65-0xFF9D 
   *
   * note: 0xFF9C and 0xFF9D are only mapped to 0x3099 and 0x309A
   * as a fallback when they cannot properly combine with a preceding 
   * character into a composed form.
   */
  private static final char KANA_NORM[] = new char[] {
    0x30fb, 0x30f2, 0x30a1, 0x30a3, 0x30a5, 0x30a7, 0x30a9, 0x30e3, 0x30e5,
    0x30e7, 0x30c3, 0x30fc, 0x30a2, 0x30a4, 0x30a6, 0x30a8, 0x30aa, 0x30ab,
    0x30ad, 0x30af, 0x30b1, 0x30b3, 0x30b5, 0x30b7, 0x30b9, 0x30bb, 0x30bd,
    0x30bf, 0x30c1, 0x30c4, 0x30c6, 0x30c8, 0x30ca, 0x30cb, 0x30cc, 0x30cd,
    0x30ce, 0x30cf, 0x30d2, 0x30d5, 0x30d8, 0x30db, 0x30de, 0x30df, 0x30e0,
    0x30e1, 0x30e2, 0x30e4, 0x30e6, 0x30e8, 0x30e9, 0x30ea, 0x30eb, 0x30ec,
    0x30ed, 0x30ef, 0x30f3, 0x3099, 0x309A
  };

  public CJKWidthFilter(TokenStream input) {
    super(input);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      char text[] = termAtt.buffer();
      int length = termAtt.length();
      for (int i = 0; i < length; i++) {
        final char ch = text[i];
        if (ch >= 0xFF01 && ch <= 0xFF5E) {
          // Fullwidth ASCII variants
          text[i] -= 0xFEE0;
        } else if (ch >= 0xFF65 && ch <= 0xFF9F) {
          // Halfwidth Katakana variants
          if ((ch == 0xFF9E || ch == 0xFF9F) && i > 0 && combine(text, i, ch)) {
            length = StemmerUtil.delete(text, i--, length);
          } else {
            text[i] = KANA_NORM[ch - 0xFF65];
          }
        }
      }
      termAtt.setLength(length);
      return true;
    } else {
      return false;
    }
  }

  /* kana combining diffs: 0x30A6-0x30FD */
  private static final byte KANA_COMBINE_VOICED[] = new byte[] {
    78, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1,
     0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 
     0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
     0, 8, 8, 8, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1
  };
  
  private static final byte KANA_COMBINE_HALF_VOICED[] = new byte[] {
     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 2, 0, 0, 2, 
     0, 0, 2, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
     0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
  };
  
  /** returns true if we successfully combined the voice mark */
  private static boolean combine(char text[], int pos, char ch) {
    final char prev = text[pos-1];
    if (prev >= 0x30A6 && prev <= 0x30FD) {
      text[pos-1] += (ch == 0xFF9F)
        ? KANA_COMBINE_HALF_VOICED[prev - 0x30A6] 
        : KANA_COMBINE_VOICED[prev - 0x30A6];
      return text[pos-1] != prev;
    }
    return false;
  }
}
