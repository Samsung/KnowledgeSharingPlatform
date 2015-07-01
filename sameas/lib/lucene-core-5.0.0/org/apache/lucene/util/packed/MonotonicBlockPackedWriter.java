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

import static org.apache.lucene.util.BitUtil.zigZagEncode;
import static org.apache.lucene.util.packed.MonotonicBlockPackedReader.expected;

import java.io.IOException;

import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.BitUtil;

/**
 * A writer for large monotonically increasing sequences of positive longs.
 * <p>
 * The sequence is divided into fixed-size blocks and for each block, values
 * are modeled after a linear function f: x &rarr; A &times; x + B. The block
 * encodes deltas from the expected values computed from this function using as
 * few bits as possible.
 * <p>
 * Format:
 * <ul>
 * <li>&lt;BLock&gt;<sup>BlockCount</sup>
 * <li>BlockCount: &lceil; ValueCount / BlockSize &rceil;
 * <li>Block: &lt;Header, (Ints)&gt;
 * <li>Header: &lt;B, A, BitsPerValue&gt;
 * <li>B: the B from f: x &rarr; A &times; x + B using a
 *     {@link BitUtil#zigZagEncode(long) zig-zag encoded}
 *     {@link DataOutput#writeVLong(long) vLong}
 * <li>A: the A from f: x &rarr; A &times; x + B encoded using
 *     {@link Float#floatToIntBits(float)} on
 *     {@link DataOutput#writeInt(int) 4 bytes}
 * <li>BitsPerValue: a {@link DataOutput#writeVInt(int) variable-length int}
 * <li>Ints: if BitsPerValue is <tt>0</tt>, then there is nothing to read and
 *     all values perfectly match the result of the function. Otherwise, these
 *     are the {@link PackedInts packed} deltas from the expected value
 *     (computed from the function) using exaclty BitsPerValue bits per value.
 * </ul>
 * @see MonotonicBlockPackedReader
 * @lucene.internal
 */
public final class MonotonicBlockPackedWriter extends AbstractBlockPackedWriter {

  /**
   * Sole constructor.
   * @param blockSize the number of values of a single block, must be a power of 2
   */
  public MonotonicBlockPackedWriter(DataOutput out, int blockSize) {
    super(out, blockSize);
  }

  @Override
  public void add(long l) throws IOException {
    assert l >= 0;
    super.add(l);
  }

  protected void flush() throws IOException {
    assert off > 0;

    final float avg = off == 1 ? 0f : (float) (values[off - 1] - values[0]) / (off - 1);
    long min = values[0];
    // adjust min so that all deltas will be positive
    for (int i = 1; i < off; ++i) {
      final long actual = values[i];
      final long expected = expected(min, avg, i);
      if (expected > actual) {
        min -= (expected - actual);
      }
    }

    long maxDelta = 0;
    for (int i = 0; i < off; ++i) {
      values[i] = values[i] - expected(min, avg, i);
      maxDelta = Math.max(maxDelta, values[i]);
    }

    out.writeZLong(min);
    out.writeInt(Float.floatToIntBits(avg));
    if (maxDelta == 0) {
      out.writeVInt(0);
    } else {
      final int bitsRequired = PackedInts.bitsRequired(maxDelta);
      out.writeVInt(bitsRequired);
      writeValues(bitsRequired);
    }

    off = 0;
  }

}
