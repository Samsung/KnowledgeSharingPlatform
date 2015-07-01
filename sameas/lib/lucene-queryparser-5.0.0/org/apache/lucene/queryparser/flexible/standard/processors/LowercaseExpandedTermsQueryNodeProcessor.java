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
import java.util.Locale;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.RangeQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.TextableQueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.core.util.UnescapedCharSequence;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.nodes.RegexpQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;

/**
 * This processor verifies if 
 * {@link ConfigurationKeys#LOWERCASE_EXPANDED_TERMS} is defined in the
 * {@link QueryConfigHandler}. If it is and the expanded terms should be
 * lower-cased, it looks for every {@link WildcardQueryNode},
 * {@link FuzzyQueryNode} and children of a {@link RangeQueryNode} and lower-case its
 * term. <br/>
 * 
 * @see ConfigurationKeys#LOWERCASE_EXPANDED_TERMS
 */
public class LowercaseExpandedTermsQueryNodeProcessor extends
    QueryNodeProcessorImpl {

  public LowercaseExpandedTermsQueryNodeProcessor() {
  }

  @Override
  public QueryNode process(QueryNode queryTree) throws QueryNodeException {
    Boolean lowercaseExpandedTerms = getQueryConfigHandler().get(ConfigurationKeys.LOWERCASE_EXPANDED_TERMS);

    if (lowercaseExpandedTerms != null && lowercaseExpandedTerms) {
      return super.process(queryTree);
    }

    return queryTree;

  }

  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    
    Locale locale = getQueryConfigHandler().get(ConfigurationKeys.LOCALE);
    if (locale == null) {
      locale = Locale.getDefault();
    }

    if (node instanceof WildcardQueryNode
        || node instanceof FuzzyQueryNode
        || (node instanceof FieldQueryNode && node.getParent() instanceof RangeQueryNode)
        || node instanceof RegexpQueryNode) {

      TextableQueryNode txtNode = (TextableQueryNode) node;
      CharSequence text = txtNode.getText();
      txtNode.setText(text != null ? UnescapedCharSequence.toLowerCase(text, locale) : null);
    }

    return node;

  }

  @Override
  protected QueryNode preProcessNode(QueryNode node) throws QueryNodeException {

    return node;

  }

  @Override
  protected List<QueryNode> setChildrenOrder(List<QueryNode> children)
      throws QueryNodeException {

    return children;

  }

}
