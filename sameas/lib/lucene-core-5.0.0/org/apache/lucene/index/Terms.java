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

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.automaton.CompiledAutomaton;

/**
 * Access to the terms in a specific field.  See {@link Fields}.
 * @lucene.experimental
 */

public abstract class Terms {

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected Terms() {
  }

  /** Returns an iterator that will step through all
   *  terms. This method will not return null.  If you have
   *  a previous TermsEnum, for example from a different
   *  field, you can pass it for possible reuse if the
   *  implementation can do so. */
  public abstract TermsEnum iterator(TermsEnum reuse) throws IOException;

  /** Returns a TermsEnum that iterates over all terms that
   *  are accepted by the provided {@link
   *  CompiledAutomaton}.  If the <code>startTerm</code> is
   *  provided then the returned enum will only accept terms
   *  {@code > startTerm}, but you still must call
   *  next() first to get to the first term.  Note that the
   *  provided <code>startTerm</code> must be accepted by
   *  the automaton.
   *
   * <p><b>NOTE</b>: the returned TermsEnum cannot
   * seek</p>. */
  public TermsEnum intersect(CompiledAutomaton compiled, final BytesRef startTerm) throws IOException {
    // TODO: eventually we could support seekCeil/Exact on
    // the returned enum, instead of only being able to seek
    // at the start
    if (compiled.type != CompiledAutomaton.AUTOMATON_TYPE.NORMAL) {
      throw new IllegalArgumentException("please use CompiledAutomaton.getTermsEnum instead");
    }
    if (startTerm == null) {
      return new AutomatonTermsEnum(iterator(null), compiled);
    } else {
      return new AutomatonTermsEnum(iterator(null), compiled) {
        @Override
        protected BytesRef nextSeekTerm(BytesRef term) throws IOException {
          if (term == null) {
            term = startTerm;
          }
          return super.nextSeekTerm(term);
        }
      };
    }
  }

  /** Returns the number of terms for this field, or -1 if this 
   *  measure isn't stored by the codec. Note that, just like 
   *  other term measures, this measure does not take deleted 
   *  documents into account. */
  public abstract long size() throws IOException;
  
  /** Returns the sum of {@link TermsEnum#totalTermFreq} for
   *  all terms in this field, or -1 if this measure isn't
   *  stored by the codec (or if this fields omits term freq
   *  and positions).  Note that, just like other term
   *  measures, this measure does not take deleted documents
   *  into account. */
  public abstract long getSumTotalTermFreq() throws IOException;

  /** Returns the sum of {@link TermsEnum#docFreq()} for
   *  all terms in this field, or -1 if this measure isn't
   *  stored by the codec.  Note that, just like other term
   *  measures, this measure does not take deleted documents
   *  into account. */
  public abstract long getSumDocFreq() throws IOException;

  /** Returns the number of documents that have at least one
   *  term for this field, or -1 if this measure isn't
   *  stored by the codec.  Note that, just like other term
   *  measures, this measure does not take deleted documents
   *  into account. */
  public abstract int getDocCount() throws IOException;

  /** Returns true if documents in this field store
   *  per-document term frequency ({@link DocsEnum#freq}). */
  public abstract boolean hasFreqs();

  /** Returns true if documents in this field store offsets. */
  public abstract boolean hasOffsets();
  
  /** Returns true if documents in this field store positions. */
  public abstract boolean hasPositions();
  
  /** Returns true if documents in this field store payloads. */
  public abstract boolean hasPayloads();

  /** Zero-length array of {@link Terms}. */
  public final static Terms[] EMPTY_ARRAY = new Terms[0];
  
  /** Returns the smallest term (in lexicographic order) in the field. 
   *  Note that, just like other term measures, this measure does not 
   *  take deleted documents into account.  This returns
   *  null when there are no terms. */
  public BytesRef getMin() throws IOException {
    return iterator(null).next();
  }

  /** Returns the largest term (in lexicographic order) in the field. 
   *  Note that, just like other term measures, this measure does not 
   *  take deleted documents into account.  This returns
   *  null when there are no terms. */
  @SuppressWarnings("fallthrough")
  public BytesRef getMax() throws IOException {
    long size = size();
    
    if (size == 0) {
      // empty: only possible from a FilteredTermsEnum...
      return null;
    } else if (size >= 0) {
      // try to seek-by-ord
      try {
        TermsEnum iterator = iterator(null);
        iterator.seekExact(size - 1);
        return iterator.term();
      } catch (UnsupportedOperationException e) {
        // ok
      }
    }
    
    // otherwise: binary search
    TermsEnum iterator = iterator(null);
    BytesRef v = iterator.next();
    if (v == null) {
      // empty: only possible from a FilteredTermsEnum...
      return v;
    }

    BytesRefBuilder scratch = new BytesRefBuilder();
    scratch.append((byte) 0);

    // Iterates over digits:
    while (true) {

      int low = 0;
      int high = 256;

      // Binary search current digit to find the highest
      // digit before END:
      while (low != high) {
        int mid = (low+high) >>> 1;
        scratch.setByteAt(scratch.length()-1, (byte) mid);
        if (iterator.seekCeil(scratch.get()) == TermsEnum.SeekStatus.END) {
          // Scratch was too high
          if (mid == 0) {
            scratch.setLength(scratch.length() - 1);
            return scratch.get();
          }
          high = mid;
        } else {
          // Scratch was too low; there is at least one term
          // still after it:
          if (low == mid) {
            break;
          }
          low = mid;
        }
      }

      // Recurse to next digit:
      scratch.setLength(scratch.length() + 1);
      scratch.grow(scratch.length());
    }
  }
  
  /** 
   * Expert: returns additional information about this Terms instance
   * for debugging purposes.
   */
  public Object getStats() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("impl=" + getClass().getSimpleName());
    sb.append(",size=" + size());
    sb.append(",docCount=" + getDocCount());
    sb.append(",sumTotalTermFreq=" + getSumTotalTermFreq());
    sb.append(",sumDocFreq=" + getSumDocFreq());
    return sb.toString();
  }
}
