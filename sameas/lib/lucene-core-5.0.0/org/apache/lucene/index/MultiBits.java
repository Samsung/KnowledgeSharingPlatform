package org.apache.lucene.index;

import org.apache.lucene.util.Bits;

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

/**
 * Concatenates multiple Bits together, on every lookup.
 *
 * <p><b>NOTE</b>: This is very costly, as every lookup must
 * do a binary search to locate the right sub-reader.
 *
 * @lucene.experimental
 */
final class MultiBits implements Bits {
  private final Bits[] subs;

  // length is 1+subs.length (the last entry has the maxDoc):
  private final int[] starts;

  private final boolean defaultValue;

  public MultiBits(Bits[] subs, int[] starts, boolean defaultValue) {
    assert starts.length == 1+subs.length;
    this.subs = subs;
    this.starts = starts;
    this.defaultValue = defaultValue;
  }

  private boolean checkLength(int reader, int doc) {
    final int length = starts[1+reader]-starts[reader];
    assert doc - starts[reader] < length: "doc=" + doc + " reader=" + reader + " starts[reader]=" + starts[reader] + " length=" + length;
    return true;
  }

  @Override
  public boolean get(int doc) {
    final int reader = ReaderUtil.subIndex(doc, starts);
    assert reader != -1;
    final Bits bits = subs[reader];
    if (bits == null) {
      return defaultValue;
    } else {
      assert checkLength(reader, doc);
      return bits.get(doc-starts[reader]);
    }
  }
  
  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(subs.length + " subs: ");
    for(int i=0;i<subs.length;i++) {
      if (i != 0) {
        b.append("; ");
      }
      if (subs[i] == null) {
        b.append("s=" + starts[i] + " l=null");
      } else {
        b.append("s=" + starts[i] + " l=" + subs[i].length() + " b=" + subs[i]);
      }
    }
    b.append(" end=" + starts[subs.length]);
    return b.toString();
  }

  /**
   * Represents a sub-Bits from 
   * {@link MultiBits#getMatchingSub(org.apache.lucene.index.ReaderSlice) getMatchingSub()}.
   */
  public final static class SubResult {
    public boolean matches;
    public Bits result;
  }

  /**
   * Returns a sub-Bits matching the provided <code>slice</code>
   * <p>
   * Because <code>null</code> usually has a special meaning for
   * Bits (e.g. no deleted documents), you must check
   * {@link SubResult#matches} instead to ensure the sub was 
   * actually found.
   */
  public SubResult getMatchingSub(ReaderSlice slice) {
    int reader = ReaderUtil.subIndex(slice.start, starts);
    assert reader != -1;
    assert reader < subs.length: "slice=" + slice + " starts[-1]=" + starts[starts.length-1];
    final SubResult subResult = new SubResult();
    if (starts[reader] == slice.start && starts[1+reader] == slice.start+slice.length) {
      subResult.matches = true;
      subResult.result = subs[reader];
    } else {
      subResult.matches = false;
    }
    return subResult;
  }

  @Override
  public int length() {
    return starts[starts.length-1];
  }
}
