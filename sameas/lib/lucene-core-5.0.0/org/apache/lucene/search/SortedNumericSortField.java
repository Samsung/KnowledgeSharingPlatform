package org.apache.lucene.search;

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

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedNumericDocValues;

/** 
 * SortField for {@link SortedNumericDocValues}.
 * <p>
 * A SortedNumericDocValues contains multiple values for a field, so sorting with
 * this technique "selects" a value as the representative sort value for the document.
 * <p>
 * By default, the minimum value in the list is selected as the sort value, but
 * this can be customized.
 * <p>
 * Like sorting by string, this also supports sorting missing values as first or last,
 * via {@link #setMissingValue(Object)}.
 * <p>
 * @see SortedNumericSelector
 */
public class SortedNumericSortField extends SortField {
  
  private final SortedNumericSelector.Type selector;
  private final SortField.Type type;
  
  /**
   * Creates a sort, by the minimum value in the set 
   * for the document.
   * @param field Name of field to sort by.  Must not be null.
   * @param type Type of values
   */
  public SortedNumericSortField(String field, SortField.Type type) {
    this(field, type, false);
  }
  
  /**
   * Creates a sort, possibly in reverse, by the minimum value in the set 
   * for the document.
   * @param field Name of field to sort by.  Must not be null.
   * @param type Type of values
   * @param reverse True if natural order should be reversed.
   */
  public SortedNumericSortField(String field, SortField.Type type, boolean reverse) {
    this(field, type, reverse, SortedNumericSelector.Type.MIN);
  }

  /**
   * Creates a sort, possibly in reverse, specifying how the sort value from 
   * the document's set is selected.
   * @param field Name of field to sort by.  Must not be null.
   * @param type Type of values
   * @param reverse True if natural order should be reversed.
   * @param selector custom selector type for choosing the sort value from the set.
   */
  public SortedNumericSortField(String field, SortField.Type type, boolean reverse, SortedNumericSelector.Type selector) {
    super(field, SortField.Type.CUSTOM, reverse);
    if (selector == null) {
      throw new NullPointerException();
    }
    if (type == null) {
      throw new NullPointerException();
    }
    this.selector = selector;
    this.type = type;
  }
  
  /** Returns the selector in use for this sort */
  public SortedNumericSelector.Type getSelector() {
    return selector;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + selector.hashCode();
    result = prime * result + type.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    SortedNumericSortField other = (SortedNumericSortField) obj;
    if (selector != other.selector) return false;
    if (type != other.type) return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<sortednumeric" + ": \"").append(getField()).append("\">");
    if (getReverse()) buffer.append('!');
    if (missingValue != null) {
      buffer.append(" missingValue=");
      buffer.append(missingValue);
    }
    buffer.append(" selector=");
    buffer.append(selector);
    buffer.append(" type=");
    buffer.append(type);

    return buffer.toString();
  }
  
  @Override
  public void setMissingValue(Object missingValue) {
    this.missingValue = missingValue;
  }
  
  @Override
  public FieldComparator<?> getComparator(int numHits, int sortPos) throws IOException {
    switch(type) {
      case INT:
        return new FieldComparator.IntComparator(numHits, getField(), (Integer) missingValue) {
          @Override
          protected NumericDocValues getNumericDocValues(LeafReaderContext context, String field) throws IOException {
            return SortedNumericSelector.wrap(DocValues.getSortedNumeric(context.reader(), field), selector, type);
          } 
        };
      case FLOAT:
        return new FieldComparator.FloatComparator(numHits, getField(), (Float) missingValue) {
          @Override
          protected NumericDocValues getNumericDocValues(LeafReaderContext context, String field) throws IOException {
            return SortedNumericSelector.wrap(DocValues.getSortedNumeric(context.reader(), field), selector, type);
          } 
        };
      case LONG:
        return new FieldComparator.LongComparator(numHits, getField(), (Long) missingValue) {
          @Override
          protected NumericDocValues getNumericDocValues(LeafReaderContext context, String field) throws IOException {
            return SortedNumericSelector.wrap(DocValues.getSortedNumeric(context.reader(), field), selector, type);
          }
        };
      case DOUBLE:
        return new FieldComparator.DoubleComparator(numHits, getField(), (Double) missingValue) {
          @Override
          protected NumericDocValues getNumericDocValues(LeafReaderContext context, String field) throws IOException {
            return SortedNumericSelector.wrap(DocValues.getSortedNumeric(context.reader(), field), selector, type);
          } 
        };
      default:
        throw new AssertionError();
    }
  }
}
