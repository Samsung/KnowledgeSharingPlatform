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

import static org.apache.lucene.util.BitUtil.zigZagDecode;
import static org.apache.lucene.util.packed.AbstractBlockPackedWriter.BPV_SHIFT;
import static org.apache.lucene.util.packed.AbstractBlockPackedWriter.MAX_BLOCK_SIZE;
import static org.apache.lucene.util.packed.AbstractBlockPackedWriter.MIN_BLOCK_SIZE;
import static org.apache.lucene.util.packed.AbstractBlockPackedWriter.MIN_VALUE_EQUALS_0;
import static org.apache.lucene.util.packed.BlockPackedReaderIterator.readVLong;
import static org.apache.lucene.util.packed.PackedInts.checkBlockSize;
import static org.apache.lucene.util.packed.PackedInts.numBlocks;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.LongValues;

/**
 * Provides random access to a stream written with {@link BlockPackedWriter}.
 * @lucene.internal
 */
public final class BlockPackedReader extends LongValues implements Accountable {

  private final int blockShift, blockMask;
  private final long valueCount;
  private final long[] minValues;
  private final PackedInts.Reader[] subReaders;
  private final long sumBPV;

  /** Sole constructor. */
  public BlockPackedReader(IndexInput in, int packedIntsVersion, int blockSize, long valueCount, boolean direct) throws IOException {
    this.valueCount = valueCount;
    blockShift = checkBlockSize(blockSize, MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);
    blockMask = blockSize - 1;
    final int numBlocks = numBlocks(valueCount, blockSize);
    long[] minValues = null;
    subReaders = new PackedInts.Reader[numBlocks];
    long sumBPV = 0;
    for (int i = 0; i < numBlocks; ++i) {
      final int token = in.readByte() & 0xFF;
      final int bitsPerValue = token >>> BPV_SHIFT;
      sumBPV += bitsPerValue;
      if (bitsPerValue > 64) {
        throw new IOException("Corrupted");
      }
      if ((token & MIN_VALUE_EQUALS_0) == 0) {
        if (minValues == null) {
          minValues = new long[numBlocks];
        }
        minValues[i] = zigZagDecode(1L + readVLong(in));
      }
      if (bitsPerValue == 0) {
        subReaders[i] = new PackedInts.NullReader(blockSize);
      } else {
        final int size = (int) Math.min(blockSize, valueCount - (long) i * blockSize);
        if (direct) {
          final long pointer = in.getFilePointer();
          subReaders[i] = PackedInts.getDirectReaderNoHeader(in, PackedInts.Format.PACKED, packedIntsVersion, size, bitsPerValue);
          in.seek(pointer + PackedInts.Format.PACKED.byteCount(packedIntsVersion, size, bitsPerValue));
        } else {
          subReaders[i] = PackedInts.getReaderNoHeader(in, PackedInts.Format.PACKED, packedIntsVersion, size, bitsPerValue);
        }
      }
    }
    this.minValues = minValues;
    this.sumBPV = sumBPV;
  }

  @Override
  public long get(long index) {
    assert index >= 0 && index < valueCount;
    final int block = (int) (index >>> blockShift);
    final int idx = (int) (index & blockMask);
    return (minValues == null ? 0 : minValues[block]) + subReaders[block].get(idx);
  }

  @Override
  public long ramBytesUsed() {
    long size = 0;
    for (PackedInts.Reader reader : subReaders) {
      size += reader.ramBytesUsed();
    }
    return size;
  }

  @Override
  public Collection<Accountable> getChildResources() {
    return Collections.emptyList();
  }
  
  @Override
  public String toString() {
    long avgBPV = subReaders.length == 0 ? 0 : sumBPV / subReaders.length;
    return getClass().getSimpleName() + "(blocksize=" + (1<<blockShift) + ",size=" + valueCount + ",avgBPV=" + avgBPV + ")";
  }
}
