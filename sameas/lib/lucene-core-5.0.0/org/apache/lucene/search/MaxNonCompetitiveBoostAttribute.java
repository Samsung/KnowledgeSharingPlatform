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
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.index.Terms; // javadocs only

/** Add this {@link Attribute} to a fresh {@link AttributeSource} before calling
 * {@link MultiTermQuery#getTermsEnum(Terms,AttributeSource)}.
 * {@link FuzzyQuery} is using this to control its internal behaviour
 * to only return competitive terms.
 * <p><b>Please note:</b> This attribute is intended to be added by the {@link MultiTermQuery.RewriteMethod}
 * to an empty {@link AttributeSource} that is shared for all segments
 * during query rewrite. This attribute source is passed to all segment enums
 * on {@link MultiTermQuery#getTermsEnum(Terms,AttributeSource)}.
 * {@link TopTermsRewrite} uses this attribute to
 * inform all enums about the current boost, that is not competitive.
 * @lucene.internal
 */
public interface MaxNonCompetitiveBoostAttribute extends Attribute {
  /** This is the maximum boost that would not be competitive. */
  public void setMaxNonCompetitiveBoost(float maxNonCompetitiveBoost);
  /** This is the maximum boost that would not be competitive. Default is negative infinity, which means every term is competitive. */
  public float getMaxNonCompetitiveBoost();
  /** This is the term or <code>null</code> of the term that triggered the boost change. */
  public void setCompetitiveTerm(BytesRef competitiveTerm);
  /** This is the term or <code>null</code> of the term that triggered the boost change. Default is <code>null</code>, which means every term is competitoive. */
  public BytesRef getCompetitiveTerm();
}
