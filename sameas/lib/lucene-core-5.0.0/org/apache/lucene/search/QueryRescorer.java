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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.index.LeafReaderContext;

/** A {@link Rescorer} that uses a provided Query to assign
 *  scores to the first-pass hits.
 *
 * @lucene.experimental */
public abstract class QueryRescorer extends Rescorer {

  private final Query query;

  /** Sole constructor, passing the 2nd pass query to
   *  assign scores to the 1st pass hits.  */
  public QueryRescorer(Query query) {
    this.query = query;
  }

  /**
   * Implement this in a subclass to combine the first pass and
   * second pass scores.  If secondPassMatches is false then
   * the second pass query failed to match a hit from the
   * first pass query, and you should ignore the
   * secondPassScore.
   */
  protected abstract float combine(float firstPassScore, boolean secondPassMatches, float secondPassScore);

  @Override
  public TopDocs rescore(IndexSearcher searcher, TopDocs firstPassTopDocs, int topN) throws IOException {
    ScoreDoc[] hits = firstPassTopDocs.scoreDocs.clone();
    Arrays.sort(hits,
                new Comparator<ScoreDoc>() {
                  @Override
                  public int compare(ScoreDoc a, ScoreDoc b) {
                    return a.doc - b.doc;
                  }
                });

    List<LeafReaderContext> leaves = searcher.getIndexReader().leaves();

    Weight weight = searcher.createNormalizedWeight(query);

    // Now merge sort docIDs from hits, with reader's leaves:
    int hitUpto = 0;
    int readerUpto = -1;
    int endDoc = 0;
    int docBase = 0;
    Scorer scorer = null;

    while (hitUpto < hits.length) {
      ScoreDoc hit = hits[hitUpto];
      int docID = hit.doc;
      LeafReaderContext readerContext = null;
      while (docID >= endDoc) {
        readerUpto++;
        readerContext = leaves.get(readerUpto);
        endDoc = readerContext.docBase + readerContext.reader().maxDoc();
      }

      if (readerContext != null) {
        // We advanced to another segment:
        docBase = readerContext.docBase;
        scorer = weight.scorer(readerContext, null);
      }

      if(scorer != null) {
        int targetDoc = docID - docBase;
        int actualDoc = scorer.docID();
        if (actualDoc < targetDoc) {
          actualDoc = scorer.advance(targetDoc);
        }

        if (actualDoc == targetDoc) {
          // Query did match this doc:
          hit.score = combine(hit.score, true, scorer.score());
        } else {
          // Query did not match this doc:
          assert actualDoc > targetDoc;
          hit.score = combine(hit.score, false, 0.0f);
        }
      } else {
        // Query did not match this doc:
        hit.score = combine(hit.score, false, 0.0f);
      }

      hitUpto++;
    }

    // TODO: we should do a partial sort (of only topN)
    // instead, but typically the number of hits is
    // smallish:
    Arrays.sort(hits,
                new Comparator<ScoreDoc>() {
                  @Override
                  public int compare(ScoreDoc a, ScoreDoc b) {
                    // Sort by score descending, then docID ascending:
                    if (a.score > b.score) {
                      return -1;
                    } else if (a.score < b.score) {
                      return 1;
                    } else {
                      // This subtraction can't overflow int
                      // because docIDs are >= 0:
                      return a.doc - b.doc;
                    }
                  }
                });

    if (topN < hits.length) {
      ScoreDoc[] subset = new ScoreDoc[topN];
      System.arraycopy(hits, 0, subset, 0, topN);
      hits = subset;
    }

    return new TopDocs(firstPassTopDocs.totalHits, hits, hits[0].score);
  }

  @Override
  public Explanation explain(IndexSearcher searcher, Explanation firstPassExplanation, int docID) throws IOException {
    Explanation secondPassExplanation = searcher.explain(query, docID);

    Float secondPassScore = secondPassExplanation.isMatch() ? secondPassExplanation.getValue() : null;

    float score;
    if (secondPassScore == null) {
      score = combine(firstPassExplanation.getValue(), false, 0.0f);
    } else {
      score = combine(firstPassExplanation.getValue(), true,  secondPassScore.floatValue());
    }

    Explanation result = new Explanation(score, "combined first and second pass score using " + getClass());

    Explanation first = new Explanation(firstPassExplanation.getValue(), "first pass score");
    first.addDetail(firstPassExplanation);
    result.addDetail(first);

    Explanation second;
    if (secondPassScore == null) {
      second = new Explanation(0.0f, "no second pass score");
    } else {
      second = new Explanation(secondPassScore, "second pass score");
    }
    second.addDetail(secondPassExplanation);
    result.addDetail(second);

    return result;
  }

  /** Sugar API, calling {#rescore} using a simple linear
   *  combination of firstPassScore + weight * secondPassScore */
  public static TopDocs rescore(IndexSearcher searcher, TopDocs topDocs, Query query, final double weight, int topN) throws IOException {
    return new QueryRescorer(query) {
      @Override
      protected float combine(float firstPassScore, boolean secondPassMatches, float secondPassScore) {
        float score = firstPassScore;
        if (secondPassMatches) {
          score += weight * secondPassScore;
        }
        return score;
      }
    }.rescore(searcher, topDocs, topN);
  }
}
