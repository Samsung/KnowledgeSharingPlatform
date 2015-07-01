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

import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.util.NumericUtils;

/** 
 * Selects a value from the document's list to use as the representative value 
 * <p>
 * This provides a NumericDocValues view over the SortedNumeric, for use with sorting,
 * expressions, function queries, etc.
 */
public class SortedNumericSelector {
  
  /** 
   * Type of selection to perform.
   */
  public enum Type {
    /** 
     * Selects the minimum value in the set 
     */
    MIN,
    /** 
     * Selects the maximum value in the set 
     */
    MAX,
    // TODO: we could do MEDIAN in constant time (at most 2 lookups)
  }
  
  /** 
   * Wraps a multi-valued SortedNumericDocValues as a single-valued view, using the specified selector 
   * and numericType.
   */
  public static NumericDocValues wrap(SortedNumericDocValues sortedNumeric, Type selector, SortField.Type numericType) {
    if (numericType != SortField.Type.INT &&
        numericType != SortField.Type.LONG && 
        numericType != SortField.Type.FLOAT &&
        numericType != SortField.Type.DOUBLE) {
      throw new IllegalArgumentException("numericType must be a numeric type");
    }
    final NumericDocValues view;
    NumericDocValues singleton = DocValues.unwrapSingleton(sortedNumeric);
    if (singleton != null) {
      // it's actually single-valued in practice, but indexed as multi-valued,
      // so just sort on the underlying single-valued dv directly.
      // regardless of selector type, this optimization is safe!
      view = singleton;
    } else { 
      switch(selector) {
        case MIN: 
          view = new MinValue(sortedNumeric);
          break;
        case MAX:
          view = new MaxValue(sortedNumeric);
          break;
        default: 
          throw new AssertionError();
      }
    }
    // undo the numericutils sortability
    switch(numericType) {
      case FLOAT:
        return new NumericDocValues() {
          @Override
          public long get(int docID) {
            return NumericUtils.sortableFloatBits((int) view.get(docID));
          }
        };
      case DOUBLE:
        return new NumericDocValues() {
          @Override
          public long get(int docID) {
            return NumericUtils.sortableDoubleBits(view.get(docID));
          }
        };
      default:
        return view;
    }
  }
  
  /** Wraps a SortedNumericDocValues and returns the first value (min) */
  static class MinValue extends NumericDocValues {
    final SortedNumericDocValues in;
    
    MinValue(SortedNumericDocValues in) {
      this.in = in;
    }

    @Override
    public long get(int docID) {
      in.setDocument(docID);
      if (in.count() == 0) {
        return 0; // missing
      } else {
        return in.valueAt(0);
      }
    }
  }
  
  /** Wraps a SortedNumericDocValues and returns the last value (max) */
  static class MaxValue extends NumericDocValues {
    final SortedNumericDocValues in;
    
    MaxValue(SortedNumericDocValues in) {
      this.in = in;
    }

    @Override
    public long get(int docID) {
      in.setDocument(docID);
      final int count = in.count();
      if (count == 0) {
        return 0; // missing
      } else {
        return in.valueAt(count-1);
      }
    }
  }
}
