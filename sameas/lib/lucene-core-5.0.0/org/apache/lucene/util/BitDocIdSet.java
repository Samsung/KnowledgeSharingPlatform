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
 * Implementation of the {@link DocIdSet} interface on top of a {@link BitSet}.
 * @lucene.internal
 */
public class BitDocIdSet extends DocIdSet {

  private static final long BASE_RAM_BYTES_USED = RamUsageEstimator.shallowSizeOfInstance(BitDocIdSet.class);

  private final BitSet set;
  private final long cost;

  /**
   * Wrap the given {@link FixedBitSet} as a {@link DocIdSet}. The provided
   * {@link FixedBitSet} should not be modified after having wrapped as a
   * {@link DocIdSet}.
   */
  public BitDocIdSet(BitSet set, long cost) {
    this.set = set;
    this.cost = cost;
  }

  /**
   * Same as {@link #BitDocIdSet(BitSet, long)} but uses the set's
   * {@link BitSet#approximateCardinality() approximate cardinality} as a cost.
   */
  public BitDocIdSet(BitSet set) {
    this(set, set.approximateCardinality());
  }

  @Override
  public DocIdSetIterator iterator() {
    return new BitSetIterator(set, cost);
  }

  @Override
  public BitSet bits() {
    return set;
  }

  /** This DocIdSet implementation is cacheable. */
  @Override
  public boolean isCacheable() {
    return true;
  }

  @Override
  public long ramBytesUsed() {
    return BASE_RAM_BYTES_USED + set.ramBytesUsed();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(set=" + set + ",cost=" + cost + ")";
  }

  /**
   * A builder of {@link DocIdSet}s that supports random access.
   * @lucene.internal
   */
  public static final class Builder {

    private final int maxDoc;
    private final int threshold;
    private SparseFixedBitSet sparseSet;
    private FixedBitSet denseSet;

    // we cache an upper bound of the cost of this builder so that we don't have
    // to re-compute approximateCardinality on the sparse set every time 
    private long costUpperBound;

    /** Create a new instance that can hold <code>maxDoc</code> documents and is optionally <code>full</code>. */
    public Builder(int maxDoc, boolean full) {
      this.maxDoc = maxDoc;
      threshold = maxDoc >>> 10;
      if (full) {
        denseSet = new FixedBitSet(maxDoc);
        denseSet.set(0, maxDoc);
      }
    }

    /** Create a new empty instance. */
    public Builder(int maxDoc) {
      this(maxDoc, false);
    }

    // pkg-private for testing
    boolean dense() {
      return denseSet != null;
    }

    /**
     * Add the content of the provided {@link DocIdSetIterator} to this builder.
     */
    public void or(DocIdSetIterator it) throws IOException {
      if (denseSet != null) {
        // already upgraded
        denseSet.or(it);
        return;
      }

      final long itCost = it.cost();
      costUpperBound += itCost;
      if (costUpperBound >= threshold) {
        costUpperBound = (sparseSet == null ? 0 : sparseSet.approximateCardinality()) + itCost;

        if (costUpperBound >= threshold) {
          // upgrade
          denseSet = new FixedBitSet(maxDoc);
          denseSet.or(it);
          if (sparseSet != null) {
            denseSet.or(new BitSetIterator(sparseSet, 0L));
          }
          return;
        }
      }

      // we are still sparse
      if (sparseSet == null) {
        sparseSet = new SparseFixedBitSet(maxDoc);
      }
      sparseSet.or(it);
    }

    /**
     * Removes from this builder documents that are not contained in <code>it</code>.
     */
    public void and(DocIdSetIterator it) throws IOException {
      if (denseSet != null) {
        denseSet.and(it);
      } else if (sparseSet != null) {
        sparseSet.and(it);
      }
    }

    /**
     * Removes from this builder documents that are contained in <code>it</code>.
     */
    public void andNot(DocIdSetIterator it) throws IOException {
      if (denseSet != null) {
        denseSet.andNot(it);
      } else if (sparseSet != null) {
        sparseSet.andNot(it);
      }
    }

    /**
     * Build a {@link DocIdSet} that contains all doc ids that have been added.
     * This method may return <tt>null</tt> if no documents were addded to this
     * builder.
     * NOTE: this is a destructive operation, the builder should not be used
     * anymore after this method has been called.
     */
    public BitDocIdSet build() {
      final BitDocIdSet result;
      if (denseSet != null) {
        result = new BitDocIdSet(denseSet);
      } else if (sparseSet != null) {
        result = new BitDocIdSet(sparseSet);
      } else {
        result = null;
      }
      denseSet = null;
      sparseSet = null;
      costUpperBound = 0;
      return result;
    }

  }

}
