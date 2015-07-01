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

import org.apache.lucene.document.BinaryDocValuesField;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.InPlaceMergeSorter;
import org.apache.lucene.util.packed.PackedInts;
import org.apache.lucene.util.packed.PagedGrowableWriter;
import org.apache.lucene.util.packed.PagedMutable;

/**
 * A {@link DocValuesFieldUpdates} which holds updates of documents, of a single
 * {@link BinaryDocValuesField}.
 * 
 * @lucene.experimental
 */
class BinaryDocValuesFieldUpdates extends DocValuesFieldUpdates {
  
  final static class Iterator extends DocValuesFieldUpdates.Iterator {
    private final PagedGrowableWriter offsets;
    private final int size;
    private final PagedGrowableWriter lengths;
    private final PagedMutable docs;
    private long idx = 0; // long so we don't overflow if size == Integer.MAX_VALUE
    private int doc = -1;
    private final BytesRef value;
    private int offset, length;
    
    Iterator(int size, PagedGrowableWriter offsets, PagedGrowableWriter lengths, 
        PagedMutable docs, BytesRef values) {
      this.offsets = offsets;
      this.size = size;
      this.lengths = lengths;
      this.docs = docs;
      value = values.clone();
    }
    
    @Override
    BytesRef value() {
      value.offset = offset;
      value.length = length;
      return value;
    }
    
    @Override
    int nextDoc() {
      if (idx >= size) {
        offset = -1;
        return doc = DocIdSetIterator.NO_MORE_DOCS;
      }
      doc = (int) docs.get(idx);
      ++idx;
      while (idx < size && docs.get(idx) == doc) {
        ++idx;
      }
      // idx points to the "next" element
      long prevIdx = idx - 1;
      // cannot change 'value' here because nextDoc is called before the
      // value is used, and it's a waste to clone the BytesRef when we
      // obtain the value
      offset = (int) offsets.get(prevIdx);
      length = (int) lengths.get(prevIdx);
      return doc;
    }
    
    @Override
    int doc() {
      return doc;
    }
    
    @Override
    void reset() {
      doc = -1;
      offset = -1;
      idx = 0;
    }
  }

  private PagedMutable docs;
  private PagedGrowableWriter offsets, lengths;
  private BytesRefBuilder values;
  private int size;
  private final int bitsPerValue;
  
  public BinaryDocValuesFieldUpdates(String field, int maxDoc) {
    super(field, DocValuesType.BINARY);
    bitsPerValue = PackedInts.bitsRequired(maxDoc - 1);
    docs = new PagedMutable(1, PAGE_SIZE, bitsPerValue, PackedInts.COMPACT);
    offsets = new PagedGrowableWriter(1, PAGE_SIZE, 1, PackedInts.FAST);
    lengths = new PagedGrowableWriter(1, PAGE_SIZE, 1, PackedInts.FAST);
    values = new BytesRefBuilder();
    size = 0;
  }
  
  @Override
  public void add(int doc, Object value) {
    // TODO: if the Sorter interface changes to take long indexes, we can remove that limitation
    if (size == Integer.MAX_VALUE) {
      throw new IllegalStateException("cannot support more than Integer.MAX_VALUE doc/value entries");
    }

    BytesRef val = (BytesRef) value;
    
    // grow the structures to have room for more elements
    if (docs.size() == size) {
      docs = docs.grow(size + 1);
      offsets = offsets.grow(size + 1);
      lengths = lengths.grow(size + 1);
    }
    
    docs.set(size, doc);
    offsets.set(size, values.length());
    lengths.set(size, val.length);
    values.append(val);
    ++size;
  }

  @Override
  public Iterator iterator() {
    final PagedMutable docs = this.docs;
    final PagedGrowableWriter offsets = this.offsets;
    final PagedGrowableWriter lengths = this.lengths;
    final BytesRef values = this.values.get();
    new InPlaceMergeSorter() {
      @Override
      protected void swap(int i, int j) {
        long tmpDoc = docs.get(j);
        docs.set(j, docs.get(i));
        docs.set(i, tmpDoc);
        
        long tmpOffset = offsets.get(j);
        offsets.set(j, offsets.get(i));
        offsets.set(i, tmpOffset);

        long tmpLength = lengths.get(j);
        lengths.set(j, lengths.get(i));
        lengths.set(i, tmpLength);
      }
      
      @Override
      protected int compare(int i, int j) {
        int x = (int) docs.get(i);
        int y = (int) docs.get(j);
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
      }
    }.sort(0, size);
    
    return new Iterator(size, offsets, lengths, docs, values);
  }

  @Override
  public void merge(DocValuesFieldUpdates other) {
    BinaryDocValuesFieldUpdates otherUpdates = (BinaryDocValuesFieldUpdates) other;
    int newSize = size  + otherUpdates.size;
    if (newSize > Integer.MAX_VALUE) {
      throw new IllegalStateException(
          "cannot support more than Integer.MAX_VALUE doc/value entries; size="
              + size + " other.size=" + otherUpdates.size);
    }
    docs = docs.grow(newSize);
    offsets = offsets.grow(newSize);
    lengths = lengths.grow(newSize);
    for (int i = 0; i < otherUpdates.size; i++) {
      int doc = (int) otherUpdates.docs.get(i);
      docs.set(size, doc);
      offsets.set(size, values.length() + otherUpdates.offsets.get(i)); // correct relative offset
      lengths.set(size, otherUpdates.lengths.get(i));
      ++size;
    }

    values.append(otherUpdates.values);
  }

  @Override
  public boolean any() {
    return size > 0;
  }

  @Override
  public long ramBytesPerDoc() {
    long bytesPerDoc = (long) Math.ceil((double) (bitsPerValue) / 8); // docs
    final int capacity = estimateCapacity(size);
    bytesPerDoc += (long) Math.ceil((double) offsets.ramBytesUsed() / capacity); // offsets
    bytesPerDoc += (long) Math.ceil((double) lengths.ramBytesUsed() / capacity); // lengths
    bytesPerDoc += (long) Math.ceil((double) values.length() / size); // values
    return bytesPerDoc;
  }

}
