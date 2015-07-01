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

import java.util.Locale;


/**
 * A {@link FieldQueryNode} represents a element that contains field/text tuple
 */
public class FieldQueryNode extends QueryNodeImpl implements FieldValuePairQueryNode<CharSequence>, TextableQueryNode {

  /**
   * The term's field
   */
  protected CharSequence field;

  /**
   * The term's text.
   */
  protected CharSequence text;

  /**
   * The term's begin position.
   */
  protected int begin;

  /**
   * The term's end position.
   */
  protected int end;

  /**
   * The term's position increment.
   */
  protected int positionIncrement;

  /**
   * @param field
   *          - field name
   * @param text
   *          - value
   * @param begin
   *          - position in the query string
   * @param end
   *          - position in the query string
   */
  public FieldQueryNode(CharSequence field, CharSequence text, int begin,
      int end) {
    this.field = field;
    this.text = text;
    this.begin = begin;
    this.end = end;
    this.setLeaf(true);

  }

  protected CharSequence getTermEscaped(EscapeQuerySyntax escaper) {
    return escaper.escape(this.text, Locale.getDefault(), EscapeQuerySyntax.Type.NORMAL);
  }

  protected CharSequence getTermEscapeQuoted(EscapeQuerySyntax escaper) {
    return escaper.escape(this.text, Locale.getDefault(), EscapeQuerySyntax.Type.STRING);
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    if (isDefaultField(this.field)) {
      return getTermEscaped(escaper);
    } else {
      return this.field + ":" + getTermEscaped(escaper);
    }
  }

  @Override
  public String toString() {
    return "<field start='" + this.begin + "' end='" + this.end + "' field='"
        + this.field + "' text='" + this.text + "'/>";
  }

  /**
   * @return the term
   */
  public String getTextAsString() {
    if (this.text == null)
      return null;
    else
      return this.text.toString();
  }

  /**
   * returns null if the field was not specified in the query string
   * 
   * @return the field
   */
  public String getFieldAsString() {
    if (this.field == null)
      return null;
    else
      return this.field.toString();
  }

  public int getBegin() {
    return this.begin;
  }

  public void setBegin(int begin) {
    this.begin = begin;
  }

  public int getEnd() {
    return this.end;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  @Override
  public CharSequence getField() {
    return this.field;
  }

  @Override
  public void setField(CharSequence field) {
    this.field = field;
  }

  public int getPositionIncrement() {
    return this.positionIncrement;
  }

  public void setPositionIncrement(int pi) {
    this.positionIncrement = pi;
  }

  /**
   * Returns the term.
   * 
   * @return The "original" form of the term.
   */
  @Override
  public CharSequence getText() {
    return this.text;
  }

  /**
   * @param text
   *          the text to set
   */
  @Override
  public void setText(CharSequence text) {
    this.text = text;
  }

  @Override
  public FieldQueryNode cloneTree() throws CloneNotSupportedException {
    FieldQueryNode fqn = (FieldQueryNode) super.cloneTree();
    fqn.begin = this.begin;
    fqn.end = this.end;
    fqn.field = this.field;
    fqn.text = this.text;
    fqn.positionIncrement = this.positionIncrement;
    fqn.toQueryStringIgnoreFields = this.toQueryStringIgnoreFields;

    return fqn;

  }

  @Override
  public CharSequence getValue() {
    return getText();
  }

  @Override
  public void setValue(CharSequence value) {
    setText(value);
  }

}
