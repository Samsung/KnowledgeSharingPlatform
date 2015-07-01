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
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;

/**
 * Base rewrite method for collecting only the top terms
 * via a priority queue.
 * @lucene.internal Only public to be accessible by spans package.
 */
public abstract class TopTermsRewrite<Q extends Query> extends TermCollectingRewrite<Q> {

  private final int size;
  
  /** 
   * Create a TopTermsBooleanQueryRewrite for 
   * at most <code>size</code> terms.
   * <p>
   * NOTE: if {@link BooleanQuery#getMaxClauseCount} is smaller than 
   * <code>size</code>, then it will be used instead. 
   */
  public TopTermsRewrite(int size) {
    this.size = size;
  }
  
  /** return the maximum priority queue size */
  public int getSize() {
    return size;
  }
  
  /** return the maximum size of the priority queue (for boolean rewrites this is BooleanQuery#getMaxClauseCount). */
  protected abstract int getMaxSize();
  
  @Override
  public final Q rewrite(final IndexReader reader, final MultiTermQuery query) throws IOException {
    final int maxSize = Math.min(size, getMaxSize());
    final PriorityQueue<ScoreTerm> stQueue = new PriorityQueue<>();
    collectTerms(reader, query, new TermCollector() {
      private final MaxNonCompetitiveBoostAttribute maxBoostAtt =
        attributes.addAttribute(MaxNonCompetitiveBoostAttribute.class);
      
      private final Map<BytesRef,ScoreTerm> visitedTerms = new HashMap<>();
      
      private TermsEnum termsEnum;
      private BoostAttribute boostAtt;        
      private ScoreTerm st;
      
      @Override
      public void setNextEnum(TermsEnum termsEnum) {
        this.termsEnum = termsEnum;
        
        assert compareToLastTerm(null);

        // lazy init the initial ScoreTerm because comparator is not known on ctor:
        if (st == null)
          st = new ScoreTerm(new TermContext(topReaderContext));
        boostAtt = termsEnum.attributes().addAttribute(BoostAttribute.class);
      }
    
      // for assert:
      private BytesRefBuilder lastTerm;
      private boolean compareToLastTerm(BytesRef t) {
        if (lastTerm == null && t != null) {
          lastTerm = new BytesRefBuilder();
          lastTerm.append(t);
        } else if (t == null) {
          lastTerm = null;
        } else {
          assert lastTerm.get().compareTo(t) < 0: "lastTerm=" + lastTerm + " t=" + t;
          lastTerm.copyBytes(t);
        }
        return true;
      }
  
      @Override
      public boolean collect(BytesRef bytes) throws IOException {
        final float boost = boostAtt.getBoost();

        // make sure within a single seg we always collect
        // terms in order
        assert compareToLastTerm(bytes);

        //System.out.println("TTR.collect term=" + bytes.utf8ToString() + " boost=" + boost + " ord=" + readerContext.ord);
        // ignore uncompetitive hits
        if (stQueue.size() == maxSize) {
          final ScoreTerm t = stQueue.peek();
          if (boost < t.boost)
            return true;
          if (boost == t.boost && bytes.compareTo(t.bytes.get()) > 0)
            return true;
        }
        ScoreTerm t = visitedTerms.get(bytes);
        final TermState state = termsEnum.termState();
        assert state != null;
        if (t != null) {
          // if the term is already in the PQ, only update docFreq of term in PQ
          assert t.boost == boost : "boost should be equal in all segment TermsEnums";
          t.termState.register(state, readerContext.ord, termsEnum.docFreq(), termsEnum.totalTermFreq());
        } else {
          // add new entry in PQ, we must clone the term, else it may get overwritten!
          st.bytes.copyBytes(bytes);
          st.boost = boost;
          visitedTerms.put(st.bytes.get(), st);
          assert st.termState.docFreq() == 0;
          st.termState.register(state, readerContext.ord, termsEnum.docFreq(), termsEnum.totalTermFreq());
          stQueue.offer(st);
          // possibly drop entries from queue
          if (stQueue.size() > maxSize) {
            st = stQueue.poll();
            visitedTerms.remove(st.bytes.get());
            st.termState.clear(); // reset the termstate! 
          } else {
            st = new ScoreTerm(new TermContext(topReaderContext));
          }
          assert stQueue.size() <= maxSize : "the PQ size must be limited to maxSize";
          // set maxBoostAtt with values to help FuzzyTermsEnum to optimize
          if (stQueue.size() == maxSize) {
            t = stQueue.peek();
            maxBoostAtt.setMaxNonCompetitiveBoost(t.boost);
            maxBoostAtt.setCompetitiveTerm(t.bytes.get());
          }
        }
       
        return true;
      }
    });
    
    final Q q = getTopLevelQuery();
    final ScoreTerm[] scoreTerms = stQueue.toArray(new ScoreTerm[stQueue.size()]);
    ArrayUtil.timSort(scoreTerms, scoreTermSortByTermComp);
    
    for (final ScoreTerm st : scoreTerms) {
      final Term term = new Term(query.field, st.bytes.toBytesRef());
      assert reader.docFreq(term) == st.termState.docFreq() : "reader DF is " + reader.docFreq(term) + " vs " + st.termState.docFreq() + " term=" + term;
      addClause(q, term, st.termState.docFreq(), query.getBoost() * st.boost, st.termState); // add to query
    }
    return q;
  }

  @Override
  public int hashCode() {
    return 31 * size;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final TopTermsRewrite<?> other = (TopTermsRewrite<?>) obj;
    if (size != other.size) return false;
    return true;
  }
  
  private static final Comparator<ScoreTerm> scoreTermSortByTermComp = 
    new Comparator<ScoreTerm>() {
      @Override
      public int compare(ScoreTerm st1, ScoreTerm st2) {
        return st1.bytes.get().compareTo(st2.bytes.get());
      }
    };

  static final class ScoreTerm implements Comparable<ScoreTerm> {
    public final BytesRefBuilder bytes = new BytesRefBuilder();
    public float boost;
    public final TermContext termState;
    public ScoreTerm(TermContext termState) {
      this.termState = termState;
    }
    
    @Override
    public int compareTo(ScoreTerm other) {
      if (this.boost == other.boost)
        return other.bytes.get().compareTo(this.bytes.get());
      else
        return Float.compare(this.boost, other.boost);
    }
  }
}
