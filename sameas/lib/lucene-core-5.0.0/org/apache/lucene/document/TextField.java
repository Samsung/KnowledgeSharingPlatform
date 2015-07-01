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

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexOptions;

/** A field that is indexed and tokenized, without term
 *  vectors.  For example this would be used on a 'body'
 *  field, that contains the bulk of a document's text. */

public final class TextField extends Field {

  /** Indexed, tokenized, not stored. */
  public static final FieldType TYPE_NOT_STORED = new FieldType();

  /** Indexed, tokenized, stored. */
  public static final FieldType TYPE_STORED = new FieldType();

  static {
    TYPE_NOT_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    TYPE_NOT_STORED.setTokenized(true);
    TYPE_NOT_STORED.freeze();

    TYPE_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
    TYPE_STORED.setTokenized(true);
    TYPE_STORED.setStored(true);
    TYPE_STORED.freeze();
  }

  // TODO: add sugar for term vectors...?

  /** Creates a new un-stored TextField with Reader value. 
   * @param name field name
   * @param reader reader value
   * @throws IllegalArgumentException if the field name is null
   * @throws NullPointerException if the reader is null
   */
  public TextField(String name, Reader reader) {
    super(name, reader, TYPE_NOT_STORED);
  }

  /** Creates a new TextField with String value. 
   * @param name field name
   * @param value string value
   * @param store Store.YES if the content should also be stored
   * @throws IllegalArgumentException if the field name or value is null.
   */
  public TextField(String name, String value, Store store) {
    super(name, value, store == Store.YES ? TYPE_STORED : TYPE_NOT_STORED);
  }
  
  /** Creates a new un-stored TextField with TokenStream value. 
   * @param name field name
   * @param stream TokenStream value
   * @throws IllegalArgumentException if the field name is null.
   * @throws NullPointerException if the tokenStream is null
   */
  public TextField(String name, TokenStream stream) {
    super(name, stream, TYPE_NOT_STORED);
  }
}
