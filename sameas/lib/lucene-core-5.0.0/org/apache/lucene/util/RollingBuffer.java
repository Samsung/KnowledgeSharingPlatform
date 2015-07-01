package org.apache.lucene.util;

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

/** Acts like forever growing T[], but internally uses a
 *  circular buffer to reuse instances of T.
 * 
 *  @lucene.internal */
public abstract class RollingBuffer<T extends RollingBuffer.Resettable> {

  /**
   * Implement to reset an instance
   */
  public static interface Resettable {
    public void reset();
  }

  @SuppressWarnings("unchecked") private T[] buffer = (T[]) new RollingBuffer.Resettable[8];

  // Next array index to write to:
  private int nextWrite;

  // Next position to write:
  private int nextPos;

  // How many valid Position are held in the
  // array:
  private int count;

  public RollingBuffer() {
    for(int idx=0;idx<buffer.length;idx++) {
      buffer[idx] = newInstance();
    }
  }

  protected abstract T newInstance();

  public void reset() {
    nextWrite--;
    while (count > 0) {
      if (nextWrite == -1) {
        nextWrite = buffer.length - 1;
      }
      buffer[nextWrite--].reset();
      count--;
    }
    nextWrite = 0;
    nextPos = 0;
    count = 0;
  }

  // For assert:
  private boolean inBounds(int pos) {
    return pos < nextPos && pos >= nextPos - count;
  }

  private int getIndex(int pos) {
    int index = nextWrite - (nextPos - pos);
    if (index < 0) {
      index += buffer.length;
    }
    return index;
  }

  /** Get T instance for this absolute position;
   *  this is allowed to be arbitrarily far "in the
   *  future" but cannot be before the last freeBefore. */
  public T get(int pos) {
    //System.out.println("RA.get pos=" + pos + " nextPos=" + nextPos + " nextWrite=" + nextWrite + " count=" + count);
    while (pos >= nextPos) {
      if (count == buffer.length) {
        @SuppressWarnings("unchecked") T[] newBuffer = (T[]) new Resettable[ArrayUtil.oversize(1+count, RamUsageEstimator.NUM_BYTES_OBJECT_REF)];
        //System.out.println("  grow length=" + newBuffer.length);
        System.arraycopy(buffer, nextWrite, newBuffer, 0, buffer.length-nextWrite);
        System.arraycopy(buffer, 0, newBuffer, buffer.length-nextWrite, nextWrite);
        for(int i=buffer.length;i<newBuffer.length;i++) {
          newBuffer[i] = newInstance();
        }
        nextWrite = buffer.length;
        buffer = newBuffer;
      }
      if (nextWrite == buffer.length) {
        nextWrite = 0;
      }
      // Should have already been reset:
      nextWrite++;
      nextPos++;
      count++;
    }
    assert inBounds(pos);
    final int index = getIndex(pos);
    //System.out.println("  pos=" + pos + " nextPos=" + nextPos + " -> index=" + index);
    //assert buffer[index].pos == pos;
    return buffer[index];
  }

  /** Returns the maximum position looked up, or -1 if no
  *  position has been looked up sinc reset/init.  */
  public int getMaxPos() {
    return nextPos-1;
  }

  public void freeBefore(int pos) {
    final int toFree = count - (nextPos - pos);
    assert toFree >= 0;
    assert toFree <= count: "toFree=" + toFree + " count=" + count;
    int index = nextWrite - count;
    if (index < 0) {
      index += buffer.length;
    }
    for(int i=0;i<toFree;i++) {
      if (index == buffer.length) {
        index = 0;
      }
      //System.out.println("  fb idx=" + index);
      buffer[index].reset();
      index++;
    }
    count -= toFree;
  }
}
