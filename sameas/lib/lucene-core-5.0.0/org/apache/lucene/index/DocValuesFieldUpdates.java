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

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.packed.PagedGrowableWriter;

/**
 * Holds updates of a single DocValues field, for a set of documents.
 * 
 * @lucene.experimental
 */
abstract class DocValuesFieldUpdates {
  
  protected static final int PAGE_SIZE = 1024;

  /**
   * An iterator over documents and their updated values. Only documents with
   * updates are returned by this iterator, and the documents are returned in
   * increasing order.
   */
  static abstract class Iterator {
    
    /**
     * Returns the next document which has an update, or
     * {@link DocIdSetIterator#NO_MORE_DOCS} if there are no more documents to
     * return.
     */
    abstract int nextDoc();
    
    /** Returns the current document this iterator is on. */
    abstract int doc();
    
    /**
     * Returns the value of the document returned from {@link #nextDoc()}. A
     * {@code null} value means that it was unset for this document.
     */
    abstract Object value();
    
    /**
     * Reset the iterator's state. Should be called before {@link #nextDoc()}
     * and {@link #value()}.
     */
    abstract void reset();
    
  }

  static class Container {
  
    final Map<String,NumericDocValuesFieldUpdates> numericDVUpdates = new HashMap<>();
    final Map<String,BinaryDocValuesFieldUpdates> binaryDVUpdates = new HashMap<>();
    
    boolean any() {
      for (NumericDocValuesFieldUpdates updates : numericDVUpdates.values()) {
        if (updates.any()) {
          return true;
        }
      }
      for (BinaryDocValuesFieldUpdates updates : binaryDVUpdates.values()) {
        if (updates.any()) {
          return true;
        }
      }
      return false;
    }
    
    int size() {
      return numericDVUpdates.size() + binaryDVUpdates.size();
    }
    
    long ramBytesPerDoc() {
      long ramBytesPerDoc = 0;
      for (NumericDocValuesFieldUpdates updates : numericDVUpdates.values()) {
        ramBytesPerDoc += updates.ramBytesPerDoc();
      }
      for (BinaryDocValuesFieldUpdates updates : binaryDVUpdates.values()) {
        ramBytesPerDoc += updates.ramBytesPerDoc();
      }
      return ramBytesPerDoc;
    }
    
    DocValuesFieldUpdates getUpdates(String field, DocValuesType type) {
      switch (type) {
        case NUMERIC:
          return numericDVUpdates.get(field);
        case BINARY:
          return binaryDVUpdates.get(field);
        default:
          throw new IllegalArgumentException("unsupported type: " + type);
      }
    }
    
    DocValuesFieldUpdates newUpdates(String field, DocValuesType type, int maxDoc) {
      switch (type) {
        case NUMERIC:
          assert numericDVUpdates.get(field) == null;
          NumericDocValuesFieldUpdates numericUpdates = new NumericDocValuesFieldUpdates(field, maxDoc);
          numericDVUpdates.put(field, numericUpdates);
          return numericUpdates;
        case BINARY:
          assert binaryDVUpdates.get(field) == null;
          BinaryDocValuesFieldUpdates binaryUpdates = new BinaryDocValuesFieldUpdates(field, maxDoc);
          binaryDVUpdates.put(field, binaryUpdates);
          return binaryUpdates;
        default:
          throw new IllegalArgumentException("unsupported type: " + type);
      }
    }
    
    @Override
    public String toString() {
      return "numericDVUpdates=" + numericDVUpdates + " binaryDVUpdates=" + binaryDVUpdates;
    }
  }
  
  final String field;
  final DocValuesType type;
  
  protected DocValuesFieldUpdates(String field, DocValuesType type) {
    this.field = field;
    if (type == null) {
      throw new NullPointerException("DocValuesType cannot be null");
    }
    this.type = type;
  }
  
  /**
   * Returns the estimated capacity of a {@link PagedGrowableWriter} given the
   * actual number of stored elements.
   */
  protected static int estimateCapacity(int size) {
    return (int) Math.ceil((double) size / PAGE_SIZE) * PAGE_SIZE;
  }
  
  /**
   * Add an update to a document. For unsetting a value you should pass
   * {@code null}.
   */
  public abstract void add(int doc, Object value);
  
  /**
   * Returns an {@link Iterator} over the updated documents and their
   * values.
   */
  public abstract Iterator iterator();
  
  /**
   * Merge with another {@link DocValuesFieldUpdates}. This is called for a
   * segment which received updates while it was being merged. The given updates
   * should override whatever updates are in that instance.
   */
  public abstract void merge(DocValuesFieldUpdates other);

  /** Returns true if this instance contains any updates. */
  public abstract boolean any();
  
  /** Returns approximate RAM bytes used per document. */
  public abstract long ramBytesPerDoc();

}
