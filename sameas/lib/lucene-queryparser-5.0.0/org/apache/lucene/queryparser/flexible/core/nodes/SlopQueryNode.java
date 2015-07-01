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

import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryparser.flexible.core.QueryNodeError;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;

/**
 * A {@link SlopQueryNode} represents phrase query with a slop.
 * 
 * From Lucene FAQ: Is there a way to use a proximity operator (like near or
 * within) with Lucene? There is a variable called slop that allows you to
 * perform NEAR/WITHIN-like queries. By default, slop is set to 0 so that only
 * exact phrases will match. When using TextParser you can use this syntax to
 * specify the slop: "doug cutting"~2 will find documents that contain
 * "doug cutting" as well as ones that contain "cutting doug".
 */
public class SlopQueryNode extends QueryNodeImpl implements FieldableNode {

  private int value = 0;

  /**
   * @param query
   *          - QueryNode Tree with the phrase
   * @param value
   *          - slop value
   */
  public SlopQueryNode(QueryNode query, int value) {
    if (query == null) {
      throw new QueryNodeError(new MessageImpl(
          QueryParserMessages.NODE_ACTION_NOT_SUPPORTED, "query", "null"));
    }

    this.value = value;
    setLeaf(false);
    allocate();
    add(query);
  }

  public QueryNode getChild() {
    return getChildren().get(0);
  }

  public int getValue() {
    return this.value;
  }

  private CharSequence getValueString() {
    Float f = Float.valueOf(this.value);
    if (f == f.longValue())
      return "" + f.longValue();
    else
      return "" + f;

  }

  @Override
  public String toString() {
    return "<slop value='" + getValueString() + "'>" + "\n"
        + getChild().toString() + "\n</slop>";
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChild() == null)
      return "";
    return getChild().toQueryString(escapeSyntaxParser) + "~"
        + getValueString();
  }

  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    SlopQueryNode clone = (SlopQueryNode) super.cloneTree();

    clone.value = this.value;

    return clone;
  }

  @Override
  public CharSequence getField() {
    QueryNode child = getChild();

    if (child instanceof FieldableNode) {
      return ((FieldableNode) child).getField();
    }

    return null;

  }

  @Override
  public void setField(CharSequence fieldName) {
    QueryNode child = getChild();

    if (child instanceof FieldableNode) {
      ((FieldableNode) child).setField(fieldName);
    }

  }

}
