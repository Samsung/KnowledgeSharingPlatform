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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Fields;
import org.apache.lucene.index.MappedMultiFields;
import org.apache.lucene.index.MergeState;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.ReaderSlice;

/** 
 * Abstract API that consumes terms, doc, freq, prox, offset and
 * payloads postings.  Concrete implementations of this
 * actually do "something" with the postings (write it into
 * the index in a specific format).
 *
 * @lucene.experimental
 */

public abstract class FieldsConsumer implements Closeable {

  /** Sole constructor. (For invocation by subclass 
   *  constructors, typically implicit.) */
  protected FieldsConsumer() {
  }

  // TODO: can we somehow compute stats for you...?

  // TODO: maybe we should factor out "limited" (only
  // iterables, no counts/stats) base classes from
  // Fields/Terms/Docs/AndPositions?

  /** Write all fields, terms and postings.  This the "pull"
   *  API, allowing you to iterate more than once over the
   *  postings, somewhat analogous to using a DOM API to
   *  traverse an XML tree.
   *
   *  <p><b>Notes</b>:
   *
   *  <ul>
   *    <li> You must compute index statistics,
   *         including each Term's docFreq and totalTermFreq,
   *         as well as the summary sumTotalTermFreq,
   *         sumTotalDocFreq and docCount.
   *
   *    <li> You must skip terms that have no docs and
   *         fields that have no terms, even though the provided
   *         Fields API will expose them; this typically
   *         requires lazily writing the field or term until
   *         you've actually seen the first term or
   *         document.
   *
   *    <li> The provided Fields instance is limited: you
   *         cannot call any methods that return
   *         statistics/counts; you cannot pass a non-null
   *         live docs when pulling docs/positions enums.
   *  </ul>
   */
  public abstract void write(Fields fields) throws IOException;
  
  /** Merges in the fields from the readers in 
   *  <code>mergeState</code>. The default implementation skips
   *  and maps around deleted documents, and calls {@link #write(Fields)}.
   *  Implementations can override this method for more sophisticated
   *  merging (bulk-byte copying, etc). */
  public void merge(MergeState mergeState) throws IOException {
    final List<Fields> fields = new ArrayList<>();
    final List<ReaderSlice> slices = new ArrayList<>();

    int docBase = 0;

    for(int readerIndex=0;readerIndex<mergeState.fieldsProducers.length;readerIndex++) {
      final FieldsProducer f = mergeState.fieldsProducers[readerIndex];

      final int maxDoc = mergeState.maxDocs[readerIndex];
      f.checkIntegrity();
      slices.add(new ReaderSlice(docBase, maxDoc, readerIndex));
      fields.add(f);
      docBase += maxDoc;
    }

    Fields mergedFields = new MappedMultiFields(mergeState, 
                                                new MultiFields(fields.toArray(Fields.EMPTY_ARRAY),
                                                                slices.toArray(ReaderSlice.EMPTY_ARRAY)));
    write(mergedFields);
  }

  // NOTE: strange but necessary so javadocs linting is happy:
  @Override
  public abstract void close() throws IOException;
}
