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

/**
 * Efficient sequential read/write of packed integers.
 */
final class BulkOperationPacked10 extends BulkOperationPacked {

  public BulkOperationPacked10() {
    super(10);
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (block0 >>> 54);
      values[valuesOffset++] = (int) ((block0 >>> 44) & 1023L);
      values[valuesOffset++] = (int) ((block0 >>> 34) & 1023L);
      values[valuesOffset++] = (int) ((block0 >>> 24) & 1023L);
      values[valuesOffset++] = (int) ((block0 >>> 14) & 1023L);
      values[valuesOffset++] = (int) ((block0 >>> 4) & 1023L);
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block0 & 15L) << 6) | (block1 >>> 58));
      values[valuesOffset++] = (int) ((block1 >>> 48) & 1023L);
      values[valuesOffset++] = (int) ((block1 >>> 38) & 1023L);
      values[valuesOffset++] = (int) ((block1 >>> 28) & 1023L);
      values[valuesOffset++] = (int) ((block1 >>> 18) & 1023L);
      values[valuesOffset++] = (int) ((block1 >>> 8) & 1023L);
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block1 & 255L) << 2) | (block2 >>> 62));
      values[valuesOffset++] = (int) ((block2 >>> 52) & 1023L);
      values[valuesOffset++] = (int) ((block2 >>> 42) & 1023L);
      values[valuesOffset++] = (int) ((block2 >>> 32) & 1023L);
      values[valuesOffset++] = (int) ((block2 >>> 22) & 1023L);
      values[valuesOffset++] = (int) ((block2 >>> 12) & 1023L);
      values[valuesOffset++] = (int) ((block2 >>> 2) & 1023L);
      final long block3 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block2 & 3L) << 8) | (block3 >>> 56));
      values[valuesOffset++] = (int) ((block3 >>> 46) & 1023L);
      values[valuesOffset++] = (int) ((block3 >>> 36) & 1023L);
      values[valuesOffset++] = (int) ((block3 >>> 26) & 1023L);
      values[valuesOffset++] = (int) ((block3 >>> 16) & 1023L);
      values[valuesOffset++] = (int) ((block3 >>> 6) & 1023L);
      final long block4 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block3 & 63L) << 4) | (block4 >>> 60));
      values[valuesOffset++] = (int) ((block4 >>> 50) & 1023L);
      values[valuesOffset++] = (int) ((block4 >>> 40) & 1023L);
      values[valuesOffset++] = (int) ((block4 >>> 30) & 1023L);
      values[valuesOffset++] = (int) ((block4 >>> 20) & 1023L);
      values[valuesOffset++] = (int) ((block4 >>> 10) & 1023L);
      values[valuesOffset++] = (int) (block4 & 1023L);
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final int byte0 = blocks[blocksOffset++] & 0xFF;
      final int byte1 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = (byte0 << 2) | (byte1 >>> 6);
      final int byte2 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte1 & 63) << 4) | (byte2 >>> 4);
      final int byte3 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte2 & 15) << 6) | (byte3 >>> 2);
      final int byte4 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte3 & 3) << 8) | byte4;
    }
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = block0 >>> 54;
      values[valuesOffset++] = (block0 >>> 44) & 1023L;
      values[valuesOffset++] = (block0 >>> 34) & 1023L;
      values[valuesOffset++] = (block0 >>> 24) & 1023L;
      values[valuesOffset++] = (block0 >>> 14) & 1023L;
      values[valuesOffset++] = (block0 >>> 4) & 1023L;
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block0 & 15L) << 6) | (block1 >>> 58);
      values[valuesOffset++] = (block1 >>> 48) & 1023L;
      values[valuesOffset++] = (block1 >>> 38) & 1023L;
      values[valuesOffset++] = (block1 >>> 28) & 1023L;
      values[valuesOffset++] = (block1 >>> 18) & 1023L;
      values[valuesOffset++] = (block1 >>> 8) & 1023L;
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block1 & 255L) << 2) | (block2 >>> 62);
      values[valuesOffset++] = (block2 >>> 52) & 1023L;
      values[valuesOffset++] = (block2 >>> 42) & 1023L;
      values[valuesOffset++] = (block2 >>> 32) & 1023L;
      values[valuesOffset++] = (block2 >>> 22) & 1023L;
      values[valuesOffset++] = (block2 >>> 12) & 1023L;
      values[valuesOffset++] = (block2 >>> 2) & 1023L;
      final long block3 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block2 & 3L) << 8) | (block3 >>> 56);
      values[valuesOffset++] = (block3 >>> 46) & 1023L;
      values[valuesOffset++] = (block3 >>> 36) & 1023L;
      values[valuesOffset++] = (block3 >>> 26) & 1023L;
      values[valuesOffset++] = (block3 >>> 16) & 1023L;
      values[valuesOffset++] = (block3 >>> 6) & 1023L;
      final long block4 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block3 & 63L) << 4) | (block4 >>> 60);
      values[valuesOffset++] = (block4 >>> 50) & 1023L;
      values[valuesOffset++] = (block4 >>> 40) & 1023L;
      values[valuesOffset++] = (block4 >>> 30) & 1023L;
      values[valuesOffset++] = (block4 >>> 20) & 1023L;
      values[valuesOffset++] = (block4 >>> 10) & 1023L;
      values[valuesOffset++] = block4 & 1023L;
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long byte0 = blocks[blocksOffset++] & 0xFF;
      final long byte1 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = (byte0 << 2) | (byte1 >>> 6);
      final long byte2 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte1 & 63) << 4) | (byte2 >>> 4);
      final long byte3 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte2 & 15) << 6) | (byte3 >>> 2);
      final long byte4 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte3 & 3) << 8) | byte4;
    }
  }

}
