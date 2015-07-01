package org.apache.lucene.index;

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

import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_ARRAY_HEADER;
import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_CHAR;
import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_INT;
import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_OBJECT_HEADER;
import static org.apache.lucene.util.RamUsageEstimator.NUM_BYTES_OBJECT_REF;

import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.RamUsageEstimator;

/** An in-place update to a DocValues field. */
abstract class DocValuesUpdate {
  
  /* Rough logic: OBJ_HEADER + 3*PTR + INT
   * Term: OBJ_HEADER + 2*PTR
   *   Term.field: 2*OBJ_HEADER + 4*INT + PTR + string.length*CHAR
   *   Term.bytes: 2*OBJ_HEADER + 2*INT + PTR + bytes.length
   * String: 2*OBJ_HEADER + 4*INT + PTR + string.length*CHAR
   * T: OBJ_HEADER
   */
  private static final int RAW_SIZE_IN_BYTES = 8*NUM_BYTES_OBJECT_HEADER + 8*NUM_BYTES_OBJECT_REF + 8*NUM_BYTES_INT;
  
  final DocValuesType type;
  final Term term;
  final String field;
  final Object value;
  int docIDUpto = -1; // unassigned until applied, and confusing that it's here, when it's just used in BufferedDeletes...

  /**
   * Constructor.
   * 
   * @param term the {@link Term} which determines the documents that will be updated
   * @param field the {@link NumericDocValuesField} to update
   * @param value the updated value
   */
  protected DocValuesUpdate(DocValuesType type, Term term, String field, Object value) {
    this.type = type;
    this.term = term;
    this.field = field;
    this.value = value;
  }

  abstract long valueSizeInBytes();
  
  final int sizeInBytes() {
    int sizeInBytes = RAW_SIZE_IN_BYTES;
    sizeInBytes += term.field.length() * NUM_BYTES_CHAR;
    sizeInBytes += term.bytes.bytes.length;
    sizeInBytes += field.length() * NUM_BYTES_CHAR;
    sizeInBytes += valueSizeInBytes();
    return sizeInBytes;
  }
  
  @Override
  public String toString() {
    return "term=" + term + ",field=" + field + ",value=" + value;
  }
  
  /** An in-place update to a binary DocValues field */
  static final class BinaryDocValuesUpdate extends DocValuesUpdate {
    
    /* Size of BytesRef: 2*INT + ARRAY_HEADER + PTR */
    private static final long RAW_VALUE_SIZE_IN_BYTES = NUM_BYTES_ARRAY_HEADER + 2*NUM_BYTES_INT + NUM_BYTES_OBJECT_REF;
    
    BinaryDocValuesUpdate(Term term, String field, BytesRef value) {
      super(DocValuesType.BINARY, term, field, value);
    }

    @Override
    long valueSizeInBytes() {
      return RAW_VALUE_SIZE_IN_BYTES + ((BytesRef) value).bytes.length;
    }
    
  }

  /** An in-place update to a numeric DocValues field */
  static final class NumericDocValuesUpdate extends DocValuesUpdate {

    NumericDocValuesUpdate(Term term, String field, Long value) {
      super(DocValuesType.NUMERIC, term, field, value);
    }

    @Override
    long valueSizeInBytes() {
      return RamUsageEstimator.NUM_BYTES_LONG;
    }
    
  }

}
