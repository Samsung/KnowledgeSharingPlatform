package org.apache.lucene.search.spans;
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


import org.apache.lucene.util.ToStringUtils;

import java.io.IOException;


/**
 * Checks to see if the {@link #getMatch()} lies between a start and end position
 *
 * @see org.apache.lucene.search.spans.SpanFirstQuery for a derivation that is optimized for the case where start position is 0
 */
public class SpanPositionRangeQuery extends SpanPositionCheckQuery {
  protected int start = 0;
  protected int end;

  public SpanPositionRangeQuery(SpanQuery match, int start, int end) {
    super(match);
    this.start = start;
    this.end = end;
  }


  @Override
  protected AcceptStatus acceptPosition(Spans spans) throws IOException {
    assert spans.start() != spans.end();
    if (spans.start() >= end)
      return AcceptStatus.NO_AND_ADVANCE;
    else if (spans.start() >= start && spans.end() <= end)
      return AcceptStatus.YES;
    else
      return AcceptStatus.NO;
  }


  /**
   * @return The minimum position permitted in a match
   */
  public int getStart() {
    return start;
  }

  /**
   * @return the maximum end position permitted in a match.
   */
  public int getEnd() {
    return end;
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("spanPosRange(");
    buffer.append(match.toString(field));
    buffer.append(", ").append(start).append(", ");
    buffer.append(end);
    buffer.append(")");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  @Override
  public SpanPositionRangeQuery clone() {
    SpanPositionRangeQuery result = new SpanPositionRangeQuery((SpanQuery) match.clone(), start, end);
    result.setBoost(getBoost());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SpanPositionRangeQuery)) return false;

    SpanPositionRangeQuery other = (SpanPositionRangeQuery)o;
    return this.end == other.end && this.start == other.start
         && this.match.equals(other.match)
         && this.getBoost() == other.getBoost();
  }

  @Override
  public int hashCode() {
    int h = match.hashCode();
    h ^= (h << 8) | (h >>> 25);  // reversible
    h ^= Float.floatToRawIntBits(getBoost()) ^ end ^ start;
    return h;
  }

}