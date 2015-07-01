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

import java.util.List;


/**
 * A {@link AnyQueryNode} represents an ANY operator performed on a list of
 * nodes.
 */
public class AnyQueryNode extends AndQueryNode {
  private CharSequence field = null;
  private int minimumMatchingmElements = 0;

  /**
   * @param clauses
   *          - the query nodes to be or'ed
   */
  public AnyQueryNode(List<QueryNode> clauses, CharSequence field,
      int minimumMatchingElements) {
    super(clauses);
    this.field = field;
    this.minimumMatchingmElements = minimumMatchingElements;

    if (clauses != null) {

      for (QueryNode clause : clauses) {

        if (clause instanceof FieldQueryNode) {

          if (clause instanceof QueryNodeImpl) {
            ((QueryNodeImpl) clause).toQueryStringIgnoreFields = true;
          }

          if (clause instanceof FieldableNode) {
            ((FieldableNode) clause).setField(field);
          }

        }
      }

    }

  }

  public int getMinimumMatchingElements() {
    return this.minimumMatchingmElements;
  }

  /**
   * returns null if the field was not specified
   * 
   * @return the field
   */
  public CharSequence getField() {
    return this.field;
  }

  /**
   * returns - null if the field was not specified
   * 
   * @return the field as a String
   */
  public String getFieldAsString() {
    if (this.field == null)
      return null;
    else
      return this.field.toString();
  }

  /**
   * @param field
   *          - the field to set
   */
  public void setField(CharSequence field) {
    this.field = field;
  }

  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    AnyQueryNode clone = (AnyQueryNode) super.cloneTree();

    clone.field = this.field;
    clone.minimumMatchingmElements = this.minimumMatchingmElements;

    return clone;
  }

  @Override
  public String toString() {
    if (getChildren() == null || getChildren().size() == 0)
      return "<any field='" + this.field + "'  matchelements="
          + this.minimumMatchingmElements + "/>";
    StringBuilder sb = new StringBuilder();
    sb.append("<any field='" + this.field + "'  matchelements="
        + this.minimumMatchingmElements + ">");
    for (QueryNode clause : getChildren()) {
      sb.append("\n");
      sb.append(clause.toString());
    }
    sb.append("\n</any>");
    return sb.toString();
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    String anySTR = "ANY " + this.minimumMatchingmElements;

    StringBuilder sb = new StringBuilder();
    if (getChildren() == null || getChildren().size() == 0) {
      // no childs case
    } else {
      String filler = "";
      for (QueryNode clause : getChildren()) {
        sb.append(filler).append(clause.toQueryString(escapeSyntaxParser));
        filler = " ";
      }
    }

    if (isDefaultField(this.field)) {
      return "( " + sb.toString() + " ) " + anySTR;
    } else {
      return this.field + ":(( " + sb.toString() + " ) " + anySTR + ")";
    }
  }

}
