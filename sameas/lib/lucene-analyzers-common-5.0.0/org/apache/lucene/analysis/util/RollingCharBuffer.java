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

import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.RamUsageEstimator;

/** Acts like a forever growing char[] as you read
 *  characters into it from the provided reader, but
 *  internally it uses a circular buffer to only hold the
 *  characters that haven't been freed yet.  This is like a
 *  PushbackReader, except you don't have to specify
 *  up-front the max size of the buffer, but you do have to
 *  periodically call {@link #freeBefore}. */

public final class RollingCharBuffer {

  private Reader reader;

  private char[] buffer = new char[512];

  // Next array index to write to in buffer:
  private int nextWrite;

  // Next absolute position to read from reader:
  private int nextPos;

  // How many valid chars (wrapped) are in the buffer:
  private int count;

  // True if we hit EOF
  private boolean end;
    
  /** Clear array and switch to new reader. */
  public void reset(Reader reader) {
    this.reader = reader;
    nextPos = 0;
    nextWrite = 0;
    count = 0;
    end = false;
  }

  /* Absolute position read.  NOTE: pos must not jump
   * ahead by more than 1!  Ie, it's OK to read arbitarily
   * far back (just not prior to the last {@link
   * #freeBefore}), but NOT ok to read arbitrarily far
   * ahead.  Returns -1 if you hit EOF. */
  public int get(int pos) throws IOException {
    //System.out.println("    get pos=" + pos + " nextPos=" + nextPos + " count=" + count);
    if (pos == nextPos) {
      if (end) {
        return -1;
      }
      if (count == buffer.length) {
        // Grow
        final char[] newBuffer = new char[ArrayUtil.oversize(1+count, RamUsageEstimator.NUM_BYTES_CHAR)];
        //System.out.println(Thread.currentThread().getName() + ": cb grow " + newBuffer.length);
        System.arraycopy(buffer, nextWrite, newBuffer, 0, buffer.length - nextWrite);
        System.arraycopy(buffer, 0, newBuffer, buffer.length - nextWrite, nextWrite);
        nextWrite = buffer.length;
        buffer = newBuffer;
      }
      if (nextWrite == buffer.length) {
        nextWrite = 0;
      }

      final int toRead = buffer.length - Math.max(count, nextWrite);
      final int readCount = reader.read(buffer, nextWrite, toRead);
      if (readCount == -1) {
        end = true;
        return -1;
      }
      final int ch = buffer[nextWrite];
      nextWrite += readCount;
      count += readCount;
      nextPos += readCount;
      return ch;
    } else {
      // Cannot read from future (except by 1):
      assert pos < nextPos;

      // Cannot read from already freed past:
      assert nextPos - pos <= count: "nextPos=" + nextPos + " pos=" + pos + " count=" + count;

      return buffer[getIndex(pos)];
    }
  }

  // For assert:
  private boolean inBounds(int pos) {
    return pos >= 0 && pos < nextPos && pos >= nextPos - count;
  }

  private int getIndex(int pos) {
    int index = nextWrite - (nextPos - pos);
    if (index < 0) {
      // Wrap:
      index += buffer.length;
      assert index >= 0;
    }
    return index;
  }

  public char[] get(int posStart, int length) {
    assert length > 0;
    assert inBounds(posStart): "posStart=" + posStart + " length=" + length;
    //System.out.println("    buffer.get posStart=" + posStart + " len=" + length);
      
    final int startIndex = getIndex(posStart);
    final int endIndex = getIndex(posStart + length);
    //System.out.println("      startIndex=" + startIndex + " endIndex=" + endIndex);

    final char[] result = new char[length];
    if (endIndex >= startIndex && length < buffer.length) {
      System.arraycopy(buffer, startIndex, result, 0, endIndex-startIndex);
    } else {
      // Wrapped:
      final int part1 = buffer.length-startIndex;
      System.arraycopy(buffer, startIndex, result, 0, part1);
      System.arraycopy(buffer, 0, result, buffer.length-startIndex, length-part1);
    }
    return result;
  }

  /** Call this to notify us that no chars before this
   *  absolute position are needed anymore. */
  public void freeBefore(int pos) {
    assert pos >= 0;
    assert pos <= nextPos;
    final int newCount = nextPos - pos;
    assert newCount <= count: "newCount=" + newCount + " count=" + count;
    assert newCount <= buffer.length: "newCount=" + newCount + " buf.length=" + buffer.length;
    count = newCount;
  }
}
