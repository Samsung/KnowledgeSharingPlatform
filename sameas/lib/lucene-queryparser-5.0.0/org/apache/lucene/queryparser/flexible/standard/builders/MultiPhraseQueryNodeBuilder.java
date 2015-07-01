package org.apache.lucene.queryparser.flexible.standard.builders;

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

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.MultiPhraseQueryNode;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.TermQuery;

/**
 * Builds a {@link MultiPhraseQuery} object from a {@link MultiPhraseQueryNode}
 * object.
 */
public class MultiPhraseQueryNodeBuilder implements StandardQueryBuilder {

  public MultiPhraseQueryNodeBuilder() {
    // empty constructor
  }

  @Override
  public MultiPhraseQuery build(QueryNode queryNode) throws QueryNodeException {
    MultiPhraseQueryNode phraseNode = (MultiPhraseQueryNode) queryNode;

    MultiPhraseQuery phraseQuery = new MultiPhraseQuery();

    List<QueryNode> children = phraseNode.getChildren();

    if (children != null) {
      TreeMap<Integer, List<Term>> positionTermMap = new TreeMap<>();

      for (QueryNode child : children) {
        FieldQueryNode termNode = (FieldQueryNode) child;
        TermQuery termQuery = (TermQuery) termNode
            .getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
        List<Term> termList = positionTermMap.get(termNode
            .getPositionIncrement());

        if (termList == null) {
          termList = new LinkedList<>();
          positionTermMap.put(termNode.getPositionIncrement(), termList);

        }

        termList.add(termQuery.getTerm());

      }

      for (int positionIncrement : positionTermMap.keySet()) {
        List<Term> termList = positionTermMap.get(positionIncrement);

        phraseQuery.add(termList.toArray(new Term[termList.size()]),
            positionIncrement);

      }

    }

    return phraseQuery;

  }

}
