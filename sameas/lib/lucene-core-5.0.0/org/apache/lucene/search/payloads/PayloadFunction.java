package org.apache.lucene.search.payloads;
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

import org.apache.lucene.search.Explanation;

/**
 * An abstract class that defines a way for Payload*Query instances to transform
 * the cumulative effects of payload scores for a document.
 * 
 * @see org.apache.lucene.search.payloads.PayloadTermQuery for more information
 * 
 * @lucene.experimental This class and its derivations are experimental and subject to
 *               change
 * 
 **/
public abstract class PayloadFunction {

  /**
   * Calculate the score up to this point for this doc and field
   * @param docId The current doc
   * @param field The field
   * @param start The start position of the matching Span
   * @param end The end position of the matching Span
   * @param numPayloadsSeen The number of payloads seen so far
   * @param currentScore The current score so far
   * @param currentPayloadScore The score for the current payload
   * @return The new current Score
   *
   * @see org.apache.lucene.search.spans.Spans
   */
  public abstract float currentScore(int docId, String field, int start, int end, int numPayloadsSeen, float currentScore, float currentPayloadScore);

  /**
   * Calculate the final score for all the payloads seen so far for this doc/field
   * @param docId The current doc
   * @param field The current field
   * @param numPayloadsSeen The total number of payloads seen on this document
   * @param payloadScore The raw score for those payloads
   * @return The final score for the payloads
   */
  public abstract float docScore(int docId, String field, int numPayloadsSeen, float payloadScore);
  
  public Explanation explain(int docId, String field, int numPayloadsSeen, float payloadScore){
    Explanation result = new Explanation();
    result.setDescription(getClass().getSimpleName() + ".docScore()");
    result.setValue(docScore(docId, field, numPayloadsSeen, payloadScore));
    return result;
  };
  
  @Override
  public abstract int hashCode();
  
  @Override
  public abstract boolean equals(Object o);

}
