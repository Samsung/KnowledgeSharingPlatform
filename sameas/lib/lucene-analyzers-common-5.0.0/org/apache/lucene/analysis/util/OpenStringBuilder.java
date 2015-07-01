package org.apache.lucene.analysis.util;

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
 * A StringBuilder that allows one to access the array.
 */
public class OpenStringBuilder implements Appendable, CharSequence {
  protected char[] buf;
  protected int len;

  public OpenStringBuilder() {
    this(32);
  }

  public OpenStringBuilder(int size) {
    buf = new char[size];
  }

  public OpenStringBuilder(char[] arr, int len) {
    set(arr, len);
  }

  public void setLength(int len) { this.len = len; }

  public void set(char[] arr, int end) {
    this.buf = arr;
    this.len = end;
  }

  public char[] getArray() { return buf; }
  public int size() { return len; }
  @Override
  public int length() { return len; }
  public int capacity() { return buf.length; }

  @Override
  public Appendable append(CharSequence csq) {
    return append(csq, 0, csq.length());
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) {
    reserve(end-start);
    for (int i=start; i<end; i++) {
      unsafeWrite(csq.charAt(i));
    }
    return this;
  }

  @Override
  public Appendable append(char c) {
    write(c);
    return this;
  }

  @Override
  public char charAt(int index) {
    return buf[index];
  }

  public void setCharAt(int index, char ch) {
    buf[index] = ch;    
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    throw new UnsupportedOperationException(); // todo
  }

  public void unsafeWrite(char b) {
    buf[len++] = b;
  }

  public void unsafeWrite(int b) { unsafeWrite((char)b); }

  public void unsafeWrite(char b[], int off, int len) {
    System.arraycopy(b, off, buf, this.len, len);
    this.len += len;
  }

  protected void resize(int len) {
    char newbuf[] = new char[Math.max(buf.length << 1, len)];
    System.arraycopy(buf, 0, newbuf, 0, size());
    buf = newbuf;
  }

  public void reserve(int num) {
    if (len + num > buf.length) resize(len + num);
  }

  public void write(char b) {
    if (len >= buf.length) {
      resize(len +1);
    }
    unsafeWrite(b);
  }

  public void write(int b) { write((char)b); }

  public final void write(char[] b) {
    write(b,0,b.length);
  }

  public void write(char b[], int off, int len) {
    reserve(len);
    unsafeWrite(b, off, len);
  }

  public final void write(OpenStringBuilder arr) {
    write(arr.buf, 0, len);
  }

  public void write(String s) {
    reserve(s.length());
    s.getChars(0,s.length(),buf, len);
    len +=s.length();
  }

  public void flush() {
  }

  public final void reset() {
    len =0;
  }

  public char[] toCharArray() {
    char newbuf[] = new char[size()];
    System.arraycopy(buf, 0, newbuf, 0, size());
    return newbuf;
  }

  @Override
  public String toString() {
    return new String(buf, 0, size());
  }
}
