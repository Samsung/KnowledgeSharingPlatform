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
import java.util.Collection;
import java.util.Collections;

/** A Scorer for queries with a required subscorer
 * and an excluding (prohibited) sub DocIdSetIterator.
 * <br>
 * This <code>Scorer</code> implements {@link Scorer#advance(int)},
 * and it uses the skipTo() on the given scorers.
 */
class ReqExclScorer extends Scorer {
  private Scorer reqScorer;
  private DocIdSetIterator exclDisi;
  private int doc = -1;

  /** Construct a <code>ReqExclScorer</code>.
   * @param reqScorer The scorer that must match, except where
   * @param exclDisi indicates exclusion.
   */
  public ReqExclScorer(Scorer reqScorer, DocIdSetIterator exclDisi) {
    super(reqScorer.weight);
    this.reqScorer = reqScorer;
    this.exclDisi = exclDisi;
  }

  @Override
  public int nextDoc() throws IOException {
    if (reqScorer == null) {
      return doc;
    }
    doc = reqScorer.nextDoc();
    if (doc == NO_MORE_DOCS) {
      reqScorer = null; // exhausted, nothing left
      return doc;
    }
    if (exclDisi == null) {
      return doc;
    }
    return doc = toNonExcluded();
  }
  
  /** Advance to non excluded doc.
   * <br>On entry:
   * <ul>
   * <li>reqScorer != null,
   * <li>exclScorer != null,
   * <li>reqScorer was advanced once via next() or skipTo()
   *      and reqScorer.doc() may still be excluded.
   * </ul>
   * Advances reqScorer a non excluded required doc, if any.
   * @return true iff there is a non excluded required doc.
   */
  private int toNonExcluded() throws IOException {
    int exclDoc = exclDisi.docID();
    int reqDoc = reqScorer.docID(); // may be excluded
    do {  
      if (reqDoc < exclDoc) {
        return reqDoc; // reqScorer advanced to before exclScorer, ie. not excluded
      } else if (reqDoc > exclDoc) {
        exclDoc = exclDisi.advance(reqDoc);
        if (exclDoc == NO_MORE_DOCS) {
          exclDisi = null; // exhausted, no more exclusions
          return reqDoc;
        }
        if (exclDoc > reqDoc) {
          return reqDoc; // not excluded
        }
      }
    } while ((reqDoc = reqScorer.nextDoc()) != NO_MORE_DOCS);
    reqScorer = null; // exhausted, nothing left
    return NO_MORE_DOCS;
  }

  @Override
  public int docID() {
    return doc;
  }

  /** Returns the score of the current document matching the query.
   * Initially invalid, until {@link #nextDoc()} is called the first time.
   * @return The score of the required scorer.
   */
  @Override
  public float score() throws IOException {
    return reqScorer.score(); // reqScorer may be null when next() or skipTo() already return false
  }
  
  @Override
  public int freq() throws IOException {
    return reqScorer.freq();
  }

  @Override
  public Collection<ChildScorer> getChildren() {
    return Collections.singleton(new ChildScorer(reqScorer, "MUST"));
  }

  @Override
  public int advance(int target) throws IOException {
    if (reqScorer == null) {
      return doc = NO_MORE_DOCS;
    }
    if (exclDisi == null) {
      return doc = reqScorer.advance(target);
    }
    if (reqScorer.advance(target) == NO_MORE_DOCS) {
      reqScorer = null;
      return doc = NO_MORE_DOCS;
    }
    return doc = toNonExcluded();
  }

  @Override
  public long cost() {
    return reqScorer.cost();
  }
}
