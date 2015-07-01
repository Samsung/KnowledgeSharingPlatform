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
import java.util.Map;


/**
 * A {@link QueryNode} is a interface implemented by all nodes on a QueryNode
 * tree.
 */
public interface QueryNode {

  /** convert to a query string understood by the query parser */
  // TODO: this interface might be changed in the future
  public CharSequence toQueryString(EscapeQuerySyntax escapeSyntaxParser);

  /** for printing */
  @Override
  public String toString();

  /** get Children nodes */
  public List<QueryNode> getChildren();

  /** verify if a node is a Leaf node */
  public boolean isLeaf();

  /** verify if a node contains a tag */
  public boolean containsTag(String tagName);
  
  /**
   * Returns object stored under that tag name
   */
  public Object getTag(String tagName);
  
  public QueryNode getParent();

  /**
   * Recursive clone the QueryNode tree The tags are not copied to the new tree
   * when you call the cloneTree() method
   * 
   * @return the cloned tree
   */
  public QueryNode cloneTree() throws CloneNotSupportedException;

  // Below are the methods that can change state of a QueryNode
  // Write Operations (not Thread Safe)

  // add a new child to a non Leaf node
  public void add(QueryNode child);

  public void add(List<QueryNode> children);

  // reset the children of a node
  public void set(List<QueryNode> children);

  /**
   * Associate the specified value with the specified tagName. If the tagName
   * already exists, the old value is replaced. The tagName and value cannot be
   * null. tagName will be converted to lowercase.
   */
  public void setTag(String tagName, Object value);
  
  /**
   * Unset a tag. tagName will be converted to lowercase.
   */
  public void unsetTag(String tagName);
  
  /**
   * Returns a map containing all tags attached to this query node. 
   * 
   * @return a map containing all tags attached to this query node
   */
  public Map<String, Object> getTagMap();

  /**
   * Removes this query node from its parent.
   */
  public void removeFromParent();
}
