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

import java.io.IOException;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * When the plain text is extracted from documents, we will often have many words hyphenated and broken into
 * two lines. This is often the case with documents where narrow text columns are used, such as newsletters.
 * In order to increase search efficiency, this filter puts hyphenated words broken into two lines back together.
 * This filter should be used on indexing time only.
 * Example field definition in schema.xml:
 * <pre class="prettyprint">
 * &lt;fieldtype name="text" class="solr.TextField" positionIncrementGap="100"&gt;
 *  &lt;analyzer type="index"&gt;
 *    &lt;tokenizer class="solr.WhitespaceTokenizerFactory"/&gt;
 *      &lt;filter class="solr.SynonymFilterFactory" synonyms="index_synonyms.txt" ignoreCase="true" expand="false"/&gt;
 *      &lt;filter class="solr.StopFilterFactory" ignoreCase="true"/&gt;
 *      &lt;filter class="solr.HyphenatedWordsFilterFactory"/&gt;
 *      &lt;filter class="solr.WordDelimiterFilterFactory" generateWordParts="1" generateNumberParts="1" catenateWords="1" catenateNumbers="1" catenateAll="0"/&gt;
 *      &lt;filter class="solr.LowerCaseFilterFactory"/&gt;
 *      &lt;filter class="solr.RemoveDuplicatesTokenFilterFactory"/&gt;
 *  &lt;/analyzer&gt;
 *  &lt;analyzer type="query"&gt;
 *      &lt;tokenizer class="solr.WhitespaceTokenizerFactory"/&gt;
 *      &lt;filter class="solr.SynonymFilterFactory" synonyms="synonyms.txt" ignoreCase="true" expand="true"/&gt;
 *      &lt;filter class="solr.StopFilterFactory" ignoreCase="true"/&gt;
 *      &lt;filter class="solr.WordDelimiterFilterFactory" generateWordParts="1" generateNumberParts="1" catenateWords="0" catenateNumbers="0" catenateAll="0"/&gt;
 *      &lt;filter class="solr.LowerCaseFilterFactory"/&gt;
 *      &lt;filter class="solr.RemoveDuplicatesTokenFilterFactory"/&gt;
 *  &lt;/analyzer&gt;
 * &lt;/fieldtype&gt;
 * </pre>
 * 
 */
public final class HyphenatedWordsFilter extends TokenFilter {

  private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);
  
  private final StringBuilder hyphenated = new StringBuilder();
  private State savedState;
  private boolean exhausted = false;
  private int lastEndOffset = 0;

  /**
   * Creates a new HyphenatedWordsFilter
   *
   * @param in TokenStream that will be filtered
   */
  public HyphenatedWordsFilter(TokenStream in) {
    super(in);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean incrementToken() throws IOException {
    while (!exhausted && input.incrementToken()) {
      char[] term = termAttribute.buffer();
      int termLength = termAttribute.length();
      lastEndOffset = offsetAttribute.endOffset();
      
      if (termLength > 0 && term[termLength - 1] == '-') {
        // a hyphenated word
        // capture the state of the first token only
        if (savedState == null) {
          savedState = captureState();
        }
        hyphenated.append(term, 0, termLength - 1);
      } else if (savedState == null) {
        // not part of a hyphenated word.
        return true;
      } else {
        // the final portion of a hyphenated word
        hyphenated.append(term, 0, termLength);
        unhyphenate();
        return true;
      }
    }
    
    exhausted = true;

    if (savedState != null) {
      // the final term ends with a hyphen
      // add back the hyphen, for backwards compatibility.
      hyphenated.append('-');
      unhyphenate();
      return true;
    }
    
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset() throws IOException {
    super.reset();
    hyphenated.setLength(0);
    savedState = null;
    exhausted = false;
    lastEndOffset = 0;
  }

  // ================================================= Helper Methods ================================================

  /**
   * Writes the joined unhyphenated term
   */
  private void unhyphenate() {
    restoreState(savedState);
    savedState = null;
    
    char term[] = termAttribute.buffer();
    int length = hyphenated.length();
    if (length > termAttribute.length()) {
      term = termAttribute.resizeBuffer(length);
    }
    
    hyphenated.getChars(0, length, term, 0);
    termAttribute.setLength(length);
    offsetAttribute.setOffset(offsetAttribute.startOffset(), lastEndOffset);
    hyphenated.setLength(0);
  }
}
