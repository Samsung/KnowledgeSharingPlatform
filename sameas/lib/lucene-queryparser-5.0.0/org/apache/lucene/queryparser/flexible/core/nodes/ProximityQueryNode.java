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
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryparser.flexible.core.QueryNodeError;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;

/**
 * A {@link ProximityQueryNode} represents a query where the terms should meet
 * specific distance conditions. (a b c) WITHIN [SENTENCE|PARAGRAPH|NUMBER]
 * [INORDER] ("a" "b" "c") WITHIN [SENTENCE|PARAGRAPH|NUMBER] [INORDER]
 * 
 * TODO: Add this to the future standard Lucene parser/processor/builder
 */
public class ProximityQueryNode extends BooleanQueryNode {

  /**
   * Distance condition: PARAGRAPH, SENTENCE, or NUMBER
   */
  public enum Type {
    PARAGRAPH {
      @Override
      CharSequence toQueryString() { return "WITHIN PARAGRAPH"; } 
    },
    SENTENCE  { 
      @Override
      CharSequence toQueryString() { return "WITHIN SENTENCE";  }
    },
    NUMBER    {
      @Override
      CharSequence toQueryString() { return "WITHIN";           }
    };

    abstract CharSequence toQueryString();
  }

  /** utility class containing the distance condition and number */
  static public class ProximityType {
    int pDistance = 0;

    Type pType = null;

    public ProximityType(Type type) {
      this(type, 0);
    }

    public ProximityType(Type type, int distance) {
      this.pType = type;
      this.pDistance = distance;
    }
  }

  private Type proximityType = Type.SENTENCE;
  private int distance = -1;
  private boolean inorder = false;
  private CharSequence field = null;

  /**
   * @param clauses
   *          - QueryNode children
   * @param field
   *          - field name
   * @param type
   *          - type of proximity query
   * @param distance
   *          - positive integer that specifies the distance
   * @param inorder
   *          - true, if the tokens should be matched in the order of the
   *          clauses
   */
  public ProximityQueryNode(List<QueryNode> clauses, CharSequence field,
      Type type, int distance, boolean inorder) {
    super(clauses);
    setLeaf(false);
    this.proximityType = type;
    this.inorder = inorder;
    this.field = field;
    if (type == Type.NUMBER) {
      if (distance <= 0) {
        throw new QueryNodeError(new MessageImpl(
            QueryParserMessages.PARAMETER_VALUE_NOT_SUPPORTED, "distance",
            distance));

      } else {
        this.distance = distance;
      }

    }
    clearFields(clauses, field);
  }

  /**
   * @param clauses
   *          - QueryNode children
   * @param field
   *          - field name
   * @param type
   *          - type of proximity query
   * @param inorder
   *          - true, if the tokens should be matched in the order of the
   *          clauses
   */
  public ProximityQueryNode(List<QueryNode> clauses, CharSequence field,
      Type type, boolean inorder) {
    this(clauses, field, type, -1, inorder);
  }

  static private void clearFields(List<QueryNode> nodes, CharSequence field) {
    if (nodes == null || nodes.size() == 0)
      return;

    for (QueryNode clause : nodes) {

      if (clause instanceof FieldQueryNode) {
        ((FieldQueryNode) clause).toQueryStringIgnoreFields = true;
        ((FieldQueryNode) clause).setField(field);
      }
    }
  }

  public Type getProximityType() {
    return this.proximityType;
  }

  @Override
  public String toString() {
    String distanceSTR = ((this.distance == -1) ? ("")
        : (" distance='" + this.distance) + "'");

    if (getChildren() == null || getChildren().size() == 0)
      return "<proximity field='" + this.field + "' inorder='" + this.inorder
          + "' type='" + this.proximityType.toString() + "'" + distanceSTR
          + "/>";
    StringBuilder sb = new StringBuilder();
    sb.append("<proximity field='" + this.field + "' inorder='" + this.inorder
        + "' type='" + this.proximityType.toString() + "'" + distanceSTR + ">");
    for (QueryNode child : getChildren()) {
      sb.append("\n");
      sb.append(child.toString());
    }
    sb.append("\n</proximity>");
    return sb.toString();
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser) {
    String withinSTR = this.proximityType.toQueryString()
        + ((this.distance == -1) ? ("") : (" " + this.distance))
        + ((this.inorder) ? (" INORDER") : (""));

    StringBuilder sb = new StringBuilder();
    if (getChildren() == null || getChildren().size() == 0) {
      // no children case
    } else {
      String filler = "";
      for (QueryNode child : getChildren()) {
        sb.append(filler).append(child.toQueryString(escapeSyntaxParser));
        filler = " ";
      }
    }

    if (isDefaultField(this.field)) {
      return "( " + sb.toString() + " ) " + withinSTR;
    } else {
      return this.field + ":(( " + sb.toString() + " ) " + withinSTR + ")";
    }
  }

  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    ProximityQueryNode clone = (ProximityQueryNode) super.cloneTree();

    clone.proximityType = this.proximityType;
    clone.distance = this.distance;
    clone.field = this.field;

    return clone;
  }

  /**
   * @return the distance
   */
  public int getDistance() {
    return this.distance;
  }

  /**
   * returns null if the field was not specified in the query string
   * 
   * @return the field
   */
  public CharSequence getField() {
    return this.field;
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

  /**
   * @param field
   *          the field to set
   */
  public void setField(CharSequence field) {
    this.field = field;
  }

  /**
   * @return terms must be matched in the specified order
   */
  public boolean isInOrder() {
    return this.inorder;
  }

}
