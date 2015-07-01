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

package org.apache.lucene.util.packed;

import java.io.IOException;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.FixedBitSet; // for javadocs
import org.apache.lucene.util.RamUsageEstimator;


/** A DocIdSet in Elias-Fano encoding.
 * @lucene.internal
 */
public class EliasFanoDocIdSet extends DocIdSet {

  private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(EliasFanoDocIdSet.class);

  final EliasFanoEncoder efEncoder;

  /**
   * Construct an EliasFanoDocIdSet. For efficient encoding, the parameters should be chosen as low as possible.
   * @param numValues At least the number of document ids that will be encoded.
   * @param upperBound  At least the highest document id that will be encoded.
   */
  public EliasFanoDocIdSet(int numValues, int upperBound) {
    efEncoder = new EliasFanoEncoder(numValues, upperBound);
  }

  /** Provide an indication that is better to use an {@link EliasFanoDocIdSet} than a {@link FixedBitSet}
   *  to encode document identifiers.
   *  @param numValues The number of document identifiers that is to be encoded. Should be non negative.
   *  @param upperBound The maximum possible value for a document identifier. Should be at least <code>numValues</code>.
   *  @return See {@link EliasFanoEncoder#sufficientlySmallerThanBitSet(long, long)}
   */
  public static boolean sufficientlySmallerThanBitSet(long numValues, long upperBound) {
    return EliasFanoEncoder.sufficientlySmallerThanBitSet(numValues, upperBound);
  }

  /** Encode the document ids from a DocIdSetIterator.
   *  @param disi This DocIdSetIterator should provide document ids that are consistent
   *              with <code>numValues</code> and <code>upperBound</code> as provided to the constructor.  
   */
  public void encodeFromDisi(DocIdSetIterator disi) throws IOException {
    while (efEncoder.numEncoded < efEncoder.numValues) {
      int x = disi.nextDoc();
      if (x == DocIdSetIterator.NO_MORE_DOCS) {
        throw new IllegalArgumentException("disi: " + disi.toString()
            + "\nhas " + efEncoder.numEncoded
            + " docs, but at least " + efEncoder.numValues + " are required.");
      }
      efEncoder.encodeNext(x);
    }
  }

  /**
   * Provides a {@link DocIdSetIterator} to access encoded document ids.
   */
  @Override
  public DocIdSetIterator iterator() {
    if (efEncoder.lastEncoded >= DocIdSetIterator.NO_MORE_DOCS) {
      throw new UnsupportedOperationException(
          "Highest encoded value too high for DocIdSetIterator.NO_MORE_DOCS: " + efEncoder.lastEncoded);
    }
    return new DocIdSetIterator() {
      private int curDocId = -1;
      private final EliasFanoDecoder efDecoder = efEncoder.getDecoder();

      @Override
      public int docID() {
        return curDocId;
      }

      private int setCurDocID(long value) {
        curDocId = (value == EliasFanoDecoder.NO_MORE_VALUES)
            ?  NO_MORE_DOCS
                : (int) value;
        return curDocId;
      }

      @Override
      public int nextDoc() {
        return setCurDocID(efDecoder.nextValue());
      }

      @Override
      public int advance(int target) {
        return setCurDocID(efDecoder.advanceToValue(target));
      }

      @Override
      public long cost() {
        return efDecoder.numEncoded();
      }
    };
  }

  /** This DocIdSet implementation is cacheable.
   * @return <code>true</code>
   */
  @Override
  public boolean isCacheable() {
    return true;
  }

  @Override
  public boolean equals(Object other) {
    return ((other instanceof EliasFanoDocIdSet))
        && efEncoder.equals(((EliasFanoDocIdSet) other).efEncoder);
  }

  @Override
  public int hashCode() {
    return efEncoder.hashCode() ^ getClass().hashCode();
  }

  @Override
  public long ramBytesUsed() {
    return BASE_RAM_BYTES_USED + efEncoder.ramBytesUsed();
  }
}

