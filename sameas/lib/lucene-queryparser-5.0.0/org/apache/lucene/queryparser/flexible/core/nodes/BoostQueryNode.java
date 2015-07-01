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

import java.util.List;

import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.core.QueryNodeError;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;

/**
 * A {@link BoostQueryNode} boosts the QueryNode tree which is under this node.
 * So, it must only and always have one child.
 * 
 * The boost value may vary from 0.0 to 1.0.
 * 
 */
public class BoostQueryNode extends QueryNodeImpl {

  private float value = 0;

  /**
   * Constructs a boost node
   * 
   * @param query
   *          the query to be boosted
   * @param value
   *          the boost value, it may vary from 0.0 to 1.0
   */
  public BoostQueryNode(QueryNode query, float value) {
    if (query == null) {
      throw new QueryNodeError(new MessageImpl(
          QueryParserMessages.NODE_ACTION_NOT_SUPPORTED, "query", "null"));
    }

    this.value = value;
    setLeaf(false);
    allocate();
    add(query);
  }

  /**
   * Returns the single child which this node boosts.
   * 
   * @return the single child which this node boosts
   */
  public QueryNode getChild() {
    List<QueryNode> children = getChildren();

    if (children == null || children.size() == 0) {
      return null;
    }

    return children.get(0);

  }

  /**
   * Returns the boost value. It may vary from 0.0 to 1.0.
   * 
   * @return the boost value
   */
  public float getValue() {
    return this.value;
  }

  /**
   * Returns the boost value parsed to a string.
   * 
   * @return the parsed value
   */
  private CharSequence getValueString() {
    Float f = Float.valueOf(this.value);
    if (f == f.longValue())
      return "" + f.longValue();
    else
      return "" + f;

  }

  @Override
  public String toString() {
    return "<boost value='" + getValueString() + "'>" + "\n"
        + getChild().toString() + "\n</boost>";
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChild() == null)
      return "";
    return getChild().toQueryString(escapeSyntaxParser) + "^"
        + getValueString();
  }

  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    BoostQueryNode clone = (BoostQueryNode) super.cloneTree();

    clone.value = this.value;

    return clone;
  }

}
