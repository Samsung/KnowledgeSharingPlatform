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

package org.apache.lucene.analysis.pattern;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

/**
 * This tokenizer uses regex pattern matching to construct distinct tokens
 * for the input stream.  It takes two arguments:  "pattern" and "group".
 * <p/>
 * <ul>
 * <li>"pattern" is the regular expression.</li>
 * <li>"group" says which group to extract into tokens.</li>
 *  </ul>
 * <p>
 * group=-1 (the default) is equivalent to "split".  In this case, the tokens will
 * be equivalent to the output from (without empty tokens):
 * {@link String#split(java.lang.String)}
 * </p>
 * <p>
 * Using group &gt;= 0 selects the matching group as the token.  For example, if you have:<br/>
 * <pre>
 *  pattern = \'([^\']+)\'
 *  group = 0
 *  input = aaa 'bbb' 'ccc'
 *</pre>
 * the output will be two tokens: 'bbb' and 'ccc' (including the ' marks).  With the same input
 * but using group=1, the output would be: bbb and ccc (no ' marks)
 * </p>
 * <p>NOTE: This Tokenizer does not output tokens that are of zero length.</p>
 *
 * @see Pattern
 */
public final class PatternTokenizer extends Tokenizer {

  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

  private final StringBuilder str = new StringBuilder();
  private int index;
  
  private final int group;
  private final Matcher matcher;

  /** creates a new PatternTokenizer returning tokens from group (-1 for split functionality) */
  public PatternTokenizer(Pattern pattern, int group) {
    this(DEFAULT_TOKEN_ATTRIBUTE_FACTORY, pattern, group);
  }

  /** creates a new PatternTokenizer returning tokens from group (-1 for split functionality) */
  public PatternTokenizer(AttributeFactory factory, Pattern pattern, int group) {
    super(factory);
    this.group = group;

    // Use "" instead of str so don't consume chars
    // (fillBuffer) from the input on throwing IAE below:
    matcher = pattern.matcher("");

    // confusingly group count depends ENTIRELY on the pattern but is only accessible via matcher
    if (group >= 0 && group > matcher.groupCount()) {
      throw new IllegalArgumentException("invalid group specified: pattern only has: " + matcher.groupCount() + " capturing groups");
    }
  }

  @Override
  public boolean incrementToken() {
    if (index >= str.length()) return false;
    clearAttributes();
    if (group >= 0) {
    
      // match a specific group
      while (matcher.find()) {
        index = matcher.start(group);
        final int endIndex = matcher.end(group);
        if (index == endIndex) continue;       
        termAtt.setEmpty().append(str, index, endIndex);
        offsetAtt.setOffset(correctOffset(index), correctOffset(endIndex));
        return true;
      }
      
      index = Integer.MAX_VALUE; // mark exhausted
      return false;
      
    } else {
    
      // String.split() functionality
      while (matcher.find()) {
        if (matcher.start() - index > 0) {
          // found a non-zero-length token
          termAtt.setEmpty().append(str, index, matcher.start());
          offsetAtt.setOffset(correctOffset(index), correctOffset(matcher.start()));
          index = matcher.end();
          return true;
        }
        
        index = matcher.end();
      }
      
      if (str.length() - index == 0) {
        index = Integer.MAX_VALUE; // mark exhausted
        return false;
      }
      
      termAtt.setEmpty().append(str, index, str.length());
      offsetAtt.setOffset(correctOffset(index), correctOffset(str.length()));
      index = Integer.MAX_VALUE; // mark exhausted
      return true;
    }
  }

  @Override
  public void end() throws IOException {
    super.end();
    final int ofs = correctOffset(str.length());
    offsetAtt.setOffset(ofs, ofs);
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    fillBuffer(str, input);
    matcher.reset(str);
    index = 0;
  }
  
  // TODO: we should see if we can make this tokenizer work without reading
  // the entire document into RAM, perhaps with Matcher.hitEnd/requireEnd ?
  final char[] buffer = new char[8192];
  private void fillBuffer(StringBuilder sb, Reader input) throws IOException {
    int len;
    sb.setLength(0);
    while ((len = input.read(buffer)) > 0) {
      sb.append(buffer, 0, len);
    }
  }
}
