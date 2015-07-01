package org.apache.lucene.store;

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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.BitUtil;

/**
 * Abstract base class for performing read operations of Lucene's low-level
 * data types.
 *
 * <p>{@code DataInput} may only be used from one thread, because it is not
 * thread safe (it keeps internal state like file position). To allow
 * multithreaded use, every {@code DataInput} instance must be cloned before
 * used in another thread. Subclasses must therefore implement {@link #clone()},
 * returning a new {@code DataInput} which operates on the same underlying
 * resource, but positioned independently.
 */
public abstract class DataInput implements Cloneable {

  private static final int SKIP_BUFFER_SIZE = 1024;

  /* This buffer is used to skip over bytes with the default implementation of
   * skipBytes. The reason why we need to use an instance member instead of
   * sharing a single instance across threads is that some delegating
   * implementations of DataInput might want to reuse the provided buffer in
   * order to eg. update the checksum. If we shared the same buffer across
   * threads, then another thread might update the buffer while the checksum is
   * being computed, making it invalid. See LUCENE-5583 for more information.
   */
  private byte[] skipBuffer;

  /** Reads and returns a single byte.
   * @see DataOutput#writeByte(byte)
   */
  public abstract byte readByte() throws IOException;

  /** Reads a specified number of bytes into an array at the specified offset.
   * @param b the array to read bytes into
   * @param offset the offset in the array to start storing bytes
   * @param len the number of bytes to read
   * @see DataOutput#writeBytes(byte[],int)
   */
  public abstract void readBytes(byte[] b, int offset, int len)
    throws IOException;

  /** Reads a specified number of bytes into an array at the
   * specified offset with control over whether the read
   * should be buffered (callers who have their own buffer
   * should pass in "false" for useBuffer).  Currently only
   * {@link BufferedIndexInput} respects this parameter.
   * @param b the array to read bytes into
   * @param offset the offset in the array to start storing bytes
   * @param len the number of bytes to read
   * @param useBuffer set to false if the caller will handle
   * buffering.
   * @see DataOutput#writeBytes(byte[],int)
   */
  public void readBytes(byte[] b, int offset, int len, boolean useBuffer)
    throws IOException
  {
    // Default to ignoring useBuffer entirely
    readBytes(b, offset, len);
  }

