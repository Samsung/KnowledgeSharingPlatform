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

/**
 * A builder for {@link IntsRef} instances.
 * @lucene.internal
 */
public class IntsRefBuilder {

  private final IntsRef ref;

  /** Sole constructor. */
  public IntsRefBuilder() {
    ref = new IntsRef();
  }

  /** Return a reference to the ints of this builder. */
  public int[] ints() {
    return ref.ints;
  }

  /** Return the number of ints in this buffer. */
  public int length() {
    return ref.length;
  }

  /** Set the length. */
  public void setLength(int length) {
    this.ref.length = length;
  }

  /** Empty this builder. */
  public void clear() {
    setLength(0);
  }

  /** Return the int at the given offset. */
  public int intAt(int offset) {
    return ref.ints[offset];
  }

  /** Set an int. */
  public void setIntAt(int offset, int b) {
    ref.ints[offset] = b;
  }

  /** Append the provided int to this buffer. */
  public void append(int i) {
    grow(ref.length + 1);
    ref.ints[ref.length++] = i;
  }

  /**
   * Used to grow the reference array.
   *
   * In general this should not be used as it does not take the offset into account.
   * @lucene.internal */
  public void grow(int newLength) {
    ref.ints = ArrayUtil.grow(ref.ints, newLength);
  }

  /**
   * Copies the given array into this instance.
   */
  public void copyInts(int[] otherInts, int otherOffset, int otherLength) {
    grow(otherLength);
    System.arraycopy(otherInts, otherOffset, ref.ints, 0, otherLength);
    ref.length = otherLength;
  }

  /**
   * Copies the given array into this instance.
   */
  public void copyInts(IntsRef ints) {
    copyInts(ints.ints, ints.offset, ints.length);
  }

  /**
   * Copy the given UTF-8 bytes into this builder. Works as if the bytes were
   * first converted from UTF-8 to UTF-32 and then copied into this builder.
   */
  public void copyUTF8Bytes(BytesRef bytes) {
    grow(bytes.length);
    ref.length = UnicodeUtil.UTF8toUTF32(bytes, ref.ints);
  }

  /**
   * Return a {@link IntsRef} that points to the internal content of this
   * builder. Any update to the content of this builder might invalidate
   * the provided <code>ref</code> and vice-versa.
   */
  public IntsRef get() {
    assert ref.offset == 0 : "Modifying the offset of the returned ref is illegal";
    return ref;
  }

  /** Build a new {@link CharsRef} that has the same content as this builder. */
  public IntsRef toIntsRef() {
    return IntsRef.deepCopyOf(get());
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
