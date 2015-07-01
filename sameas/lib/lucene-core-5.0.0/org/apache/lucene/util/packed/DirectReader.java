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

import java.io.IOException;

import org.apache.lucene.store.RandomAccessInput;
import org.apache.lucene.util.LongValues;

/** 
 * Retrieves an instance previously written by {@link DirectWriter} 
 * <p>
 * Example usage:
 * <pre class="prettyprint">
 *   int bitsPerValue = 100;
 *   IndexInput in = dir.openInput("packed", IOContext.DEFAULT);
 *   LongValues values = DirectReader.getInstance(in.randomAccessSlice(start, end), bitsPerValue);
 *   for (int i = 0; i &lt; numValues; i++) {
 *     long value = values.get(i);
 *   }
 * </pre>
 * @see DirectWriter
 */
public class DirectReader {
  
  /** 
   * Retrieves an instance from the specified slice written decoding
   * {@code bitsPerValue} for each value 
   */
  public static LongValues getInstance(RandomAccessInput slice, int bitsPerValue) {
    switch (bitsPerValue) {
      case 1: return new DirectPackedReader1(slice);
      case 2: return new DirectPackedReader2(slice);
      case 4: return new DirectPackedReader4(slice);
      case 8: return new DirectPackedReader8(slice);
      case 12: return new DirectPackedReader12(slice);
      case 16: return new DirectPackedReader16(slice);
      case 20: return new DirectPackedReader20(slice);
      case 24: return new DirectPackedReader24(slice);
      case 28: return new DirectPackedReader28(slice);
      case 32: return new DirectPackedReader32(slice);
      case 40: return new DirectPackedReader40(slice);
      case 48: return new DirectPackedReader48(slice);
      case 56: return new DirectPackedReader56(slice);
      case 64: return new DirectPackedReader64(slice);
      default: throw new IllegalArgumentException("unsupported bitsPerValue: " + bitsPerValue);
    }
  }
  
  static final class DirectPackedReader1 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader1(RandomAccessInput in) {
      this.in = in;
    }

    @Override
    public long get(long index) {
      try {
        int shift = 7 - (int) (index & 7);
        return (in.readByte(index >>> 3) >>> shift) & 0x1;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader2 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader2(RandomAccessInput in) {
      this.in = in;
    }

    @Override
    public long get(long index) {
      try {
        int shift = (3 - (int)(index & 3)) << 1;
        return (in.readByte(index >>> 2) >>> shift) & 0x3;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader4 extends LongValues {
    final RandomAccessInput in;

    DirectPackedReader4(RandomAccessInput in) {
      this.in = in;
    }

    @Override
    public long get(long index) {
      try {
        int shift = (int) ((index + 1) & 1) << 2;
        return (in.readByte(index >>> 1) >>> shift) & 0xF;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
    
  static final class DirectPackedReader8 extends LongValues {
    final RandomAccessInput in;

    DirectPackedReader8(RandomAccessInput in) {
      this.in = in;
    }

    @Override
    public long get(long index) {
      try {
        return in.readByte(index) & 0xFF;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader12 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader12(RandomAccessInput in) {
      this.in = in;
    }

    @Override
    public long get(long index) {
      try {
        long offset = (index * 12) >>> 3;
        int shift = (int) ((index + 1) & 1) << 2;
        return (in.readShort(offset) >>> shift) & 0xFFF;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader16 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader16(RandomAccessInput in) {
      this.in = in;
    }

    @Override
    public long get(long index) {
      try {
        return in.readShort(index << 1) & 0xFFFF;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  static final class DirectPackedReader20 extends LongValues {
    final RandomAccessInput in;

    DirectPackedReader20(RandomAccessInput in) {
      this.in = in;
    }

    @Override
    public long get(long index) {
      try {
        long offset = (index * 20) >>> 3;
        // TODO: clean this up...
        int v = in.readInt(offset) >>> 8;
        int shift = (int) ((index + 1) & 1) << 2;
        return (v >>> shift) & 0xFFFFF;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  static final class DirectPackedReader24 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader24(RandomAccessInput in) {
      this.in = in;
    }

    @Override
    public long get(long index) {
      try {
        return in.readInt(index * 3) >>> 8;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  static final class DirectPackedReader28 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader28(RandomAccessInput in) {
      this.in = in;
    }
    
    @Override
    public long get(long index) {
      try {
        long offset = (index * 28) >>> 3;
        int shift = (int) ((index + 1) & 1) << 2;
        return (in.readInt(offset) >>> shift) & 0xFFFFFFFL;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader32 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader32(RandomAccessInput in) {
      this.in = in;
    }
    
    @Override
    public long get(long index) {
      try {
        return in.readInt(index << 2) & 0xFFFFFFFFL;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader40 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader40(RandomAccessInput in) {
      this.in = in;
    }
    
    @Override
    public long get(long index) {
      try {
        return in.readLong(index * 5) >>> 24;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader48 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader48(RandomAccessInput in) {
      this.in = in;
    }
    
    @Override
    public long get(long index) {
      try {
        return in.readLong(index * 6) >>> 16;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader56 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader56(RandomAccessInput in) {
      this.in = in;
    }
    
    @Override
    public long get(long index) {
      try {
        return in.readLong(index * 7) >>> 8;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
  
  static final class DirectPackedReader64 extends LongValues {
    final RandomAccessInput in;
    
    DirectPackedReader64(RandomAccessInput in) {
      this.in = in;
    }
    
    @Override
    public long get(long index) {
      try {
        return in.readLong(index << 3);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }    
  }
}
