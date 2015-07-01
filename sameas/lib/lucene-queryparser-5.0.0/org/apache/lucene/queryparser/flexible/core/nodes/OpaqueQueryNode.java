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
 * A {@link OpaqueQueryNode} is used for specify values that are not supposed to
 * be parsed by the parser. For example: and XPATH query in the middle of a
 * query string a b @xpath:'/bookstore/book[1]/title' c d
 */
public class OpaqueQueryNode extends QueryNodeImpl {

  private CharSequence schema = null;

  private CharSequence value = null;

  /**
   * @param schema
   *          - schema identifier
   * @param value
   *          - value that was not parsed
   */
  public OpaqueQueryNode(CharSequence schema, CharSequence value) {
    this.setLeaf(true);

    this.schema = schema;
    this.value = value;

  }

  @Override
  public String toString() {
    return "<opaque schema='" + this.schema + "' value='" + this.value + "'/>";
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    return "@" + this.schema + ":'" + this.value + "'";
  }

  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    OpaqueQueryNode clone = (OpaqueQueryNode) super.cloneTree();

    clone.schema = this.schema;
    clone.value = this.value;

    return clone;
  }

  /**
   * @return the schema
   */
  public CharSequence getSchema() {
    return this.schema;
  }

  /**
   * @return the value
   */
  public CharSequence getValue() {
    return this.value;
  }

}
