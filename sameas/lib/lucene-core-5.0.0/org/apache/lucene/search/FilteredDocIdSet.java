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
import java.util.Collection;

import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.RamUsageEstimator;

/**
 * Abstract decorator class for a DocIdSet implementation
 * that provides on-demand filtering/validation
 * mechanism on a given DocIdSet.
 *
 * <p/>
 *
 * Technically, this same functionality could be achieved
 * with ChainedFilter (under queries/), however the
 * benefit of this class is it never materializes the full
 * bitset for the filter.  Instead, the {@link #match}
 * method is invoked on-demand, per docID visited during
 * searching.  If you know few docIDs will be visited, and
 * the logic behind {@link #match} is relatively costly,
 * this may be a better way to filter than ChainedFilter.
 *
 * @see DocIdSet
 */

public abstract class FilteredDocIdSet extends DocIdSet {
  private final DocIdSet _innerSet;
  
  /**
   * Constructor.
   * @param innerSet Underlying DocIdSet
   */
  public FilteredDocIdSet(DocIdSet innerSet) {
    _innerSet = innerSet;
  }

  /** Return the wrapped {@link DocIdSet}. */
  public DocIdSet getDelegate() {
    return _innerSet;
  }

  /** This DocIdSet implementation is cacheable if the inner set is cacheable. */
  @Override
  public boolean isCacheable() {
    return _innerSet.isCacheable();
  }

  @Override
  public long ramBytesUsed() {
    return RamUsageEstimator.NUM_BYTES_OBJECT_REF + _innerSet.ramBytesUsed();
  }
  
  @Override
  public Collection<Accountable> getChildResources() {
    return _innerSet.getChildResources();
  }

  @Override
  public Bits bits() throws IOException {
    final Bits bits = _innerSet.bits();
    return (bits == null) ? null : new Bits() {
      @Override
      public boolean get(int docid) {
        return bits.get(docid) && FilteredDocIdSet.this.match(docid);
      }

      @Override
      public int length() {
        return bits.length();
      }
    };
  }

  /**
   * Validation method to determine whether a docid should be in the result set.
   * @param docid docid to be tested
   * @return true if input docid should be in the result set, false otherwise.
   */
  protected abstract boolean match(int docid);

  /**
   * Implementation of the contract to build a DocIdSetIterator.
   * @see DocIdSetIterator
   * @see FilteredDocIdSetIterator
   */
  @Override
  public DocIdSetIterator iterator() throws IOException {
    final DocIdSetIterator iterator = _innerSet.iterator();
    if (iterator == null) {
      return null;
    }
    return new FilteredDocIdSetIterator(iterator) {
      @Override
      protected boolean match(int docid) {
        return FilteredDocIdSet.this.match(docid);
      }
    };
  }
}
