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
import org.apache.lucene.queryparser.flexible.core.nodes.RangeQueryNode;
import org.apache.lucene.queryparser.flexible.core.processors.QueryNodeProcessorImpl;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.NumericRangeQueryNode;

/**
 * This processor is used to convert {@link FieldQueryNode}s to
 * {@link NumericRangeQueryNode}s. It looks for
 * {@link ConfigurationKeys#NUMERIC_CONFIG} set in the {@link FieldConfig} of
 * every {@link FieldQueryNode} found. If
 * {@link ConfigurationKeys#NUMERIC_CONFIG} is found, it considers that
 * {@link FieldQueryNode} to be a numeric query and convert it to
 * {@link NumericRangeQueryNode} with upper and lower inclusive and lower and
 * upper equals to the value represented by the {@link FieldQueryNode} converted
 * to {@link Number}. It means that <b>field:1</b> is converted to <b>field:[1
 * TO 1]</b>. <br/>
 * <br/>
 * Note that {@link FieldQueryNode}s children of a
 * {@link RangeQueryNode} are ignored.
 * 
 * @see ConfigurationKeys#NUMERIC_CONFIG
 * @see FieldQueryNode
 * @see NumericConfig
 * @see NumericQueryNode
 */
public class NumericQueryNodeProcessor extends QueryNodeProcessorImpl {
  
  /**
   * Constructs a {@link NumericQueryNodeProcessor} object.
   */
  public NumericQueryNodeProcessor() {
  // empty constructor
  }
  
  @Override
  protected QueryNode postProcessNode(QueryNode node) throws QueryNodeException {
    
    if (node instanceof FieldQueryNode
        && !(node.getParent() instanceof RangeQueryNode)) {
      
      QueryConfigHandler config = getQueryConfigHandler();
      
      if (config != null) {
        FieldQueryNode fieldNode = (FieldQueryNode) node;
        FieldConfig fieldConfig = config.getFieldConfig(fieldNode
            .getFieldAsString());
        
        if (fieldConfig != null) {
          NumericConfig numericConfig = fieldConfig
              .get(ConfigurationKeys.NUMERIC_CONFIG);
          
          if (numericConfig != null) {
            
            NumberFormat numberFormat = numericConfig.getNumberFormat();
            String text = fieldNode.getTextAsString();
            Number number = null;
            
            if (text.length() > 0) {
              
              try {
                number = numberFormat.parse(text);
                
              } catch (ParseException e) {
                throw new QueryNodeParseException(new MessageImpl(
                    QueryParserMessages.COULD_NOT_PARSE_NUMBER, fieldNode
                        .getTextAsString(), numberFormat.getClass()
                        .getCanonicalName()), e);
              }
              
              switch (numericConfig.getType()) {
                case LONG:
                  number = number.longValue();
                  break;
                case INT:
                  number = number.intValue();
                  break;
                case DOUBLE:
                  number = number.doubleValue();
                  break;
                case FLOAT:
                  number = number.floatValue();
              }
              
            } else {
              throw new QueryNodeParseException(new MessageImpl(
                  QueryParserMessages.NUMERIC_CANNOT_BE_EMPTY, fieldNode.getFieldAsString()));
            }
            
            NumericQueryNode lowerNode = new NumericQueryNode(fieldNode
                .getField(), number, numberFormat);
            NumericQueryNode upperNode = new NumericQueryNode(fieldNode
                .getField(), number, numberFormat);
            
            return new NumericRangeQueryNode(lowerNode, upperNode, true, true,
                numericConfig);
            
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
