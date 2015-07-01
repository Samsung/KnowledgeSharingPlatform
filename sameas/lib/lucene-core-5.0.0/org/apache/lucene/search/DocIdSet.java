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
import java.util.Collections;

import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.Bits;

/**
 * A DocIdSet contains a set of doc ids. Implementing classes must
 * only implement {@link #iterator} to provide access to the set. 
 */
public abstract class DocIdSet implements Accountable {

  /** An empty {@code DocIdSet} instance */
  public static final DocIdSet EMPTY = new DocIdSet() {
    
    @Override
    public DocIdSetIterator iterator() {
      return DocIdSetIterator.empty();
    }
    
    @Override
    public boolean isCacheable() {
      return true;
    }
    
    // we explicitly provide no random access, as this filter is 100% sparse and iterator exits faster
    @Override
    public Bits bits() {
      return null;
    }

    @Override
    public long ramBytesUsed() {
      return 0L;
    }
  };

  /** Provides a {@link DocIdSetIterator} to access the set.
   * This implementation can return <code>null</code> if there
   * are no docs that match. */
  public abstract DocIdSetIterator iterator() throws IOException;

  // TODO: somehow this class should express the cost of
  // iteration vs the cost of random access Bits; for
  // expensive Filters (e.g. distance < 1 km) we should use
  // bits() after all other Query/Filters have matched, but
  // this is the opposite of what bits() is for now
  // (down-low filtering using e.g. FixedBitSet)

  /** Optionally provides a {@link Bits} interface for random access
   * to matching documents.
   * @return {@code null}, if this {@code DocIdSet} does not support random access.
   * In contrast to {@link #iterator()}, a return value of {@code null}
   * <b>does not</b> imply that no documents match the filter!
   * The default implementation does not provide random access, so you
   * only need to implement this method if your DocIdSet can
   * guarantee random access to every docid in O(1) time without
   * external disk access (as {@link Bits} interface cannot throw
   * {@link IOException}). This is generally true for bit sets
   * like {@link org.apache.lucene.util.FixedBitSet}, which return
   * itself if they are used as {@code DocIdSet}.
   */
  public Bits bits() throws IOException {
    return null;
  }

  /**
   * This method is a hint for {@link CachingWrapperFilter}, if this <code>DocIdSet</code>
   * should be cached without copying it. The default is to return
   * <code>false</code>. If you have an own <code>DocIdSet</code> implementation
   * that does its iteration very effective and fast without doing disk I/O,
   * override this method and return <code>true</code>.
   */
  public boolean isCacheable() {
    return false;
  }

  @Override
  public Collection<Accountable> getChildResources() {
    return Collections.emptyList();
  }
}
