package org.apache.lucene.document;

import org.apache.lucene.index.IndexReader; // javadocs
import org.apache.lucene.search.IndexSearcher; // javadocs
import org.apache.lucene.util.BytesRef;

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

/** A field whose value is stored so that {@link
 *  IndexSearcher#doc} and {@link IndexReader#document} will
 *  return the field and its value. */
public final class StoredField extends Field {

  /**
   * Type for a stored-only field.
   */
  public final static FieldType TYPE;
  static {
    TYPE = new FieldType();
    TYPE.setStored(true);
    TYPE.freeze();
  }

  /**
   * Create a stored-only field with the given binary value.
   * <p>NOTE: the provided byte[] is not copied so be sure
   * not to change it until you're done with this field.
   * @param name field name
   * @param value byte array pointing to binary content (not copied)
   * @throws IllegalArgumentException if the field name is null.
   */
  public StoredField(String name, byte[] value) {
    super(name, value, TYPE);
  }
  
  /**
   * Create a stored-only field with the given binary value.
   * <p>NOTE: the provided byte[] is not copied so be sure
   * not to change it until you're done with this field.
   * @param name field name
   * @param value byte array pointing to binary content (not copied)
   * @param offset starting position of the byte array
   * @param length valid length of the byte array
   * @throws IllegalArgumentException if the field name is null.
   */
  public StoredField(String name, byte[] value, int offset, int length) {
    super(name, value, offset, length, TYPE);
  }

  /**
   * Create a stored-only field with the given binary value.
   * <p>NOTE: the provided BytesRef is not copied so be sure
   * not to change it until you're done with this field.
   * @param name field name
   * @param value BytesRef pointing to binary content (not copied)
   * @throws IllegalArgumentException if the field name is null.
   */
  public StoredField(String name, BytesRef value) {
    super(name, value, TYPE);
  }

  /**
   * Create a stored-only field with the given string value.
   * @param name field name
   * @param value string value
   * @throws IllegalArgumentException if the field name or value is null.
   */
  public StoredField(String name, String value) {
    super(name, value, TYPE);
  }

  // TODO: not great but maybe not a big problem?
  /**
   * Create a stored-only field with the given integer value.
   * @param name field name
   * @param value integer value
   * @throws IllegalArgumentException if the field name is null.
   */
  public StoredField(String name, int value) {
    super(name, TYPE);
    fieldsData = value;
  }

  /**
   * Create a stored-only field with the given float value.
   * @param name field name
   * @param value float value
   * @throws IllegalArgumentException if the field name is null.
   */
  public StoredField(String name, float value) {
    super(name, TYPE);
    fieldsData = value;
  }

  /**
   * Create a stored-only field with the given long value.
   * @param name field name
   * @param value long value
   * @throws IllegalArgumentException if the field name is null.
   */
  public StoredField(String name, long value) {
    super(name, TYPE);
    fieldsData = value;
  }

  /**
   * Create a stored-only field with the given double value.
   * @param name field name
   * @param value double value
   * @throws IllegalArgumentException if the field name is null.
   */
  public StoredField(String name, double value) {
    super(name, TYPE);
    fieldsData = value;
  }
}
