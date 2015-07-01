package org.apache.lucene.analysis.ckb;

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

import static org.apache.lucene.analysis.util.StemmerUtil.endsWith;

/**
 * Light stemmer for Sorani
 */
public class SoraniStemmer {
  
  /**
   * Stem an input buffer of Sorani text.
   * 
   * @param s input buffer
   * @param len length of input buffer
   * @return length of input buffer after normalization
   */
  public int stem(char s[], int len) {
    // postposition
    if (len > 5 && endsWith(s, len, "دا")) {
      len -= 2;
    } else if (len > 4 && endsWith(s, len, "نا")) {
      len--;
    } else if (len > 6 && endsWith(s, len, "ەوە")) {
      len -= 3;
    }
    
    // possessive pronoun
    if (len > 6 && (endsWith(s, len, "مان") || endsWith(s, len, "یان") || endsWith(s, len, "تان"))) {
      len -= 3;
    }
    
    // indefinite singular ezafe
    if (len > 6 && endsWith(s, len, "ێکی")) {
      return len-3;
    } else if (len > 7 && endsWith(s, len, "یەکی")) {
      return len-4;
    }
    // indefinite singular
    if (len > 5 && endsWith(s, len, "ێک")) {
      return len-2;
    } else if (len > 6 && endsWith(s, len, "یەک")) {
      return len-3;
    }
    // definite singular
    else if (len > 6 && endsWith(s, len, "ەکە")) {
      return len-3;
    } else if (len > 5 && endsWith(s, len, "کە")) {
      return len-2;
    }
    // definite plural
    else if (len > 7 && endsWith(s, len, "ەکان")) {
      return len-4;
    } else if (len > 6 && endsWith(s, len, "کان")) {
      return len-3;
    }
    // indefinite plural ezafe
    else if (len > 7 && endsWith(s, len, "یانی")) {
      return len-4;
    } else if (len > 6 && endsWith(s, len, "انی")) {
      return len-3;
    }
    // indefinite plural
    else if (len > 6 && endsWith(s, len, "یان")) {
      return len-3;
    } else if (len > 5 && endsWith(s, len, "ان")) {
      return len-2;
    } 
    // demonstrative plural
    else if (len > 7 && endsWith(s, len, "یانە")) {
      return len-4;
    } else if (len > 6 && endsWith(s, len, "انە")) {
      return len-3;
    }
    // demonstrative singular
    else if (len > 5 && (endsWith(s, len, "ایە") || endsWith(s, len, "ەیە"))) {
      return len-2;
    } else if (len > 4 && endsWith(s, len, "ە")) {
      return len-1;
    }
    // absolute singular ezafe
    else if (len > 4 && endsWith(s, len, "ی")) {
      return len-1;
    }
    return len;
  }
}
