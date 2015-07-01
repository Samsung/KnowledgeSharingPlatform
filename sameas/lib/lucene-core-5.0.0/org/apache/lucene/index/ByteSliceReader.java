package org.apache.lucene.index;

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
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.ByteBlockPool;

/* IndexInput that knows how to read the byte slices written
 * by Posting and PostingVector.  We read the bytes in
 * each slice until we hit the end of that slice at which
 * point we read the forwarding address of the next slice
 * and then jump to it.*/
final class ByteSliceReader extends DataInput {
  ByteBlockPool pool;
  int bufferUpto;
  byte[] buffer;
  public int upto;
  int limit;
  int level;
  public int bufferOffset;

  public int endIndex;

  public void init(ByteBlockPool pool, int startIndex, int endIndex) {

    assert endIndex-startIndex >= 0;
    assert startIndex >= 0;
    assert endIndex >= 0;

    this.pool = pool;
    this.endIndex = endIndex;

    level = 0;
    bufferUpto = startIndex / ByteBlockPool.BYTE_BLOCK_SIZE;
    bufferOffset = bufferUpto * ByteBlockPool.BYTE_BLOCK_SIZE;
    buffer = pool.buffers[bufferUpto];
    upto = startIndex & ByteBlockPool.BYTE_BLOCK_MASK;

    final int firstSize = ByteBlockPool.LEVEL_SIZE_ARRAY[0];

    if (startIndex+firstSize >= endIndex) {
      // There is only this one slice to read
      limit = endIndex & ByteBlockPool.BYTE_BLOCK_MASK;
    } else
      limit = upto+firstSize-4;
  }

  public boolean eof() {
    assert upto + bufferOffset <= endIndex;
    return upto + bufferOffset == endIndex;
  }

  @Override
  public byte readByte() {
    assert !eof();
    assert upto <= limit;
    if (upto == limit)
      nextSlice();
    return buffer[upto++];
  }

  public long writeTo(DataOutput out) throws IOException {
    long size = 0;
    while(true) {
      if (limit + bufferOffset == endIndex) {
        assert endIndex - bufferOffset >= upto;
        out.writeBytes(buffer, upto, limit-upto);
        size += limit-upto;
        break;
      } else {
        out.writeBytes(buffer, upto, limit-upto);
        size += limit-upto;
        nextSlice();
      }
    }

    return size;
  }

  public void nextSlice() {

    // Skip to our next slice
    final int nextIndex = ((buffer[limit]&0xff)<<24) + ((buffer[1+limit]&0xff)<<16) + ((buffer[2+limit]&0xff)<<8) + (buffer[3+limit]&0xff);

    level = ByteBlockPool.NEXT_LEVEL_ARRAY[level];
    final int newSize = ByteBlockPool.LEVEL_SIZE_ARRAY[level];

    bufferUpto = nextIndex / ByteBlockPool.BYTE_BLOCK_SIZE;
    bufferOffset = bufferUpto * ByteBlockPool.BYTE_BLOCK_SIZE;

    buffer = pool.buffers[bufferUpto];
    upto = nextIndex & ByteBlockPool.BYTE_BLOCK_MASK;

    if (nextIndex + newSize >= endIndex) {
      // We are advancing to the final slice
      assert endIndex - nextIndex > 0;
      limit = endIndex - bufferOffset;
    } else {
      // This is not the final slice (subtract 4 for the
      // forwarding address at the end of this new slice)
      limit = upto+newSize-4;
    }
  }

  @Override
  public void readBytes(byte[] b, int offset, int len) {
    while(len > 0) {
      final int numLeft = limit-upto;
      if (numLeft < len) {
        // Read entire slice
        System.arraycopy(buffer, upto, b, offset, numLeft);
        offset += numLeft;
        len -= numLeft;
        nextSlice();
      } else {
        // This slice is the last one
        System.arraycopy(buffer, upto, b, offset, len);
        upto += len;
        break;
      }
    }
  }
}