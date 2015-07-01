package org.apache.lucene.queryparser.flexible.core.processors;

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

import java.util.Iterator;
import java.util.List;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.DeletedQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.MatchNoDocsQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

/**
 * A {@link QueryNodeProcessorPipeline} class removes every instance of
 * {@link DeletedQueryNode} from a query node tree. If the resulting root node
 * is a {@link DeletedQueryNode}, {@link MatchNoDocsQueryNode} is returned.
 * 
 */
public class RemoveDeletedQueryNodesProcessor extends QueryNodeProcessorImpl {

  public RemoveDeletedQueryNodesProcessor() {
    // empty constructor
  }

  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    queryTree = super.process(queryTree);

    if (queryTree instanceof DeletedQueryNode
        && !(queryTree instanceof MatchNoDocsQueryNode)) {

      return new MatchNoDocsQueryNode();

    }

    return queryTree;

  }

  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {

    if (!node.isLeaf()) {
      List<QueryNode> children = node.getChildren();
      boolean removeBoolean = false;

      if (children == null || children.size() == 0) {
        removeBoolean = true;

      } else {
        removeBoolean = true;

        for (Iterator<QueryNode> it = children.iterator(); it.hasNext();) {

          if (!(it.next() instanceof DeletedQueryNode)) {
            removeBoolean = false;
            break;

          }

        }

      }

      if (removeBoolean) {
        return new DeletedQueryNode();
      }

    }

    return node;

  }

  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {

    for (int i = 0; i < children.size(); i++) {

      if (children.get(i) instanceof DeletedQueryNode) {
        children.remove(i--);
      }

    }

    return children;

  }

  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {

    return node;

  }

}
