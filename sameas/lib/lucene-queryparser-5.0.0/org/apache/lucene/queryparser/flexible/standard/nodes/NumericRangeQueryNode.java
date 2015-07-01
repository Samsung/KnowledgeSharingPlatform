package org.apache.lucene.queryparser.flexible.standard.nodes;

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
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;

/**
 * This query node represents a range query composed by {@link NumericQueryNode}
 * bounds, which means the bound values are {@link Number}s.
 * 
 * @see NumericQueryNode
 * @see AbstractRangeQueryNode
 */
public class NumericRangeQueryNode extends
    AbstractRangeQueryNode<NumericQueryNode> {
  
  public NumericConfig numericConfig; 
  
  /**
   * Constructs a {@link NumericRangeQueryNode} object using the given
   * {@link NumericQueryNode} as its bounds and {@link NumericConfig}.
   * 
   * @param lower the lower bound
   * @param upper the upper bound
   * @param lowerInclusive <code>true</code> if the lower bound is inclusive, otherwise, <code>false</code>
   * @param upperInclusive <code>true</code> if the upper bound is inclusive, otherwise, <code>false</code>
   * @param numericConfig the {@link NumericConfig} that represents associated with the upper and lower bounds
   * 
   * @see #setBounds(NumericQueryNode, NumericQueryNode, boolean, boolean, NumericConfig)
   */
  public NumericRangeQueryNode(NumericQueryNode lower, NumericQueryNode upper,
      boolean lowerInclusive, boolean upperInclusive, NumericConfig numericConfig) throws QueryNodeException {
    setBounds(lower, upper, lowerInclusive, upperInclusive, numericConfig);
  }
  
  private static NumericType getNumericDataType(Number number) throws QueryNodeException {
    
    if (number instanceof Long) {
      return NumericType.LONG;
    } else if (number instanceof Integer) {
      return NumericType.INT;
    } else if (number instanceof Double) {
      return NumericType.DOUBLE;
    } else if (number instanceof Float) {
      return NumericType.FLOAT;
    } else {
      throw new QueryNodeException(
          new MessageImpl(
              QueryParserMessages.NUMBER_CLASS_NOT_SUPPORTED_BY_NUMERIC_RANGE_QUERY,
              number.getClass()));
    }
    
  }
  
  /**
   * Sets the upper and lower bounds of this range query node and the
   * {@link NumericConfig} associated with these bounds.
   * 
   * @param lower the lower bound
   * @param upper the upper bound
   * @param lowerInclusive <code>true</code> if the lower bound is inclusive, otherwise, <code>false</code>
   * @param upperInclusive <code>true</code> if the upper bound is inclusive, otherwise, <code>false</code>
   * @param numericConfig the {@link NumericConfig} that represents associated with the upper and lower bounds
   * 
   */
  public void setBounds(NumericQueryNode lower, NumericQueryNode upper,
      boolean lowerInclusive, boolean upperInclusive, NumericConfig numericConfig) throws QueryNodeException {
    
    if (numericConfig == null) {
      throw new IllegalArgumentException("numericConfig cannot be null!");
    }
    
    NumericType lowerNumberType, upperNumberType;
    
    if (lower != null && lower.getValue() != null) {
      lowerNumberType = getNumericDataType(lower.getValue());
    } else {
      lowerNumberType = null;
    }
    
    if (upper != null && upper.getValue() != null) {
      upperNumberType = getNumericDataType(upper.getValue());
    } else {
      upperNumberType = null;
    }
    
    if (lowerNumberType != null
        && !lowerNumberType.equals(numericConfig.getType())) {
      throw new IllegalArgumentException(
          "lower value's type should be the same as numericConfig type: "
              + lowerNumberType + " != " + numericConfig.getType());
    }
    
    if (upperNumberType != null
        && !upperNumberType.equals(numericConfig.getType())) {
      throw new IllegalArgumentException(
          "upper value's type should be the same as numericConfig type: "
              + upperNumberType + " != " + numericConfig.getType());
    }
    
    super.setBounds(lower, upper, lowerInclusive, upperInclusive);
    this.numericConfig = numericConfig;
    
  }
  
  /**
   * Returns the {@link NumericConfig} associated with the lower and upper bounds.
   * 
   * @return the {@link NumericConfig} associated with the lower and upper bounds
   */
  public NumericConfig getNumericConfig() {
    return this.numericConfig;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("<numericRange lowerInclusive='");
    
    sb.append(isLowerInclusive()).append("' upperInclusive='").append(
        isUpperInclusive()).append(
        "' precisionStep='" + numericConfig.getPrecisionStep()).append(
        "' type='" + numericConfig.getType()).append("'>\n");
    
    sb.append(getLowerBound()).append('\n');
    sb.append(getUpperBound()).append('\n');
    sb.append("</numericRange>");
    
    return sb.toString();
    
  }
  
}
