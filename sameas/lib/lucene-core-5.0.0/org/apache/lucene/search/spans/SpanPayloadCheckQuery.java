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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


/**
 *   Only return those matches that have a specific payload at
 *  the given position.
 *<p/>
 * Do not use this with an SpanQuery that contains a {@link org.apache.lucene.search.spans.SpanNearQuery}.  Instead, use
 * {@link SpanNearPayloadCheckQuery} since it properly handles the fact that payloads
 * aren't ordered by {@link org.apache.lucene.search.spans.SpanNearQuery}.
 *
 **/
public class SpanPayloadCheckQuery extends SpanPositionCheckQuery{
  protected final Collection<byte[]> payloadToMatch;

  /**
   *
   * @param match The underlying {@link org.apache.lucene.search.spans.SpanQuery} to check
   * @param payloadToMatch The {@link java.util.Collection} of payloads to match
   */
  public SpanPayloadCheckQuery(SpanQuery match, Collection<byte[]> payloadToMatch) {
    super(match);
    if (match instanceof SpanNearQuery){
      throw new IllegalArgumentException("SpanNearQuery not allowed");
    }
    this.payloadToMatch = payloadToMatch;
  }

  @Override
  protected AcceptStatus acceptPosition(Spans spans) throws IOException {
    boolean result = spans.isPayloadAvailable();
    if (result == true){
      Collection<byte[]> candidate = spans.getPayload();
      if (candidate.size() == payloadToMatch.size()){
        //TODO: check the byte arrays are the same
        Iterator<byte[]> toMatchIter = payloadToMatch.iterator();
        //check each of the byte arrays, in order
        //hmm, can't rely on order here
        for (byte[] candBytes : candidate) {
          //if one is a mismatch, then return false
          if (Arrays.equals(candBytes, toMatchIter.next()) == false){
            return AcceptStatus.NO;
          }
        }
        //we've verified all the bytes
        return AcceptStatus.YES;
      } else {
        return AcceptStatus.NO;
      }
    }
    return AcceptStatus.YES;
  } 

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("spanPayCheck(");
    buffer.append(match.toString(field));
    buffer.append(", payloadRef: ");
    for (byte[] bytes : payloadToMatch) {
      ToStringUtils.byteArray(buffer, bytes);
      buffer.append(';');
    }
    buffer.append(")");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  @Override
  public SpanPayloadCheckQuery clone() {
    SpanPayloadCheckQuery result = new SpanPayloadCheckQuery((SpanQuery) match.clone(), payloadToMatch);
    result.setBoost(getBoost());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SpanPayloadCheckQuery)) return false;

    SpanPayloadCheckQuery other = (SpanPayloadCheckQuery)o;
    return this.payloadToMatch.equals(other.payloadToMatch)
         && this.match.equals(other.match)
         && this.getBoost() == other.getBoost();
  }

  @Override
  public int hashCode() {
    int h = match.hashCode();
    h ^= (h << 8) | (h >>> 25);  // reversible
    //TODO: is this right?
    h ^= payloadToMatch.hashCode();
    h ^= Float.floatToRawIntBits(getBoost()) ;
    return h;
  }
}