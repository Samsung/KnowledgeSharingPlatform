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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import org.apache.lucene.util.ArrayUtil;

/** Scorer for conjunctions, sets of queries, all of which are required. */
class ConjunctionScorer extends Scorer {
  protected int lastDoc = -1;
  protected final DocsAndFreqs[] docsAndFreqs;
  private final DocsAndFreqs lead;
  private final float coord;

  ConjunctionScorer(Weight weight, Scorer[] scorers) {
    this(weight, scorers, 1f);
  }
  
  ConjunctionScorer(Weight weight, Scorer[] scorers, float coord) {
    super(weight);
    this.coord = coord;
    this.docsAndFreqs = new DocsAndFreqs[scorers.length];
    for (int i = 0; i < scorers.length; i++) {
      docsAndFreqs[i] = new DocsAndFreqs(scorers[i]);
    }
    // Sort the array the first time to allow the least frequent DocsEnum to
    // lead the matching.
    ArrayUtil.timSort(docsAndFreqs, new Comparator<DocsAndFreqs>() {
      @Override
      public int compare(DocsAndFreqs o1, DocsAndFreqs o2) {
        return Long.compare(o1.cost, o2.cost);
      }
    });

    lead = docsAndFreqs[0]; // least frequent DocsEnum leads the intersection
  }

  private int doNext(int doc) throws IOException {
    for(;;) {
      // doc may already be NO_MORE_DOCS here, but we don't check explicitly
      // since all scorers should advance to NO_MORE_DOCS, match, then
      // return that value.
      advanceHead: for(;;) {
        for (int i = 1; i < docsAndFreqs.length; i++) {
          // invariant: docsAndFreqs[i].doc <= doc at this point.

          // docsAndFreqs[i].doc may already be equal to doc if we "broke advanceHead"
          // on the previous iteration and the advance on the lead scorer exactly matched.
          if (docsAndFreqs[i].doc < doc) {
            docsAndFreqs[i].doc = docsAndFreqs[i].scorer.advance(doc);

            if (docsAndFreqs[i].doc > doc) {
              // DocsEnum beyond the current doc - break and advance lead to the new highest doc.
              doc = docsAndFreqs[i].doc;
              break advanceHead;
            }
          }
        }
        // success - all DocsEnums are on the same doc
        return doc;
      }
      // advance head for next iteration
      doc = lead.doc = lead.scorer.advance(doc);
    }
  }

  @Override
  public int advance(int target) throws IOException {
    lead.doc = lead.scorer.advance(target);
    return lastDoc = doNext(lead.doc);
  }

  @Override
  public int docID() {
    return lastDoc;
  }

  @Override
  public int nextDoc() throws IOException {
    lead.doc = lead.scorer.nextDoc();
    return lastDoc = doNext(lead.doc);
  }

  @Override
  public float score() throws IOException {
    // TODO: sum into a double and cast to float if we ever send required clauses to BS1
    float sum = 0.0f;
    for (DocsAndFreqs docs : docsAndFreqs) {
      sum += docs.scorer.score();
    }
    return sum * coord;
  }
  
  @Override
  public int freq() {
    return docsAndFreqs.length;
  }

  @Override
  public long cost() {
    return lead.scorer.cost();
  }

  @Override
  public Collection<ChildScorer> getChildren() {
    ArrayList<ChildScorer> children = new ArrayList<>(docsAndFreqs.length);
    for (DocsAndFreqs docs : docsAndFreqs) {
      children.add(new ChildScorer(docs.scorer, "MUST"));
    }
    return children;
  }

  static final class DocsAndFreqs {
    final long cost;
    final Scorer scorer;
    int doc = -1;
   
    DocsAndFreqs(Scorer scorer) {
      this.scorer = scorer;
      this.cost = scorer.cost();
    }
  }
}
