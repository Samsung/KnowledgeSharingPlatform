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

import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;

/** Implements a {@link TermsEnum} wrapping a provided
 * {@link SortedSetDocValues}. */

class SortedSetDocValuesTermsEnum extends TermsEnum {
  private final SortedSetDocValues values;
  private long currentOrd = -1;
  private final BytesRefBuilder scratch;

  /** Creates a new TermsEnum over the provided values */
  public SortedSetDocValuesTermsEnum(SortedSetDocValues values) {
    this.values = values;
    scratch = new BytesRefBuilder();
  }

  @Override
  public SeekStatus seekCeil(BytesRef text) throws IOException {
    long ord = values.lookupTerm(text);
    if (ord >= 0) {
      currentOrd = ord;
      scratch.copyBytes(text);
      return SeekStatus.FOUND;
    } else {
      currentOrd = -ord-1;
      if (currentOrd == values.getValueCount()) {
        return SeekStatus.END;
      } else {
        // TODO: hmm can we avoid this "extra" lookup?:
        scratch.copyBytes(values.lookupOrd(currentOrd));
        return SeekStatus.NOT_FOUND;
      }
    }
  }

  @Override
  public boolean seekExact(BytesRef text) throws IOException {
    long ord = values.lookupTerm(text);
    if (ord >= 0) {
      currentOrd = ord;
      scratch.copyBytes(text);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void seekExact(long ord) throws IOException {
    assert ord >= 0 && ord < values.getValueCount();
    currentOrd = (int) ord;
    scratch.copyBytes(values.lookupOrd(currentOrd));
  }

  @Override
  public BytesRef next() throws IOException {
    currentOrd++;
    if (currentOrd >= values.getValueCount()) {
      return null;
    }
    scratch.copyBytes(values.lookupOrd(currentOrd));
    return scratch.get();
  }

  @Override
  public BytesRef term() throws IOException {
    return scratch.get();
  }

  @Override
  public long ord() throws IOException {
    return currentOrd;
  }

  @Override
  public int docFreq() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long totalTermFreq() {
    return -1;
  }

  @Override
  public DocsEnum docs(Bits liveDocs, DocsEnum reuse, int flags) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public DocsAndPositionsEnum docsAndPositions(Bits liveDocs, DocsAndPositionsEnum reuse, int flags) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void seekExact(BytesRef term, TermState state) throws IOException {
    assert state != null && state instanceof OrdTermState;
    this.seekExact(((OrdTermState)state).ord);
  }

  @Override
  public TermState termState() throws IOException {
    OrdTermState state = new OrdTermState();
    state.ord = currentOrd;
    return state;
  }
}

