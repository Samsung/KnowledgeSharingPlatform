package org.apache.lucene.queryparser.flexible.core.util;

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

import org.apache.lucene.queryparser.flexible.core.QueryNodeError;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

import java.util.ArrayList;
import java.util.List;


/**
 * Allow joining 2 QueryNode Trees, into one.
 */
public final class QueryNodeOperation {
  private QueryNodeOperation() {
    // Exists only to defeat instantiation.
  }

  private enum ANDOperation {
    BOTH, Q1, Q2, NONE
  }

  /**
   * perform a logical and of 2 QueryNode trees. if q1 and q2 are ANDQueryNode
   * nodes it uses head Node from q1 and adds the children of q2 to q1 if q1 is
   * a AND node and q2 is not, add q2 as a child of the head node of q1 if q2 is
   * a AND node and q1 is not, add q1 as a child of the head node of q2 if q1
   * and q2 are not ANDQueryNode nodes, create a AND node and make q1 and q2
   * children of that node if q1 or q2 is null it returns the not null node if
   * q1 = q2 = null it returns null
   */
  public final static QueryNode logicalAnd(QueryNode q1, QueryNode q2) {
    if (q1 == null)
      return q2;
    if (q2 == null)
      return q1;

    ANDOperation op = null;
    if (q1 instanceof AndQueryNode && q2 instanceof AndQueryNode)
      op = ANDOperation.BOTH;
    else if (q1 instanceof AndQueryNode)
      op = ANDOperation.Q1;
    else if (q1 instanceof AndQueryNode)
      op = ANDOperation.Q2;
    else
      op = ANDOperation.NONE;

    try {
      QueryNode result = null;
      switch (op) {
      case NONE:
        List<QueryNode> children = new ArrayList<>();
        children.add(q1.cloneTree());
        children.add(q2.cloneTree());
        result = new AndQueryNode(children);
        return result;
      case Q1:
        result = q1.cloneTree();
        result.add(q2.cloneTree());
        return result;
      case Q2:
        result = q2.cloneTree();
        result.add(q1.cloneTree());
        return result;
      case BOTH:
        result = q1.cloneTree();
        result.add(q2.cloneTree().getChildren());
        return result;
      }
    } catch (CloneNotSupportedException e) {
      throw new QueryNodeError(e);
    }

    return null;

  }

}