  /** Reads two bytes and returns a short.
   * @see DataOutput#writeByte(byte)
   */
  public short readShort() throws IOException {
    return (short) (((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF));
  }

  /** Reads four bytes and returns an int.
   * @see DataOutput#writeInt(int)
   */
  public int readInt() throws IOException {
    return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
         | ((readByte() & 0xFF) <<  8) |  (readByte() & 0xFF);
  }

  /** Reads an int stored in variable-length format.  Reads between one and
   * five bytes.  Smaller values take fewer bytes.  Negative numbers are not
   * supported.
   * <p>
   * The format is described further in {@link DataOutput#writeVInt(int)}.
   * 
   * @see DataOutput#writeVInt(int)
   */
  public int readVInt() throws IOException {
    /* This is the original code of this method,
     * but a Hotspot bug (see LUCENE-2975) corrupts the for-loop if
     * readByte() is inlined. So the loop was unwinded!
    byte b = readByte();
    int i = b & 0x7F;
    for (int shift = 7; (b & 0x80) != 0; shift += 7) {
      b = readByte();
      i |= (b & 0x7F) << shift;
    }
    return i;
    */
    byte b = readByte();
    if (b >= 0) return b;
    int i = b & 0x7F;
    b = readByte();
    i |= (b & 0x7F) << 7;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7F) << 14;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7F) << 21;
    if (b >= 0) return i;
    b = readByte();
    // Warning: the next ands use 0x0F / 0xF0 - beware copy/paste errors:
    i |= (b & 0x0F) << 28;
    if ((b & 0xF0) == 0) return i;
    throw new IOException("Invalid vInt detected (too many bits)");
  }

  /**
   * Read a {@link BitUtil#zigZagDecode(int) zig-zag}-encoded
   * {@link #readVInt() variable-length} integer.
   * @see DataOutput#writeZInt(int)
   */
  public int readZInt() throws IOException {
    return BitUtil.zigZagDecode(readVInt());
  }

  /** Reads eight bytes and returns a long.
   * @see DataOutput#writeLong(long)
   */
  public long readLong() throws IOException {
    return (((long)readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
  }

  /** Reads a long stored in variable-length format.  Reads between one and
   * nine bytes.  Smaller values take fewer bytes.  Negative numbers are not
   * supported.
   * <p>
   * The format is described further in {@link DataOutput#writeVInt(int)}.
   * 
   * @see DataOutput#writeVLong(long)
   */
  public long readVLong() throws IOException {
    return readVLong(false);
  }

  private long readVLong(boolean allowNegative) throws IOException {
    /* This is the original code of this method,
     * but a Hotspot bug (see LUCENE-2975) corrupts the for-loop if
     * readByte() is inlined. So the loop was unwinded!
    byte b = readByte();
    long i = b & 0x7F;
    for (int shift = 7; (b & 0x80) != 0; shift += 7) {
      b = readByte();
      i |= (b & 0x7FL) << shift;
    }
    return i;
    */
    byte b = readByte();
    if (b >= 0) return b;
    long i = b & 0x7FL;
    b = readByte();
    i |= (b & 0x7FL) << 7;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7FL) << 14;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7FL) << 21;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7FL) << 28;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7FL) << 35;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7FL) << 42;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7FL) << 49;
    if (b >= 0) return i;
    b = readByte();
    i |= (b & 0x7FL) << 56;
    if (b >= 0) return i;
    if (allowNegative) {
      b = readByte();
      i |= (b & 0x7FL) << 63;
      if (b == 0 || b == 1) return i;
      throw new IOException("Invalid vLong detected (more than 64 bits)");
    } else {
      throw new IOException("Invalid vLong detected (negative values disallowed)");
    }
  }

  /**
   * Read a {@link BitUtil#zigZagDecode(long) zig-zag}-encoded
   * {@link #readVLong() variable-length} integer. Reads between one and ten
   * bytes.
   * @see DataOutput#writeZLong(long)
   */
  public long readZLong() throws IOException {
    return BitUtil.zigZagDecode(readVLong(true));
  }

  /** Reads a string.
   * @see DataOutput#writeString(String)
   */
  public String readString() throws IOException {
    int length = readVInt();
    final byte[] bytes = new byte[length];
    readBytes(bytes, 0, length);
    return new String(bytes, 0, length, StandardCharsets.UTF_8);
  }

  /** Returns a clone of this stream.
   *
   * <p>Clones of a stream access the same data, and are positioned at the same
   * point as the stream they were cloned from.
   *
   * <p>Expert: Subclasses must ensure that clones may be positioned at
   * different points in the input from each other and from the stream they
   * were cloned from.
   */
  @Override
  public DataInput clone() {
    try {
      return (DataInput) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new Error("This cannot happen: Failing to clone DataInput");
    }
  }

  /** Reads a Map&lt;String,String&gt; previously written
   *  with {@link DataOutput#writeStringStringMap(Map)}. */
  public Map<String,String> readStringStringMap() throws IOException {
    final Map<String,String> map = new HashMap<>();
    final int count = readInt();
    for(int i=0;i<count;i++) {
      final String key = readString();
      final String val = readString();
      map.put(key, val);
    }

    return map;
  }

  /** Reads a Set&lt;String&gt; previously written
   *  with {@link DataOutput#writeStringSet(Set)}. */
  public Set<String> readStringSet() throws IOException {
    final Set<String> set = new HashSet<>();
    final int count = readInt();
    for(int i=0;i<count;i++) {
      set.add(readString());
    }

    return set;
  }

  /**
   * Skip over <code>numBytes</code> bytes. The contract on this method is that it
   * should have the same behavior as reading the same number of bytes into a
   * buffer and discarding its content. Negative values of <code>numBytes</code>
   * are not supported.
   */
  public void skipBytes(final long numBytes) throws IOException {
    if (numBytes < 0) {
      throw new IllegalArgumentException("numBytes must be >= 0, got " + numBytes);
    }
    if (skipBuffer == null) {
      skipBuffer = new byte[SKIP_BUFFER_SIZE];
    }
    assert skipBuffer.length == SKIP_BUFFER_SIZE;
    for (long skipped = 0; skipped < numBytes; ) {
      final int step = (int) Math.min(SKIP_BUFFER_SIZE, numBytes - skipped);
      readBytes(skipBuffer, 0, step, false);
      skipped += step;
    }
  }

}
