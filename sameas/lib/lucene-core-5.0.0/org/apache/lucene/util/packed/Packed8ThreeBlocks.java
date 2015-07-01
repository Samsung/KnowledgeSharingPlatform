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
 * Packs integers into 3 bytes (24 bits per value).
 * @lucene.internal
 */
final class Packed8ThreeBlocks extends PackedInts.MutableImpl {
  final byte[] blocks;

  public static final int MAX_SIZE = Integer.MAX_VALUE / 3;

  Packed8ThreeBlocks(int valueCount) {
    super(valueCount, 24);
    if (valueCount > MAX_SIZE) {
      throw new ArrayIndexOutOfBoundsException("MAX_SIZE exceeded");
    }
    blocks = new byte[valueCount * 3];
  }

  Packed8ThreeBlocks(int packedIntsVersion, DataInput in, int valueCount) throws IOException {
    this(valueCount);
    in.readBytes(blocks, 0, 3 * valueCount);
    // because packed ints have not always been byte-aligned
    final int remaining = (int) (PackedInts.Format.PACKED.byteCount(packedIntsVersion, valueCount, 24) - 3L * valueCount * 1);
    for (int i = 0; i < remaining; ++i) {
       in.readByte();
    }
  }

  @Override
  public long get(int index) {
    final int o = index * 3;
    return (blocks[o] & 0xFFL) << 16 | (blocks[o+1] & 0xFFL) << 8 | (blocks[o+2] & 0xFFL);
  }

  @Override
  public int get(int index, long[] arr, int off, int len) {
    assert len > 0 : "len must be > 0 (got " + len + ")";
    assert index >= 0 && index < valueCount;
    assert off + len <= arr.length;

    final int gets = Math.min(valueCount - index, len);
    for (int i = index * 3, end = (index + gets) * 3; i < end; i+=3) {
      arr[off++] = (blocks[i] & 0xFFL) << 16 | (blocks[i+1] & 0xFFL) << 8 | (blocks[i+2] & 0xFFL);
    }
    return gets;
  }

  @Override
  public void set(int index, long value) {
    final int o = index * 3;
    blocks[o] = (byte) (value >>> 16);
    blocks[o+1] = (byte) (value >>> 8);
    blocks[o+2] = (byte) value;
  }

  @Override
  public int set(int index, long[] arr, int off, int len) {
    assert len > 0 : "len must be > 0 (got " + len + ")";
    assert index >= 0 && index < valueCount;
    assert off + len <= arr.length;

    final int sets = Math.min(valueCount - index, len);
    for (int i = off, o = index * 3, end = off + sets; i < end; ++i) {
      final long value = arr[i];
      blocks[o++] = (byte) (value >>> 16);
      blocks[o++] = (byte) (value >>> 8);
      blocks[o++] = (byte) value;
    }
    return sets;
  }

  @Override
  public void fill(int fromIndex, int toIndex, long val) {
    final byte block1 = (byte) (val >>> 16);
    final byte block2 = (byte) (val >>> 8);
    final byte block3 = (byte) val;
    for (int i = fromIndex * 3, end = toIndex * 3; i < end; i += 3) {
      blocks[i] = block1;
      blocks[i+1] = block2;
      blocks[i+2] = block3;
    }
  }

  @Override
  public void clear() {
    Arrays.fill(blocks, (byte) 0);
  }

  @Override
  public long ramBytesUsed() {
    return RamUsageEstimator.alignObjectSize(
        RamUsageEstimator.NUM_BYTES_OBJECT_HEADER
        + 2 * RamUsageEstimator.NUM_BYTES_INT     // valueCount,bitsPerValue
        + RamUsageEstimator.NUM_BYTES_OBJECT_REF) // blocks ref
        + RamUsageEstimator.sizeOf(blocks);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(bitsPerValue=" + bitsPerValue
        + ",size=" + size() + ",blocks=" + blocks.length + ")";
  }
}
