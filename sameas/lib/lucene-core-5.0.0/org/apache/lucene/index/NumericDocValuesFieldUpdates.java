package org.apache.lucene.index;

import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.InPlaceMergeSorter;
import org.apache.lucene.util.packed.PackedInts;
import org.apache.lucene.util.packed.PagedGrowableWriter;
import org.apache.lucene.util.packed.PagedMutable;

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
 * A {@link DocValuesFieldUpdates} which holds updates of documents, of a single
 * {@link NumericDocValuesField}.
 * 
 * @lucene.experimental
 */
class NumericDocValuesFieldUpdates extends DocValuesFieldUpdates {
  
  final static class Iterator extends DocValuesFieldUpdates.Iterator {
    private final int size;
    private final PagedGrowableWriter values;
    private final PagedMutable docs;
    private long idx = 0; // long so we don't overflow if size == Integer.MAX_VALUE
    private int doc = -1;
    private Long value = null;
    
    Iterator(int size, PagedGrowableWriter values, PagedMutable docs) {
      this.size = size;
      this.values = values;
      this.docs = docs;
    }
    
    @Override
    Long value() {
      return value;
    }
    
    @Override
    int nextDoc() {
      if (idx >= size) {
        value = null;
        return doc = DocIdSetIterator.NO_MORE_DOCS;
      }
      doc = (int) docs.get(idx);
      ++idx;
      while (idx < size && docs.get(idx) == doc) {
        ++idx;
      }
      // idx points to the "next" element
      value = Long.valueOf(values.get(idx - 1));
      return doc;
    }
    
    @Override
    int doc() {
      return doc;
    }
    
    @Override
    void reset() {
      doc = -1;
      value = null;
      idx = 0;
    }
  }

  private final int bitsPerValue;
  private PagedMutable docs;
  private PagedGrowableWriter values;
  private int size;
  
  public NumericDocValuesFieldUpdates(String field, int maxDoc) {
    super(field, DocValuesType.NUMERIC);
    bitsPerValue = PackedInts.bitsRequired(maxDoc - 1);
    docs = new PagedMutable(1, PAGE_SIZE, bitsPerValue, PackedInts.COMPACT);
    values = new PagedGrowableWriter(1, PAGE_SIZE, 1, PackedInts.FAST);
    size = 0;
  }
  
  @Override
  public void add(int doc, Object value) {
    // TODO: if the Sorter interface changes to take long indexes, we can remove that limitation
    if (size == Integer.MAX_VALUE) {
      throw new IllegalStateException("cannot support more than Integer.MAX_VALUE doc/value entries");
    }

    Long val = (Long) value;
    
    // grow the structures to have room for more elements
    if (docs.size() == size) {
      docs = docs.grow(size + 1);
      values = values.grow(size + 1);
    }
    
    docs.set(size, doc);
    values.set(size, val.longValue());
    ++size;
  }
  
  @Override
  public Iterator iterator() {
    final PagedMutable docs = this.docs;
    final PagedGrowableWriter values = this.values;
    new InPlaceMergeSorter() {
      @Override
      protected void swap(int i, int j) {
        long tmpDoc = docs.get(j);
        docs.set(j, docs.get(i));
        docs.set(i, tmpDoc);
        
        long tmpVal = values.get(j);
        values.set(j, values.get(i));
        values.set(i, tmpVal);
      }
      
      @Override
      protected int compare(int i, int j) {
        int x = (int) docs.get(i);
        int y = (int) docs.get(j);
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
      }
    }.sort(0, size);
    
    return new Iterator(size, values, docs);
  }
  
  @Override
  public void merge(DocValuesFieldUpdates other) {
    assert other instanceof NumericDocValuesFieldUpdates;
    NumericDocValuesFieldUpdates otherUpdates = (NumericDocValuesFieldUpdates) other;
    if (size  + otherUpdates.size > Integer.MAX_VALUE) {
      throw new IllegalStateException(
          "cannot support more than Integer.MAX_VALUE doc/value entries; size="
              + size + " other.size=" + otherUpdates.size);
    }
    docs = docs.grow(size + otherUpdates.size);
    values = values.grow(size + otherUpdates.size);
    for (int i = 0; i < otherUpdates.size; i++) {
      int doc = (int) otherUpdates.docs.get(i);
      docs.set(size, doc);
      values.set(size, otherUpdates.values.get(i));
      ++size;
    }
  }

  @Override
  public boolean any() {
    return size > 0;
  }

  @Override
  public long ramBytesPerDoc() {
    long bytesPerDoc = (long) Math.ceil((double) (bitsPerValue) / 8);
    final int capacity = estimateCapacity(size);
    bytesPerDoc += (long) Math.ceil((double) values.ramBytesUsed() / capacity); // values
    return bytesPerDoc;
  }
  
}
