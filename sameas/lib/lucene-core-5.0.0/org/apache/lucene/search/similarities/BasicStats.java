package org.apache.lucene.search.similarities;

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

import org.apache.lucene.index.Terms;

/**
 * Stores all statistics commonly used ranking methods.
 * @lucene.experimental
 */
public class BasicStats extends Similarity.SimWeight {
  final String field;
  /** The number of documents. */
  protected long numberOfDocuments;
  /** The total number of tokens in the field. */
  protected long numberOfFieldTokens;
  /** The average field length. */
  protected float avgFieldLength;
  /** The document frequency. */
  protected long docFreq;
  /** The total number of occurrences of this term across all documents. */
  protected long totalTermFreq;
  
  // -------------------------- Boost-related stuff --------------------------
  
  /** Query's inner boost. */
  protected final float queryBoost;
  /** Any outer query's boost. */
  protected float topLevelBoost;
  /** For most Similarities, the immediate and the top level query boosts are
   * not handled differently. Hence, this field is just the product of the
   * other two. */
  protected float totalBoost;
  
  /** Constructor. Sets the query boost. */
  public BasicStats(String field, float queryBoost) {
    this.field = field;
    this.queryBoost = queryBoost;
    this.totalBoost = queryBoost;
  }
  
  // ------------------------- Getter/setter methods -------------------------
  
  /** Returns the number of documents. */
  public long getNumberOfDocuments() {
    return numberOfDocuments;
  }
  
  /** Sets the number of documents. */
  public void setNumberOfDocuments(long numberOfDocuments) {
    this.numberOfDocuments = numberOfDocuments;
  }
  
  /**
   * Returns the total number of tokens in the field.
   * @see Terms#getSumTotalTermFreq()
   */
  public long getNumberOfFieldTokens() {
    return numberOfFieldTokens;
  }
  
  /**
   * Sets the total number of tokens in the field.
   * @see Terms#getSumTotalTermFreq()
   */
  public void setNumberOfFieldTokens(long numberOfFieldTokens) {
    this.numberOfFieldTokens = numberOfFieldTokens;
  }
  
  /** Returns the average field length. */
  public float getAvgFieldLength() {
    return avgFieldLength;
  }
  
  /** Sets the average field length. */
  public void setAvgFieldLength(float avgFieldLength) {
    this.avgFieldLength = avgFieldLength;
  }
  
  /** Returns the document frequency. */
  public long getDocFreq() {
    return docFreq;
  }
  
  /** Sets the document frequency. */
  public void setDocFreq(long docFreq) {
    this.docFreq = docFreq;
  }
  
  /** Returns the total number of occurrences of this term across all documents. */
  public long getTotalTermFreq() {
    return totalTermFreq;
  }
  
  /** Sets the total number of occurrences of this term across all documents. */
  public void setTotalTermFreq(long totalTermFreq) {
    this.totalTermFreq = totalTermFreq;
  }
  
  // -------------------------- Boost-related stuff --------------------------
  
  /** The square of the raw normalization value.
   * @see #rawNormalizationValue() */
  @Override
  public float getValueForNormalization() {
    float rawValue = rawNormalizationValue();
    return rawValue * rawValue;
  }
  
  /** Computes the raw normalization value. This basic implementation returns
   * the query boost. Subclasses may override this method to include other
   * factors (such as idf), or to save the value for inclusion in
   * {@link #normalize(float, float)}, etc.
   */
  protected float rawNormalizationValue() {
    return queryBoost;
  }
  
  /** No normalization is done. {@code topLevelBoost} is saved in the object,
   * however. */
  @Override
  public void normalize(float queryNorm, float topLevelBoost) {
    this.topLevelBoost = topLevelBoost;
    totalBoost = queryBoost * topLevelBoost;
  }
  
  /** Returns the total boost. */
  public float getTotalBoost() {
    return totalBoost;
  }
}
