package org.apache.lucene.util;

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

import java.util.Arrays;

/**
 * A builder for {@link BytesRef} instances.
 * @lucene.internal
 */
public class BytesRefBuilder {

  private final BytesRef ref;

  /** Sole constructor. */
  public BytesRefBuilder() {
    ref = new BytesRef();
  }

  /** Return a reference to the bytes of this builder. */
  public byte[] bytes() {
    return ref.bytes;
  }

  /** Return the number of bytes in this buffer. */
  public int length() {
    return ref.length;
  }

  /** Set the length. */
  public void setLength(int length) {
    this.ref.length = length;
  }
  
  /** Return the byte at the given offset. */
  public byte byteAt(int offset) {
    return ref.bytes[offset];
  }

  /** Set a byte. */
  public void setByteAt(int offset, byte b) {
    ref.bytes[offset] = b;
  }

  /**
   * Ensure that this builder can hold at least <code>capacity</code> bytes
   * without resizing.
   */
  public void grow(int capacity) {
    ref.bytes = ArrayUtil.grow(ref.bytes, capacity);
  }

  /**
   * Append a single byte to this builder.
   */
  public void append(byte b) {
    grow(ref.length + 1);
    ref.bytes[ref.length++] = b;
  }

  /**
   * Append the provided bytes to this builder.
   */
  public void append(byte[] b, int off, int len) {
    grow(ref.length + len);
    System.arraycopy(b, off, ref.bytes, ref.length, len);
    ref.length += len;
  }

  /**
   * Append the provided bytes to this builder.
   */
  public void append(BytesRef ref) {
    append(ref.bytes, ref.offset, ref.length);
  }

  /**
   * Append the provided bytes to this builder.
   */
  public void append(BytesRefBuilder builder) {
    append(builder.get());
  }

  /**
   * Reset this builder to the empty state.
   */
  public void clear() {
    setLength(0);
  }

  /**
   * Replace the content of this builder with the provided bytes. Equivalent to
   * calling {@link #clear()} and then {@link #append(byte[], int, int)}.
   */
  public void copyBytes(byte[] b, int off, int len) {
    clear();
    append(b, off, len);
  }

  /**
   * Replace the content of this builder with the provided bytes. Equivalent to
   * calling {@link #clear()} and then {@link #append(BytesRef)}.
   */
  public void copyBytes(BytesRef ref) {
    clear();
    append(ref);
  }

  /**
   * Replace the content of this builder with the provided bytes. Equivalent to
   * calling {@link #clear()} and then {@link #append(BytesRefBuilder)}.
   */
  public void copyBytes(BytesRefBuilder builder) {
    clear();
    append(builder);
  }

  /**
   * Replace the content of this buffer with UTF-8 encoded bytes that would
   * represent the provided text.
   */
  public void copyChars(CharSequence text) {
    copyChars(text, 0, text.length());
  }

  /**
   * Replace the content of this buffer with UTF-8 encoded bytes that would
   * represent the provided text.
   */
  public void copyChars(CharSequence text, int off, int len) {
    grow(len * UnicodeUtil.MAX_UTF8_BYTES_PER_CHAR);
    ref.length = UnicodeUtil.UTF16toUTF8(text, off, len, ref.bytes);
  }

  /**
   * Replace the content of this buffer with UTF-8 encoded bytes that would
   * represent the provided text.
   */
  public void copyChars(char[] text, int off, int len) {
    grow(len * UnicodeUtil.MAX_UTF8_BYTES_PER_CHAR);
    ref.length = UnicodeUtil.UTF16toUTF8(text, off, len, ref.bytes);
  }

  /**
   * Return a {@link BytesRef} that points to the internal content of this
   * builder. Any update to the content of this builder might invalidate
   * the provided <code>ref</code> and vice-versa.
   */
  public BytesRef get() {
    assert ref.offset == 0 : "Modifying the offset of the returned ref is illegal";
    return ref;
  }

  /**
   * Build a new {@link BytesRef} that has the same content as this buffer.
   */
  public BytesRef toBytesRef() {
    return new BytesRef(Arrays.copyOf(ref.bytes, ref.length));
  }

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException();
  }
}
