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
final class BulkOperationPacked14 extends BulkOperationPacked {

  public BulkOperationPacked14() {
    super(14);
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (block0 >>> 50);
      values[valuesOffset++] = (int) ((block0 >>> 36) & 16383L);
      values[valuesOffset++] = (int) ((block0 >>> 22) & 16383L);
      values[valuesOffset++] = (int) ((block0 >>> 8) & 16383L);
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block0 & 255L) << 6) | (block1 >>> 58));
      values[valuesOffset++] = (int) ((block1 >>> 44) & 16383L);
      values[valuesOffset++] = (int) ((block1 >>> 30) & 16383L);
      values[valuesOffset++] = (int) ((block1 >>> 16) & 16383L);
      values[valuesOffset++] = (int) ((block1 >>> 2) & 16383L);
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block1 & 3L) << 12) | (block2 >>> 52));
      values[valuesOffset++] = (int) ((block2 >>> 38) & 16383L);
      values[valuesOffset++] = (int) ((block2 >>> 24) & 16383L);
      values[valuesOffset++] = (int) ((block2 >>> 10) & 16383L);
      final long block3 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block2 & 1023L) << 4) | (block3 >>> 60));
      values[valuesOffset++] = (int) ((block3 >>> 46) & 16383L);
      values[valuesOffset++] = (int) ((block3 >>> 32) & 16383L);
      values[valuesOffset++] = (int) ((block3 >>> 18) & 16383L);
      values[valuesOffset++] = (int) ((block3 >>> 4) & 16383L);
      final long block4 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block3 & 15L) << 10) | (block4 >>> 54));
      values[valuesOffset++] = (int) ((block4 >>> 40) & 16383L);
      values[valuesOffset++] = (int) ((block4 >>> 26) & 16383L);
      values[valuesOffset++] = (int) ((block4 >>> 12) & 16383L);
      final long block5 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block4 & 4095L) << 2) | (block5 >>> 62));
      values[valuesOffset++] = (int) ((block5 >>> 48) & 16383L);
      values[valuesOffset++] = (int) ((block5 >>> 34) & 16383L);
      values[valuesOffset++] = (int) ((block5 >>> 20) & 16383L);
      values[valuesOffset++] = (int) ((block5 >>> 6) & 16383L);
      final long block6 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block5 & 63L) << 8) | (block6 >>> 56));
      values[valuesOffset++] = (int) ((block6 >>> 42) & 16383L);
      values[valuesOffset++] = (int) ((block6 >>> 28) & 16383L);
      values[valuesOffset++] = (int) ((block6 >>> 14) & 16383L);
      values[valuesOffset++] = (int) (block6 & 16383L);
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final int byte0 = blocks[blocksOffset++] & 0xFF;
      final int byte1 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = (byte0 << 6) | (byte1 >>> 2);
      final int byte2 = blocks[blocksOffset++] & 0xFF;
      final int byte3 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte1 & 3) << 12) | (byte2 << 4) | (byte3 >>> 4);
      final int byte4 = blocks[blocksOffset++] & 0xFF;
      final int byte5 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte3 & 15) << 10) | (byte4 << 2) | (byte5 >>> 6);
      final int byte6 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte5 & 63) << 8) | byte6;
    }
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = block0 >>> 50;
      values[valuesOffset++] = (block0 >>> 36) & 16383L;
      values[valuesOffset++] = (block0 >>> 22) & 16383L;
      values[valuesOffset++] = (block0 >>> 8) & 16383L;
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block0 & 255L) << 6) | (block1 >>> 58);
      values[valuesOffset++] = (block1 >>> 44) & 16383L;
      values[valuesOffset++] = (block1 >>> 30) & 16383L;
      values[valuesOffset++] = (block1 >>> 16) & 16383L;
      values[valuesOffset++] = (block1 >>> 2) & 16383L;
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block1 & 3L) << 12) | (block2 >>> 52);
      values[valuesOffset++] = (block2 >>> 38) & 16383L;
      values[valuesOffset++] = (block2 >>> 24) & 16383L;
      values[valuesOffset++] = (block2 >>> 10) & 16383L;
      final long block3 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block2 & 1023L) << 4) | (block3 >>> 60);
      values[valuesOffset++] = (block3 >>> 46) & 16383L;
      values[valuesOffset++] = (block3 >>> 32) & 16383L;
      values[valuesOffset++] = (block3 >>> 18) & 16383L;
      values[valuesOffset++] = (block3 >>> 4) & 16383L;
      final long block4 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block3 & 15L) << 10) | (block4 >>> 54);
      values[valuesOffset++] = (block4 >>> 40) & 16383L;
      values[valuesOffset++] = (block4 >>> 26) & 16383L;
      values[valuesOffset++] = (block4 >>> 12) & 16383L;
      final long block5 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block4 & 4095L) << 2) | (block5 >>> 62);
      values[valuesOffset++] = (block5 >>> 48) & 16383L;
      values[valuesOffset++] = (block5 >>> 34) & 16383L;
      values[valuesOffset++] = (block5 >>> 20) & 16383L;
      values[valuesOffset++] = (block5 >>> 6) & 16383L;
      final long block6 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block5 & 63L) << 8) | (block6 >>> 56);
      values[valuesOffset++] = (block6 >>> 42) & 16383L;
      values[valuesOffset++] = (block6 >>> 28) & 16383L;
      values[valuesOffset++] = (block6 >>> 14) & 16383L;
      values[valuesOffset++] = block6 & 16383L;
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long byte0 = blocks[blocksOffset++] & 0xFF;
      final long byte1 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = (byte0 << 6) | (byte1 >>> 2);
      final long byte2 = blocks[blocksOffset++] & 0xFF;
      final long byte3 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte1 & 3) << 12) | (byte2 << 4) | (byte3 >>> 4);
      final long byte4 = blocks[blocksOffset++] & 0xFF;
      final long byte5 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte3 & 15) << 10) | (byte4 << 2) | (byte5 >>> 6);
      final long byte6 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte5 & 63) << 8) | byte6;
    }
  }

}
