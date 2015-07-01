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

import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.store.DataInput;
import org.apache.lucene.util.LongsRef;

final class PackedReaderIterator extends PackedInts.ReaderIteratorImpl {

  final int packedIntsVersion;
  final PackedInts.Format format;
  final BulkOperation bulkOperation;
  final byte[] nextBlocks;
  final LongsRef nextValues;
  final int iterations;
  int position;

  PackedReaderIterator(PackedInts.Format format, int packedIntsVersion, int valueCount, int bitsPerValue, DataInput in, int mem) {
    super(valueCount, bitsPerValue, in);
    this.format = format;
    this.packedIntsVersion = packedIntsVersion;
    bulkOperation = BulkOperation.of(format, bitsPerValue);
    iterations = iterations(mem);
    assert valueCount == 0 || iterations > 0;
    nextBlocks = new byte[iterations * bulkOperation.byteBlockCount()];
    nextValues = new LongsRef(new long[iterations * bulkOperation.byteValueCount()], 0, 0);
    nextValues.offset = nextValues.longs.length;
    position = -1;
  }

  private int iterations(int mem) {
    int iterations = bulkOperation.computeIterations(valueCount, mem);
    if (packedIntsVersion < PackedInts.VERSION_BYTE_ALIGNED) {
      // make sure iterations is a multiple of 8
      iterations = (iterations + 7) & 0xFFFFFFF8;
    }
    return iterations;
  }

  @Override
  public LongsRef next(int count) throws IOException {
    assert nextValues.length >= 0;
    assert count > 0;
    assert nextValues.offset + nextValues.length <= nextValues.longs.length;
    
    nextValues.offset += nextValues.length;

    final int remaining = valueCount - position - 1;
    if (remaining <= 0) {
      throw new EOFException();
    }
    count = Math.min(remaining, count);

    if (nextValues.offset == nextValues.longs.length) {
      final long remainingBlocks = format.byteCount(packedIntsVersion, remaining, bitsPerValue);
      final int blocksToRead = (int) Math.min(remainingBlocks, nextBlocks.length);
      in.readBytes(nextBlocks, 0, blocksToRead);
      if (blocksToRead < nextBlocks.length) {
        Arrays.fill(nextBlocks, blocksToRead, nextBlocks.length, (byte) 0);
      }

      bulkOperation.decode(nextBlocks, 0, nextValues.longs, 0, iterations);
      nextValues.offset = 0;
    }

    nextValues.length = Math.min(nextValues.longs.length - nextValues.offset, count);
    position += nextValues.length;
    return nextValues;
  }

  @Override
  public int ord() {
    return position;
  }

}
