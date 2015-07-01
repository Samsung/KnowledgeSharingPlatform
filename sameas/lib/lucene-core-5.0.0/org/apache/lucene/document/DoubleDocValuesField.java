package org.apache.lucene.document;

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

/**
 * Syntactic sugar for encoding doubles as NumericDocValues
 * via {@link Double#doubleToRawLongBits(double)}.
 * <p>
 * Per-document double values can be retrieved via
 * {@link org.apache.lucene.index.LeafReader#getNumericDocValues(String)}.
 * <p>
 * <b>NOTE</b>: In most all cases this will be rather inefficient,
 * requiring eight bytes per document. Consider encoding double
 * values yourself with only as much precision as you require.
 */
public class DoubleDocValuesField extends NumericDocValuesField {

  /** 
   * Creates a new DocValues field with the specified 64-bit double value 
   * @param name field name
   * @param value 64-bit double value
   * @throws IllegalArgumentException if the field name is null
   */
  public DoubleDocValuesField(String name, double value) {
    super(name, Double.doubleToRawLongBits(value));
  }

  @Override
  public void setDoubleValue(double value) {
    super.setLongValue(Double.doubleToRawLongBits(value));
  }
  
  @Override
  public void setLongValue(long value) {
    throw new IllegalArgumentException("cannot change value type from Double to Long");
  }
}
