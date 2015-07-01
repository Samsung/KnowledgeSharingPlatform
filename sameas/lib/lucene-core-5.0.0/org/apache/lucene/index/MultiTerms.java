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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.BytesRef;

import org.apache.lucene.util.automaton.CompiledAutomaton;

/**
 * Exposes flex API, merged from flex API of
 * sub-segments.
 *
 * @lucene.experimental
 */

public final class MultiTerms extends Terms {
  private final Terms[] subs;
  private final ReaderSlice[] subSlices;
  private final boolean hasFreqs;
  private final boolean hasOffsets;
  private final boolean hasPositions;
  private final boolean hasPayloads;

  /** Sole constructor.
   *
   * @param subs The {@link Terms} instances of all sub-readers. 
   * @param subSlices A parallel array (matching {@code
   *        subs}) describing the sub-reader slices.
   */
  public MultiTerms(Terms[] subs, ReaderSlice[] subSlices) throws IOException {
    this.subs = subs;
    this.subSlices = subSlices;
    
    assert subs.length > 0 : "inefficient: don't use MultiTerms over one sub";
    boolean _hasFreqs = true;
    boolean _hasOffsets = true;
    boolean _hasPositions = true;
    boolean _hasPayloads = false;
    for(int i=0;i<subs.length;i++) {
      _hasFreqs &= subs[i].hasFreqs();
      _hasOffsets &= subs[i].hasOffsets();
      _hasPositions &= subs[i].hasPositions();
      _hasPayloads |= subs[i].hasPayloads();
    }

    hasFreqs = _hasFreqs;
    hasOffsets = _hasOffsets;
    hasPositions = _hasPositions;
    hasPayloads = hasPositions && _hasPayloads; // if all subs have pos, and at least one has payloads.
  }

  /** Expert: returns the Terms being merged. */
  public Terms[] getSubTerms() {
    return subs;
  }

  /** Expert: returns  pointers to the sub-readers corresponding to the Terms being merged. */
  public ReaderSlice[] getSubSlices() {
    return subSlices;
  }

  @Override
  public TermsEnum intersect(CompiledAutomaton compiled, BytesRef startTerm) throws IOException {
    final List<MultiTermsEnum.TermsEnumIndex> termsEnums = new ArrayList<>();
    for(int i=0;i<subs.length;i++) {
      final TermsEnum termsEnum = subs[i].intersect(compiled, startTerm);
      if (termsEnum != null) {
        termsEnums.add(new MultiTermsEnum.TermsEnumIndex(termsEnum, i));
      }
    }

    if (termsEnums.size() > 0) {
      return new MultiTermsEnum(subSlices).reset(termsEnums.toArray(MultiTermsEnum.TermsEnumIndex.EMPTY_ARRAY));
    } else {
      return TermsEnum.EMPTY;
    }
  }
  
  @Override
  public BytesRef getMin() throws IOException {
    BytesRef minTerm = null;
    for(Terms terms : subs) {
      BytesRef term = terms.getMin();
      if (minTerm == null || term.compareTo(minTerm) < 0) {
        minTerm = term;
      }
    }

    return minTerm;
  }

  @Override
  public BytesRef getMax() throws IOException {
    BytesRef maxTerm = null;
    for(Terms terms : subs) {
      BytesRef term = terms.getMax();
      if (maxTerm == null || term.compareTo(maxTerm) > 0) {
        maxTerm = term;
      }
    }

    return maxTerm;
  }

  @Override
  public TermsEnum iterator(TermsEnum reuse) throws IOException {

    final List<MultiTermsEnum.TermsEnumIndex> termsEnums = new ArrayList<>();
    for(int i=0;i<subs.length;i++) {
      final TermsEnum termsEnum = subs[i].iterator(null);
      if (termsEnum != null) {
        termsEnums.add(new MultiTermsEnum.TermsEnumIndex(termsEnum, i));
      }
    }

    if (termsEnums.size() > 0) {
      return new MultiTermsEnum(subSlices).reset(termsEnums.toArray(MultiTermsEnum.TermsEnumIndex.EMPTY_ARRAY));
    } else {
      return TermsEnum.EMPTY;
    }
  }

  @Override
  public long size() {
    return -1;
  }

  @Override
  public long getSumTotalTermFreq() throws IOException {
    long sum = 0;
    for(Terms terms : subs) {
      final long v = terms.getSumTotalTermFreq();
      if (v == -1) {
        return -1;
      }
      sum += v;
    }
    return sum;
  }
  
  @Override
  public long getSumDocFreq() throws IOException {
    long sum = 0;
    for(Terms terms : subs) {
      final long v = terms.getSumDocFreq();
      if (v == -1) {
        return -1;
      }
      sum += v;
    }
    return sum;
  }
  
  @Override
  public int getDocCount() throws IOException {
    int sum = 0;
    for(Terms terms : subs) {
      final int v = terms.getDocCount();
      if (v == -1) {
        return -1;
      }
      sum += v;
    }
    return sum;
  }

  @Override
  public boolean hasFreqs() {
    return hasFreqs;
  }

  @Override
  public boolean hasOffsets() {
    return hasOffsets;
  }

  @Override
  public boolean hasPositions() {
    return hasPositions;
  }
  
  @Override
  public boolean hasPayloads() {
    return hasPayloads;
  }
}

