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

import java.io.IOException;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;

/**
 * Emits the entire input as a single token.
 */
public final class KeywordTokenizer extends Tokenizer {
  /** Default read buffer size */ 
  public static final int DEFAULT_BUFFER_SIZE = 256;

  private boolean done = false;
  private int finalOffset;
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
  
  public KeywordTokenizer() {
    this(DEFAULT_BUFFER_SIZE);
  }

  public KeywordTokenizer(int bufferSize) {
    if (bufferSize <= 0) {
      throw new IllegalArgumentException("bufferSize must be > 0");
    }
    termAtt.resizeBuffer(bufferSize);
  }

  public KeywordTokenizer(AttributeFactory factory, int bufferSize) {
    super(factory);
    if (bufferSize <= 0) {
      throw new IllegalArgumentException("bufferSize must be > 0");
    }
    termAtt.resizeBuffer(bufferSize);
  }
  
  @Override
  public final boolean incrementToken() throws IOException {
    if (!done) {
      clearAttributes();
      done = true;
      int upto = 0;
      char[] buffer = termAtt.buffer();
      while (true) {
        final int length = input.read(buffer, upto, buffer.length-upto);
        if (length == -1) break;
        upto += length;
        if (upto == buffer.length)
          buffer = termAtt.resizeBuffer(1+buffer.length);
      }
      termAtt.setLength(upto);
      finalOffset = correctOffset(upto);
      offsetAtt.setOffset(correctOffset(0), finalOffset);
      return true;
    }
    return false;
  }
  
  @Override
  public final void end() throws IOException {
    super.end();
    // set final offset 
    offsetAtt.setOffset(finalOffset, finalOffset);
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    this.done = false;
  }
}
