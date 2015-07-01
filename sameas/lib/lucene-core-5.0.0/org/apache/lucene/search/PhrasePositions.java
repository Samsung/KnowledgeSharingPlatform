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
import org.apache.lucene.index.*;

/**
 * Position of a term in a document that takes into account the term offset within the phrase. 
 */
final class PhrasePositions {
  int doc;              // current doc
  int position;         // position in doc
  int count;            // remaining pos in this doc
  int offset;           // position in phrase
  final int ord;                                  // unique across all PhrasePositions instances
  final DocsAndPositionsEnum postings;            // stream of docs & positions
  PhrasePositions next;                           // used to make lists
  int rptGroup = -1; // >=0 indicates that this is a repeating PP
  int rptInd; // index in the rptGroup
  final Term[] terms; // for repetitions initialization 

  PhrasePositions(DocsAndPositionsEnum postings, int o, int ord, Term[] terms) {
    this.postings = postings;
    offset = o;
    this.ord = ord;
    this.terms = terms;
  }

  final boolean next() throws IOException {  // increments to next doc
    doc = postings.nextDoc();
    if (doc == DocIdSetIterator.NO_MORE_DOCS) {
      return false;
    }
    return true;
  }

  final boolean skipTo(int target) throws IOException {
    doc = postings.advance(target);
    if (doc == DocIdSetIterator.NO_MORE_DOCS) {
      return false;
    }
    return true;
  }

  final void firstPosition() throws IOException {
    count = postings.freq();  // read first pos
    nextPosition();
  }

  /**
   * Go to next location of this term current document, and set 
   * <code>position</code> as <code>location - offset</code>, so that a 
   * matching exact phrase is easily identified when all PhrasePositions 
   * have exactly the same <code>position</code>.
   */
  final boolean nextPosition() throws IOException {
    if (count-- > 0) {  // read subsequent pos's
      position = postings.nextPosition() - offset;
      return true;
    } else
      return false;
  }
  
  /** for debug purposes */
  @Override
  public String toString() {
    String s = "d:"+doc+" o:"+offset+" p:"+position+" c:"+count;
    if (rptGroup >=0 ) {
      s += " rpt:"+rptGroup+",i"+rptInd;
    }
    return s;
  }
}
