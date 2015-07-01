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

import org.apache.lucene.util.BytesRef;

/**
 * Maintains a {@link IndexReader} {@link TermState} view over
 * {@link IndexReader} instances containing a single term. The
 * {@link TermContext} doesn't track if the given {@link TermState}
 * objects are valid, neither if the {@link TermState} instances refer to the
 * same terms in the associated readers.
 * 
 * @lucene.experimental
 */
public final class TermContext {

  /** Holds the {@link IndexReaderContext} of the top-level
   *  {@link IndexReader}, used internally only for
   *  asserting.
   *
   *  @lucene.internal */
  public final IndexReaderContext topReaderContext;
  private final TermState[] states;
  private int docFreq;
  private long totalTermFreq;

  //public static boolean DEBUG = BlockTreeTermsWriter.DEBUG;

  /**
   * Creates an empty {@link TermContext} from a {@link IndexReaderContext}
   */
  public TermContext(IndexReaderContext context) {
    assert context != null && context.isTopLevel;
    topReaderContext = context;
    docFreq = 0;
    final int len;
    if (context.leaves() == null) {
      len = 1;
    } else {
      len = context.leaves().size();
    }
    states = new TermState[len];
  }
  
  /**
   * Creates a {@link TermContext} with an initial {@link TermState},
   * {@link IndexReader} pair.
   */
  public TermContext(IndexReaderContext context, TermState state, int ord, int docFreq, long totalTermFreq) {
    this(context);
    register(state, ord, docFreq, totalTermFreq);
  }

  /**
   * Creates a {@link TermContext} from a top-level {@link IndexReaderContext} and the
   * given {@link Term}. This method will lookup the given term in all context's leaf readers 
   * and register each of the readers containing the term in the returned {@link TermContext}
   * using the leaf reader's ordinal.
   * <p>
   * Note: the given context must be a top-level context.
   */
  public static TermContext build(IndexReaderContext context, Term term)
      throws IOException {
    assert context != null && context.isTopLevel;
    final String field = term.field();
    final BytesRef bytes = term.bytes();
    final TermContext perReaderTermState = new TermContext(context);
    //if (DEBUG) System.out.println("prts.build term=" + term);
    for (final LeafReaderContext ctx : context.leaves()) {
      //if (DEBUG) System.out.println("  r=" + leaves[i].reader);
      final Terms terms = ctx.reader().terms(field);
      if (terms != null) {
        final TermsEnum termsEnum = terms.iterator(null);
        if (termsEnum.seekExact(bytes)) { 
          final TermState termState = termsEnum.termState();
          //if (DEBUG) System.out.println("    found");
          perReaderTermState.register(termState, ctx.ord, termsEnum.docFreq(), termsEnum.totalTermFreq());
        }
      }
    }
    return perReaderTermState;
  }

  /**
   * Clears the {@link TermContext} internal state and removes all
   * registered {@link TermState}s
   */
  public void clear() {
    docFreq = 0;
    Arrays.fill(states, null);
  }

  /**
   * Registers and associates a {@link TermState} with an leaf ordinal. The leaf ordinal
   * should be derived from a {@link IndexReaderContext}'s leaf ord.
   */
  public void register(TermState state, final int ord, final int docFreq, final long totalTermFreq) {
    assert state != null : "state must not be null";
    assert ord >= 0 && ord < states.length;
    assert states[ord] == null : "state for ord: " + ord
        + " already registered";
    this.docFreq += docFreq;
    if (this.totalTermFreq >= 0 && totalTermFreq >= 0)
      this.totalTermFreq += totalTermFreq;
    else
      this.totalTermFreq = -1;
    states[ord] = state;
  }

  /**
   * Returns the {@link TermState} for an leaf ordinal or <code>null</code> if no
   * {@link TermState} for the ordinal was registered.
   * 
   * @param ord
   *          the readers leaf ordinal to get the {@link TermState} for.
   * @return the {@link TermState} for the given readers ord or <code>null</code> if no
   *         {@link TermState} for the reader was registered
   */
  public TermState get(int ord) {
    assert ord >= 0 && ord < states.length;
    return states[ord];
  }

  /**
   *  Returns the accumulated document frequency of all {@link TermState}
   *         instances passed to {@link #register(TermState, int, int, long)}.
   * @return the accumulated document frequency of all {@link TermState}
   *         instances passed to {@link #register(TermState, int, int, long)}.
   */
  public int docFreq() {
    return docFreq;
  }
  
  /**
   *  Returns the accumulated term frequency of all {@link TermState}
   *         instances passed to {@link #register(TermState, int, int, long)}.
   * @return the accumulated term frequency of all {@link TermState}
   *         instances passed to {@link #register(TermState, int, int, long)}.
   */
  public long totalTermFreq() {
    return totalTermFreq;
  }
  
  /** expert: only available for queries that want to lie about docfreq
   * @lucene.internal */
  public void setDocFreq(int docFreq) {
    this.docFreq = docFreq;
  }
}