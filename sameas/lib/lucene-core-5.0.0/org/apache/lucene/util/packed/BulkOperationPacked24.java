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
final class BulkOperationPacked24 extends BulkOperationPacked {

  public BulkOperationPacked24() {
    super(24);
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (block0 >>> 40);
      values[valuesOffset++] = (int) ((block0 >>> 16) & 16777215L);
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block0 & 65535L) << 8) | (block1 >>> 56));
      values[valuesOffset++] = (int) ((block1 >>> 32) & 16777215L);
      values[valuesOffset++] = (int) ((block1 >>> 8) & 16777215L);
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block1 & 255L) << 16) | (block2 >>> 48));
      values[valuesOffset++] = (int) ((block2 >>> 24) & 16777215L);
      values[valuesOffset++] = (int) (block2 & 16777215L);
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final int byte0 = blocks[blocksOffset++] & 0xFF;
      final int byte1 = blocks[blocksOffset++] & 0xFF;
      final int byte2 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = (byte0 << 16) | (byte1 << 8) | byte2;
    }
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = block0 >>> 40;
      values[valuesOffset++] = (block0 >>> 16) & 16777215L;
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block0 & 65535L) << 8) | (block1 >>> 56);
      values[valuesOffset++] = (block1 >>> 32) & 16777215L;
      values[valuesOffset++] = (block1 >>> 8) & 16777215L;
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block1 & 255L) << 16) | (block2 >>> 48);
      values[valuesOffset++] = (block2 >>> 24) & 16777215L;
      values[valuesOffset++] = block2 & 16777215L;
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long byte0 = blocks[blocksOffset++] & 0xFF;
      final long byte1 = blocks[blocksOffset++] & 0xFF;
      final long byte2 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = (byte0 << 16) | (byte1 << 8) | byte2;
    }
  }

}
