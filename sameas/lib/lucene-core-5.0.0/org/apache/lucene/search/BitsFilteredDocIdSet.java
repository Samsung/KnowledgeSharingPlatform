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

import org.apache.lucene.util.Bits;

/**
 * This implementation supplies a filtered DocIdSet, that excludes all
 * docids which are not in a Bits instance. This is especially useful in
 * {@link org.apache.lucene.search.Filter} to apply the {@code acceptDocs}
 * passed to {@code getDocIdSet()} before returning the final DocIdSet.
 *
 * @see DocIdSet
 * @see org.apache.lucene.search.Filter
 */

public final class BitsFilteredDocIdSet extends FilteredDocIdSet {

  private final Bits acceptDocs;
  
  /**
   * Convenience wrapper method: If {@code acceptDocs == null} it returns the original set without wrapping.
   * @param set Underlying DocIdSet. If {@code null}, this method returns {@code null}
   * @param acceptDocs Allowed docs, all docids not in this set will not be returned by this DocIdSet.
   * If {@code null}, this method returns the original set without wrapping.
   */
  public static DocIdSet wrap(DocIdSet set, Bits acceptDocs) {
    return (set == null || acceptDocs == null) ? set : new BitsFilteredDocIdSet(set, acceptDocs);
  }
  
  /**
   * Constructor.
   * @param innerSet Underlying DocIdSet
   * @param acceptDocs Allowed docs, all docids not in this set will not be returned by this DocIdSet
   */
  public BitsFilteredDocIdSet(DocIdSet innerSet, Bits acceptDocs) {
    super(innerSet);
    if (acceptDocs == null)
      throw new NullPointerException("acceptDocs is null");
    this.acceptDocs = acceptDocs;
  }

  @Override
  protected boolean match(int docid) {
    return acceptDocs.get(docid);
  }

}
