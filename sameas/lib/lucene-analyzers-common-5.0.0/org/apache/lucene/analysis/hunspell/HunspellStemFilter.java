package org.apache.lucene.analysis.hunspell;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.CharsRef;

/**
 * TokenFilter that uses hunspell affix rules and words to stem tokens.  Since hunspell supports a word having multiple
 * stems, this filter can emit multiple tokens for each consumed token
 *
 * <p>
 * Note: This filter is aware of the {@link KeywordAttribute}. To prevent
 * certain terms from being passed to the stemmer
 * {@link KeywordAttribute#isKeyword()} should be set to <code>true</code>
 * in a previous {@link TokenStream}.
 *
 * Note: For including the original term as well as the stemmed version, see
 * {@link org.apache.lucene.analysis.miscellaneous.KeywordRepeatFilterFactory}
 * </p>
 *
 * @lucene.experimental
 */
public final class HunspellStemFilter extends TokenFilter {
  
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
  private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
  private final Stemmer stemmer;
  
  private List<CharsRef> buffer;
  private State savedState;
  
  private final boolean dedup;
  private final boolean longestOnly;

  /** Create a {@link HunspellStemFilter} outputting all possible stems.
   *  @see #HunspellStemFilter(TokenStream, Dictionary, boolean) */
  public HunspellStemFilter(TokenStream input, Dictionary dictionary) {
    this(input, dictionary, true);
  }

  /** Create a {@link HunspellStemFilter} outputting all possible stems. 
   *  @see #HunspellStemFilter(TokenStream, Dictionary, boolean, boolean) */
  public HunspellStemFilter(TokenStream input, Dictionary dictionary, boolean dedup) {
    this(input, dictionary, dedup, false);
  }
  
  /**
   * Creates a new HunspellStemFilter that will stem tokens from the given TokenStream using affix rules in the provided
   * Dictionary
   *
   * @param input TokenStream whose tokens will be stemmed
   * @param dictionary HunspellDictionary containing the affix rules and words that will be used to stem the tokens
   * @param longestOnly true if only the longest term should be output.
   */
  public HunspellStemFilter(TokenStream input, Dictionary dictionary, boolean dedup,  boolean longestOnly) {
    super(input);
    this.dedup = dedup && longestOnly == false; // don't waste time deduping if longestOnly is set
    this.stemmer = new Stemmer(dictionary);
    this.longestOnly = longestOnly;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (buffer != null && !buffer.isEmpty()) {
      CharsRef nextStem = buffer.remove(0);
      restoreState(savedState);
      posIncAtt.setPositionIncrement(0);
      termAtt.setEmpty().append(nextStem);
      return true;
    }
    
    if (!input.incrementToken()) {
      return false;
    }
    
    if (keywordAtt.isKeyword()) {
      return true;
    }
    
    buffer = dedup ? stemmer.uniqueStems(termAtt.buffer(), termAtt.length()) : stemmer.stem(termAtt.buffer(), termAtt.length());

    if (buffer.isEmpty()) { // we do not know this word, return it unchanged
      return true;
    }     
    
    if (longestOnly && buffer.size() > 1) {
      Collections.sort(buffer, lengthComparator);
    }

    CharsRef stem = buffer.remove(0);
    termAtt.setEmpty().append(stem);

    if (longestOnly) {
      buffer.clear();
    } else {
      if (!buffer.isEmpty()) {
        savedState = captureState();
      }
    }

    return true;
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    buffer = null;
  }
  
  static final Comparator<CharsRef> lengthComparator = new Comparator<CharsRef>() {
    @Override
    public int compare(CharsRef o1, CharsRef o2) {
      int cmp = Integer.compare(o2.length, o1.length);
      if (cmp == 0) {
        // tie break on text
        return o2.compareTo(o1);
      } else {
        return cmp;
      }
    }
  };
}
