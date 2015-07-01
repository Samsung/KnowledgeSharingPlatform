package org.apache.lucene.analysis.miscellaneous;

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
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Factory for {@link CapitalizationFilter}.
 * <p/>
 * The factory takes parameters:<br/>
 * "onlyFirstWord" - should each word be capitalized or all of the words?<br/>
 * "keep" - a keep word list.  Each word that should be kept separated by whitespace.<br/>
 * "keepIgnoreCase - true or false.  If true, the keep list will be considered case-insensitive.<br/>
 * "forceFirstLetter" - Force the first letter to be capitalized even if it is in the keep list<br/>
 * "okPrefix" - do not change word capitalization if a word begins with something in this list.
 * for example if "McK" is on the okPrefix list, the word "McKinley" should not be changed to
 * "Mckinley"<br/>
 * "minWordLength" - how long the word needs to be to get capitalization applied.  If the
 * minWordLength is 3, "and" &gt; "And" but "or" stays "or"<br/>
 * "maxWordCount" - if the token contains more then maxWordCount words, the capitalization is
 * assumed to be correct.<br/>
 *
 * <pre class="prettyprint">
 * &lt;fieldType name="text_cptlztn" class="solr.TextField" positionIncrementGap="100"&gt;
 *   &lt;analyzer&gt;
 *     &lt;tokenizer class="solr.WhitespaceTokenizerFactory"/&gt;
 *     &lt;filter class="solr.CapitalizationFilterFactory" onlyFirstWord="true"
 *           keep="java solr lucene" keepIgnoreCase="false"
 *           okPrefix="McK McD McA"/&gt;   
 *   &lt;/analyzer&gt;
 * &lt;/fieldType&gt;</pre>
 *
 * @since solr 1.3
 */
public class CapitalizationFilterFactory extends TokenFilterFactory {
  public static final String KEEP = "keep";
  public static final String KEEP_IGNORE_CASE = "keepIgnoreCase";
  public static final String OK_PREFIX = "okPrefix";
  public static final String MIN_WORD_LENGTH = "minWordLength";
  public static final String MAX_WORD_COUNT = "maxWordCount";
  public static final String MAX_TOKEN_LENGTH = "maxTokenLength";
  public static final String ONLY_FIRST_WORD = "onlyFirstWord";
  public static final String FORCE_FIRST_LETTER = "forceFirstLetter";

  CharArraySet keep;

  Collection<char[]> okPrefix = Collections.emptyList(); // for Example: McK

  final int minWordLength;  // don't modify capitalization for words shorter then this
  final int maxWordCount;
  final int maxTokenLength;
  final boolean onlyFirstWord;
  final boolean forceFirstLetter; // make sure the first letter is capital even if it is in the keep list

  /** Creates a new CapitalizationFilterFactory */
  public CapitalizationFilterFactory(Map<String, String> args) {
    super(args);
    boolean ignoreCase = getBoolean(args, KEEP_IGNORE_CASE, false);
    Set<String> k = getSet(args, KEEP);
    if (k != null) {
      keep = new CharArraySet(10, ignoreCase);
      keep.addAll(k);
    }

    k = getSet(args, OK_PREFIX);
    if (k != null) {
      okPrefix = new ArrayList<>();
      for (String item : k) {
        okPrefix.add(item.toCharArray());
      }
    }

    minWordLength = getInt(args, MIN_WORD_LENGTH, 0);
    maxWordCount = getInt(args, MAX_WORD_COUNT, CapitalizationFilter.DEFAULT_MAX_WORD_COUNT);
    maxTokenLength = getInt(args, MAX_TOKEN_LENGTH, CapitalizationFilter.DEFAULT_MAX_TOKEN_LENGTH);
    onlyFirstWord = getBoolean(args, ONLY_FIRST_WORD, true);
    forceFirstLetter = getBoolean(args, FORCE_FIRST_LETTER, true);
    if (!args.isEmpty()) {
      throw new IllegalArgumentException("Unknown parameters: " + args);
    }
  }

  @Override
  public CapitalizationFilter create(TokenStream input) {
    return new CapitalizationFilter(input, onlyFirstWord, keep, 
      forceFirstLetter, okPrefix, minWordLength, maxWordCount, maxTokenLength);
  }
}
