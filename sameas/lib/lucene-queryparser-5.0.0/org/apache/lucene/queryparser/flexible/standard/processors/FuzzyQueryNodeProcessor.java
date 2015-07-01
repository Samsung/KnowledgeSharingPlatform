package org.apache.lucene.queryparser.flexible.standard.processors;

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

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.standard.config.FuzzyConfig;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.search.FuzzyQuery;

/**
 * This processor iterates the query node tree looking for every
 * {@link FuzzyQueryNode}, when this kind of node is found, it checks on the
 * query configuration for
 * {@link ConfigurationKeys#FUZZY_CONFIG}, gets the
 * fuzzy prefix length and default similarity from it and set to the fuzzy node.
 * For more information about fuzzy prefix length check: {@link FuzzyQuery}. <br/>
 * 
 * @see ConfigurationKeys#FUZZY_CONFIG
 * @see FuzzyQuery
 * @see FuzzyQueryNode
 */
public class FuzzyQueryNodeProcessor extends QueryNodeProcessorImpl {

  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {

    return node;

  }

  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {

    if (node instanceof FuzzyQueryNode) {
      FuzzyQueryNode fuzzyNode = (FuzzyQueryNode) node;
      QueryConfigHandler config = getQueryConfigHandler();

      FuzzyConfig fuzzyConfig = null;
      
      if (config != null && (fuzzyConfig = config.get(ConfigurationKeys.FUZZY_CONFIG)) != null) {
        fuzzyNode.setPrefixLength(fuzzyConfig.getPrefixLength());

        if (fuzzyNode.getSimilarity() < 0) {
          fuzzyNode.setSimilarity(fuzzyConfig.getMinSimilarity());
        }
        
      } else if (fuzzyNode.getSimilarity() < 0) {
        throw new IllegalArgumentException("No FUZZY_CONFIG set in the config");
      }

    }

    return node;

  }

  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {

    return children;

  }

}
