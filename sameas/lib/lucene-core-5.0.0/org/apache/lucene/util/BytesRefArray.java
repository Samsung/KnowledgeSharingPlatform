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

import java.util.Arrays;
import java.util.Comparator;

import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.ByteBlockPool;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefIterator;
import org.apache.lucene.util.Counter;
import org.apache.lucene.util.IntroSorter;
import org.apache.lucene.util.RamUsageEstimator;

/**
 * A simple append only random-access {@link BytesRef} array that stores full
 * copies of the appended bytes in a {@link ByteBlockPool}.
 * 
 * 
 * <b>Note: This class is not Thread-Safe!</b>
 * 
 * @lucene.internal
 * @lucene.experimental
 */
public final class BytesRefArray {
  private final ByteBlockPool pool;
  private int[] offsets = new int[1];
  private int lastElement = 0;
  private int currentOffset = 0;
  private final Counter bytesUsed;
  
  /**
   * Creates a new {@link BytesRefArray} with a counter to track allocated bytes
   */
  public BytesRefArray(Counter bytesUsed) {
    this.pool = new ByteBlockPool(new ByteBlockPool.DirectTrackingAllocator(
        bytesUsed));
    pool.nextBuffer();
    bytesUsed.addAndGet(RamUsageEstimator.NUM_BYTES_ARRAY_HEADER
        + RamUsageEstimator.NUM_BYTES_INT);
    this.bytesUsed = bytesUsed;
  }
 
  /**
   * Clears this {@link BytesRefArray}
   */
  public void clear() {
    lastElement = 0;
    currentOffset = 0;
    Arrays.fill(offsets, 0);
    pool.reset(false, true); // no need to 0 fill the buffers we control the allocator
  }
  
  /**
   * Appends a copy of the given {@link BytesRef} to this {@link BytesRefArray}.
   * @param bytes the bytes to append
   * @return the index of the appended bytes
   */
  public int append(BytesRef bytes) {
    if (lastElement >= offsets.length) {
      int oldLen = offsets.length;
      offsets = ArrayUtil.grow(offsets, offsets.length + 1);
      bytesUsed.addAndGet((offsets.length - oldLen)
          * RamUsageEstimator.NUM_BYTES_INT);
    }
    pool.append(bytes);
    offsets[lastElement++] = currentOffset;
    currentOffset += bytes.length;
    return lastElement-1;
  }
  
  /**
   * Returns the current size of this {@link BytesRefArray}
   * @return the current size of this {@link BytesRefArray}
   */
  public int size() {
    return lastElement;
  }
  
  /**
   * Returns the <i>n'th</i> element of this {@link BytesRefArray}
   * @param spare a spare {@link BytesRef} instance
   * @param index the elements index to retrieve 
   * @return the <i>n'th</i> element of this {@link BytesRefArray}
   */
  public BytesRef get(BytesRefBuilder spare, int index) {
    if (lastElement > index) {
      int offset = offsets[index];
      int length = index == lastElement - 1 ? currentOffset - offset
          : offsets[index + 1] - offset;
      spare.grow(length);
      spare.setLength(length);
      pool.readBytes(offset, spare.bytes(), 0, spare.length());
      return spare.get();
    }
    throw new IndexOutOfBoundsException("index " + index
        + " must be less than the size: " + lastElement);
    
  }
  
  private int[] sort(final Comparator<BytesRef> comp) {
    final int[] orderedEntries = new int[size()];
    for (int i = 0; i < orderedEntries.length; i++) {
      orderedEntries[i] = i;
    }
    new IntroSorter() {
      @Override
      protected void swap(int i, int j) {
        final int o = orderedEntries[i];
        orderedEntries[i] = orderedEntries[j];
        orderedEntries[j] = o;
      }
      
      @Override
      protected int compare(int i, int j) {
        final int idx1 = orderedEntries[i], idx2 = orderedEntries[j];
        return comp.compare(get(scratch1, idx1), get(scratch2, idx2));
      }
      
      @Override
      protected void setPivot(int i) {
        final int index = orderedEntries[i];
        pivot = get(pivotBuilder, index);
      }
      
      @Override
      protected int comparePivot(int j) {
        final int index = orderedEntries[j];
        return comp.compare(pivot, get(scratch2, index));
      }

      private BytesRef pivot;
      private final BytesRefBuilder pivotBuilder = new BytesRefBuilder(),
          scratch1 = new BytesRefBuilder(),
          scratch2 = new BytesRefBuilder();
    }.sort(0, size());
    return orderedEntries;
  }
  
  /**
   * sugar for {@link #iterator(Comparator)} with a <code>null</code> comparator
   */
  public BytesRefIterator iterator() {
    return iterator(null);
  }
  
  /**
   * <p>
   * Returns a {@link BytesRefIterator} with point in time semantics. The
   * iterator provides access to all so far appended {@link BytesRef} instances.
   * </p>
   * <p>
   * If a non <code>null</code> {@link Comparator} is provided the iterator will
   * iterate the byte values in the order specified by the comparator. Otherwise
   * the order is the same as the values were appended.
   * </p>
   * <p>
   * This is a non-destructive operation.
   * </p>
   */
  public BytesRefIterator iterator(final Comparator<BytesRef> comp) {
    final BytesRefBuilder spare = new BytesRefBuilder();
    final int size = size();
    final int[] indices = comp == null ? null : sort(comp);
    return new BytesRefIterator() {
      int pos = 0;
      
      @Override
      public BytesRef next() {
        if (pos < size) {
          return get(spare, indices == null ? pos++ : indices[pos++]);
        }
        return null;
      }
    };
  }
}
