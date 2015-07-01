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

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.processors.MultiTermRewriteMethodProcessor;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.TermRangeQuery;

/**
 * Builds a {@link TermRangeQuery} object from a {@link TermRangeQueryNode}
 * object.
 */
public class TermRangeQueryNodeBuilder implements StandardQueryBuilder {
  
  public TermRangeQueryNodeBuilder() {
  // empty constructor
  }
  
  @Override
  public TermRangeQuery build(QueryNode queryNode) throws QueryNodeException {
    TermRangeQueryNode rangeNode = (TermRangeQueryNode) queryNode;
    FieldQueryNode upper = rangeNode.getUpperBound();
    FieldQueryNode lower = rangeNode.getLowerBound();
    
    String field = StringUtils.toString(rangeNode.getField());
    String lowerText = lower.getTextAsString();
    String upperText = upper.getTextAsString();
    
    if (lowerText.length() == 0) {
      lowerText = null;
    }
    
    if (upperText.length() == 0) {
      upperText = null;
    }
    
    TermRangeQuery rangeQuery = TermRangeQuery.newStringRange(field, lowerText, upperText, rangeNode
        .isLowerInclusive(), rangeNode.isUpperInclusive());
    
    MultiTermQuery.RewriteMethod method = (MultiTermQuery.RewriteMethod) queryNode
        .getTag(MultiTermRewriteMethodProcessor.TAG_ID);
    if (method != null) {
      rangeQuery.setRewriteMethod(method);
    }
    
    return rangeQuery;
    
  }
  
}
