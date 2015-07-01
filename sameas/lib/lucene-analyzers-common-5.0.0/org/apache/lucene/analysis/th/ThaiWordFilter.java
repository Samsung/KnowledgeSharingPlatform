package org.apache.lucene.analysis.th;

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
import java.lang.Character.UnicodeBlock;
import java.text.BreakIterator;
import java.util.Locale;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.util.CharArrayIterator;
import org.apache.lucene.util.AttributeSource;

/**
 * {@link TokenFilter} that use {@link java.text.BreakIterator} to break each 
 * Token that is Thai into separate Token(s) for each Thai word.
 * <p>WARNING: this filter may not be supported by all JREs.
 *    It is known to work with Sun/Oracle and Harmony JREs.
 *    If your application needs to be fully portable, consider using ICUTokenizer instead,
 *    which uses an ICU Thai BreakIterator that will always be available.
 * @deprecated Use {@link ThaiTokenizer} instead.
 */
@Deprecated
public final class ThaiWordFilter extends TokenFilter {
  /** 
   * True if the JRE supports a working dictionary-based breakiterator for Thai.
   * If this is false, this filter will not work at all!
   */
  public static final boolean DBBI_AVAILABLE = ThaiTokenizer.DBBI_AVAILABLE;
  private static final BreakIterator proto = BreakIterator.getWordInstance(new Locale("th"));
  private final BreakIterator breaker = (BreakIterator) proto.clone();
  private final CharArrayIterator charIterator = CharArrayIterator.newWordInstance();
  
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  private final PositionIncrementAttribute posAtt = addAttribute(PositionIncrementAttribute.class);
  
  private AttributeSource clonedToken = null;
  private CharTermAttribute clonedTermAtt = null;
  private OffsetAttribute clonedOffsetAtt = null;
  private boolean hasMoreTokensInClone = false;
  private boolean hasIllegalOffsets = false; // only if the length changed before this filter

  /** Creates a new ThaiWordFilter with the specified match version. */
  public ThaiWordFilter(TokenStream input) {
    super(input);
    if (!DBBI_AVAILABLE)
      throw new UnsupportedOperationException("This JRE does not have support for Thai segmentation");
  }
  
  @Override
  public boolean incrementToken() throws IOException {
    if (hasMoreTokensInClone) {
      int start = breaker.current();
      int end = breaker.next();
      if (end != BreakIterator.DONE) {
        clonedToken.copyTo(this);
        termAtt.copyBuffer(clonedTermAtt.buffer(), start, end - start);
        if (hasIllegalOffsets) {
          offsetAtt.setOffset(clonedOffsetAtt.startOffset(), clonedOffsetAtt.endOffset());
        } else {
          offsetAtt.setOffset(clonedOffsetAtt.startOffset() + start, clonedOffsetAtt.startOffset() + end);
        }
        posAtt.setPositionIncrement(1);
        return true;
      }
      hasMoreTokensInClone = false;
    }

    if (!input.incrementToken()) {
      return false;
    }
    
    if (termAtt.length() == 0 || UnicodeBlock.of(termAtt.charAt(0)) != UnicodeBlock.THAI) {
      return true;
    }
    
    hasMoreTokensInClone = true;
    
    // if length by start + end offsets doesn't match the term text then assume
    // this is a synonym and don't adjust the offsets.
    hasIllegalOffsets = offsetAtt.endOffset() - offsetAtt.startOffset() != termAtt.length();

    // we lazy init the cloned token, as in ctor not all attributes may be added
    if (clonedToken == null) {
      clonedToken = cloneAttributes();
      clonedTermAtt = clonedToken.getAttribute(CharTermAttribute.class);
      clonedOffsetAtt = clonedToken.getAttribute(OffsetAttribute.class);
    } else {
      this.copyTo(clonedToken);
    }
    
    // reinit CharacterIterator
    charIterator.setText(clonedTermAtt.buffer(), 0, clonedTermAtt.length());
    breaker.setText(charIterator);
    int end = breaker.next();
    if (end != BreakIterator.DONE) {
      termAtt.setLength(end);
      if (hasIllegalOffsets) {
        offsetAtt.setOffset(clonedOffsetAtt.startOffset(), clonedOffsetAtt.endOffset());
      } else {
        offsetAtt.setOffset(clonedOffsetAtt.startOffset(), clonedOffsetAtt.startOffset() + end);
      }
      // position increment keeps as it is for first token
      return true;
    }
    return false;
  }
  
  @Override
  public void reset() throws IOException {
    super.reset();
    hasMoreTokensInClone = false;
    clonedToken = null;
    clonedTermAtt = null;
    clonedOffsetAtt = null;
  }
}
