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

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.store.DataOutput;

abstract class AbstractBlockPackedWriter {

  static final int MIN_BLOCK_SIZE = 64;
  static final int MAX_BLOCK_SIZE = 1 << (30 - 3);
  static final int MIN_VALUE_EQUALS_0 = 1 << 0;
  static final int BPV_SHIFT = 1;

  // same as DataOutput.writeVLong but accepts negative values
  static void writeVLong(DataOutput out, long i) throws IOException {
    int k = 0;
    while ((i & ~0x7FL) != 0L && k++ < 8) {
      out.writeByte((byte)((i & 0x7FL) | 0x80L));
      i >>>= 7;
    }
    out.writeByte((byte) i);
  }

  protected DataOutput out;
  protected final long[] values;
  protected byte[] blocks;
  protected int off;
  protected long ord;
  protected boolean finished;

  /**
   * Sole constructor.
   * @param blockSize the number of values of a single block, must be a multiple of <tt>64</tt>
   */
  public AbstractBlockPackedWriter(DataOutput out, int blockSize) {
    checkBlockSize(blockSize, MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);
    reset(out);
    values = new long[blockSize];
  }

  /** Reset this writer to wrap <code>out</code>. The block size remains unchanged. */
  public void reset(DataOutput out) {
    assert out != null;
    this.out = out;
    off = 0;
    ord = 0L;
    finished = false;
  }

  private void checkNotFinished() {
    if (finished) {
      throw new IllegalStateException("Already finished");
    }
  }

  /** Append a new long. */
  public void add(long l) throws IOException {
    checkNotFinished();
    if (off == values.length) {
      flush();
    }
    values[off++] = l;
    ++ord;
  }

  // For testing only
  void addBlockOfZeros() throws IOException {
    checkNotFinished();
    if (off != 0 && off != values.length) {
      throw new IllegalStateException("" + off);
    }
    if (off == values.length) {
      flush();
    }
    Arrays.fill(values, 0);
    off = values.length;
    ord += values.length;
  }

  /** Flush all buffered data to disk. This instance is not usable anymore
   *  after this method has been called until {@link #reset(DataOutput)} has
   *  been called. */
  public void finish() throws IOException {
    checkNotFinished();
    if (off > 0) {
      flush();
    }
    finished = true;
  }

  /** Return the number of values which have been added. */
  public long ord() {
    return ord;
  }

  protected abstract void flush() throws IOException;

  protected final void writeValues(int bitsRequired) throws IOException {
    final PackedInts.Encoder encoder = PackedInts.getEncoder(PackedInts.Format.PACKED, PackedInts.VERSION_CURRENT, bitsRequired);
    final int iterations = values.length / encoder.byteValueCount();
    final int blockSize = encoder.byteBlockCount() * iterations;
    if (blocks == null || blocks.length < blockSize) {
      blocks = new byte[blockSize];
    }
    if (off < values.length) {
      Arrays.fill(values, off, values.length, 0L);
    }
    encoder.encode(values, 0, blocks, 0, iterations);
    final int blockCount = (int) PackedInts.Format.PACKED.byteCount(PackedInts.VERSION_CURRENT, off, bitsRequired);
    out.writeBytes(blocks, blockCount);
  }

}
