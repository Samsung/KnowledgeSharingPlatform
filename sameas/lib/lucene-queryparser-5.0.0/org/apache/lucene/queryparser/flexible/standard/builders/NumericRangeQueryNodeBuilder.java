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

import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericRangeQueryNode;
import org.apache.lucene.search.NumericRangeQuery;

/**
 * Builds {@link NumericRangeQuery}s out of {@link NumericRangeQueryNode}s.
 *
 * @see NumericRangeQuery
 * @see NumericRangeQueryNode
 */
public class NumericRangeQueryNodeBuilder implements StandardQueryBuilder {
  
  /**
   * Constructs a {@link NumericRangeQueryNodeBuilder} object.
   */
  public NumericRangeQueryNodeBuilder() {
  // empty constructor
  }
  
  @Override
  public NumericRangeQuery<? extends Number> build(QueryNode queryNode)
      throws QueryNodeException {
    NumericRangeQueryNode numericRangeNode = (NumericRangeQueryNode) queryNode;
    
    NumericQueryNode lowerNumericNode = numericRangeNode.getLowerBound();
    NumericQueryNode upperNumericNode = numericRangeNode.getUpperBound();
    
    Number lowerNumber = lowerNumericNode.getValue();
    Number upperNumber = upperNumericNode.getValue();
    
    NumericConfig numericConfig = numericRangeNode.getNumericConfig();
    NumericType numberType = numericConfig.getType();
    String field = StringUtils.toString(numericRangeNode.getField());
    boolean minInclusive = numericRangeNode.isLowerInclusive();
    boolean maxInclusive = numericRangeNode.isUpperInclusive();
    int precisionStep = numericConfig.getPrecisionStep();
    
    switch (numberType) {
      
      case LONG:
        return NumericRangeQuery.newLongRange(field, precisionStep,
            (Long) lowerNumber, (Long) upperNumber, minInclusive, maxInclusive);
      
      case INT:
        return NumericRangeQuery.newIntRange(field, precisionStep,
            (Integer) lowerNumber, (Integer) upperNumber, minInclusive,
            maxInclusive);
      
      case FLOAT:
        return NumericRangeQuery.newFloatRange(field, precisionStep,
            (Float) lowerNumber, (Float) upperNumber, minInclusive,
            maxInclusive);
      
      case DOUBLE:
        return NumericRangeQuery.newDoubleRange(field, precisionStep,
            (Double) lowerNumber, (Double) upperNumber, minInclusive,
            maxInclusive);
        
        default :
          throw new QueryNodeException(new MessageImpl(
            QueryParserMessages.UNSUPPORTED_NUMERIC_DATA_TYPE, numberType));
        
    }
  }
  
}
