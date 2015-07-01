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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.IOException;

/**
 * A TokenFilter which applies a Pattern to each token in the stream,
 * replacing match occurances with the specified replacement string.
 *
 * <p>
 * <b>Note:</b> Depending on the input and the pattern used and the input
 * TokenStream, this TokenFilter may produce Tokens whose text is the empty
 * string.
 * </p>
 * 
 * @see Pattern
 */
public final class PatternReplaceFilter extends TokenFilter {
  private final String replacement;
  private final boolean all;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final Matcher m;

  /**
   * Constructs an instance to replace either the first, or all occurances
   *
   * @param in the TokenStream to process
   * @param p the patterm to apply to each Token
   * @param replacement the "replacement string" to substitute, if null a
   *        blank string will be used. Note that this is not the literal
   *        string that will be used, '$' and '\' have special meaning.
   * @param all if true, all matches will be replaced otherwise just the first match.
   * @see Matcher#quoteReplacement
   */
  public PatternReplaceFilter(TokenStream in,
                              Pattern p,
                              String replacement,
                              boolean all) {
    super(in);
    this.replacement = (null == replacement) ? "" : replacement;
    this.all=all;
    this.m = p.matcher(termAtt);
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) return false;
    
    m.reset();
    if (m.find()) {
      // replaceAll/replaceFirst will reset() this previous find.
      String transformed = all ? m.replaceAll(replacement) : m.replaceFirst(replacement);
      termAtt.setEmpty().append(transformed);
    }

    return true;
  }

}
