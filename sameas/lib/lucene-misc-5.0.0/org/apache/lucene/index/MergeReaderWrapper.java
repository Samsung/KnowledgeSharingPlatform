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

import java.io.IOException;

import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.codecs.FieldsProducer;
import org.apache.lucene.codecs.NormsProducer;
import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.TermVectorsReader;
import org.apache.lucene.util.Bits;

/** this is a hack to make SortingMP fast! */
class MergeReaderWrapper extends LeafReader {
  final SegmentReader in;
  final FieldsProducer fields;
  final NormsProducer norms;
  final DocValuesProducer docValues;
  final StoredFieldsReader store;
  final TermVectorsReader vectors;
  
  MergeReaderWrapper(SegmentReader in) throws IOException {
    this.in = in;
    
    FieldsProducer fields = in.getPostingsReader();
    if (fields != null) {
      fields = fields.getMergeInstance();
    }
    this.fields = fields;
    
    NormsProducer norms = in.getNormsReader();
    if (norms != null) {
      norms = norms.getMergeInstance();
    }
    this.norms = norms;
    
    DocValuesProducer docValues = in.getDocValuesReader();
    if (docValues != null) {
      docValues = docValues.getMergeInstance();
    }
    this.docValues = docValues;
    
    StoredFieldsReader store = in.getFieldsReader();
    if (store != null) {
      store = store.getMergeInstance();
    }
    this.store = store;
    
    TermVectorsReader vectors = in.getTermVectorsReader();
    if (vectors != null) {
      vectors = vectors.getMergeInstance();
    }
    this.vectors = vectors;
  }

  @Override
  public void addCoreClosedListener(CoreClosedListener listener) {
    in.addCoreClosedListener(listener);
  }

  @Override
  public void removeCoreClosedListener(CoreClosedListener listener) {
    in.removeCoreClosedListener(listener);
  }

  @Override
  public Fields fields() throws IOException {
    return fields;
  }

  @Override
  public NumericDocValues getNumericDocValues(String field) throws IOException {
    ensureOpen();
    FieldInfo fi = getFieldInfos().fieldInfo(field);
    if (fi == null) {
      // Field does not exist
      return null;
    }
    if (fi.getDocValuesType() != DocValuesType.NUMERIC) {
      // Field was not indexed with doc values
      return null;
    }
    return docValues.getNumeric(fi);
  }

  @Override
  public BinaryDocValues getBinaryDocValues(String field) throws IOException {
    ensureOpen();
    FieldInfo fi = getFieldInfos().fieldInfo(field);
    if (fi == null) {
      // Field does not exist
      return null;
    }
    if (fi.getDocValuesType() != DocValuesType.BINARY) {
      // Field was not indexed with doc values
      return null;
    }
    return docValues.getBinary(fi);
  }

  @Override
  public SortedDocValues getSortedDocValues(String field) throws IOException {
    ensureOpen();
    FieldInfo fi = getFieldInfos().fieldInfo(field);
    if (fi == null) {
      // Field does not exist
      return null;
    }
    if (fi.getDocValuesType() != DocValuesType.SORTED) {
      // Field was not indexed with doc values
      return null;
    }
    return docValues.getSorted(fi);
  }

  @Override
  public SortedNumericDocValues getSortedNumericDocValues(String field) throws IOException {
    ensureOpen();
    FieldInfo fi = getFieldInfos().fieldInfo(field);
    if (fi == null) {
      // Field does not exist
      return null;
    }
    if (fi.getDocValuesType() != DocValuesType.SORTED_NUMERIC) {
      // Field was not indexed with doc values
      return null;
    }
    return docValues.getSortedNumeric(fi);
  }

  @Override
  public SortedSetDocValues getSortedSetDocValues(String field) throws IOException {
    ensureOpen();
    FieldInfo fi = getFieldInfos().fieldInfo(field);
    if (fi == null) {
      // Field does not exist
      return null;
    }
    if (fi.getDocValuesType() != DocValuesType.SORTED_SET) {
      // Field was not indexed with doc values
      return null;
    }
    return docValues.getSortedSet(fi);
  }

  @Override
  public Bits getDocsWithField(String field) throws IOException {
    ensureOpen();
    FieldInfo fi = getFieldInfos().fieldInfo(field);
    if (fi == null) {
      // Field does not exist
      return null;
    }
    if (fi.getDocValuesType() == DocValuesType.NONE) {
      // Field was not indexed with doc values
      return null;
    }
    return docValues.getDocsWithField(fi);
  }

  @Override
  public NumericDocValues getNormValues(String field) throws IOException {
    ensureOpen();
    FieldInfo fi = getFieldInfos().fieldInfo(field);
    if (fi == null || !fi.hasNorms()) {
      // Field does not exist or does not index norms
      return null;
    }
    return norms.getNorms(fi);
  }

  @Override
  public FieldInfos getFieldInfos() {
    return in.getFieldInfos();
  }

  @Override
  public Bits getLiveDocs() {
    return in.getLiveDocs();
  }

  @Override
  public void checkIntegrity() throws IOException {
    in.checkIntegrity();
  }

  @Override
  public Fields getTermVectors(int docID) throws IOException {
    ensureOpen();
    checkBounds(docID);
    if (vectors == null) {
      return null;
    }
    return vectors.get(docID);
  }

  @Override
  public int numDocs() {
    return in.numDocs();
  }

  @Override
  public int maxDoc() {
    return in.maxDoc();
  }

  @Override
  public void document(int docID, StoredFieldVisitor visitor) throws IOException {
    ensureOpen();
    checkBounds(docID);
    store.visitDocument(docID, visitor);
  }

  @Override
  protected void doClose() throws IOException {
    in.close();
  }

  @Override
  public Object getCoreCacheKey() {
    return in.getCoreCacheKey();
  }

  @Override
  public Object getCombinedCoreAndDeletesKey() {
    return in.getCombinedCoreAndDeletesKey();
  }
  
  private void checkBounds(int docID) {
    if (docID < 0 || docID >= maxDoc()) {       
      throw new IndexOutOfBoundsException("docID must be >= 0 and < maxDoc=" + maxDoc() + " (got docID=" + docID + ")");
    }
  }

  @Override
  public String toString() {
    return "MergeReaderWrapper(" + in + ")";
  }
}
