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

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;

/**
 * Expert: provides a low-level means of accessing the stored field
 * values in an index.  See {@link IndexReader#document(int,
 * StoredFieldVisitor)}.
 *
 * <p><b>NOTE</b>: a {@code StoredFieldVisitor} implementation
 * should not try to load or visit other stored documents in
 * the same reader because the implementation of stored
 * fields for most codecs is not reeentrant and you will see
 * strange exceptions as a result.
 *
 * <p>See {@link DocumentStoredFieldVisitor}, which is a
 * <code>StoredFieldVisitor</code> that builds the
 * {@link Document} containing all stored fields.  This is
 * used by {@link IndexReader#document(int)}.
 *
 * @lucene.experimental */

public abstract class StoredFieldVisitor {

  /** Sole constructor. (For invocation by subclass 
   * constructors, typically implicit.) */
  protected StoredFieldVisitor() {
  }
  
  /** Process a binary field. 
   * @param value newly allocated byte array with the binary contents. 
   */
  public void binaryField(FieldInfo fieldInfo, byte[] value) throws IOException {
  }

  /** Process a string field */
  public void stringField(FieldInfo fieldInfo, String value) throws IOException {
  }

  /** Process a int numeric field. */
  public void intField(FieldInfo fieldInfo, int value) throws IOException {
  }

  /** Process a long numeric field. */
  public void longField(FieldInfo fieldInfo, long value) throws IOException {
  }

  /** Process a float numeric field. */
  public void floatField(FieldInfo fieldInfo, float value) throws IOException {
  }

  /** Process a double numeric field. */
  public void doubleField(FieldInfo fieldInfo, double value) throws IOException {
  }
  
  /**
   * Hook before processing a field.
   * Before a field is processed, this method is invoked so that
   * subclasses can return a {@link Status} representing whether
   * they need that particular field or not, or to stop processing
   * entirely.
   */
  public abstract Status needsField(FieldInfo fieldInfo) throws IOException;
  
  /**
   * Enumeration of possible return values for {@link #needsField}.
   */
  public static enum Status {
    /** YES: the field should be visited. */
    YES,
    /** NO: don't visit this field, but continue processing fields for this document. */
    NO,
    /** STOP: don't visit this field and stop processing any other fields for this document. */
    STOP
  }
}