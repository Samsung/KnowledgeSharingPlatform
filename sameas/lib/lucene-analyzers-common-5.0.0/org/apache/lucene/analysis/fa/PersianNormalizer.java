package org.apache.lucene.analysis.fa;

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
 * Normalizer for Persian.
 * <p>
 * Normalization is done in-place for efficiency, operating on a termbuffer.
 * <p>
 * Normalization is defined as:
 * <ul>
 * <li>Normalization of various heh + hamza forms and heh goal to heh.
 * <li>Normalization of farsi yeh and yeh barree to arabic yeh
 * <li>Normalization of persian keheh to arabic kaf
 * </ul>
 * 
 */
public class PersianNormalizer {
  public static final char YEH = '\u064A';

  public static final char FARSI_YEH = '\u06CC';

  public static final char YEH_BARREE = '\u06D2';

  public static final char KEHEH = '\u06A9';

  public static final char KAF = '\u0643';

  public static final char HAMZA_ABOVE = '\u0654';

  public static final char HEH_YEH = '\u06C0';

  public static final char HEH_GOAL = '\u06C1';

  public static final char HEH = '\u0647';

  /**
   * Normalize an input buffer of Persian text
   * 
   * @param s input buffer
   * @param len length of input buffer
   * @return length of input buffer after normalization
   */
  public int normalize(char s[], int len) {

    for (int i = 0; i < len; i++) {
      switch (s[i]) {
      case FARSI_YEH:
      case YEH_BARREE:
        s[i] = YEH;
        break;
      case KEHEH:
        s[i] = KAF;
        break;
      case HEH_YEH:
      case HEH_GOAL:
        s[i] = HEH;
        break;
      case HAMZA_ABOVE: // necessary for HEH + HAMZA
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
