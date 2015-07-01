package org.apache.lucene.queryparser.flexible.standard.config;

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

import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.search.NumericRangeQuery;

/**
 * This class holds the configuration used to parse numeric queries and create
 * {@link NumericRangeQuery}s.
 * 
 * @see NumericRangeQuery
 * @see NumberFormat
 */
public class NumericConfig {
  
  private int precisionStep;
  
  private NumberFormat format;
  
  private NumericType type;
  
  /**
   * Constructs a {@link NumericConfig} object.
   * 
   * @param precisionStep
   *          the precision used to index the numeric values
   * @param format
   *          the {@link NumberFormat} used to parse a {@link String} to
   *          {@link Number}
   * @param type
   *          the numeric type used to index the numeric values
   * 
   * @see NumericConfig#setPrecisionStep(int)
   * @see NumericConfig#setNumberFormat(NumberFormat)
   * @see #setType(org.apache.lucene.document.FieldType.NumericType)
   */
  public NumericConfig(int precisionStep, NumberFormat format,
      NumericType type) {
    setPrecisionStep(precisionStep);
    setNumberFormat(format);
    setType(type);
    
  }
  
  /**
   * Returns the precision used to index the numeric values
   * 
   * @return the precision used to index the numeric values
   * 
   * @see NumericRangeQuery#getPrecisionStep()
   */
  public int getPrecisionStep() {
    return precisionStep;
  }
  
  /**
   * Sets the precision used to index the numeric values
   * 
   * @param precisionStep
   *          the precision used to index the numeric values
   * 
   * @see NumericRangeQuery#getPrecisionStep()
   */
  public void setPrecisionStep(int precisionStep) {
    this.precisionStep = precisionStep;
  }
  
  /**
   * Returns the {@link NumberFormat} used to parse a {@link String} to
   * {@link Number}
   * 
   * @return the {@link NumberFormat} used to parse a {@link String} to
   *         {@link Number}
   */
  public NumberFormat getNumberFormat() {
    return format;
  }
  
  /**
   * Returns the numeric type used to index the numeric values
   * 
   * @return the numeric type used to index the numeric values
   */
  public NumericType getType() {
    return type;
  }
  
  /**
   * Sets the numeric type used to index the numeric values
   * 
   * @param type the numeric type used to index the numeric values
   */
  public void setType(NumericType type) {
    
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null!");
    }
    
    this.type = type;
    
  }
  
  /**
   * Sets the {@link NumberFormat} used to parse a {@link String} to
   * {@link Number}
   * 
   * @param format
   *          the {@link NumberFormat} used to parse a {@link String} to
   *          {@link Number}, cannot be <code>null</code>
   */
  public void setNumberFormat(NumberFormat format) {
    
    if (format == null) {
      throw new IllegalArgumentException("format cannot be null!");
    }
    
    this.format = format;
    
  }
  
  @Override
  public boolean equals(Object obj) {
    
    if (obj == this) return true;
    
    if (obj instanceof NumericConfig) {
      NumericConfig other = (NumericConfig) obj;
      
      if (this.precisionStep == other.precisionStep
          && this.type == other.type
          && (this.format == other.format || (this.format.equals(other.format)))) {
        return true;
      }
      
    }
    
    return false;
    
  }
  
}
