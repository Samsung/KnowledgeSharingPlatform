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
import java.util.Locale;

import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax;
import org.apache.lucene.queryparser.flexible.core.parser.EscapeQuerySyntax.Type;

/**
 * A {@link PathQueryNode} is used to store queries like
 * /company/USA/California /product/shoes/brown. QueryText are objects that
 * contain the text, begin position and end position in the query.
 * <p>
 * Example how the text parser creates these objects:
 * </p>
 * <pre class="prettyprint">
 * List values = ArrayList(); 
 * values.add(new PathQueryNode.QueryText("company", 1, 7)); 
 * values.add(new PathQueryNode.QueryText("USA", 9, 12)); 
 * values.add(new PathQueryNode.QueryText("California", 14, 23)); 
 * QueryNode q = new PathQueryNode(values);
 * </pre>
 */
public class PathQueryNode extends QueryNodeImpl {

  /**
   * Term text with a beginning and end position
   */
  public static class QueryText implements Cloneable {
    CharSequence value = null;
    /**
     * != null The term's begin position.
     */
    int begin;

    /**
     * The term's end position.
     */
    int end;

    /**
     * @param value
     *          - text value
     * @param begin
     *          - position in the query string
     * @param end
     *          - position in the query string
     */
    public QueryText(CharSequence value, int begin, int end) {
      super();
      this.value = value;
      this.begin = begin;
      this.end = end;
    }

    @Override
    public QueryText clone() throws CloneNotSupportedException {
      QueryText clone = (QueryText) super.clone();
      clone.value = this.value;
      clone.begin = this.begin;
      clone.end = this.end;
      return clone;
    }

    /**
     * @return the value
     */
    public CharSequence getValue() {
      return value;
    }

    /**
     * @return the begin
     */
    public int getBegin() {
      return begin;
    }

    /**
     * @return the end
     */
    public int getEnd() {
      return end;
    }

    @Override
    public String toString() {
      return value + ", " + begin + ", " + end;
    }
  }

  private List<QueryText> values = null;

  /**
   * @param pathElements
   *          - List of QueryText objects
   */
  public PathQueryNode(List<QueryText> pathElements) {
    this.values = pathElements;
    if (pathElements.size() <= 1) {
      // this should not happen
      throw new RuntimeException(
          "PathQuerynode requires more 2 or more path elements.");
    }
  }

  /**
   * Returns the a List with all QueryText elements
   * 
   * @return QueryText List size
   */
  public List<QueryText> getPathElements() {
    return values;
  }

  /**
   * Returns the a List with all QueryText elements
   */
  public void setPathElements(List<QueryText> elements) {
    this.values = elements;
  }

  /**
   * Returns the a specific QueryText element
   * 
   * @return QueryText List size
   */
  public QueryText getPathElement(int index) {
    return values.get(index);
  }

  /**
   * Returns the CharSequence value of a specific QueryText element
   * 
   * @return the CharSequence for a specific QueryText element
   */
  public CharSequence getFirstPathElement() {
    return values.get(0).value;
  }

  /**
   * Returns a List QueryText element from position startIndex
   * 
   * @return a List QueryText element from position startIndex
   */
  public List<QueryText> getPathElements(int startIndex) {
    List<PathQueryNode.QueryText> rValues = new ArrayList<>();
    for (int i = startIndex; i < this.values.size(); i++) {
      try {
        rValues.add(this.values.get(i).clone());
      } catch (CloneNotSupportedException e) {
        // this will not happen
      }
    }
    return rValues;
  }

  private CharSequence getPathString() {
    StringBuilder path = new StringBuilder();

    for (QueryText pathelement : values) {
      path.append("/").append(pathelement.value);
    }
    return path.toString();
  }

  @Override
  public CharSequence toQueryString(EscapeQuerySyntax escaper) {
    StringBuilder path = new StringBuilder();
    path.append("/").append(getFirstPathElement());

    for (QueryText pathelement : getPathElements(1)) {
      CharSequence value = escaper.escape(pathelement.value, Locale
          .getDefault(), Type.STRING);
      path.append("/\"").append(value).append("\"");
    }
    return path.toString();
  }

  @Override
  public String toString() {
    QueryText text = this.values.get(0);

    return "<path start='" + text.begin + "' end='" + text.end + "' path='"
        + getPathString() + "'/>";
  }

  @Override
  public QueryNode cloneTree() throws CloneNotSupportedException {
    PathQueryNode clone = (PathQueryNode) super.cloneTree();

    // copy children
    if (this.values != null) {
      List<QueryText> localValues = new ArrayList<>();
      for (QueryText value : this.values) {
        localValues.add(value.clone());
      }
      clone.values = localValues;
    }

    return clone;
  }

}
