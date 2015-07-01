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
import java.util.Collection;
import java.util.Collections;

import org.apache.lucene.search.DocIdSetIterator;

/**
 * Base implementation for a bit set.
 * @lucene.internal
 */
public abstract class BitSet implements MutableBits, Accountable {

  /** Set the bit at <code>i</code>. */
  public abstract void set(int i);

  /** Clears a range of bits.
   *
   * @param startIndex lower index
   * @param endIndex one-past the last bit to clear
   */
  public abstract void clear(int startIndex, int endIndex);

  /**
   * Return the number of bits that are set.
   * NOTE: this method is likely to run in linear time
   */
  public abstract int cardinality();

  /**
   * Return an approximation of the cardinality of this set. Some
   * implementations may trade accuracy for speed if they have the ability to
   * estimate the cardinality of the set without iterating over all the data.
   * The default implementation returns {@link #cardinality()}.
   */
  public int approximateCardinality() {
    return cardinality();
  }

  /** Returns the index of the last set bit before or on the index specified.
   *  -1 is returned if there are no more set bits.
   */
  public abstract int prevSetBit(int index);

  /** Returns the index of the first set bit starting at the index specified.
   *  {@link DocIdSetIterator#NO_MORE_DOCS} is returned if there are no more set bits.
   */
  public abstract int nextSetBit(int index);

  /** Assert that the current doc is -1. */
  protected final void assertUnpositioned(DocIdSetIterator iter) {
    if (iter.docID() != -1) {
      throw new IllegalStateException("This operation only works with an unpositioned iterator, got current position = " + iter.docID());
    }
  }

  /** Does in-place OR of the bits provided by the iterator. The state of the
   *  iterator after this operation terminates is undefined. */
  public void or(DocIdSetIterator iter) throws IOException {
    assertUnpositioned(iter);
    for (int doc = iter.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = iter.nextDoc()) {
      set(doc);
    }
  }

  private static abstract class LeapFrogCallBack {
    abstract void onMatch(int doc);
    void finish() {}
  }

  /** Performs a leap frog between this and the provided iterator in order to find common documents. */
  private void leapFrog(DocIdSetIterator iter, LeapFrogCallBack callback) throws IOException {
    final int length = length();
    int bitSetDoc = -1;
    int disiDoc = iter.nextDoc();
    while (true) {
      // invariant: bitSetDoc <= disiDoc
      assert bitSetDoc <= disiDoc;
      if (disiDoc >= length) {
        callback.finish();
        return;
      }
      if (bitSetDoc < disiDoc) {
        bitSetDoc = nextSetBit(disiDoc);
      }
      if (bitSetDoc == disiDoc) {
        callback.onMatch(bitSetDoc);
        disiDoc = iter.nextDoc();
      } else {
        disiDoc = iter.advance(bitSetDoc);
      }
    }
  }

  /** Does in-place AND of the bits provided by the iterator. The state of the
   *  iterator after this operation terminates is undefined. */
  public void and(DocIdSetIterator iter) throws IOException {
    assertUnpositioned(iter);
    leapFrog(iter, new LeapFrogCallBack() {
      int previous = -1;

      @Override
      public void onMatch(int doc) {
        clear(previous + 1, doc);
        previous = doc;
      }

      @Override
      public void finish() {
        if (previous + 1 < length()) {
          clear(previous + 1, length());
        }
      }

    });
  }

  /** this = this AND NOT other. The state of the iterator after this operation
   *  terminates is undefined. */
  public void andNot(DocIdSetIterator iter) throws IOException {
    assertUnpositioned(iter);
    leapFrog(iter, new LeapFrogCallBack() {

      @Override
      public void onMatch(int doc) {
        clear(doc);
      }

    });
  }

  @Override
  public Collection<Accountable> getChildResources() {
    return Collections.emptyList();
  }
}
