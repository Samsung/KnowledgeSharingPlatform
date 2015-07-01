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
import java.util.Collection;

import org.apache.lucene.search.BooleanQuery.BooleanWeight;

/**
 * BulkSorer that is used for pure disjunctions: no MUST clauses and
 * minShouldMatch == 1. This scorer scores documents by batches of 2048 docs.
 */
final class BooleanScorer extends BulkScorer {

  static final int SHIFT = 11;
  static final int SIZE = 1 << SHIFT;
  static final int MASK = SIZE - 1;
  static final int SET_SIZE = 1 << (SHIFT - 6);
  static final int SET_MASK = SET_SIZE - 1;

  static class Bucket {
    double score;
    int freq;
  }

  final Bucket[] buckets = new Bucket[SIZE];
  // This is basically an inlined FixedBitSet... seems to help with bound checks
  final long[] matching = new long[SET_SIZE];

  final float[] coordFactors;
  final BulkScorer[] optionalScorers;
  final FakeScorer fakeScorer = new FakeScorer();

  boolean hasMatches;
  int max = 0;

  final class OrCollector implements LeafCollector {
    Scorer scorer;

    @Override
    public void setScorer(Scorer scorer) {
      this.scorer = scorer;
    }

    @Override
    public void collect(int doc) throws IOException {
      hasMatches = true;
      final int i = doc & MASK;
      final int idx = i >>> 6;
      matching[idx] |= 1L << i;
      final Bucket bucket = buckets[i];
      bucket.freq++;
      bucket.score += scorer.score();
    }
  }

  final OrCollector orCollector = new OrCollector();

  BooleanScorer(BooleanWeight weight, boolean disableCoord, int maxCoord, Collection<BulkScorer> optionalScorers) {
    for (int i = 0; i < buckets.length; i++) {
      buckets[i] = new Bucket();
    }
    this.optionalScorers = optionalScorers.toArray(new BulkScorer[0]);

    coordFactors = new float[optionalScorers.size() + 1];
    for (int i = 0; i < coordFactors.length; i++) {
      coordFactors[i] = disableCoord ? 1.0f : weight.coord(i, maxCoord);
    }
  }

  private void scoreDocument(LeafCollector collector, int base, int i) throws IOException {
    final Bucket bucket = buckets[i];
    fakeScorer.freq = bucket.freq;
    fakeScorer.score = (float) bucket.score * coordFactors[bucket.freq];
    final int doc = base | i;
    fakeScorer.doc = doc;
    collector.collect(doc);
    bucket.freq = 0;
    bucket.score = 0;
  }

  private void scoreMatches(LeafCollector collector, int base) throws IOException {
    long matching[] = this.matching;
    for (int idx = 0; idx < matching.length; idx++) {
      long bits = matching[idx];
      while (bits != 0L) {
        int ntz = Long.numberOfTrailingZeros(bits);
        int doc = idx << 6 | ntz;
        scoreDocument(collector, base, doc);
        bits ^= 1L << ntz;
      }
    }
  }

  private boolean collectMatches() throws IOException {
    boolean more = false;
    for (BulkScorer scorer : optionalScorers) {
      more |= scorer.score(orCollector, max);
    }
    return more;
  }

  private boolean scoreWindow(LeafCollector collector, int base, int max) throws IOException {
    this.max = Math.min(base + SIZE, max);
    hasMatches = false;
    boolean more = collectMatches();

    if (hasMatches) {
      scoreMatches(collector, base);
      Arrays.fill(matching, 0L);
    }

    return more;
  }

  @Override
  public boolean score(LeafCollector collector, int max) throws IOException {
    fakeScorer.doc = -1;
    collector.setScorer(fakeScorer);

    for (int docBase = this.max & ~MASK; docBase < max; docBase += SIZE) {
      if (scoreWindow(collector, docBase, max) == false) {
        return false;
      }
    }

    return true;
  }
}
