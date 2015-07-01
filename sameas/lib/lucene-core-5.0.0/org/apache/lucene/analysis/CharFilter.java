package org.apache.lucene.analysis;

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

/**
 * Subclasses of CharFilter can be chained to filter a Reader
 * They can be used as {@link java.io.Reader} with additional offset
 * correction. {@link Tokenizer}s will automatically use {@link #correctOffset}
 * if a CharFilter subclass is used.
 * <p>
 * This class is abstract: at a minimum you must implement {@link #read(char[], int, int)},
 * transforming the input in some way from {@link #input}, and {@link #correct(int)}
 * to adjust the offsets to match the originals.
 * <p>
 * You can optionally provide more efficient implementations of additional methods 
 * like {@link #read()}, {@link #read(char[])}, {@link #read(java.nio.CharBuffer)},
 * but this is not required.
 * <p>
 * For examples and integration with {@link Analyzer}, see the 
 * {@link org.apache.lucene.analysis Analysis package documentation}.
 */
// the way java.io.FilterReader should work!
public abstract class CharFilter extends Reader {
  /** 
   * The underlying character-input stream. 
   */
  protected final Reader input;

  /**
   * Create a new CharFilter wrapping the provided reader.
   * @param input a Reader, can also be a CharFilter for chaining.
   */
  public CharFilter(Reader input) {
    super(input);
    this.input = input;
  }
  
  /** 
   * Closes the underlying input stream.
   * <p>
   * <b>NOTE:</b> 
   * The default implementation closes the input Reader, so
   * be sure to call <code>super.close()</code> when overriding this method.
   */
  @Override
  public void close() throws IOException {
    input.close();
  }

  /**
   * Subclasses override to correct the current offset.
   *
   * @param currentOff current offset
   * @return corrected offset
   */
  protected abstract int correct(int currentOff);
  
  /**
   * Chains the corrected offset through the input
   * CharFilter(s).
   */
  public final int correctOffset(int currentOff) {
    final int corrected = correct(currentOff);
    return (input instanceof CharFilter) ? ((CharFilter) input).correctOffset(corrected) : corrected;
  }
}
