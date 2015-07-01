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
final class BulkOperationPacked3 extends BulkOperationPacked {

  public BulkOperationPacked3() {
    super(3);
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (block0 >>> 61);
      values[valuesOffset++] = (int) ((block0 >>> 58) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 55) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 52) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 49) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 46) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 43) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 40) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 37) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 34) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 31) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 28) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 25) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 22) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 19) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 16) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 13) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 10) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 7) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 4) & 7L);
      values[valuesOffset++] = (int) ((block0 >>> 1) & 7L);
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block0 & 1L) << 2) | (block1 >>> 62));
      values[valuesOffset++] = (int) ((block1 >>> 59) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 56) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 53) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 50) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 47) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 44) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 41) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 38) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 35) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 32) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 29) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 26) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 23) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 20) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 17) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 14) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 11) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 8) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 5) & 7L);
      values[valuesOffset++] = (int) ((block1 >>> 2) & 7L);
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block1 & 3L) << 1) | (block2 >>> 63));
      values[valuesOffset++] = (int) ((block2 >>> 60) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 57) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 54) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 51) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 48) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 45) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 42) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 39) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 36) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 33) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 30) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 27) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 24) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 21) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 18) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 15) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 12) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 9) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 6) & 7L);
      values[valuesOffset++] = (int) ((block2 >>> 3) & 7L);
      values[valuesOffset++] = (int) (block2 & 7L);
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final int byte0 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = byte0 >>> 5;
      values[valuesOffset++] = (byte0 >>> 2) & 7;
      final int byte1 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte0 & 3) << 1) | (byte1 >>> 7);
      values[valuesOffset++] = (byte1 >>> 4) & 7;
      values[valuesOffset++] = (byte1 >>> 1) & 7;
      final int byte2 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte1 & 1) << 2) | (byte2 >>> 6);
      values[valuesOffset++] = (byte2 >>> 3) & 7;
      values[valuesOffset++] = byte2 & 7;
    }
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = block0 >>> 61;
      values[valuesOffset++] = (block0 >>> 58) & 7L;
      values[valuesOffset++] = (block0 >>> 55) & 7L;
      values[valuesOffset++] = (block0 >>> 52) & 7L;
      values[valuesOffset++] = (block0 >>> 49) & 7L;
      values[valuesOffset++] = (block0 >>> 46) & 7L;
      values[valuesOffset++] = (block0 >>> 43) & 7L;
      values[valuesOffset++] = (block0 >>> 40) & 7L;
      values[valuesOffset++] = (block0 >>> 37) & 7L;
      values[valuesOffset++] = (block0 >>> 34) & 7L;
      values[valuesOffset++] = (block0 >>> 31) & 7L;
      values[valuesOffset++] = (block0 >>> 28) & 7L;
      values[valuesOffset++] = (block0 >>> 25) & 7L;
      values[valuesOffset++] = (block0 >>> 22) & 7L;
      values[valuesOffset++] = (block0 >>> 19) & 7L;
      values[valuesOffset++] = (block0 >>> 16) & 7L;
      values[valuesOffset++] = (block0 >>> 13) & 7L;
      values[valuesOffset++] = (block0 >>> 10) & 7L;
      values[valuesOffset++] = (block0 >>> 7) & 7L;
      values[valuesOffset++] = (block0 >>> 4) & 7L;
      values[valuesOffset++] = (block0 >>> 1) & 7L;
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block0 & 1L) << 2) | (block1 >>> 62);
      values[valuesOffset++] = (block1 >>> 59) & 7L;
      values[valuesOffset++] = (block1 >>> 56) & 7L;
      values[valuesOffset++] = (block1 >>> 53) & 7L;
      values[valuesOffset++] = (block1 >>> 50) & 7L;
      values[valuesOffset++] = (block1 >>> 47) & 7L;
      values[valuesOffset++] = (block1 >>> 44) & 7L;
      values[valuesOffset++] = (block1 >>> 41) & 7L;
      values[valuesOffset++] = (block1 >>> 38) & 7L;
      values[valuesOffset++] = (block1 >>> 35) & 7L;
      values[valuesOffset++] = (block1 >>> 32) & 7L;
      values[valuesOffset++] = (block1 >>> 29) & 7L;
      values[valuesOffset++] = (block1 >>> 26) & 7L;
      values[valuesOffset++] = (block1 >>> 23) & 7L;
      values[valuesOffset++] = (block1 >>> 20) & 7L;
      values[valuesOffset++] = (block1 >>> 17) & 7L;
      values[valuesOffset++] = (block1 >>> 14) & 7L;
      values[valuesOffset++] = (block1 >>> 11) & 7L;
      values[valuesOffset++] = (block1 >>> 8) & 7L;
      values[valuesOffset++] = (block1 >>> 5) & 7L;
      values[valuesOffset++] = (block1 >>> 2) & 7L;
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block1 & 3L) << 1) | (block2 >>> 63);
      values[valuesOffset++] = (block2 >>> 60) & 7L;
      values[valuesOffset++] = (block2 >>> 57) & 7L;
      values[valuesOffset++] = (block2 >>> 54) & 7L;
      values[valuesOffset++] = (block2 >>> 51) & 7L;
      values[valuesOffset++] = (block2 >>> 48) & 7L;
      values[valuesOffset++] = (block2 >>> 45) & 7L;
      values[valuesOffset++] = (block2 >>> 42) & 7L;
      values[valuesOffset++] = (block2 >>> 39) & 7L;
      values[valuesOffset++] = (block2 >>> 36) & 7L;
      values[valuesOffset++] = (block2 >>> 33) & 7L;
      values[valuesOffset++] = (block2 >>> 30) & 7L;
      values[valuesOffset++] = (block2 >>> 27) & 7L;
      values[valuesOffset++] = (block2 >>> 24) & 7L;
      values[valuesOffset++] = (block2 >>> 21) & 7L;
      values[valuesOffset++] = (block2 >>> 18) & 7L;
      values[valuesOffset++] = (block2 >>> 15) & 7L;
      values[valuesOffset++] = (block2 >>> 12) & 7L;
      values[valuesOffset++] = (block2 >>> 9) & 7L;
      values[valuesOffset++] = (block2 >>> 6) & 7L;
      values[valuesOffset++] = (block2 >>> 3) & 7L;
      values[valuesOffset++] = block2 & 7L;
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long byte0 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = byte0 >>> 5;
      values[valuesOffset++] = (byte0 >>> 2) & 7;
      final long byte1 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte0 & 3) << 1) | (byte1 >>> 7);
      values[valuesOffset++] = (byte1 >>> 4) & 7;
      values[valuesOffset++] = (byte1 >>> 1) & 7;
      final long byte2 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte1 & 1) << 2) | (byte2 >>> 6);
      values[valuesOffset++] = (byte2 >>> 3) & 7;
      values[valuesOffset++] = byte2 & 7;
    }
  }

}
