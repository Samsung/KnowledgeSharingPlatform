package org.apache.lucene.search;

import org.apache.lucene.index.IndexReader; // javadocs
import org.apache.lucene.index.Terms;       // javadocs

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

/**
 * Contains statistics for a collection (field)
 * @lucene.experimental
 */
public class CollectionStatistics {
  private final String field;
  private final long maxDoc;
  private final long docCount;
  private final long sumTotalTermFreq;
  private final long sumDocFreq;
  
  public CollectionStatistics(String field, long maxDoc, long docCount, long sumTotalTermFreq, long sumDocFreq) {
    assert maxDoc >= 0;
    assert docCount >= -1 && docCount <= maxDoc; // #docs with field must be <= #docs
    assert sumDocFreq == -1 || sumDocFreq >= docCount; // #postings must be >= #docs with field
    assert sumTotalTermFreq == -1 || sumTotalTermFreq >= sumDocFreq; // #positions must be >= #postings
    this.field = field;
    this.maxDoc = maxDoc;
    this.docCount = docCount;
    this.sumTotalTermFreq = sumTotalTermFreq;
    this.sumDocFreq = sumDocFreq;
  }
  
  /** returns the field name */
  public final String field() {
    return field;
  }
  
  /** returns the total number of documents, regardless of 
   * whether they all contain values for this field. 
   * @see IndexReader#maxDoc() */
  public final long maxDoc() {
    return maxDoc;
  }
  
  /** returns the total number of documents that
   * have at least one term for this field. 
   * @see Terms#getDocCount() */
  public final long docCount() {
    return docCount;
  }
  
  /** returns the total number of tokens for this field
   * @see Terms#getSumTotalTermFreq() */
  public final long sumTotalTermFreq() {
    return sumTotalTermFreq;
  }
  
  /** returns the total number of postings for this field 
   * @see Terms#getSumDocFreq() */
  public final long sumDocFreq() {
    return sumDocFreq;
  }
}
