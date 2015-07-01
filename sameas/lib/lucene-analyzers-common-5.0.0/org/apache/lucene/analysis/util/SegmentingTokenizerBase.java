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

import java.io.IOException;
import java.io.Reader;
import java.text.BreakIterator;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;

/**
 * Breaks text into sentences with a {@link BreakIterator} and
 * allows subclasses to decompose these sentences into words.
 * <p>
 * This can be used by subclasses that need sentence context 
 * for tokenization purposes, such as CJK segmenters.
 * <p>
 * Additionally it can be used by subclasses that want to mark
 * sentence boundaries (with a custom attribute, extra token, position
 * increment, etc) for downstream processing.
 * 
 * @lucene.experimental
 */
public abstract class SegmentingTokenizerBase extends Tokenizer {
  protected static final int BUFFERMAX = 1024;
  protected final char buffer[] = new char[BUFFERMAX];
  /** true length of text in the buffer */
  private int length = 0; 
  /** length in buffer that can be evaluated safely, up to a safe end point */
  private int usableLength = 0; 
  /** accumulated offset of previous buffers for this reader, for offsetAtt */
  protected int offset = 0;
  
  private final BreakIterator iterator;
  private final CharArrayIterator wrapper = CharArrayIterator.newSentenceInstance();

  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

  /**
   * Construct a new SegmenterBase, using
   * the provided BreakIterator for sentence segmentation.
   * <p>
   * Note that you should never share BreakIterators across different
   * TokenStreams, instead a newly created or cloned one should always
   * be provided to this constructor.
   */
  public SegmentingTokenizerBase(BreakIterator iterator) {
    this(DEFAULT_TOKEN_ATTRIBUTE_FACTORY, iterator);
  }
  
  /**
   * Construct a new SegmenterBase, also supplying the AttributeFactory
   */
  public SegmentingTokenizerBase(AttributeFactory factory, BreakIterator iterator) {
    super(factory);
    this.iterator = iterator;
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (length == 0 || !incrementWord()) {
      while (!incrementSentence()) {
        refill();
        if (length <= 0) // no more bytes to read;
          return false;
      }
    }
    
    return true;
  }
  
  @Override
  public void reset() throws IOException {
    super.reset();
    wrapper.setText(buffer, 0, 0);
    iterator.setText(wrapper);
    length = usableLength = offset = 0;
  }
  
  @Override
  public final void end() throws IOException {
    super.end();
    final int finalOffset = correctOffset(length < 0 ? offset : offset + length);
    offsetAtt.setOffset(finalOffset, finalOffset);
  }  

  /** Returns the last unambiguous break position in the text. */
  private int findSafeEnd() {
    for (int i = length - 1; i >= 0; i--)
      if (isSafeEnd(buffer[i]))
        return i + 1;
    return -1;
  }
  
  /** For sentence tokenization, these are the unambiguous break positions. */
  protected boolean isSafeEnd(char ch) {
    switch(ch) {
      case 0x000D:
      case 0x000A:
      case 0x0085:
      case 0x2028:
      case 0x2029:
        return true;
      default:
        return false;
    }
  }

  /**
   * Refill the buffer, accumulating the offset and setting usableLength to the
   * last unambiguous break position
   */
  private void refill() throws IOException {
    offset += usableLength;
    int leftover = length - usableLength;
    System.arraycopy(buffer, usableLength, buffer, 0, leftover);
    int requested = buffer.length - leftover;
    int returned = read(input, buffer, leftover, requested);
    length = returned < 0 ? leftover : returned + leftover;
    if (returned < requested) /* reader has been emptied, process the rest */
      usableLength = length;
    else { /* still more data to be read, find a safe-stopping place */
      usableLength = findSafeEnd();
      if (usableLength < 0)
        usableLength = length; /*
                                * more than IOBUFFER of text without breaks,
                                * gonna possibly truncate tokens
                                */
    }

    wrapper.setText(buffer, 0, Math.max(0, usableLength));
    iterator.setText(wrapper);
  }
  
  // TODO: refactor to a shared readFully somewhere
  // (NGramTokenizer does this too):
  /** commons-io's readFully, but without bugs if offset != 0 */
  private static int read(Reader input, char[] buffer, int offset, int length) throws IOException {
    assert length >= 0 : "length must not be negative: " + length;
 
    int remaining = length;
    while (remaining > 0) {
      int location = length - remaining;
      int count = input.read(buffer, offset + location, remaining);
      if (-1 == count) { // EOF
        break;
      }
      remaining -= count;
    }
    return length - remaining;
  }

  /**
   * return true if there is a token from the buffer, or null if it is
   * exhausted.
   */
  private boolean incrementSentence() throws IOException {
    if (length == 0) // we must refill the buffer
      return false;
    
    while (true) {
      int start = iterator.current();

      if (start == BreakIterator.DONE)
        return false; // BreakIterator exhausted

      // find the next set of boundaries
      int end = iterator.next();

      if (end == BreakIterator.DONE)
        return false; // BreakIterator exhausted

      setNextSentence(start, end);
      if (incrementWord()) {
        return true;
      }
    }
  }
  
  /** Provides the next input sentence for analysis */
  protected abstract void setNextSentence(int sentenceStart, int sentenceEnd);
  
  /** Returns true if another word is available */
  protected abstract boolean incrementWord();
}
