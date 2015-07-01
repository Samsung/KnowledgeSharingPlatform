package org.apache.lucene.queryparser.flexible.standard.nodes;

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

import org.apache.lucene.queryparser.flexible.core.nodes.FieldableNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNodeImpl;
import org.apache.lucene.queryparser.flexible.core.nodes.TextableQueryNode;
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;
import org.apache.lucene.search.RegexpQuery;
import org.apache.lucene.util.BytesRef;

/**
 * A {@link RegexpQueryNode} represents {@link RegexpQuery} query Examples: /[a-z]|[0-9]/
 */
public class RegexpQueryNode extends QueryNodeImpl  implements TextableQueryNode,
FieldableNode {
  private CharSequence text;
  private CharSequence field;
  /**
   * @param field
   *          - field name
   * @param text
   *          - value that contains a regular expression
   * @param begin
   *          - position in the query string
   * @param end
   *          - position in the query string
   */
  public RegexpQueryNode(CharSequence field, CharSequence text, int begin,
      int end) {
    this.field = field;
    this.text = text.subSequence(begin, end);
  }

  public BytesRef textToBytesRef() {
    return new BytesRef(text);
  }

  @Override
  public String toString() {
    return "<regexp field='" + this.field + "' term='" + this.text + "'/>";
  }

  @Override
  public RegexpQueryNode cloneTree() throws CloneNotSupportedException {
    RegexpQueryNode clone = (RegexpQueryNode) super.cloneTree();
    clone.field = this.field;
    clone.text = this.text;
    return clone;
  }

  @Override
  public CharSequence getText() {
    return text;
  }

  @Override
  public void setText(CharSequence text) {
    this.text = text;
  }

  @Override
  public CharSequence getField() {
    return field;
  }
  
  public String getFieldAsString() {
    return field.toString();
  }

  @Override
  public void setField(CharSequence field) {
    this.field = field;
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    return isDefaultField(field)? "/"+text+"/": field + ":/" + text + "/";
  }

}
