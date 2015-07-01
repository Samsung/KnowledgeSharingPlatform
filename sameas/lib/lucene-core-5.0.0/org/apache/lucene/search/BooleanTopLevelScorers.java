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

import org.apache.lucene.search.Scorer.ChildScorer;

/** Internal document-at-a-time scorers used to deal with stupid coord() computation */
class BooleanTopLevelScorers {
  
  /** 
   * Used when there is more than one scorer in a query, but a segment
   * only had one non-null scorer. This just wraps that scorer directly
   * to factor in coord().
   */
  static class BoostedScorer extends FilterScorer {
    private final float boost;
    
    BoostedScorer(Scorer in, float boost) {
      super(in);
      this.boost = boost;
    }

    @Override
    public float score() throws IOException {
      return in.score() * boost;
    }

    @Override
    public Collection<ChildScorer> getChildren() {
      return Collections.singleton(new ChildScorer(in, "BOOSTED"));
    }
  }
  
  /** 
   * Used when there are both mandatory and optional clauses, but minShouldMatch
   * dictates that some of the optional clauses must match. The query is a conjunction,
   * but must compute coord based on how many optional subscorers matched (freq).
   */
  static class CoordinatingConjunctionScorer extends ConjunctionScorer {
    private final float coords[];
    private final int reqCount;
    private final Scorer req;
    private final Scorer opt;
    
    CoordinatingConjunctionScorer(Weight weight, float coords[], Scorer req, int reqCount, Scorer opt) {
      super(weight, new Scorer[] { req, opt });
      this.coords = coords;
      this.req = req;
      this.reqCount = reqCount;
      this.opt = opt;
    }
    
    @Override
    public float score() throws IOException {
      return (req.score() + opt.score()) * coords[reqCount + opt.freq()];
    }
  }
  
  /** 
   * Used when there are mandatory clauses with one optional clause: we compute
   * coord based on whether the optional clause matched or not.
   */
  static class ReqSingleOptScorer extends ReqOptSumScorer {
    // coord factor if just the required part matches
    private final float coordReq;
    // coord factor if both required and optional part matches 
    private final float coordBoth;
    
    public ReqSingleOptScorer(Scorer reqScorer, Scorer optScorer, float coordReq, float coordBoth) {
      super(reqScorer, optScorer);
      this.coordReq = coordReq;
      this.coordBoth = coordBoth;
    }
    
    @Override
    public float score() throws IOException {
      int curDoc = reqScorer.docID();
      float reqScore = reqScorer.score();
      if (optScorer == null) {
        return reqScore * coordReq;
      }
      
      int optScorerDoc = optScorer.docID();
      if (optScorerDoc < curDoc && (optScorerDoc = optScorer.advance(curDoc)) == NO_MORE_DOCS) {
        optScorer = null;
        return reqScore * coordReq;
      }
      
      return optScorerDoc == curDoc ? (reqScore + optScorer.score()) * coordBoth : reqScore * coordReq;
    }
  }

  /** 
   * Used when there are mandatory clauses with optional clauses: we compute
   * coord based on how many optional subscorers matched (freq).
   */
  static class ReqMultiOptScorer extends ReqOptSumScorer {
    private final int requiredCount;
    private final float coords[];
    
    public ReqMultiOptScorer(Scorer reqScorer, Scorer optScorer, int requiredCount, float coords[]) {
      super(reqScorer, optScorer);
      this.requiredCount = requiredCount;
      this.coords = coords;
    }
    
    @Override
    public float score() throws IOException {
      int curDoc = reqScorer.docID();
      float reqScore = reqScorer.score();
      if (optScorer == null) {
        return reqScore * coords[requiredCount];
      }
      
      int optScorerDoc = optScorer.docID();
      if (optScorerDoc < curDoc && (optScorerDoc = optScorer.advance(curDoc)) == NO_MORE_DOCS) {
        optScorer = null;
        return reqScore * coords[requiredCount];
      }
      
      return optScorerDoc == curDoc ? (reqScore + optScorer.score()) * coords[requiredCount + optScorer.freq()] : reqScore * coords[requiredCount];
    }
  }
}
