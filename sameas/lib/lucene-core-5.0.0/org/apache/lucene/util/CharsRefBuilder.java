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

import java.io.IOException;
import java.util.Arrays;

/**
 * A builder for {@link CharsRef} instances.
 * @lucene.internal
 */
public class CharsRefBuilder implements Appendable {

  private static final String NULL_STRING = "null";

  private final CharsRef ref;

  /** Sole constructor. */
  public CharsRefBuilder() {
    ref = new CharsRef();
  }

  /** Return a reference to the chars of this builder. */
  public char[] chars() {
    return ref.chars;
  }

  /** Return the number of chars in this buffer. */
  public int length() {
    return ref.length;
  }

  /** Set the length. */
  public void setLength(int length) {
    this.ref.length = length;
  }

  /** Return the char at the given offset. */
  public char charAt(int offset) {
    return ref.chars[offset];
  }

  /** Set a char. */
  public void setCharAt(int offset, char b) {
    ref.chars[offset] = b;
  }

  /**
   * Reset this builder to the empty state.
   */
  public void clear() {
    ref.length = 0;
  }

  @Override
  public CharsRefBuilder append(CharSequence csq) {
    if (csq == null) {
      return append(NULL_STRING);
    }
    return append(csq, 0, csq.length());
  }

  @Override
  public CharsRefBuilder append(CharSequence csq, int start, int end) {
    if (csq == null) {
      return append(NULL_STRING);
    }
    grow(ref.length + end - start);
    for (int i = start; i < end; ++i) {
      setCharAt(ref.length++, csq.charAt(i));
    }
    return this;
  }

  @Override
  public CharsRefBuilder append(char c) {
    grow(ref.length + 1);
    setCharAt(ref.length++, c);
    return this;
  }

  /**
   * Copies the given {@link CharsRef} referenced content into this instance.
   */
  public void copyChars(CharsRef other) {
    copyChars(other.chars, other.offset, other.length);
  }

  /**
   * Used to grow the reference array.
   */
  public void grow(int newLength) {
    ref.chars = ArrayUtil.grow(ref.chars, newLength);
  }

  /**
   * Copy the provided bytes, interpreted as UTF-8 bytes.
   */
  public void copyUTF8Bytes(byte[] bytes, int offset, int length) {
    grow(length);
    ref.length = UnicodeUtil.UTF8toUTF16(bytes, offset, length, ref.chars);
  }

  /**
   * Copy the provided bytes, interpreted as UTF-8 bytes.
   */
  public void copyUTF8Bytes(BytesRef bytes) {
    copyUTF8Bytes(bytes.bytes, bytes.offset, bytes.length);
  }

  /**
   * Copies the given array into this instance.
   */
  public void copyChars(char[] otherChars, int otherOffset, int otherLength) {
    grow(otherLength);
    System.arraycopy(otherChars, otherOffset, ref.chars, 0, otherLength);
    ref.length = otherLength;
  }

  /**
   * Appends the given array to this CharsRef
   */
  public void append(char[] otherChars, int otherOffset, int otherLength) {
    int newLen = ref.length + otherLength;
    grow(newLen);
    System.arraycopy(otherChars, otherOffset, ref.chars, ref.length, otherLength);
    ref.length = newLen;
  }

  /**
   * Return a {@link CharsRef} that points to the internal content of this
   * builder. Any update to the content of this builder might invalidate
   * the provided <code>ref</code> and vice-versa.
   */
  public CharsRef get() {
    assert ref.offset == 0 : "Modifying the offset of the returned ref is illegal";
    return ref;
  }

  /** Build a new {@link CharsRef} that has the same content as this builder. */
  public CharsRef toCharsRef() {
    return new CharsRef(Arrays.copyOf(ref.chars, ref.length), 0, ref.length);
  }

  @Override
  public String toString() {
    return get().toString();
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
