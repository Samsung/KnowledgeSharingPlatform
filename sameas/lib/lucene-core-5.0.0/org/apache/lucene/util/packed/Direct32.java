// This file has been automatically generated, DO NOT EDIT

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

import org.apache.lucene.store.DataInput;
import org.apache.lucene.util.RamUsageEstimator;

import java.io.IOException;
import java.util.Arrays;

/**
 * Direct wrapping of 32-bits values to a backing array.
 * @lucene.internal
 */
final class Direct32 extends PackedInts.MutableImpl {
  final int[] values;

  Direct32(int valueCount) {
    super(valueCount, 32);
    values = new int[valueCount];
  }

  Direct32(int packedIntsVersion, DataInput in, int valueCount) throws IOException {
    this(valueCount);
    for (int i = 0; i < valueCount; ++i) {
      values[i] = in.readInt();
    }
    // because packed ints have not always been byte-aligned
    final int remaining = (int) (PackedInts.Format.PACKED.byteCount(packedIntsVersion, valueCount, 32) - 4L * valueCount);
    for (int i = 0; i < remaining; ++i) {
      in.readByte();
    }
  }

  @Override
  public long get(final int index) {
    return values[index] & 0xFFFFFFFFL;
  }

  @Override
  public void set(final int index, final long value) {
    values[index] = (int) (value);
  }

  @Override
  public long ramBytesUsed() {
    return RamUsageEstimator.alignObjectSize(
        RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
        + 2 * RamUsageEstimator.NUM_BYTES_INT     // valueCount,bitsPerValue
        + RamUsageEstimator.NUM_BYTES_OBJECT_REF) // values ref
        + RamUsageEstimator.sizeOf(values);
  }

  @Override
  public void clear() {
    Arrays.fill(values, (int) 0L);
  }

  @Override
  public int get(int index, long[] arr, int off, int len) {
    assert len > 0 : "len must be > 0 (got " + len + ")";
    assert index >= 0 && index < valueCount;
    assert off + len <= arr.length;

    final int gets = Math.min(valueCount - index, len);
    for (int i = index, o = off, end = index + gets; i < end; ++i, ++o) {
      arr[o] = values[i] & 0xFFFFFFFFL;
    }
    return gets;
  }

  @Override
  public int set(int index, long[] arr, int off, int len) {
    assert len > 0 : "len must be > 0 (got " + len + ")";
    assert index >= 0 && index < valueCount;
    assert off + len <= arr.length;

    final int sets = Math.min(valueCount - index, len);
    for (int i = index, o = off, end = index + sets; i < end; ++i, ++o) {
      values[i] = (int) arr[o];
    }
    return sets;
  }

  @Override
  public void fill(int fromIndex, int toIndex, long val) {
    assert val == (val & 0xFFFFFFFFL);
    Arrays.fill(values, fromIndex, toIndex, (int) val);
  }
}
