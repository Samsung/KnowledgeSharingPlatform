package org.apache.lucene.analysis.ar;

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
 *  Normalizer for Arabic.
 *  <p>
 *  Normalization is done in-place for efficiency, operating on a termbuffer.
 *  <p>
 *  Normalization is defined as:
 *  <ul>
 *  <li> Normalization of hamza with alef seat to a bare alef.
 *  <li> Normalization of teh marbuta to heh
 *  <li> Normalization of dotless yeh (alef maksura) to yeh.
 *  <li> Removal of Arabic diacritics (the harakat)
 *  <li> Removal of tatweel (stretching character).
 * </ul>
 *
 */
public class ArabicNormalizer {
  public static final char ALEF = '\u0627';
  public static final char ALEF_MADDA = '\u0622';
  public static final char ALEF_HAMZA_ABOVE = '\u0623';
  public static final char ALEF_HAMZA_BELOW = '\u0625';

  public static final char YEH = '\u064A';
  public static final char DOTLESS_YEH = '\u0649';

  public static final char TEH_MARBUTA = '\u0629';
  public static final char HEH = '\u0647';

  public static final char TATWEEL = '\u0640';

  public static final char FATHATAN = '\u064B';
  public static final char DAMMATAN = '\u064C';
  public static final char KASRATAN = '\u064D';
  public static final char FATHA = '\u064E';
  public static final char DAMMA = '\u064F';
  public static final char KASRA = '\u0650';
  public static final char SHADDA = '\u0651';
  public static final char SUKUN = '\u0652';

  /**
   * Normalize an input buffer of Arabic text
   * 
   * @param s input buffer
   * @param len length of input buffer
   * @return length of input buffer after normalization
   */
  public int normalize(char s[], int len) {

    for (int i = 0; i < len; i++) {
      switch (s[i]) {
      case ALEF_MADDA:
      case ALEF_HAMZA_ABOVE:
      case ALEF_HAMZA_BELOW:
        s[i] = ALEF;
        break;
      case DOTLESS_YEH:
        s[i] = YEH;
        break;
      case TEH_MARBUTA:
        s[i] = HEH;
        break;
      case TATWEEL:
      case KASRATAN:
      case DAMMATAN:
      case FATHATAN:
      case FATHA:
      case DAMMA:
      case KASRA:
      case SHADDA:
      case SUKUN:
        len = delete(s, i, len);
        i--;
        break;
      default:
        break;
      }
    }

    return len;
  }
}
