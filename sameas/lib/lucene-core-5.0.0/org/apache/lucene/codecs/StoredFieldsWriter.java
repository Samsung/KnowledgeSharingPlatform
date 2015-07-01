package org.apache.lucene.codecs;

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

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.index.MergeState;
import org.apache.lucene.index.StoredFieldVisitor;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

/**
 * Codec API for writing stored fields:
 * <p>
 * <ol>
 *   <li>For every document, {@link #startDocument()} is called,
 *       informing the Codec that a new document has started.
 *   <li>{@link #writeField(FieldInfo, IndexableField)} is called for 
 *       each field in the document.
 *   <li>After all documents have been written, {@link #finish(FieldInfos, int)} 
 *       is called for verification/sanity-checks.
 *   <li>Finally the writer is closed ({@link #close()})
 * </ol>
 * 
 * @lucene.experimental
 */
public abstract class StoredFieldsWriter implements Closeable {
  
  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected StoredFieldsWriter() {
  }

  /** Called before writing the stored fields of the document.
   *  {@link #writeField(FieldInfo, IndexableField)} will be called
   *  for each stored field. Note that this is
   *  called even if the document has no stored fields. */
  public abstract void startDocument() throws IOException;

  /** Called when a document and all its fields have been added. */
  public void finishDocument() throws IOException {}

  /** Writes a single stored field. */
  public abstract void writeField(FieldInfo info, IndexableField field) throws IOException;
  
  /** Called before {@link #close()}, passing in the number
   *  of documents that were written. Note that this is 
   *  intentionally redundant (equivalent to the number of
   *  calls to {@link #startDocument()}, but a Codec should
   *  check that this is the case to detect the JRE bug described 
   *  in LUCENE-1282. */
  public abstract void finish(FieldInfos fis, int numDocs) throws IOException;
  
  /** Merges in the stored fields from the readers in 
   *  <code>mergeState</code>. The default implementation skips
   *  over deleted documents, and uses {@link #startDocument()},
   *  {@link #writeField(FieldInfo, IndexableField)}, and {@link #finish(FieldInfos, int)},
   *  returning the number of documents that were written.
   *  Implementations can override this method for more sophisticated
   *  merging (bulk-byte copying, etc). */
  public int merge(MergeState mergeState) throws IOException {
    int docCount = 0;
    for (int i=0;i<mergeState.storedFieldsReaders.length;i++) {
      StoredFieldsReader storedFieldsReader = mergeState.storedFieldsReaders[i];
      storedFieldsReader.checkIntegrity();
      MergeVisitor visitor = new MergeVisitor(mergeState, i);
      int maxDoc = mergeState.maxDocs[i];
      Bits liveDocs = mergeState.liveDocs[i];
      for (int docID=0;docID<maxDoc;docID++) {
        if (liveDocs != null && !liveDocs.get(docID)) {
          // skip deleted docs
          continue;
        }
        startDocument();
        storedFieldsReader.visitDocument(docID, visitor);
        finishDocument();
        docCount++;
      }
    }
    finish(mergeState.mergeFieldInfos, docCount);
    return docCount;
  }
  
  /** 
   * A visitor that adds every field it sees.
   * <p>
   * Use like this:
   * <pre>
   * MergeVisitor visitor = new MergeVisitor(mergeState, readerIndex);
   * for (...) {
   *   startDocument();
   *   storedFieldsReader.visitDocument(docID, visitor);
   *   finishDocument();
   * }
   * </pre>
   */
  protected class MergeVisitor extends StoredFieldVisitor implements IndexableField {
    BytesRef binaryValue;
    String stringValue;
    Number numericValue;
    FieldInfo currentField;
    FieldInfos remapper;
    
    /**
     * Create new merge visitor.
     */
    public MergeVisitor(MergeState mergeState, int readerIndex) {
      // if field numbers are aligned, we can save hash lookups
      // on every field access. Otherwise, we need to lookup
      // fieldname each time, and remap to a new number.
      for (FieldInfo fi : mergeState.fieldInfos[readerIndex]) {
        FieldInfo other = mergeState.mergeFieldInfos.fieldInfo(fi.number);
        if (other == null || !other.name.equals(fi.name)) {
          remapper = mergeState.mergeFieldInfos;
          break;
        }
      }
    }
    
    @Override
    public void binaryField(FieldInfo fieldInfo, byte[] value) throws IOException {
      reset(fieldInfo);
      binaryValue = new BytesRef(value);
      write();
    }

    @Override
    public void stringField(FieldInfo fieldInfo, String value) throws IOException {
      reset(fieldInfo);
      stringValue = value;
      write();
    }

    @Override
    public void intField(FieldInfo fieldInfo, int value) throws IOException {
      reset(fieldInfo);
      numericValue = value;
      write();
    }

    @Override
    public void longField(FieldInfo fieldInfo, long value) throws IOException {
      reset(fieldInfo);
      numericValue = value;
      write();
    }

    @Override
    public void floatField(FieldInfo fieldInfo, float value) throws IOException {
      reset(fieldInfo);
      numericValue = value;
      write();
    }

    @Override
    public void doubleField(FieldInfo fieldInfo, double value) throws IOException {
      reset(fieldInfo);
      numericValue = value;
      write();
    }

    @Override
    public Status needsField(FieldInfo fieldInfo) throws IOException {
      return Status.YES;
    }

    @Override
    public String name() {
      return currentField.name;
    }

    @Override
    public IndexableFieldType fieldType() {
      return StoredField.TYPE;
    }

    @Override
    public BytesRef binaryValue() {
      return binaryValue;
    }

    @Override
    public String stringValue() {
      return stringValue;
    }

    @Override
    public Number numericValue() {
      return numericValue;
    }

    @Override
    public Reader readerValue() {
      return null;
    }
    
    @Override
    public float boost() {
      return 1F;
    }

    @Override
    public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) throws IOException {
      return null;
    }

    void reset(FieldInfo field) {
      if (remapper != null) {
        // field numbers are not aligned, we need to remap to the new field number
        currentField = remapper.fieldInfo(field.name);
      } else {
        currentField = field;
      }
      binaryValue = null;
      stringValue = null;
      numericValue = null;
    }
    
    void write() throws IOException {
      writeField(currentField, this);
    }
  }

  @Override
  public abstract void close() throws IOException;
}
