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

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;

/**
 * This {@link DocIdSet} encodes the negation of another {@link DocIdSet}.
 * It is cacheable and supports random-access if the underlying set is
 * cacheable and supports random-access.
 * @lucene.internal
 */
public final class NotDocIdSet extends DocIdSet {

  private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(NotDocIdSet.class);

  private final int maxDoc;
  private final DocIdSet in;

  /** Sole constructor. */
  public NotDocIdSet(int maxDoc, DocIdSet in) {
    this.maxDoc = maxDoc;
    this.in = in;
  }

  @Override
  public boolean isCacheable() {
    return in.isCacheable();
  }

  @Override
  public Bits bits() throws IOException {
    final Bits inBits = in.bits();
    if (inBits == null) {
      return null;
    }
    return new Bits() {

      @Override
      public boolean get(int index) {
        return !inBits.get(index);
      }

      @Override
      public int length() {
        return inBits.length();
      }

    };
  }

  @Override
  public long ramBytesUsed() {
    return BASE_RAM_BYTES_USED + in.ramBytesUsed();
  }

  @Override
  public DocIdSetIterator iterator() throws IOException {
    final DocIdSetIterator inIterator = in.iterator();
    return new DocIdSetIterator() {

      int doc = -1;
      int nextSkippedDoc = -1;

      @Override
      public int nextDoc() throws IOException {
        return advance(doc + 1);
      }

      @Override
      public int advance(int target) throws IOException {
        doc = target;
        if (doc > nextSkippedDoc) {
          nextSkippedDoc = inIterator.advance(doc);
        }
        while (true) {
          if (doc >= maxDoc) {
            return doc = NO_MORE_DOCS;
          }
          assert doc <= nextSkippedDoc;
          if (doc != nextSkippedDoc) {
            return doc;
          }
          doc += 1;
          nextSkippedDoc = inIterator.nextDoc();
        }
      }

      @Override
      public int docID() {
        return doc;
      }

      @Override
      public long cost() {
        // even if there are few docs in this set, iterating over all documents
        // costs O(maxDoc) in all cases
        return maxDoc;
      }
    };
  }

}
