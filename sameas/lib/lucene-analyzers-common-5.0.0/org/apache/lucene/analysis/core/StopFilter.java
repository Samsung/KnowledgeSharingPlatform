package org.apache.lucene.analysis.core;

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

import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * Removes stop words from a token stream.
 */
public final class StopFilter extends FilteringTokenFilter {

  private final CharArraySet stopWords;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  
  /**
   * Constructs a filter which removes words from the input TokenStream that are
   * named in the Set.
   * 
   * @param in
   *          Input stream
   * @param stopWords
   *          A {@link CharArraySet} representing the stopwords.
   * @see #makeStopSet(java.lang.String...)
   */
  public StopFilter(TokenStream in, CharArraySet stopWords) {
    super(in);
    this.stopWords = stopWords;
  }

  /**
   * Builds a Set from an array of stop words,
   * appropriate for passing into the StopFilter constructor.
   * This permits this stopWords construction to be cached once when
   * an Analyzer is constructed.
   * 
   * @param stopWords An array of stopwords
   * @see #makeStopSet(java.lang.String[], boolean) passing false to ignoreCase
   */
  public static CharArraySet makeStopSet(String... stopWords) {
    return makeStopSet(stopWords, false);
  }
  
  /**
   * Builds a Set from an array of stop words,
   * appropriate for passing into the StopFilter constructor.
   * This permits this stopWords construction to be cached once when
   * an Analyzer is constructed.
   * 
   * @param stopWords A List of Strings or char[] or any other toString()-able list representing the stopwords
   * @return A Set ({@link CharArraySet}) containing the words
   * @see #makeStopSet(java.lang.String[], boolean) passing false to ignoreCase
   */
  public static CharArraySet makeStopSet(List<?> stopWords) {
    return makeStopSet(stopWords, false);
  }
    
  /**
   * Creates a stopword set from the given stopword array.
   * 
   * @param stopWords An array of stopwords
   * @param ignoreCase If true, all words are lower cased first.  
   * @return a Set containing the words
   */    
  public static CharArraySet makeStopSet(String[] stopWords, boolean ignoreCase) {
    CharArraySet stopSet = new CharArraySet(stopWords.length, ignoreCase);
    stopSet.addAll(Arrays.asList(stopWords));
    return stopSet;
  }
  
  /**
   * Creates a stopword set from the given stopword list.
   * @param stopWords A List of Strings or char[] or any other toString()-able list representing the stopwords
   * @param ignoreCase if true, all words are lower cased first
   * @return A Set ({@link CharArraySet}) containing the words
   */
  public static CharArraySet makeStopSet(List<?> stopWords, boolean ignoreCase){
    CharArraySet stopSet = new CharArraySet(stopWords.size(), ignoreCase);
    stopSet.addAll(stopWords);
    return stopSet;
  }
  
  /**
   * Returns the next input Token whose term() is not a stop word.
   */
  @Override
  protected boolean accept() {
    return !stopWords.contains(termAtt.buffer(), 0, termAtt.length());
  }

}
