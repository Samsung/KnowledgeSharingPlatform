package org.apache.lucene.analysis.tr;

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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Normalizes Turkish token text to lower case.
 * <p>
 * Turkish and Azeri have unique casing behavior for some characters. This
 * filter applies Turkish lowercase rules. For more information, see <a
 * href="http://en.wikipedia.org/wiki/Turkish_dotted_and_dotless_I"
 * >http://en.wikipedia.org/wiki/Turkish_dotted_and_dotless_I</a>
 * </p>
 */
public final class TurkishLowerCaseFilter extends TokenFilter {
  private static final int LATIN_CAPITAL_LETTER_I = '\u0049';
  private static final int LATIN_SMALL_LETTER_I = '\u0069';
  private static final int LATIN_SMALL_LETTER_DOTLESS_I = '\u0131';
  private static final int COMBINING_DOT_ABOVE = '\u0307';
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  
  /**
   * Create a new TurkishLowerCaseFilter, that normalizes Turkish token text 
   * to lower case.
   * 
   * @param in TokenStream to filter
   */
  public TurkishLowerCaseFilter(TokenStream in) {
    super(in);
  }
  
  @Override
  public final boolean incrementToken() throws IOException {
    boolean iOrAfter = false;
    
    if (input.incrementToken()) {
      final char[] buffer = termAtt.buffer();
      int length = termAtt.length();
      for (int i = 0; i < length;) {
        final int ch = Character.codePointAt(buffer, i, length);
    
        iOrAfter = (ch == LATIN_CAPITAL_LETTER_I || 
            (iOrAfter && Character.getType(ch) == Character.NON_SPACING_MARK));
        
        if (iOrAfter) { // all the special I turkish handling happens here.
          switch(ch) {
            // remove COMBINING_DOT_ABOVE to mimic composed lowercase
            case COMBINING_DOT_ABOVE:
              length = delete(buffer, i, length);
              continue;
            // i itself, it depends if it is followed by COMBINING_DOT_ABOVE
            // if it is, we will make it small i and later remove the dot
            case LATIN_CAPITAL_LETTER_I:
              if (isBeforeDot(buffer, i + 1, length)) {
                buffer[i] = LATIN_SMALL_LETTER_I;
              } else {
                buffer[i] = LATIN_SMALL_LETTER_DOTLESS_I;
                // below is an optimization. no COMBINING_DOT_ABOVE follows,
                // so don't waste time calculating Character.getType(), etc
                iOrAfter = false;
              }
              i++;
              continue;
          }
        }
        
        i += Character.toChars(Character.toLowerCase(ch), buffer, i);
      }
      
      termAtt.setLength(length);
      return true;
    } else
      return false;
  }
  
  
  /**
   * lookahead for a combining dot above.
   * other NSMs may be in between.
   */
  private boolean isBeforeDot(char s[], int pos, int len) {
    for (int i = pos; i < len;) {
      final int ch = Character.codePointAt(s, i, len);
      if (Character.getType(ch) != Character.NON_SPACING_MARK)
        return false;
      if (ch == COMBINING_DOT_ABOVE)
        return true;
      i += Character.charCount(ch);
    }
    
    return false;
  }
  
  /**
   * delete a character in-place.
   * rarely happens, only if COMBINING_DOT_ABOVE is found after an i
   */
  private int delete(char s[], int pos, int len) {
    if (pos < len) 
      System.arraycopy(s, pos + 1, s, pos, len - pos - 1);
    
    return len - 1;
  }
}
