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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeParseException;
import org.apache.lucene.queryparser.flexible.core.config.FieldConfig;
import org.apache.lucene.queryparser.flexible.core.config.QueryConfigHandler;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;

/**
 * This processor is used to convert {@link TermRangeQueryNode}s to
 * {@link NumericRangeQueryNode}s. It looks for
 * {@link ConfigurationKeys#NUMERIC_CONFIG} set in the {@link FieldConfig} of
 * every {@link TermRangeQueryNode} found. If
 * {@link ConfigurationKeys#NUMERIC_CONFIG} is found, it considers that
 * {@link TermRangeQueryNode} to be a numeric range query and convert it to
 * {@link NumericRangeQueryNode}.
 * 
 * @see ConfigurationKeys#NUMERIC_CONFIG
 * @see TermRangeQueryNode
 * @see NumericConfig
 * @see NumericRangeQueryNode
 */
public class NumericRangeQueryNodeProcessor extends QueryNodeProcessorImpl {
  
  /**
   * Constructs an empty {@link NumericRangeQueryNode} object.
   */
  public NumericRangeQueryNodeProcessor() {
  // empty constructor
  }
  
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    
    if (node instanceof TermRangeQueryNode) {
      QueryConfigHandler config = getQueryConfigHandler();
      
      if (config != null) {
        TermRangeQueryNode termRangeNode = (TermRangeQueryNode) node;
        FieldConfig fieldConfig = config.getFieldConfig(StringUtils
            .toString(termRangeNode.getField()));
        
        if (fieldConfig != null) {
          
          NumericConfig numericConfig = fieldConfig
              .get(ConfigurationKeys.NUMERIC_CONFIG);
          
          if (numericConfig != null) {
            
            FieldQueryNode lower = termRangeNode.getLowerBound();
            FieldQueryNode upper = termRangeNode.getUpperBound();
            
            String lowerText = lower.getTextAsString();
            String upperText = upper.getTextAsString();
            NumberFormat numberFormat = numericConfig.getNumberFormat();
            Number lowerNumber = null, upperNumber = null;
            
             if (lowerText.length() > 0) {
              
              try {
                lowerNumber = numberFormat.parse(lowerText);
                
              } catch (ParseException e) {
                throw new QueryNodeParseException(new MessageImpl(
                    QueryParserMessages.COULD_NOT_PARSE_NUMBER, lower
                        .getTextAsString(), numberFormat.getClass()
                        .getCanonicalName()), e);
              }
              
            }
            
             if (upperText.length() > 0) {
            
              try {
                upperNumber = numberFormat.parse(upperText);
                
              } catch (ParseException e) {
                throw new QueryNodeParseException(new MessageImpl(
                    QueryParserMessages.COULD_NOT_PARSE_NUMBER, upper
                        .getTextAsString(), numberFormat.getClass()
                        .getCanonicalName()), e);
              }
            
            }
            
            switch (numericConfig.getType()) {
              case LONG:
                if (upperNumber != null) upperNumber = upperNumber.longValue();
                if (lowerNumber != null) lowerNumber = lowerNumber.longValue();
                break;
              case INT:
                if (upperNumber != null) upperNumber = upperNumber.intValue();
                if (lowerNumber != null) lowerNumber = lowerNumber.intValue();
                break;
              case DOUBLE:
                if (upperNumber != null) upperNumber = upperNumber.doubleValue();
                if (lowerNumber != null) lowerNumber = lowerNumber.doubleValue();
                break;
              case FLOAT:
                if (upperNumber != null) upperNumber = upperNumber.floatValue();
                if (lowerNumber != null) lowerNumber = lowerNumber.floatValue();
            }
            
            NumericQueryNode lowerNode = new NumericQueryNode(
                termRangeNode.getField(), lowerNumber, numberFormat);
            NumericQueryNode upperNode = new NumericQueryNode(
                termRangeNode.getField(), upperNumber, numberFormat);
            
            boolean lowerInclusive = termRangeNode.isLowerInclusive();
            boolean upperInclusive = termRangeNode.isUpperInclusive();
            
            return new NumericRangeQueryNode(lowerNode, upperNode,
                lowerInclusive, upperInclusive, numericConfig);
            
          }
          
        }
        
      }
      
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
