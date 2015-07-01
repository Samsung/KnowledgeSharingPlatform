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
final class BulkOperationPacked22 extends BulkOperationPacked {

  public BulkOperationPacked22() {
    super(22);
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (block0 >>> 42);
      values[valuesOffset++] = (int) ((block0 >>> 20) & 4194303L);
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block0 & 1048575L) << 2) | (block1 >>> 62));
      values[valuesOffset++] = (int) ((block1 >>> 40) & 4194303L);
      values[valuesOffset++] = (int) ((block1 >>> 18) & 4194303L);
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block1 & 262143L) << 4) | (block2 >>> 60));
      values[valuesOffset++] = (int) ((block2 >>> 38) & 4194303L);
      values[valuesOffset++] = (int) ((block2 >>> 16) & 4194303L);
      final long block3 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block2 & 65535L) << 6) | (block3 >>> 58));
      values[valuesOffset++] = (int) ((block3 >>> 36) & 4194303L);
      values[valuesOffset++] = (int) ((block3 >>> 14) & 4194303L);
      final long block4 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block3 & 16383L) << 8) | (block4 >>> 56));
      values[valuesOffset++] = (int) ((block4 >>> 34) & 4194303L);
      values[valuesOffset++] = (int) ((block4 >>> 12) & 4194303L);
      final long block5 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block4 & 4095L) << 10) | (block5 >>> 54));
      values[valuesOffset++] = (int) ((block5 >>> 32) & 4194303L);
      values[valuesOffset++] = (int) ((block5 >>> 10) & 4194303L);
      final long block6 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block5 & 1023L) << 12) | (block6 >>> 52));
      values[valuesOffset++] = (int) ((block6 >>> 30) & 4194303L);
      values[valuesOffset++] = (int) ((block6 >>> 8) & 4194303L);
      final long block7 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block6 & 255L) << 14) | (block7 >>> 50));
      values[valuesOffset++] = (int) ((block7 >>> 28) & 4194303L);
      values[valuesOffset++] = (int) ((block7 >>> 6) & 4194303L);
      final long block8 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block7 & 63L) << 16) | (block8 >>> 48));
      values[valuesOffset++] = (int) ((block8 >>> 26) & 4194303L);
      values[valuesOffset++] = (int) ((block8 >>> 4) & 4194303L);
      final long block9 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block8 & 15L) << 18) | (block9 >>> 46));
      values[valuesOffset++] = (int) ((block9 >>> 24) & 4194303L);
      values[valuesOffset++] = (int) ((block9 >>> 2) & 4194303L);
      final long block10 = blocks[blocksOffset++];
      values[valuesOffset++] = (int) (((block9 & 3L) << 20) | (block10 >>> 44));
      values[valuesOffset++] = (int) ((block10 >>> 22) & 4194303L);
      values[valuesOffset++] = (int) (block10 & 4194303L);
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, int[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final int byte0 = blocks[blocksOffset++] & 0xFF;
      final int byte1 = blocks[blocksOffset++] & 0xFF;
      final int byte2 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = (byte0 << 14) | (byte1 << 6) | (byte2 >>> 2);
      final int byte3 = blocks[blocksOffset++] & 0xFF;
      final int byte4 = blocks[blocksOffset++] & 0xFF;
      final int byte5 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte2 & 3) << 20) | (byte3 << 12) | (byte4 << 4) | (byte5 >>> 4);
      final int byte6 = blocks[blocksOffset++] & 0xFF;
      final int byte7 = blocks[blocksOffset++] & 0xFF;
      final int byte8 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte5 & 15) << 18) | (byte6 << 10) | (byte7 << 2) | (byte8 >>> 6);
      final int byte9 = blocks[blocksOffset++] & 0xFF;
      final int byte10 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte8 & 63) << 16) | (byte9 << 8) | byte10;
    }
  }

  @Override
  public void decode(long[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long block0 = blocks[blocksOffset++];
      values[valuesOffset++] = block0 >>> 42;
      values[valuesOffset++] = (block0 >>> 20) & 4194303L;
      final long block1 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block0 & 1048575L) << 2) | (block1 >>> 62);
      values[valuesOffset++] = (block1 >>> 40) & 4194303L;
      values[valuesOffset++] = (block1 >>> 18) & 4194303L;
      final long block2 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block1 & 262143L) << 4) | (block2 >>> 60);
      values[valuesOffset++] = (block2 >>> 38) & 4194303L;
      values[valuesOffset++] = (block2 >>> 16) & 4194303L;
      final long block3 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block2 & 65535L) << 6) | (block3 >>> 58);
      values[valuesOffset++] = (block3 >>> 36) & 4194303L;
      values[valuesOffset++] = (block3 >>> 14) & 4194303L;
      final long block4 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block3 & 16383L) << 8) | (block4 >>> 56);
      values[valuesOffset++] = (block4 >>> 34) & 4194303L;
      values[valuesOffset++] = (block4 >>> 12) & 4194303L;
      final long block5 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block4 & 4095L) << 10) | (block5 >>> 54);
      values[valuesOffset++] = (block5 >>> 32) & 4194303L;
      values[valuesOffset++] = (block5 >>> 10) & 4194303L;
      final long block6 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block5 & 1023L) << 12) | (block6 >>> 52);
      values[valuesOffset++] = (block6 >>> 30) & 4194303L;
      values[valuesOffset++] = (block6 >>> 8) & 4194303L;
      final long block7 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block6 & 255L) << 14) | (block7 >>> 50);
      values[valuesOffset++] = (block7 >>> 28) & 4194303L;
      values[valuesOffset++] = (block7 >>> 6) & 4194303L;
      final long block8 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block7 & 63L) << 16) | (block8 >>> 48);
      values[valuesOffset++] = (block8 >>> 26) & 4194303L;
      values[valuesOffset++] = (block8 >>> 4) & 4194303L;
      final long block9 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block8 & 15L) << 18) | (block9 >>> 46);
      values[valuesOffset++] = (block9 >>> 24) & 4194303L;
      values[valuesOffset++] = (block9 >>> 2) & 4194303L;
      final long block10 = blocks[blocksOffset++];
      values[valuesOffset++] = ((block9 & 3L) << 20) | (block10 >>> 44);
      values[valuesOffset++] = (block10 >>> 22) & 4194303L;
      values[valuesOffset++] = block10 & 4194303L;
    }
  }

  @Override
  public void decode(byte[] blocks, int blocksOffset, long[] values, int valuesOffset, int iterations) {
    for (int i = 0; i < iterations; ++i) {
      final long byte0 = blocks[blocksOffset++] & 0xFF;
      final long byte1 = blocks[blocksOffset++] & 0xFF;
      final long byte2 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = (byte0 << 14) | (byte1 << 6) | (byte2 >>> 2);
      final long byte3 = blocks[blocksOffset++] & 0xFF;
      final long byte4 = blocks[blocksOffset++] & 0xFF;
      final long byte5 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte2 & 3) << 20) | (byte3 << 12) | (byte4 << 4) | (byte5 >>> 4);
      final long byte6 = blocks[blocksOffset++] & 0xFF;
      final long byte7 = blocks[blocksOffset++] & 0xFF;
      final long byte8 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte5 & 15) << 18) | (byte6 << 10) | (byte7 << 2) | (byte8 >>> 6);
      final long byte9 = blocks[blocksOffset++] & 0xFF;
      final long byte10 = blocks[blocksOffset++] & 0xFF;
      values[valuesOffset++] = ((byte8 & 63) << 16) | (byte9 << 8) | byte10;
    }
  }

}
