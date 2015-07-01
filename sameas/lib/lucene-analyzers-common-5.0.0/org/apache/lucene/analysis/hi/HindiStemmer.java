package org.apache.lucene.analysis.hi;

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
 * Light Stemmer for Hindi.
 * <p>
 * Implements the algorithm specified in:
 * <i>A Lightweight Stemmer for Hindi</i>
 * Ananthakrishnan Ramanathan and Durgesh D Rao.
 * http://computing.open.ac.uk/Sites/EACLSouthAsia/Papers/p6-Ramanathan.pdf
 * </p>
 */
public class HindiStemmer {
  public int stem(char buffer[], int len) {
    // 5
    if ((len > 6) && (endsWith(buffer, len, "ाएंगी")
        || endsWith(buffer, len, "ाएंगे")
        || endsWith(buffer, len, "ाऊंगी")
        || endsWith(buffer, len, "ाऊंगा")
        || endsWith(buffer, len, "ाइयाँ")
        || endsWith(buffer, len, "ाइयों")
        || endsWith(buffer, len, "ाइयां")
      ))
      return len - 5;
    
    // 4
    if ((len > 5) && (endsWith(buffer, len, "ाएगी")
        || endsWith(buffer, len, "ाएगा")
        || endsWith(buffer, len, "ाओगी")
        || endsWith(buffer, len, "ाओगे")
        || endsWith(buffer, len, "एंगी")
        || endsWith(buffer, len, "ेंगी")
        || endsWith(buffer, len, "एंगे")
        || endsWith(buffer, len, "ेंगे")
        || endsWith(buffer, len, "ूंगी")
        || endsWith(buffer, len, "ूंगा")
        || endsWith(buffer, len, "ातीं")
        || endsWith(buffer, len, "नाओं")
        || endsWith(buffer, len, "नाएं")
        || endsWith(buffer, len, "ताओं")
        || endsWith(buffer, len, "ताएं")
        || endsWith(buffer, len, "ियाँ")
        || endsWith(buffer, len, "ियों")
        || endsWith(buffer, len, "ियां")
        ))
      return len - 4;
    
    // 3
    if ((len > 4) && (endsWith(buffer, len, "ाकर")
        || endsWith(buffer, len, "ाइए")
        || endsWith(buffer, len, "ाईं")
        || endsWith(buffer, len, "ाया")
        || endsWith(buffer, len, "ेगी")
        || endsWith(buffer, len, "ेगा")
        || endsWith(buffer, len, "ोगी")
        || endsWith(buffer, len, "ोगे")
        || endsWith(buffer, len, "ाने")
        || endsWith(buffer, len, "ाना")
        || endsWith(buffer, len, "ाते")
        || endsWith(buffer, len, "ाती")
        || endsWith(buffer, len, "ाता")
        || endsWith(buffer, len, "तीं")
        || endsWith(buffer, len, "ाओं")
        || endsWith(buffer, len, "ाएं")
        || endsWith(buffer, len, "ुओं")
        || endsWith(buffer, len, "ुएं")
        || endsWith(buffer, len, "ुआं")
        ))
      return len - 3;
    
    // 2
    if ((len > 3) && (endsWith(buffer, len, "कर")
        || endsWith(buffer, len, "ाओ")
        || endsWith(buffer, len, "िए")
        || endsWith(buffer, len, "ाई")
        || endsWith(buffer, len, "ाए")
        || endsWith(buffer, len, "ने")
        || endsWith(buffer, len, "नी")
        || endsWith(buffer, len, "ना")
        || endsWith(buffer, len, "ते")
        || endsWith(buffer, len, "ीं")
        || endsWith(buffer, len, "ती")
        || endsWith(buffer, len, "ता")
        || endsWith(buffer, len, "ाँ")
        || endsWith(buffer, len, "ां")
        || endsWith(buffer, len, "ों")
        || endsWith(buffer, len, "ें")
        ))
      return len - 2;
    
    // 1
    if ((len > 2) && (endsWith(buffer, len, "ो")
        || endsWith(buffer, len, "े")
        || endsWith(buffer, len, "ू")
        || endsWith(buffer, len, "ु")
        || endsWith(buffer, len, "ी")
        || endsWith(buffer, len, "ि")
        || endsWith(buffer, len, "ा")
       ))
      return len - 1;
    return len;
  }
}
