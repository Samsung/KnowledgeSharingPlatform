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

/**
 * A {@link Rescorer} that re-sorts according to a provided
 * Sort.
 */

public class SortRescorer extends Rescorer {

  private final Sort sort;

  /** Sole constructor. */
  public SortRescorer(Sort sort) {
    this.sort = sort;
  }

  @Override
  public TopDocs rescore(IndexSearcher searcher, TopDocs firstPassTopDocs, int topN) throws IOException {

    // Copy ScoreDoc[] and sort by ascending docID:
    ScoreDoc[] hits = firstPassTopDocs.scoreDocs.clone();
    Arrays.sort(hits,
                new Comparator<ScoreDoc>() {
                  @Override
                  public int compare(ScoreDoc a, ScoreDoc b) {
                    return a.doc - b.doc;
                  }
                });

    List<LeafReaderContext> leaves = searcher.getIndexReader().leaves();

    TopFieldCollector collector = TopFieldCollector.create(sort, topN, true, true, true);

    // Now merge sort docIDs from hits, with reader's leaves:
    int hitUpto = 0;
    int readerUpto = -1;
    int endDoc = 0;
    int docBase = 0;

    LeafCollector leafCollector = null;
    FakeScorer fakeScorer = new FakeScorer();

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
        leafCollector = collector.getLeafCollector(readerContext);
        leafCollector.setScorer(fakeScorer);
        docBase = readerContext.docBase;
      }

      fakeScorer.score = hit.score;
      fakeScorer.doc = docID - docBase;

      leafCollector.collect(fakeScorer.doc);

      hitUpto++;
    }

    return collector.topDocs();
  }

  @Override
  public Explanation explain(IndexSearcher searcher, Explanation firstPassExplanation, int docID) throws IOException {
    TopDocs oneHit = new TopDocs(1, new ScoreDoc[] {new ScoreDoc(docID, firstPassExplanation.getValue())});
    TopDocs hits = rescore(searcher, oneHit, 1);
    assert hits.totalHits == 1;

    // TODO: if we could ask the Sort to explain itself then
    // we wouldn't need the separate ExpressionRescorer...
    Explanation result = new Explanation(0.0f, "sort field values for sort=" + sort.toString());

    // Add first pass:
    Explanation first = new Explanation(firstPassExplanation.getValue(), "first pass score");
    first.addDetail(firstPassExplanation);
    result.addDetail(first);

    FieldDoc fieldDoc = (FieldDoc) hits.scoreDocs[0];

    // Add sort values:
    SortField[] sortFields = sort.getSort();
    for(int i=0;i<sortFields.length;i++) {
      result.addDetail(new Explanation(0.0f, "sort field " + sortFields[i].toString() + " value=" + fieldDoc.fields[i]));
    }

    return result;
  }
}
