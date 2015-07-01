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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.codecs.FieldsProducer;
import org.apache.lucene.codecs.NormsProducer;
import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.TermVectorsReader;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.Bits;

/**
 * Wraps arbitrary readers for merging. Note that this can cause slow
 * and memory-intensive merges. Consider using {@link FilterCodecReader}
 * instead.
 */
public final class SlowCodecReaderWrapper {
  
  /** No instantiation */
  private SlowCodecReaderWrapper() {}
  
  /**
   * Returns a {@code CodecReader} view of reader. 
   * <p>
   * If {@code reader} is already a {@code CodecReader}, it is returned
   * directly. Otherwise, a (slow) view is returned.
   */
  public static CodecReader wrap(final LeafReader reader) throws IOException {
    if (reader instanceof CodecReader) {
      return (CodecReader)reader;
    } else {
      // simulate it slowly, over the leafReader api:
      reader.checkIntegrity();
      return new CodecReader() {

        @Override
        public TermVectorsReader getTermVectorsReader() {
          reader.ensureOpen();
          return readerToTermVectorsReader(reader);
        }

        @Override
        public StoredFieldsReader getFieldsReader() {
          reader.ensureOpen();
          return readerToStoredFieldsReader(reader);
        }

        @Override
        public NormsProducer getNormsReader() {
          reader.ensureOpen();
          return readerToNormsProducer(reader);
        }

        @Override
        public DocValuesProducer getDocValuesReader() {
          reader.ensureOpen();
          return readerToDocValuesProducer(reader);
        }

        @Override
        public FieldsProducer getPostingsReader() {
          reader.ensureOpen();
          try {
            return readerToFieldsProducer(reader);
          } catch (IOException bogus) {
            throw new AssertionError(bogus);
          }
        }

        @Override
        public FieldInfos getFieldInfos() {
          return reader.getFieldInfos();
        }

        @Override
        public Bits getLiveDocs() {
          return reader.getLiveDocs();
        }

        @Override
        public int numDocs() {
          return reader.numDocs();
        }

        @Override
        public int maxDoc() {
          return reader.maxDoc();
        }

        @Override
        public void addCoreClosedListener(CoreClosedListener listener) {
          reader.addCoreClosedListener(listener);
        }

        @Override
        public void removeCoreClosedListener(CoreClosedListener listener) {
          reader.removeCoreClosedListener(listener);
        }
      };
    }
  }
  
  private static NormsProducer readerToNormsProducer(final LeafReader reader) {
    return new NormsProducer() {

      @Override
      public NumericDocValues getNorms(FieldInfo field) throws IOException {
        return reader.getNormValues(field.name);
      }

      @Override
      public void checkIntegrity() throws IOException {
        // We already checkIntegrity the entire reader up front
      }

      @Override
      public void close() {
      }

      @Override
      public long ramBytesUsed() {
        return 0;
      }

      @Override
      public Collection<Accountable> getChildResources() {
        return Collections.emptyList();
      }
    };
  }

  private static DocValuesProducer readerToDocValuesProducer(final LeafReader reader) {
    return new DocValuesProducer() {

      @Override
      public NumericDocValues getNumeric(FieldInfo field) throws IOException {  
        return reader.getNumericDocValues(field.name);
      }

      @Override
      public BinaryDocValues getBinary(FieldInfo field) throws IOException {
        return reader.getBinaryDocValues(field.name);
      }

      @Override
      public SortedDocValues getSorted(FieldInfo field) throws IOException {
        return reader.getSortedDocValues(field.name);
      }

      @Override
      public SortedNumericDocValues getSortedNumeric(FieldInfo field) throws IOException {
        return reader.getSortedNumericDocValues(field.name);
      }

      @Override
      public SortedSetDocValues getSortedSet(FieldInfo field) throws IOException {
        return reader.getSortedSetDocValues(field.name);
      }

      @Override
      public Bits getDocsWithField(FieldInfo field) throws IOException {
        return reader.getDocsWithField(field.name);
      }

      @Override
      public void checkIntegrity() throws IOException {
        // We already checkIntegrity the entire reader up front
      }

      @Override
      public void close() {
      }

      @Override
      public long ramBytesUsed() {
        return 0;
      }
      
      @Override
      public Collection<Accountable> getChildResources() {
        return Collections.emptyList();
      }
    };
  }

  private static StoredFieldsReader readerToStoredFieldsReader(final LeafReader reader) {
    return new StoredFieldsReader() {
      @Override
      public void visitDocument(int docID, StoredFieldVisitor visitor) throws IOException {
        reader.document(docID, visitor);
      }

      @Override
      public StoredFieldsReader clone() {
        return readerToStoredFieldsReader(reader);
      }

      @Override
      public void checkIntegrity() throws IOException {
        // We already checkIntegrity the entire reader up front
      }

      @Override
      public void close() {
      }

      @Override
      public long ramBytesUsed() {
        return 0;
      }
      
      @Override
      public Collection<Accountable> getChildResources() {
        return Collections.emptyList();
      }
    };
  }

  private static TermVectorsReader readerToTermVectorsReader(final LeafReader reader) {
    return new TermVectorsReader() {
      @Override
      public Fields get(int docID) throws IOException {
        return reader.getTermVectors(docID);
      }

      @Override
      public TermVectorsReader clone() {
        return readerToTermVectorsReader(reader);
      }

      @Override
      public void checkIntegrity() throws IOException {
        // We already checkIntegrity the entire reader up front
      }

      @Override
      public void close() {
      }

      @Override
      public long ramBytesUsed() {
        return 0;
      }
      
      @Override
      public Collection<Accountable> getChildResources() {
        return Collections.emptyList();
      }
    };
  }

  private static FieldsProducer readerToFieldsProducer(final LeafReader reader) throws IOException {
    final Fields fields = reader.fields();
    return new FieldsProducer() {
      @Override
      public Iterator<String> iterator() {
        return fields.iterator();
      }

      @Override
      public Terms terms(String field) throws IOException {
        return fields.terms(field);
      }

      @Override
      public int size() {
        return fields.size();
      }

      @Override
      public void checkIntegrity() throws IOException {
        // We already checkIntegrity the entire reader up front
      }

      @Override
      public void close() {
      }

      @Override
      public long ramBytesUsed() {
        return 0;
      }
      
      @Override
      public Collection<Accountable> getChildResources() {
        return Collections.emptyList();
      }
    };
  }
}
