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


package org.apache.lucene.analysis.compound.hyphenation;

/**
 * This class implements a simple byte vector with access to the underlying
 * array.
 * This class has been taken from the Apache FOP project (http://xmlgraphics.apache.org/fop/). They have been slightly modified. 
 */
public class ByteVector {

  /**
   * Capacity increment size
   */
  private static final int DEFAULT_BLOCK_SIZE = 2048;

  private int blockSize;

  /**
   * The encapsulated array
   */
  private byte[] array;

  /**
   * Points to next free item
   */
  private int n;

  public ByteVector() {
    this(DEFAULT_BLOCK_SIZE);
  }

  public ByteVector(int capacity) {
    if (capacity > 0) {
      blockSize = capacity;
    } else {
      blockSize = DEFAULT_BLOCK_SIZE;
    }
    array = new byte[blockSize];
    n = 0;
  }

  public ByteVector(byte[] a) {
    blockSize = DEFAULT_BLOCK_SIZE;
    array = a;
    n = 0;
  }

  public ByteVector(byte[] a, int capacity) {
    if (capacity > 0) {
      blockSize = capacity;
    } else {
      blockSize = DEFAULT_BLOCK_SIZE;
    }
    array = a;
    n = 0;
  }

  public byte[] getArray() {
    return array;
  }

  /**
   * return number of items in array
   */
  public int length() {
    return n;
  }

  /**
   * returns current capacity of array
   */
  public int capacity() {
    return array.length;
  }

  public void put(int index, byte val) {
    array[index] = val;
  }

  public byte get(int index) {
    return array[index];
  }

  /**
   * This is to implement memory allocation in the array. Like malloc().
   */
  public int alloc(int size) {
    int index = n;
    int len = array.length;
    if (n + size >= len) {
      byte[] aux = new byte[len + blockSize];
      System.arraycopy(array, 0, aux, 0, len);
      array = aux;
    }
    n += size;
    return index;
  }

  public void trimToSize() {
    if (n < array.length) {
      byte[] aux = new byte[n];
      System.arraycopy(array, 0, aux, 0, n);
      array = aux;
    }
  }

}
