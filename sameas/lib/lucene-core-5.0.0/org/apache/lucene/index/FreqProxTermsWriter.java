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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.lucene.codecs.FieldsConsumer;
import org.apache.lucene.util.CollectionUtil;
import org.apache.lucene.util.IOUtils;

final class FreqProxTermsWriter extends TermsHash {

  public FreqProxTermsWriter(DocumentsWriterPerThread docWriter, TermsHash termVectors) {
    super(docWriter, true, termVectors);
  }

  private void applyDeletes(SegmentWriteState state, Fields fields) throws IOException {

    // Process any pending Term deletes for this newly
    // flushed segment:
    if (state.segUpdates != null && state.segUpdates.terms.size() > 0) {
      Map<Term,Integer> segDeletes = state.segUpdates.terms;
      List<Term> deleteTerms = new ArrayList<>(segDeletes.keySet());
      Collections.sort(deleteTerms);
      String lastField = null;
      TermsEnum termsEnum = null;
      DocsEnum docsEnum = null;
      for(Term deleteTerm : deleteTerms) {
        if (deleteTerm.field().equals(lastField) == false) {
          lastField = deleteTerm.field();
          Terms terms = fields.terms(lastField);
          if (terms != null) {
            termsEnum = terms.iterator(termsEnum);
          } else {
            termsEnum = null;
          }
        }

        if (termsEnum != null && termsEnum.seekExact(deleteTerm.bytes())) {
          docsEnum = termsEnum.docs(null, docsEnum, 0);
          int delDocLimit = segDeletes.get(deleteTerm);
          assert delDocLimit < DocsEnum.NO_MORE_DOCS;
          while (true) {
            int doc = docsEnum.nextDoc();
            if (doc < delDocLimit) {
              if (state.liveDocs == null) {
                state.liveDocs = state.segmentInfo.getCodec().liveDocsFormat().newLiveDocs(state.segmentInfo.getDocCount());
              }
              if (state.liveDocs.get(doc)) {
                state.delCountOnFlush++;
                state.liveDocs.clear(doc);
              }
            } else {
              break;
            }
          }
        }
      }
    }
  }

  @Override
  public void flush(Map<String,TermsHashPerField> fieldsToFlush, final SegmentWriteState state) throws IOException {
    super.flush(fieldsToFlush, state);

    // Gather all fields that saw any postings:
    List<FreqProxTermsWriterPerField> allFields = new ArrayList<>();

    for (TermsHashPerField f : fieldsToFlush.values()) {
      final FreqProxTermsWriterPerField perField = (FreqProxTermsWriterPerField) f;
      if (perField.bytesHash.size() > 0) {
        perField.sortPostings();
        assert perField.fieldInfo.getIndexOptions() != IndexOptions.NONE;
        allFields.add(perField);
      }
    }

    // Sort by field name
    CollectionUtil.introSort(allFields);

    Fields fields = new FreqProxFields(allFields);

    applyDeletes(state, fields);

    FieldsConsumer consumer = state.segmentInfo.getCodec().postingsFormat().fieldsConsumer(state);
    boolean success = false;
    try {
      consumer.write(fields);
      success = true;
    } finally {
      if (success) {
        IOUtils.close(consumer);
      } else {
        IOUtils.closeWhileHandlingException(consumer);
      }
    }

  }

  @Override
  public TermsHashPerField addField(FieldInvertState invertState, FieldInfo fieldInfo) {
    return new FreqProxTermsWriterPerField(invertState, this, fieldInfo, nextTermsHash.addField(invertState, fieldInfo));
  }
}
