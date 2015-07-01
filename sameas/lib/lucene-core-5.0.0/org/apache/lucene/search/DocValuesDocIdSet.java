package org.apache.lucene.search;
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

import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.FixedBitSet;

/**
 * Base class for DocIdSet to be used with DocValues. The implementation
 * of its iterator is very stupid and slow if the implementation of the
 * {@link #matchDoc} method is not optimized, as iterators simply increment
 * the document id until {@code matchDoc(int)} returns true. Because of this
 * {@code matchDoc(int)} must be as fast as possible.
 * @lucene.internal
 */
public abstract class DocValuesDocIdSet extends DocIdSet {

  protected final int maxDoc;
  protected final Bits acceptDocs;

  public DocValuesDocIdSet(int maxDoc, Bits acceptDocs) {
    this.maxDoc = maxDoc;
    this.acceptDocs = acceptDocs;
  }

  /**
   * this method checks, if a doc is a hit
   */
  protected abstract boolean matchDoc(int doc);

  @Override
  public long ramBytesUsed() {
    return 0L;
  }

  @Override
  public final Bits bits() {
    return (acceptDocs == null) ? new Bits() {
      @Override
      public boolean get(int docid) {
        return matchDoc(docid);
      }

      @Override
      public int length() {
        return maxDoc;
      }
    } : new Bits() {
      @Override
      public boolean get(int docid) {
        return acceptDocs.get(docid) && matchDoc(docid);
      }

      @Override
      public int length() {
        return maxDoc;
      }
    };
  }

  @Override
  public final DocIdSetIterator iterator() throws IOException {
    if (acceptDocs == null) {
      // Specialization optimization disregard acceptDocs
      return new DocIdSetIterator() {
        private int doc = -1;
        
        @Override
        public int docID() {
          return doc;
        }
      
        @Override
        public int nextDoc() {
          do {
            doc++;
            if (doc >= maxDoc) {
              return doc = NO_MORE_DOCS;
            }
          } while (!matchDoc(doc));
          return doc;
        }
      
        @Override
        public int advance(int target) {
          for(doc=target; doc<maxDoc; doc++) {
            if (matchDoc(doc)) {
              return doc;
            }
          }
          return doc = NO_MORE_DOCS;
        }

        @Override
        public long cost() {
          return maxDoc;
        }
      };
    } else if (acceptDocs instanceof FixedBitSet) {
      // special case for FixedBitSet: use the iterator and filter it
      // (used e.g. when Filters are chained by FilteredQuery)
      return new FilteredDocIdSetIterator(new BitDocIdSet((FixedBitSet) acceptDocs).iterator()) {
        @Override
        protected boolean match(int doc) {
          return DocValuesDocIdSet.this.matchDoc(doc);
        }
      };
    } else {
      // Stupid consultation of acceptDocs and matchDoc()
      return new DocIdSetIterator() {
        private int doc = -1;
        
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
          for(doc=target; doc<maxDoc; doc++) {
            if (acceptDocs.get(doc) && matchDoc(doc)) {
              return doc;
            }
          }
          return doc = NO_MORE_DOCS;
        }

        @Override
        public long cost() {
          return maxDoc;
        }
      };
    }
  }
}
