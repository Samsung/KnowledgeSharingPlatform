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


import java.io.IOException;
import java.util.Arrays;

/**
 * Exposes {@link DocsEnum}, merged from {@link DocsEnum}
 * API of sub-segments.
 *
 * @lucene.experimental
 */

public final class MultiDocsEnum extends DocsEnum {
  private final MultiTermsEnum parent;
  final DocsEnum[] subDocsEnum;
  private final EnumWithSlice[] subs;
  int numSubs;
  int upto;
  DocsEnum current;
  int currentBase;
  int doc = -1;

  /** Sole constructor
   * @param parent The {@link MultiTermsEnum} that created us.
   * @param subReaderCount How many sub-readers are being merged. */
  public MultiDocsEnum(MultiTermsEnum parent, int subReaderCount) {
    this.parent = parent;
    subDocsEnum = new DocsEnum[subReaderCount];
    this.subs = new EnumWithSlice[subReaderCount];
    for (int i = 0; i < subs.length; i++) {
      subs[i] = new EnumWithSlice();
    }
  }

  MultiDocsEnum reset(final EnumWithSlice[] subs, final int numSubs) {
    this.numSubs = numSubs;

    for(int i=0;i<numSubs;i++) {
      this.subs[i].docsEnum = subs[i].docsEnum;
      this.subs[i].slice = subs[i].slice;
    }
    upto = -1;
    doc = -1;
    current = null;
    return this;
  }

  /** Returns {@code true} if this instance can be reused by
   *  the provided {@link MultiTermsEnum}. */
  public boolean canReuse(MultiTermsEnum parent) {
    return this.parent == parent;
  }

  /** How many sub-readers we are merging.
   *  @see #getSubs */
  public int getNumSubs() {
    return numSubs;
  }

  /** Returns sub-readers we are merging. */
  public EnumWithSlice[] getSubs() {
    return subs;
  }

  @Override
  public int freq() throws IOException {
    return current.freq();
  }

  @Override
  public int docID() {
    return doc;
  }

  @Override
  public int advance(int target) throws IOException {
    assert target > doc;
    while(true) {
      if (current != null) {
        final int doc;
        if (target < currentBase) {
          // target was in the previous slice but there was no matching doc after it
          doc = current.nextDoc();
        } else {
          doc = current.advance(target-currentBase);
        }
        if (doc == NO_MORE_DOCS) {
          current = null;
        } else {
          return this.doc = doc + currentBase;
        }
      } else if (upto == numSubs-1) {
        return this.doc = NO_MORE_DOCS;
      } else {
        upto++;
        current = subs[upto].docsEnum;
        currentBase = subs[upto].slice.start;
      }
    }
  }

  @Override
  public int nextDoc() throws IOException {
    while(true) {
      if (current == null) {
        if (upto == numSubs-1) {
          return this.doc = NO_MORE_DOCS;
        } else {
          upto++;
          current = subs[upto].docsEnum;
          currentBase = subs[upto].slice.start;
        }
      }

      final int doc = current.nextDoc();
      if (doc != NO_MORE_DOCS) {
        return this.doc = currentBase + doc;
      } else {
        current = null;
      }
    }
  }
  
  @Override
  public long cost() {
    long cost = 0;
    for (int i = 0; i < numSubs; i++) {
      cost += subs[i].docsEnum.cost();
    }
    return cost;
  }

  // TODO: implement bulk read more efficiently than super
  /** Holds a {@link DocsEnum} along with the
   *  corresponding {@link ReaderSlice}. */
  public final static class EnumWithSlice {
    EnumWithSlice() {
    }

    /** {@link DocsEnum} of this sub-reader. */
    public DocsEnum docsEnum;

    /** {@link ReaderSlice} describing how this sub-reader
     *  fits into the composite reader. */
    public ReaderSlice slice;
    
    @Override
    public String toString() {
      return slice.toString()+":"+docsEnum;
    }
  }

  @Override
  public String toString() {
    return "MultiDocsEnum(" + Arrays.toString(getSubs()) + ")";
  }
}

