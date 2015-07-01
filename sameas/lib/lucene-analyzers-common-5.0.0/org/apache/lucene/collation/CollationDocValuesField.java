package org.apache.lucene.collation;

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

import java.text.Collator;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.search.DocValuesRangeFilter;
import org.apache.lucene.util.BytesRef;

/**
 * Indexes collation keys as a single-valued {@link SortedDocValuesField}.
 * <p>
 * This is more efficient that {@link CollationKeyAnalyzer} if the field 
 * only has one value: no uninversion is necessary to sort on the field, 
 * locale-sensitive range queries can still work via {@link DocValuesRangeFilter}, 
 * and the underlying data structures built at index-time are likely more efficient 
 * and use less memory than FieldCache.
 */
public final class CollationDocValuesField extends Field {
  private final String name;
  private final Collator collator;
  private final BytesRef bytes = new BytesRef();
  
  /**
   * Create a new ICUCollationDocValuesField.
   * <p>
   * NOTE: you should not create a new one for each document, instead
   * just make one and reuse it during your indexing process, setting
   * the value via {@link #setStringValue(String)}.
   * @param name field name
   * @param collator Collator for generating collation keys.
   */
  // TODO: can we make this trap-free? maybe just synchronize on the collator
  // instead? 
  public CollationDocValuesField(String name, Collator collator) {
    super(name, SortedDocValuesField.TYPE);
    this.name = name;
    this.collator = (Collator) collator.clone();
    fieldsData = bytes; // so wrong setters cannot be called
  }

  @Override
  public String name() {
    return name;
  }
  
  @Override
  public void setStringValue(String value) {
    bytes.bytes = collator.getCollationKey(value).toByteArray();
    bytes.offset = 0;
    bytes.length = bytes.bytes.length;
  }
}
