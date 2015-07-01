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

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryparser.flexible.core.QueryNodeError;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;

/**
 * A {@link ModifierQueryNode} indicates the modifier value (+,-,?,NONE) for
 * each term on the query string. For example "+t1 -t2 t3" will have a tree of:
 * <blockquote>
 * &lt;BooleanQueryNode&gt; &lt;ModifierQueryNode modifier="MOD_REQ"&gt; &lt;t1/&gt;
 * &lt;/ModifierQueryNode&gt; &lt;ModifierQueryNode modifier="MOD_NOT"&gt; &lt;t2/&gt;
 * &lt;/ModifierQueryNode&gt; &lt;t3/&gt; &lt;/BooleanQueryNode&gt;
 * </blockquote>
 */
public class ModifierQueryNode extends QueryNodeImpl {

  /**
   * Modifier type: such as required (REQ), prohibited (NOT)
   */
  public enum Modifier {
    MOD_NONE, MOD_NOT, MOD_REQ;

    @Override
    public String toString() {
      switch (this) {
      case MOD_NONE:
        return "MOD_NONE";
      case MOD_NOT:
        return "MOD_NOT";
      case MOD_REQ:
        return "MOD_REQ";
      }
      // this code is never executed
      return "MOD_DEFAULT";
    }

    public String toDigitString() {
      switch (this) {
      case MOD_NONE:
        return "";
      case MOD_NOT:
        return "-";
      case MOD_REQ:
        return "+";
      }
      // this code is never executed
      return "";
    }

    public String toLargeString() {
      switch (this) {
      case MOD_NONE:
        return "";
      case MOD_NOT:
        return "NOT ";
      case MOD_REQ:
        return "+";
      }
      // this code is never executed
      return "";
    }
  }

  private Modifier modifier = Modifier.MOD_NONE;

  /**
   * Used to store the modifier value on the original query string
   * 
   * @param query
   *          - QueryNode subtree
   * @param mod
   *          - Modifier Value
   */
  public ModifierQueryNode(QueryNode query, Modifier mod) {
    if (query == null) {
      throw new QueryNodeError(new MessageImpl(
          QueryParserMessages.PARAMETER_VALUE_NOT_SUPPORTED, "query", "null"));
    }

    allocate();
    setLeaf(false);
    add(query);
    this.modifier = mod;
  }

  public QueryNode getChild() {
    return getChildren().get(0);
  }

  public Modifier getModifier() {
    return this.modifier;
  }

  @Override
  public String toString() {
    return "<modifier operation='" + this.modifier.toString() + "'>" + "\n"
        + getChild().toString() + "\n</modifier>";
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    if (getChild() == null)
      return "";

    String leftParenthensis = "";
    String rightParenthensis = "";

    if (getChild() != null && getChild() instanceof ModifierQueryNode) {
      leftParenthensis = "(";
      rightParenthensis = ")";
    }

    if (getChild() instanceof BooleanQueryNode) {
      return this.modifier.toLargeString() + leftParenthensis
          + getChild().toQueryString(escapeSyntaxParser) + rightParenthensis;
    } else {
      return this.modifier.toDigitString() + leftParenthensis
          + getChild().toQueryString(escapeSyntaxParser) + rightParenthensis;
    }
  }

  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    ModifierQueryNode clone = (ModifierQueryNode) super.cloneTree();

    clone.modifier = this.modifier;

    return clone;
  }

  public void setChild(QueryNode child) {
    List<QueryNode> list = new ArrayList<>();
    list.add(child);
    this.set(list);
  }

}
