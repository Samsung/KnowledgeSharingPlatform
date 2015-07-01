package org.apache.lucene.analysis.cz;

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
 * Light Stemmer for Czech.
 * <p>
 * Implements the algorithm described in:  
 * <i>
 * Indexing and stemming approaches for the Czech language
 * </i>
 * http://portal.acm.org/citation.cfm?id=1598600
 * </p>
 */
public class CzechStemmer {
  
  /**
   * Stem an input buffer of Czech text.
   * 
   * @param s input buffer
   * @param len length of input buffer
   * @return length of input buffer after normalization
   * 
   * <p><b>NOTE</b>: Input is expected to be in lowercase, 
   * but with diacritical marks</p>
   */
  public int stem(char s[], int len) {
    len = removeCase(s, len);
    len = removePossessives(s, len);
    if (len > 0) {
      len = normalize(s, len);
    }
    return len;
  }
  
  private int removeCase(char s[], int len) {  
    if (len > 7 && endsWith(s, len, "atech"))
      return len - 5;
    
    if (len > 6 && 
        (endsWith(s, len,"ětem") ||
        endsWith(s, len,"etem") ||
        endsWith(s, len,"atům")))
      return len - 4;
        
    if (len > 5 && 
        (endsWith(s, len, "ech") ||
        endsWith(s, len, "ich") ||
        endsWith(s, len, "ích") ||
        endsWith(s, len, "ého") ||
        endsWith(s, len, "ěmi") ||
        endsWith(s, len, "emi") ||
        endsWith(s, len, "ému") ||
        endsWith(s, len, "ěte") ||
        endsWith(s, len, "ete") ||
        endsWith(s, len, "ěti") ||
        endsWith(s, len, "eti") ||
        endsWith(s, len, "ího") ||
        endsWith(s, len, "iho") ||
        endsWith(s, len, "ími") ||
        endsWith(s, len, "ímu") ||
        endsWith(s, len, "imu") ||
        endsWith(s, len, "ách") ||
        endsWith(s, len, "ata") ||
        endsWith(s, len, "aty") ||
        endsWith(s, len, "ých") ||
        endsWith(s, len, "ama") ||
        endsWith(s, len, "ami") ||
        endsWith(s, len, "ové") ||
        endsWith(s, len, "ovi") ||
        endsWith(s, len, "ými")))
      return len - 3;
    
    if (len > 4 && 
        (endsWith(s, len, "em") ||
        endsWith(s, len, "es") ||
        endsWith(s, len, "ém") ||
        endsWith(s, len, "ím") ||
        endsWith(s, len, "ům") ||
        endsWith(s, len, "at") ||
        endsWith(s, len, "ám") ||
        endsWith(s, len, "os") ||
        endsWith(s, len, "us") ||
        endsWith(s, len, "ým") ||
        endsWith(s, len, "mi") ||
        endsWith(s, len, "ou")))
      return len - 2;
    
    if (len > 3) {
      switch (s[len - 1]) {
        case 'a':
        case 'e':
        case 'i':
        case 'o':
        case 'u':
        case 'ů':
        case 'y':
        case 'á':
        case 'é':
        case 'í':
        case 'ý':
        case 'ě':
          return len - 1;
      }
    }
    
    return len;
  }
  
  private int removePossessives(char s[], int len) {
    if (len > 5 &&
        (endsWith(s, len, "ov") ||
        endsWith(s, len, "in") ||
        endsWith(s, len, "ův")))
      return len - 2;

    return len;
  }
  
  private int normalize(char s[], int len) {
    if (endsWith(s, len, "čt")) { // čt -> ck
      s[len - 2] = 'c';
      s[len - 1] = 'k';
      return len;
    }
    
    if (endsWith(s, len, "št")) { // št -> sk
      s[len - 2] = 's';
      s[len - 1] = 'k';
      return len;
    }
    
    switch(s[len - 1]) {
      case 'c': // [cč] -> k
      case 'č':
        s[len - 1] = 'k';
        return len;
      case 'z': // [zž] -> h
      case 'ž':
        s[len - 1] = 'h';
        return len;
    }
    
    if (len > 1 && s[len - 2] == 'e') {
      s[len - 2] = s[len - 1]; // e* > *
      return len - 1;
    }
    
    if (len > 2 && s[len - 2] == 'ů') {
      s[len - 2] = 'o'; // *ů* -> *o*
      return len;
    }

    return len;
  }
}
