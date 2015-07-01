package org.apache.lucene.index;

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

import org.apache.lucene.util.BytesRef;

/** 
 * Exposes multi-valued view over a single-valued instance.
 * <p>
 * This can be used if you want to have one multi-valued implementation
 * that works for single or multi-valued types.
 */
final class SingletonSortedSetDocValues extends RandomAccessOrds {
  private final SortedDocValues in;
  private long currentOrd;
  private long ord;
  
  /** Creates a multi-valued view over the provided SortedDocValues */
  public SingletonSortedSetDocValues(SortedDocValues in) {
    this.in = in;
    assert NO_MORE_ORDS == -1; // this allows our nextOrd() to work for missing values without a check
  }

  /** Return the wrapped {@link SortedDocValues} */
  public SortedDocValues getSortedDocValues() {
    return in;
  }

  @Override
  public long nextOrd() {
    long v = currentOrd;
    currentOrd = NO_MORE_ORDS;
    return v;
  }

  @Override
  public void setDocument(int docID) {
    currentOrd = ord = in.getOrd(docID);
  }

  @Override
  public BytesRef lookupOrd(long ord) {
    // cast is ok: single-valued cannot exceed Integer.MAX_VALUE
    return in.lookupOrd((int) ord);
  }

  @Override
  public long getValueCount() {
    return in.getValueCount();
  }

  @Override
  public long lookupTerm(BytesRef key) {
    return in.lookupTerm(key);
  }

  @Override
  public long ordAt(int index) {
    return ord;
  }

  @Override
  public int cardinality() {
    return (int) (ord >>> 63) ^ 1;
  }

  @Override
  public TermsEnum termsEnum() {
    return in.termsEnum();
  }
}
