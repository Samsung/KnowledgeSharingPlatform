package org.apache.lucene.analysis.util;

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

import java.text.BreakIterator; // javadoc
import java.text.CharacterIterator;
import java.util.Locale;

/** 
 * A CharacterIterator used internally for use with {@link BreakIterator}
 * @lucene.internal
 */
public abstract class CharArrayIterator implements CharacterIterator {
  private char array[];
  private int start;
  private int index;
  private int length;
  private int limit;

  public char [] getText() {
    return array;
  }
  
  public int getStart() {
    return start;
  }
  
  public int getLength() {
    return length;
  }
  
  /**
   * Set a new region of text to be examined by this iterator
   * 
   * @param array text buffer to examine
   * @param start offset into buffer
   * @param length maximum length to examine
   */
  public void setText(final char array[], int start, int length) {
    this.array = array;
    this.start = start;
    this.index = start;
    this.length = length;
    this.limit = start + length;
  }

  @Override
  public char current() {
    return (index == limit) ? DONE : jreBugWorkaround(array[index]);
  }
  
  protected abstract char jreBugWorkaround(char ch);

  @Override
  public char first() {
    index = start;
    return current();
  }

  @Override
  public int getBeginIndex() {
    return 0;
  }

  @Override
  public int getEndIndex() {
    return length;
  }

  @Override
  public int getIndex() {
    return index - start;
  }

  @Override
  public char last() {
    index = (limit == start) ? limit : limit - 1;
    return current();
  }

  @Override
  public char next() {
    if (++index >= limit) {
      index = limit;
      return DONE;
    } else {
      return current();
    }
  }

  @Override
  public char previous() {
    if (--index < start) {
      index = start;
      return DONE;
    } else {
      return current();
    }
  }

  @Override
  public char setIndex(int position) {
    if (position < getBeginIndex() || position > getEndIndex())
      throw new IllegalArgumentException("Illegal Position: " + position);
    index = start + position;
    return current();
  }
  
  @Override
  public CharArrayIterator clone() {
    try {
      return (CharArrayIterator)super.clone();
    } catch (CloneNotSupportedException e) {
      // CharacterIterator does not allow you to throw CloneNotSupported
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Create a new CharArrayIterator that works around JRE bugs
   * in a manner suitable for {@link BreakIterator#getSentenceInstance()}
   */
  public static CharArrayIterator newSentenceInstance() {
    if (HAS_BUGGY_BREAKITERATORS) {
      return new CharArrayIterator() {
        // work around this for now by lying about all surrogates to 
        // the sentence tokenizer, instead we treat them all as 
        // SContinue so we won't break around them.
        @Override
        protected char jreBugWorkaround(char ch) {
          return ch >= 0xD800 && ch <= 0xDFFF ? 0x002C : ch;
        }
      };
    } else {
      return new CharArrayIterator() {
        // no bugs
        @Override
        protected char jreBugWorkaround(char ch) {
          return ch;
        }
      };
    }
  }
  
  /**
   * Create a new CharArrayIterator that works around JRE bugs
   * in a manner suitable for {@link BreakIterator#getWordInstance()}
   */
  public static CharArrayIterator newWordInstance() {
    if (HAS_BUGGY_BREAKITERATORS) {
      return new CharArrayIterator() {
        // work around this for now by lying about all surrogates to the word, 
        // instead we treat them all as ALetter so we won't break around them.
        @Override
        protected char jreBugWorkaround(char ch) {
          return ch >= 0xD800 && ch <= 0xDFFF ? 0x0041 : ch;
        }
      };
    } else {
      return new CharArrayIterator() {
        // no bugs
        @Override
        protected char jreBugWorkaround(char ch) {
          return ch;
        }
      };
    }
  }
  
  /**
   * True if this JRE has a buggy BreakIterator implementation
   */
  public static final boolean HAS_BUGGY_BREAKITERATORS;
  static {
    boolean v;
    try {
      BreakIterator bi = BreakIterator.getSentenceInstance(Locale.US);
      bi.setText("\udb40\udc53");
      bi.next();
      v = false;
    } catch (Exception e) {
      v = true;
    }
    HAS_BUGGY_BREAKITERATORS = v;
  }
}
