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

import org.apache.lucene.store.IndexInput;

import java.io.IOException;

/* Reads directly from disk on each get */
// just for back compat, use DirectReader/DirectWriter for more efficient impl
class DirectPackedReader extends PackedInts.ReaderImpl {
  final IndexInput in;
  final int bitsPerValue;
  final long startPointer;
  final long valueMask;

  DirectPackedReader(int bitsPerValue, int valueCount, IndexInput in) {
    super(valueCount);
    this.in = in;
    this.bitsPerValue = bitsPerValue;

    startPointer = in.getFilePointer();
    if (bitsPerValue == 64) {
      valueMask = -1L;
    } else {
      valueMask = (1L << bitsPerValue) - 1;
    }
  }

  @Override
  public long get(int index) {
    final long majorBitPos = (long)index * bitsPerValue;
    final long elementPos = majorBitPos >>> 3;
    try {
      in.seek(startPointer + elementPos);

      final int bitPos = (int) (majorBitPos & 7);
      // round up bits to a multiple of 8 to find total bytes needed to read
      final int roundedBits = ((bitPos + bitsPerValue + 7) & ~7);
      // the number of extra bits read at the end to shift out
      int shiftRightBits = roundedBits - bitPos - bitsPerValue;

      long rawValue;
      switch (roundedBits >>> 3) {
        case 1:
          rawValue = in.readByte();
          break;
        case 2:
          rawValue = in.readShort();
          break;
        case 3:
          rawValue = ((long)in.readShort() << 8) | (in.readByte() & 0xFFL);
          break;
        case 4:
          rawValue = in.readInt();
          break;
        case 5:
          rawValue = ((long)in.readInt() << 8) | (in.readByte() & 0xFFL);
          break;
        case 6:
          rawValue = ((long)in.readInt() << 16) | (in.readShort() & 0xFFFFL);
          break;
        case 7:
          rawValue = ((long)in.readInt() << 24) | ((in.readShort() & 0xFFFFL) << 8) | (in.readByte() & 0xFFL);
          break;
        case 8:
          rawValue = in.readLong();
          break;
        case 9:
          // We must be very careful not to shift out relevant bits. So we account for right shift
          // we would normally do on return here, and reset it.
          rawValue = (in.readLong() << (8 - shiftRightBits)) | ((in.readByte() & 0xFFL) >>> shiftRightBits);
          shiftRightBits = 0;
          break;
        default:
          throw new AssertionError("bitsPerValue too large: " + bitsPerValue);
      }
      return (rawValue >>> shiftRightBits) & valueMask;

    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public long ramBytesUsed() {
    return 0;
  }
}
