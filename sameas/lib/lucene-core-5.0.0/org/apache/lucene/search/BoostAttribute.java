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

import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeSource; // javadocs only
import org.apache.lucene.index.TermsEnum; // javadocs only
import org.apache.lucene.index.Terms; // javadocs only

/** Add this {@link Attribute} to a {@link TermsEnum} returned by {@link MultiTermQuery#getTermsEnum(Terms,AttributeSource)}
 * and update the boost on each returned term. This enables to control the boost factor
 * for each matching term in {@link MultiTermQuery#SCORING_BOOLEAN_QUERY_REWRITE} or
 * {@link TopTermsRewrite} mode.
 * {@link FuzzyQuery} is using this to take the edit distance into account.
 * <p><b>Please note:</b> This attribute is intended to be added only by the TermsEnum
 * to itself in its constructor and consumed by the {@link MultiTermQuery.RewriteMethod}.
 * @lucene.internal
 */
public interface BoostAttribute extends Attribute {
  /** Sets the boost in this attribute */
  public void setBoost(float boost);
  /** Retrieves the boost, default is {@code 1.0f}. */
  public float getBoost();
}
