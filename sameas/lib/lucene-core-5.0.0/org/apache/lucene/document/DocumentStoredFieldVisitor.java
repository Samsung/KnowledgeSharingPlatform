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

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFieldVisitor;

/** A {@link StoredFieldVisitor} that creates a {@link
 *  Document} containing all stored fields, or only specific
 *  requested fields provided to {@link #DocumentStoredFieldVisitor(Set)}.
 *  <p>
 *  This is used by {@link IndexReader#document(int)} to load a
 *  document.
 *
 * @lucene.experimental */

public class DocumentStoredFieldVisitor extends StoredFieldVisitor {
  private final Document doc = new Document();
  private final Set<String> fieldsToAdd;

  /** 
   * Load only fields named in the provided <code>Set&lt;String&gt;</code>. 
   * @param fieldsToAdd Set of fields to load, or <code>null</code> (all fields).
   */
  public DocumentStoredFieldVisitor(Set<String> fieldsToAdd) {
    this.fieldsToAdd = fieldsToAdd;
  }

  /** Load only fields named in the provided fields. */
  public DocumentStoredFieldVisitor(String... fields) {
    fieldsToAdd = new HashSet<>(fields.length);
    for(String field : fields) {
      fieldsToAdd.add(field);
    }
  }

  /** Load all stored fields. */
  public DocumentStoredFieldVisitor() {
    this.fieldsToAdd = null;
  }

  @Override
  public void binaryField(FieldInfo fieldInfo, byte[] value) throws IOException {
    doc.add(new StoredField(fieldInfo.name, value));
  }

  @Override
  public void stringField(FieldInfo fieldInfo, String value) throws IOException {
    final FieldType ft = new FieldType(TextField.TYPE_STORED);
    ft.setStoreTermVectors(fieldInfo.hasVectors());
    ft.setOmitNorms(fieldInfo.omitsNorms());
    ft.setIndexOptions(fieldInfo.getIndexOptions());
    doc.add(new Field(fieldInfo.name, value, ft));
  }

  @Override
  public void intField(FieldInfo fieldInfo, int value) {
    doc.add(new StoredField(fieldInfo.name, value));
  }

  @Override
  public void longField(FieldInfo fieldInfo, long value) {
    doc.add(new StoredField(fieldInfo.name, value));
  }

  @Override
  public void floatField(FieldInfo fieldInfo, float value) {
    doc.add(new StoredField(fieldInfo.name, value));
  }

  @Override
  public void doubleField(FieldInfo fieldInfo, double value) {
    doc.add(new StoredField(fieldInfo.name, value));
  }

  @Override
  public Status needsField(FieldInfo fieldInfo) throws IOException {
    return fieldsToAdd == null || fieldsToAdd.contains(fieldInfo.name) ? Status.YES : Status.NO;
  }

  /**
   * Retrieve the visited document.
   * @return Document populated with stored fields. Note that only
   *         the stored information in the field instances is valid,
   *         data such as boosts, indexing options, term vector options,
   *         etc is not set.
   */
  public Document getDocument() {
    return doc;
  }
}
