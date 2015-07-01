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

import java.io.IOException;

import org.apache.lucene.store.IndexInput;

final class DirectPacked64SingleBlockReader extends PackedInts.ReaderImpl {

  private final IndexInput in;
  private final int bitsPerValue;
  private final long startPointer;
  private final int valuesPerBlock;
  private final long mask;

  DirectPacked64SingleBlockReader(int bitsPerValue, int valueCount,
      IndexInput in) {
    super(valueCount);
    this.in = in;
    this.bitsPerValue = bitsPerValue;
    startPointer = in.getFilePointer();
    valuesPerBlock = 64 / bitsPerValue;
    mask = ~(~0L << bitsPerValue);
  }

  @Override
  public long get(int index) {
    final int blockOffset = index / valuesPerBlock;
    final long skip = ((long) blockOffset) << 3;
    try {
      in.seek(startPointer + skip);

      long block = in.readLong();
      final int offsetInBlock = index % valuesPerBlock;
      return (block >>> (offsetInBlock * bitsPerValue)) & mask;
    } catch (IOException e) {
      throw new IllegalStateException("failed", e);
    }
  }

  @Override
  public long ramBytesUsed() {
    return 0;
  }
}
