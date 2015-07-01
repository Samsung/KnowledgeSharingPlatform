package org.apache.lucene.util.packed;

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

import static org.apache.lucene.util.packed.PackedInts.checkBlockSize;
import static org.apache.lucene.util.packed.PackedInts.numBlocks;

import java.util.Collection;
import java.util.Collections;

import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.LongValues;
import org.apache.lucene.util.RamUsageEstimator;

/**
 * Base implementation for {@link PagedMutable} and {@link PagedGrowableWriter}.
 * @lucene.internal
 */
abstract class AbstractPagedMutable<T extends AbstractPagedMutable<T>> extends LongValues implements Accountable {

  static final int MIN_BLOCK_SIZE = 1 << 6;
  static final int MAX_BLOCK_SIZE = 1 << 30;

  final long size;
  final int pageShift;
  final int pageMask;
  final PackedInts.Mutable[] subMutables;
  final int bitsPerValue;

  AbstractPagedMutable(int bitsPerValue, long size, int pageSize) {
    this.bitsPerValue = bitsPerValue;
    this.size = size;
    pageShift = checkBlockSize(pageSize, MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);
    pageMask = pageSize - 1;
    final int numPages = numBlocks(size, pageSize);
    subMutables = new PackedInts.Mutable[numPages];
  }

  protected final void fillPages() {
    final int numPages = numBlocks(size, pageSize());
    for (int i = 0; i < numPages; ++i) {
      // do not allocate for more entries than necessary on the last page
      final int valueCount = i == numPages - 1 ? lastPageSize(size) : pageSize();
      subMutables[i] = newMutable(valueCount, bitsPerValue);
    }
  }

  protected abstract PackedInts.Mutable newMutable(int valueCount, int bitsPerValue);

  final int lastPageSize(long size) {
    final int sz = indexInPage(size);
    return sz == 0 ? pageSize() : sz;
  }

  final int pageSize() {
    return pageMask + 1;
  }

  /** The number of values. */
  public final long size() {
    return size;
  }

  final int pageIndex(long index) {
    return (int) (index >>> pageShift);
  }

  final int indexInPage(long index) {
    return (int) index & pageMask;
  }

  @Override
  public final long get(long index) {
    assert index >= 0 && index < size;
    final int pageIndex = pageIndex(index);
    final int indexInPage = indexInPage(index);
    return subMutables[pageIndex].get(indexInPage);
  }

  /** Set value at <code>index</code>. */
  public final void set(long index, long value) {
    assert index >= 0 && index < size;
    final int pageIndex = pageIndex(index);
    final int indexInPage = indexInPage(index);
    subMutables[pageIndex].set(indexInPage, value);
  }

  protected long baseRamBytesUsed() {
    return RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
        + RamUsageEstimator.NUM_BYTES_OBJECT_REF
        + RamUsageEstimator.NUM_BYTES_LONG
        + 3 * RamUsageEstimator.NUM_BYTES_INT;
  }

  @Override
  public long ramBytesUsed() {
    long bytesUsed = RamUsageEstimator.alignObjectSize(baseRamBytesUsed());
    bytesUsed += RamUsageEstimator.alignObjectSize(RamUsageEstimator.shallowSizeOf(subMutables));
    for (PackedInts.Mutable gw : subMutables) {
      bytesUsed += gw.ramBytesUsed();
    }
    return bytesUsed;
  }
  
  @Override
  public Collection<Accountable> getChildResources() {
    return Collections.emptyList();
  }

  protected abstract T newUnfilledCopy(long newSize);

  /** Create a new copy of size <code>newSize</code> based on the content of
   *  this buffer. This method is much more efficient than creating a new
   *  instance and copying values one by one. */
  public final T resize(long newSize) {
    final T copy = newUnfilledCopy(newSize);
    final int numCommonPages = Math.min(copy.subMutables.length, subMutables.length);
    final long[] copyBuffer = new long[1024];
    for (int i = 0; i < copy.subMutables.length; ++i) {
      final int valueCount = i == copy.subMutables.length - 1 ? lastPageSize(newSize) : pageSize();
      final int bpv = i < numCommonPages ? subMutables[i].getBitsPerValue() : this.bitsPerValue;
      copy.subMutables[i] = newMutable(valueCount, bpv);
      if (i < numCommonPages) {
        final int copyLength = Math.min(valueCount, subMutables[i].size());
        PackedInts.copy(subMutables[i], 0, copy.subMutables[i], 0, copyLength, copyBuffer);
      }
    }
    return copy;
  }

  /** Similar to {@link ArrayUtil#grow(long[], int)}. */
  public final T grow(long minSize) {
    assert minSize >= 0;
    if (minSize <= size()) {
      @SuppressWarnings("unchecked")
      final T result = (T) this;
      return result;
    }
    long extra = minSize >>> 3;
    if (extra < 3) {
      extra = 3;
    }
    final long newSize = minSize + extra;
    return resize(newSize);
  }

  /** Similar to {@link ArrayUtil#grow(long[])}. */
  public final T grow() {
    return grow(size() + 1);
  }

  @Override
  public final String toString() {
    return getClass().getSimpleName() + "(size=" + size() + ",pageSize=" + pageSize() + ")";
  }

}
