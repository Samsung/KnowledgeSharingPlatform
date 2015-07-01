package org.apache.lucene.analysis.compound;

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


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * A {@link org.apache.lucene.analysis.TokenFilter} that decomposes compound words found in many Germanic languages.
 * <p>
 * "Donaudampfschiff" becomes Donau, dampf, schiff so that you can find
 * "Donaudampfschiff" even when you only enter "schiff".
 *  It uses a brute-force algorithm to achieve this.
 * <p>
 */
public class DictionaryCompoundWordTokenFilter extends CompoundWordTokenFilterBase {

  /**
   * Creates a new {@link DictionaryCompoundWordTokenFilter}
   *
   * @param input
   *          the {@link org.apache.lucene.analysis.TokenStream} to process
   * @param dictionary
   *          the word dictionary to match against.
   */
  public DictionaryCompoundWordTokenFilter(TokenStream input, CharArraySet dictionary) {
    super(input, dictionary);
    if (dictionary == null) {
      throw new IllegalArgumentException("dictionary cannot be null");
    }
  }

  /**
   * Creates a new {@link DictionaryCompoundWordTokenFilter}
   *
   * @param input
   *          the {@link org.apache.lucene.analysis.TokenStream} to process
   * @param dictionary
   *          the word dictionary to match against.
   * @param minWordSize
   *          only words longer than this get processed
   * @param minSubwordSize
   *          only subwords longer than this get to the output stream
   * @param maxSubwordSize
   *          only subwords shorter than this get to the output stream
   * @param onlyLongestMatch
   *          Add only the longest matching subword to the stream
   */
  public DictionaryCompoundWordTokenFilter(TokenStream input, CharArraySet dictionary,
                                           int minWordSize, int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    super(input, dictionary, minWordSize, minSubwordSize, maxSubwordSize, onlyLongestMatch);
    if (dictionary == null) {
      throw new IllegalArgumentException("dictionary cannot be null");
    }
  }

  @Override
  protected void decompose() {
    final int len = termAtt.length();
    for (int i=0;i<=len-this.minSubwordSize;++i) {
        CompoundToken longestMatchToken=null;
        for (int j=this.minSubwordSize;j<=this.maxSubwordSize;++j) {
            if(i+j>len) {
                break;
            }
            if(dictionary.contains(termAtt.buffer(), i, j)) {
                if (this.onlyLongestMatch) {
                   if (longestMatchToken!=null) {
                     if (longestMatchToken.txt.length()<j) {
                       longestMatchToken=new CompoundToken(i,j);
                     }
                   } else {
                     longestMatchToken=new CompoundToken(i,j);
                   }
                } else {
                   tokens.add(new CompoundToken(i,j));
                }
            } 
        }
        if (this.onlyLongestMatch && longestMatchToken!=null) {
          tokens.add(longestMatchToken);
        }
    }
  }
}
