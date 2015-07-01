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
import org.apache.lucene.analysis.compound.hyphenation.Hyphenation;
import org.apache.lucene.analysis.compound.hyphenation.HyphenationTree;
import org.apache.lucene.analysis.util.CharArraySet;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;

/**
 * A {@link org.apache.lucene.analysis.TokenFilter} that decomposes compound words found in many Germanic languages.
 *
 * "Donaudampfschiff" becomes Donau, dampf, schiff so that you can find
 * "Donaudampfschiff" even when you only enter "schiff". It uses a hyphenation
 * grammar and a word dictionary to achieve this.
 */
public class HyphenationCompoundWordTokenFilter extends
    CompoundWordTokenFilterBase {
  private HyphenationTree hyphenator;

  /**
   * Creates a new {@link HyphenationCompoundWordTokenFilter} instance.
   *
   * @param input
   *          the {@link org.apache.lucene.analysis.TokenStream} to process
   * @param hyphenator
   *          the hyphenation pattern tree to use for hyphenation
   * @param dictionary
   *          the word dictionary to match against.
   */
  public HyphenationCompoundWordTokenFilter(TokenStream input,
                                            HyphenationTree hyphenator, CharArraySet dictionary) {
    this(input, hyphenator, dictionary, DEFAULT_MIN_WORD_SIZE,
        DEFAULT_MIN_SUBWORD_SIZE, DEFAULT_MAX_SUBWORD_SIZE, false);
  }

  /**
   * Creates a new {@link HyphenationCompoundWordTokenFilter} instance.
   *
   * @param input
   *          the {@link org.apache.lucene.analysis.TokenStream} to process
   * @param hyphenator
   *          the hyphenation pattern tree to use for hyphenation
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
  public HyphenationCompoundWordTokenFilter(TokenStream input,
                                            HyphenationTree hyphenator, CharArraySet dictionary, int minWordSize,
                                            int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    super(input, dictionary, minWordSize, minSubwordSize, maxSubwordSize,
        onlyLongestMatch);

    this.hyphenator = hyphenator;
  }

  /**
   * Create a HyphenationCompoundWordTokenFilter with no dictionary.
   * <p>
   * Calls {@link #HyphenationCompoundWordTokenFilter(org.apache.lucene.analysis.TokenStream, org.apache.lucene.analysis.compound.hyphenation.HyphenationTree, org.apache.lucene.analysis.util.CharArraySet, int, int, int, boolean)
   * HyphenationCompoundWordTokenFilter(matchVersion, input, hyphenator,
   * null, minWordSize, minSubwordSize, maxSubwordSize }
   */
  public HyphenationCompoundWordTokenFilter(TokenStream input,
                                            HyphenationTree hyphenator, int minWordSize, int minSubwordSize,
                                            int maxSubwordSize) {
    this(input, hyphenator, null, minWordSize, minSubwordSize,
        maxSubwordSize, false);
  }

  /**
   * Create a HyphenationCompoundWordTokenFilter with no dictionary.
   * <p>
   * Calls {@link #HyphenationCompoundWordTokenFilter(org.apache.lucene.analysis.TokenStream, org.apache.lucene.analysis.compound.hyphenation.HyphenationTree, int, int, int)
   * HyphenationCompoundWordTokenFilter(matchVersion, input, hyphenator,
   * DEFAULT_MIN_WORD_SIZE, DEFAULT_MIN_SUBWORD_SIZE, DEFAULT_MAX_SUBWORD_SIZE }
   */
  public HyphenationCompoundWordTokenFilter(TokenStream input,
                                            HyphenationTree hyphenator) {
    this(input, hyphenator, DEFAULT_MIN_WORD_SIZE, DEFAULT_MIN_SUBWORD_SIZE,
        DEFAULT_MAX_SUBWORD_SIZE);
  }

  /**
   * Create a hyphenator tree
   *
   * @param hyphenationFilename the filename of the XML grammar to load
   * @return An object representing the hyphenation patterns
   * @throws java.io.IOException If there is a low-level I/O error.
   */
  public static HyphenationTree getHyphenationTree(String hyphenationFilename)
      throws IOException {
    return getHyphenationTree(new InputSource(hyphenationFilename));
  }

  /**
   * Create a hyphenator tree
   *
   * @param hyphenationSource the InputSource pointing to the XML grammar
   * @return An object representing the hyphenation patterns
   * @throws java.io.IOException If there is a low-level I/O error.
   */
  public static HyphenationTree getHyphenationTree(InputSource hyphenationSource)
      throws IOException {
    HyphenationTree tree = new HyphenationTree();
    tree.loadPatterns(hyphenationSource);
    return tree;
  }

  @Override
  protected void decompose() {
    // get the hyphenation points
    Hyphenation hyphens = hyphenator.hyphenate(termAtt.buffer(), 0, termAtt.length(), 1, 1);
    // No hyphen points found -> exit
    if (hyphens == null) {
      return;
    }

    final int[] hyp = hyphens.getHyphenationPoints();

    for (int i = 0; i < hyp.length; ++i) {
      int remaining = hyp.length - i;
      int start = hyp[i];
      CompoundToken longestMatchToken = null;
      for (int j = 1; j < remaining; j++) {
        int partLength = hyp[i + j] - start;

        // if the part is longer than maxSubwordSize we
        // are done with this round
        if (partLength > this.maxSubwordSize) {
          break;
        }

        // we only put subwords to the token stream
        // that are longer than minPartSize
        if (partLength < this.minSubwordSize) {
          // BOGUS/BROKEN/FUNKY/WACKO: somehow we have negative 'parts' according to the 
          // calculation above, and we rely upon minSubwordSize being >=0 to filter them out...
          continue;
        }

        // check the dictionary
        if (dictionary == null || dictionary.contains(termAtt.buffer(), start, partLength)) {
          if (this.onlyLongestMatch) {
            if (longestMatchToken != null) {
              if (longestMatchToken.txt.length() < partLength) {
                longestMatchToken = new CompoundToken(start, partLength);
              }
            } else {
              longestMatchToken = new CompoundToken(start, partLength);
            }
          } else {
            tokens.add(new CompoundToken(start, partLength));
          }
        } else if (dictionary.contains(termAtt.buffer(), start, partLength - 1)) {
          // check the dictionary again with a word that is one character
          // shorter
          // to avoid problems with genitive 's characters and other binding
          // characters
          if (this.onlyLongestMatch) {
            if (longestMatchToken != null) {
              if (longestMatchToken.txt.length() < partLength - 1) {
                longestMatchToken = new CompoundToken(start, partLength - 1);
              }
            } else {
              longestMatchToken = new CompoundToken(start, partLength - 1);
            }
          } else {
            tokens.add(new CompoundToken(start, partLength - 1));
          }
        }
      }
      if (this.onlyLongestMatch && longestMatchToken!=null) {
        tokens.add(longestMatchToken);
      }
    }
  }
}
