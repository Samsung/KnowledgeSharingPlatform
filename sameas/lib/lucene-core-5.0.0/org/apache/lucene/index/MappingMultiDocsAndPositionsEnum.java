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

import org.apache.lucene.index.MultiDocsAndPositionsEnum.EnumWithSlice;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * Exposes flex API, merged from flex API of sub-segments,
 * remapping docIDs (this is used for segment merging).
 *
 * @lucene.experimental
 */

final class MappingMultiDocsAndPositionsEnum extends DocsAndPositionsEnum {
  private MultiDocsAndPositionsEnum.EnumWithSlice[] subs;
  int numSubs;
  int upto;
  MergeState.DocMap currentMap;
  DocsAndPositionsEnum current;
  int currentBase;
  int doc = -1;
  private MergeState mergeState;
  MultiDocsAndPositionsEnum multiDocsAndPositionsEnum;

  /** Sole constructor. */
  public MappingMultiDocsAndPositionsEnum(MergeState mergeState) {
    this.mergeState = mergeState;
  }

  MappingMultiDocsAndPositionsEnum reset(MultiDocsAndPositionsEnum postingsEnum) {
    this.numSubs = postingsEnum.getNumSubs();
    this.subs = postingsEnum.getSubs();
    upto = -1;
    current = null;
    this.multiDocsAndPositionsEnum = postingsEnum;
    return this;
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
  public int advance(int target) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int nextDoc() throws IOException {
    while(true) {
      if (current == null) {
        if (upto == numSubs-1) {
          return this.doc = NO_MORE_DOCS;
        } else {
          upto++;
          final int reader = subs[upto].slice.readerIndex;
          current = subs[upto].docsAndPositionsEnum;
          currentBase = mergeState.docBase[reader];
          currentMap = mergeState.docMaps[reader];
        }
      }

      int doc = current.nextDoc();
      if (doc != NO_MORE_DOCS) {
        // compact deletions
        doc = currentMap.get(doc);
        if (doc == -1) {
          continue;
        }
        return this.doc = currentBase + doc;
      } else {
        current = null;
      }
    }
  }

  @Override
  public int nextPosition() throws IOException {
    return current.nextPosition();
  }

  @Override
  public int startOffset() throws IOException {
    return current.startOffset();
  }
  
  @Override
  public int endOffset() throws IOException {
    return current.endOffset();
  }
  
  @Override
  public BytesRef getPayload() throws IOException {
    return current.getPayload();
  }

  @Override
  public long cost() {
    long cost = 0;
    for (EnumWithSlice enumWithSlice : subs) {
      cost += enumWithSlice.docsAndPositionsEnum.cost();
    }
    return cost;
  }
}

