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

import org.apache.lucene.store.DataInput;

/**
 * A {@link DataInput} wrapper to read unaligned, variable-length packed
 * integers. This API is much slower than the {@link PackedInts} fixed-length
 * API but can be convenient to save space.
 * @see PackedDataOutput
 * @lucene.internal
 */
public final class PackedDataInput {

  final DataInput in;
  long current;
  int remainingBits;

  /**
   * Create a new instance that wraps <code>in</code>.
   */
  public PackedDataInput(DataInput in) {
    this.in = in;
    skipToNextByte();
  }

  /**
   * Read the next long using exactly <code>bitsPerValue</code> bits.
   */
  public long readLong(int bitsPerValue) throws IOException {
    assert bitsPerValue > 0 && bitsPerValue <= 64 : bitsPerValue;
    long r = 0;
    while (bitsPerValue > 0) {
      if (remainingBits == 0) {
        current = in.readByte() & 0xFF;
        remainingBits = 8;
      }
      final int bits = Math.min(bitsPerValue, remainingBits);
      r = (r << bits) | ((current >>> (remainingBits - bits)) & ((1L << bits) - 1));
      bitsPerValue -= bits;
      remainingBits -= bits;
    }
    return r;
  }

  /**
   * If there are pending bits (at most 7), they will be ignored and the next
   * value will be read starting at the next byte.
   */
  public void skipToNextByte() {
    remainingBits = 0;
  }

}
