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

import org.apache.lucene.search.DocIdSetIterator;

/**
 * A {@link DocIdSetIterator} which iterates over set bits in a
 * bit set.
 * @lucene.internal
 */
public class BitSetIterator extends DocIdSetIterator {

  private static <T extends BitSet> T getBitSet(DocIdSetIterator iterator, Class<? extends T> clazz) {
    if (iterator instanceof BitSetIterator) {
      BitSet bits = ((BitSetIterator) iterator).bits;
      assert bits != null;
      if (clazz.isInstance(bits)) {
        return clazz.cast(bits);
      }
    }
    return null;
  }

  /** If the provided iterator wraps a {@link FixedBitSet}, returns it, otherwise returns null. */
  public static FixedBitSet getFixedBitSetOrNull(DocIdSetIterator iterator) {
    return getBitSet(iterator, FixedBitSet.class);
  }

  /** If the provided iterator wraps a {@link SparseFixedBitSet}, returns it, otherwise returns null. */
  public static SparseFixedBitSet getSparseFixedBitSetOrNull(DocIdSetIterator iterator) {
    return getBitSet(iterator, SparseFixedBitSet.class);
  }

  private final BitSet bits;
  private final int length;
  private final long cost;
  private int doc = -1;

  /** Sole constructor. */
  public BitSetIterator(BitSet bits, long cost) {
    this.bits = bits;
    this.length = bits.length();
    this.cost = cost;
  }

  @Override
  public int docID() {
    return doc;
  }

  @Override
  public int nextDoc() {
    return advance(doc + 1);
  }

  @Override
  public int advance(int target) {
    if (target >= length) {
      return doc = NO_MORE_DOCS;
    }
    return doc = bits.nextSetBit(target);
  }

  @Override
  public long cost() {
    return cost;
  }

}
