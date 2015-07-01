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

import org.apache.lucene.util.Bits;

import static org.apache.lucene.index.FilterLeafReader.FilterFields;
import static org.apache.lucene.index.FilterLeafReader.FilterTerms;
import static org.apache.lucene.index.FilterLeafReader.FilterTermsEnum;

/** A {@link Fields} implementation that merges multiple
 *  Fields into one, and maps around deleted documents.
 *  This is used for merging. 
 *  @lucene.internal
 */
public class MappedMultiFields extends FilterFields {
  final MergeState mergeState;

  /** Create a new MappedMultiFields for merging, based on the supplied
   * mergestate and merged view of terms. */
  public MappedMultiFields(MergeState mergeState, MultiFields multiFields) {
    super(multiFields);
    this.mergeState = mergeState;
  }

  @Override
  public Terms terms(String field) throws IOException {
    MultiTerms terms = (MultiTerms) in.terms(field);
    if (terms == null) {
      return null;
    } else {
      return new MappedMultiTerms(mergeState, terms);
    }
  }

  private static class MappedMultiTerms extends FilterTerms {
    final MergeState mergeState;

    public MappedMultiTerms(MergeState mergeState, MultiTerms multiTerms) {
      super(multiTerms);
      this.mergeState = mergeState;
    }

    @Override
    public TermsEnum iterator(TermsEnum reuse) throws IOException {
      return new MappedMultiTermsEnum(mergeState, (MultiTermsEnum) in.iterator(reuse));
    }

    @Override
    public long size() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getSumTotalTermFreq() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getSumDocFreq() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getDocCount() throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  private static class MappedMultiTermsEnum extends FilterTermsEnum {
    final MergeState mergeState;

    public MappedMultiTermsEnum(MergeState mergeState, MultiTermsEnum multiTermsEnum) {
      super(multiTermsEnum);
      this.mergeState = mergeState;
    }

    @Override
    public int docFreq() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public long totalTermFreq() throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public DocsEnum docs(Bits liveDocs, DocsEnum reuse, int flags) throws IOException {
      if (liveDocs != null) {
        throw new IllegalArgumentException("liveDocs must be null");
      }
      MappingMultiDocsEnum mappingDocsEnum;
      if (reuse instanceof MappingMultiDocsEnum) {
        mappingDocsEnum = (MappingMultiDocsEnum) reuse;
      } else {
        mappingDocsEnum = new MappingMultiDocsEnum(mergeState);
      }
      
      MultiDocsEnum docsEnum = (MultiDocsEnum) in.docs(liveDocs, mappingDocsEnum.multiDocsEnum, flags);
      mappingDocsEnum.reset(docsEnum);
      return mappingDocsEnum;
    }

    @Override
    public DocsAndPositionsEnum docsAndPositions(Bits liveDocs, DocsAndPositionsEnum reuse, int flags) throws IOException {
      if (liveDocs != null) {
        throw new IllegalArgumentException("liveDocs must be null");
      }
      MappingMultiDocsAndPositionsEnum mappingDocsAndPositionsEnum;
      if (reuse instanceof MappingMultiDocsAndPositionsEnum) {
        mappingDocsAndPositionsEnum = (MappingMultiDocsAndPositionsEnum) reuse;
      } else {
        mappingDocsAndPositionsEnum = new MappingMultiDocsAndPositionsEnum(mergeState);
      }
      
      MultiDocsAndPositionsEnum docsAndPositionsEnum = (MultiDocsAndPositionsEnum) in.docsAndPositions(liveDocs, mappingDocsAndPositionsEnum.multiDocsAndPositionsEnum, flags);
      mappingDocsAndPositionsEnum.reset(docsAndPositionsEnum);
      return mappingDocsAndPositionsEnum;
    }
  }
}
