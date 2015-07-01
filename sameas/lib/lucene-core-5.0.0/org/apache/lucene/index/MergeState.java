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
import java.util.List;

import org.apache.lucene.codecs.DocValuesProducer;
import org.apache.lucene.codecs.FieldsProducer;
import org.apache.lucene.codecs.NormsProducer;
import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.TermVectorsReader;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.InfoStream;
import org.apache.lucene.util.packed.PackedInts;
import org.apache.lucene.util.packed.PackedLongValues;

/** Holds common state used during segment merging.
 *
 * @lucene.experimental */
public class MergeState {

  /** {@link SegmentInfo} of the newly merged segment. */
  public final SegmentInfo segmentInfo;

  /** {@link FieldInfos} of the newly merged segment. */
  public FieldInfos mergeFieldInfos;

  /** Stored field producers being merged */
  public final StoredFieldsReader[] storedFieldsReaders;

  /** Term vector producers being merged */
  public final TermVectorsReader[] termVectorsReaders;

  /** Norms producers being merged */
  public final NormsProducer[] normsProducers;

  /** DocValues producers being merged */
  public final DocValuesProducer[] docValuesProducers;

  /** FieldInfos being merged */
  public final FieldInfos[] fieldInfos;

  /** Live docs for each reader */
  public final Bits[] liveDocs;

  /** Maps docIDs around deletions. */
  public final DocMap[] docMaps;

  /** Postings to merge */
  public final FieldsProducer[] fieldsProducers;

  /** New docID base per reader. */
  public final int[] docBase;

  /** Max docs per reader */
  public final int[] maxDocs;

  /** InfoStream for debugging messages. */
  public final InfoStream infoStream;

  /** Sole constructor. */
  MergeState(List<CodecReader> readers, SegmentInfo segmentInfo, InfoStream infoStream) throws IOException {

    int numReaders = readers.size();
    docMaps = new DocMap[numReaders];
    docBase = new int[numReaders];
    maxDocs = new int[numReaders];
    fieldsProducers = new FieldsProducer[numReaders];
    normsProducers = new NormsProducer[numReaders];
    storedFieldsReaders = new StoredFieldsReader[numReaders];
    termVectorsReaders = new TermVectorsReader[numReaders];
    docValuesProducers = new DocValuesProducer[numReaders];
    fieldInfos = new FieldInfos[numReaders];
    liveDocs = new Bits[numReaders];

    for(int i=0;i<numReaders;i++) {
      final CodecReader reader = readers.get(i);

      maxDocs[i] = reader.maxDoc();
      liveDocs[i] = reader.getLiveDocs();
      fieldInfos[i] = reader.getFieldInfos();

      normsProducers[i] = reader.getNormsReader();
      if (normsProducers[i] != null) {
        normsProducers[i] = normsProducers[i].getMergeInstance();
      }
      
      docValuesProducers[i] = reader.getDocValuesReader();
      if (docValuesProducers[i] != null) {
        docValuesProducers[i] = docValuesProducers[i].getMergeInstance();
      }
      
      storedFieldsReaders[i] = reader.getFieldsReader();
      if (storedFieldsReaders[i] != null) {
        storedFieldsReaders[i] = storedFieldsReaders[i].getMergeInstance();
      }
      
      termVectorsReaders[i] = reader.getTermVectorsReader();
      if (termVectorsReaders[i] != null) {
        termVectorsReaders[i] = termVectorsReaders[i].getMergeInstance();
      }
      
      fieldsProducers[i] = reader.getPostingsReader().getMergeInstance();
    }

    this.segmentInfo = segmentInfo;
    this.infoStream = infoStream;

    setDocMaps(readers);
  }

  // NOTE: removes any "all deleted" readers from mergeState.readers
  private void setDocMaps(List<CodecReader> readers) throws IOException {
    final int numReaders = maxDocs.length;

    // Remap docIDs
    int docBase = 0;
    for(int i=0;i<numReaders;i++) {
      final CodecReader reader = readers.get(i);
      this.docBase[i] = docBase;
      final DocMap docMap = DocMap.build(reader);
      docMaps[i] = docMap;
      docBase += docMap.numDocs();
    }

    segmentInfo.setDocCount(docBase);
  }

  /**
   * Remaps docids around deletes during merge
   */
  public static abstract class DocMap {

    DocMap() {}

    /** Returns the mapped docID corresponding to the provided one. */
    public abstract int get(int docID);

    /** Returns the total number of documents, ignoring
     *  deletions. */
    public abstract int maxDoc();

    /** Returns the number of not-deleted documents. */
    public final int numDocs() {
      return maxDoc() - numDeletedDocs();
    }

    /** Returns the number of deleted documents. */
    public abstract int numDeletedDocs();

    /** Returns true if there are any deletions. */
    public boolean hasDeletions() {
      return numDeletedDocs() > 0;
    }

    /** Creates a {@link DocMap} instance appropriate for
     *  this reader. */
    public static DocMap build(CodecReader reader) {
      final int maxDoc = reader.maxDoc();
      if (!reader.hasDeletions()) {
        return new NoDelDocMap(maxDoc);
      }
      final Bits liveDocs = reader.getLiveDocs();
      return build(maxDoc, liveDocs);
    }

    static DocMap build(final int maxDoc, final Bits liveDocs) {
      assert liveDocs != null;
      final PackedLongValues.Builder docMapBuilder = PackedLongValues.monotonicBuilder(PackedInts.COMPACT);
      int del = 0;
      for (int i = 0; i < maxDoc; ++i) {
        docMapBuilder.add(i - del);
        if (!liveDocs.get(i)) {
          ++del;
        }
      }
      final PackedLongValues docMap = docMapBuilder.build();
      final int numDeletedDocs = del;
      assert docMap.size() == maxDoc;
      return new DocMap() {

        @Override
        public int get(int docID) {
          if (!liveDocs.get(docID)) {
            return -1;
          }
          return (int) docMap.get(docID);
        }

        @Override
        public int maxDoc() {
          return maxDoc;
        }

        @Override
        public int numDeletedDocs() {
          return numDeletedDocs;
        }
      };
    }
  }

  private static final class NoDelDocMap extends DocMap {

    private final int maxDoc;

    NoDelDocMap(int maxDoc) {
      this.maxDoc = maxDoc;
    }

    @Override
    public int get(int docID) {
      return docID;
    }

    @Override
    public int maxDoc() {
      return maxDoc;
    }

    @Override
    public int numDeletedDocs() {
      return 0;
    }
  }
}
