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

/**
 * Non-specialized {@link BulkOperation} for {@link PackedInts.Format#PACKED_SINGLE_BLOCK}.
 */
final class BulkOperationPackedSingleBlock extends BulkOperation {

  private static final int BLOCK_COUNT = 1;

  private final int bitsPerValue;
  private final int valueCount;
  private final long mask;

  public BulkOperationPackedSingleBlock(int bitsPerValue) {
    this.bitsPerValue = bitsPerValue;
    this.valueCount = 64 / bitsPerValue;
    this.mask = (1L << bitsPerValue) - 1;
  }

  @Override
  public final int longBlockCount() {
    return BLOCK_COUNT;
  }

  @Override
  public final int byteBlockCount() {
    return BLOCK_COUNT * 8;
  }

  @Override
  public int longValueCount() {
    return valueCount;
  }

  @Override
  public final int byteValueCount() {
    return valueCount;
  }

  private static long readLong(byte[] blocks, int blocksOffset) {
    return (blocks[blocksOffset++] & 0xFFL) << 56
        | (blocks[blocksOffset++] & 0xFFL) << 48
        | (blocks[blocksOffset++] & 0xFFL) << 40
        | (blocks[blocksOffset++] & 0xFFL) << 32
        | (blocks[blocksOffset++] & 0xFFL) << 24
        | (blocks[blocksOffset++] & 0xFFL) << 16
        | (blocks[blocksOffset++] & 0xFFL) << 8
        | blocks[blocksOffset++] & 0xFFL;
  }

  private int decode(long block, long[] values, int valuesOffset) {
    values[valuesOffset++] = block & mask;
    for (int j = 1; j < valueCount; ++j) {
      block >>>= bitsPerValue;
      values[valuesOffset++] = block & mask;
    }
    return valuesOffset;
  }

  private int decode(long block, int[] values, int valuesOffset) {
    values[valuesOffset++] = (int) (block & mask);
    for (int j = 1; j < valueCount; ++j) {
      block >>>= bitsPerValue;
      values[valuesOffset++] = (int) (block & mask);
    }
    return valuesOffset;
  }

  private long encode(long[] values, int valuesOffset) {
    long block = values[valuesOffset++];
    for (int j = 1; j < valueCount; ++j) {
      block |= values[valuesOffset++] << (j * bitsPerValue);
    }
    return block;
  }

  private long encode(int[] values, int valuesOffset) {
    long block = values[valuesOffset++] & 0xFFFFFFFFL;
    for (int j = 1; j < valueCount; ++j) {
      block |= (values[valuesOffset++] & 0xFFFFFFFFL) << (j * bitsPerValue);
    }
    return block;
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, long[] values,
      int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block = blocks[blocksOffset++];
      valuesOffset = decode(block, values, valuesOffset);
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, long[] values,
      int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block = readLong(blocks, blocksOffset);
      blocksOffset += 8;
      valuesOffset = decode(block, values, valuesOffset);
    }
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, int[] values,
      int valuesOffset, int iterations) {
    if (bitsPerValue > 32) {
      throw new UnsupportedOperationException("Cannot decode " + bitsPerValue + "-bits values into an int[]");
    }
    for (int i = 0; i < iterations; ++i) {
      final long block = blocks[blocksOffset++];
      valuesOffset = decode(block, values, valuesOffset);
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, int[] values,
      int valuesOffset, int iterations) {
    if (bitsPerValue > 32) {
      throw new UnsupportedOperationException("Cannot decode " + bitsPerValue + "-bits values into an int[]");
    }
    for (int i = 0; i < iterations; ++i) {
      final long block = readLong(blocks, blocksOffset);
      blocksOffset += 8;
      valuesOffset = decode(block, values, valuesOffset);
    }
  }

  @Override
  public void encode(long[] values, int valuesOffset, long[] blocks,
      int blocksOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      blocks[blocksOffset++] = encode(values, valuesOffset);
      valuesOffset += valueCount;
    }
  }

  @Override
  public void encode(int[] values, int valuesOffset, long[] blocks,
      int blocksOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      blocks[blocksOffset++] = encode(values, valuesOffset);
      valuesOffset += valueCount;
    }
  }

  @Override
  public void encode(long[] values, int valuesOffset, byte[] blocks, int blocksOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block = encode(values, valuesOffset);
      valuesOffset += valueCount;
      blocksOffset = writeLong(block, blocks, blocksOffset);
    }
  }

  @Override
  public void encode(int[] values, int valuesOffset, byte[] blocks,
      int blocksOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block = encode(values, valuesOffset);
      valuesOffset += valueCount;
      blocksOffset = writeLong(block, blocks, blocksOffset);
    }
  }

}
