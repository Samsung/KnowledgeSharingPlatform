package org.apache.lucene.queryparser.flexible.core.nodes;

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

import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;

/**
 * A {@link FuzzyQueryNode} represents a element that contains
 * field/text/similarity tuple
 */
public class FuzzyQueryNode extends FieldQueryNode {

  private float similarity;

  private int prefixLength;

  /**
   * @param field
   *          Name of the field query will use.
   * @param termStr
   *          Term token to use for building term for the query
   */
  /**
   * @param field
   *          - Field name
   * @param term
   *          - Value
   * @param minSimilarity
   *          - similarity value
   * @param begin
   *          - position in the query string
   * @param end
   *          - position in the query string
   */
  public FuzzyQueryNode(CharSequence field, CharSequence term,
      float minSimilarity, int begin, int end) {
    super(field, term, begin, end);
    this.similarity = minSimilarity;
    setLeaf(true);
  }

  public void setPrefixLength(int prefixLength) {
    this.prefixLength = prefixLength;
  }

  public int getPrefixLength() {
    return this.prefixLength;
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    if (isDefaultField(this.field)) {
      return getTermEscaped(escaper) + "~" + this.similarity;
    } else {
      return this.field + ":" + getTermEscaped(escaper) + "~" + this.similarity;
    }
  }

  @Override
  public String toString() {
    return "<fuzzy field='" + this.field + "' similarity='" + this.similarity
        + "' term='" + this.text + "'/>";
  }

  public void setSimilarity(float similarity) {
    this.similarity = similarity;
  }

  @Override
  public FuzzyQueryNode cloneTree() throws CloneNotSupportedException {
    FuzzyQueryNode clone = (FuzzyQueryNode) super.cloneTree();

    clone.similarity = this.similarity;

    return clone;
  }

  /**
   * @return the similarity
   */
  public float getSimilarity() {
    return this.similarity;
  }
}
