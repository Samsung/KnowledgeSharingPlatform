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

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

import org.apache.lucene.util.BytesRef;

/**
  A Term represents a word from text.  This is the unit of search.  It is
  composed of two elements, the text of the word, as a string, and the name of
  the field that the text occurred in.

  Note that terms may represent more than words from text fields, but also
  things like dates, email addresses, urls, etc.  */

public final class Term implements Comparable<Term> {
  String field;
  BytesRef bytes;

  /** Constructs a Term with the given field and bytes.
   * <p>Note that a null field or null bytes value results in undefined
   * behavior for most Lucene APIs that accept a Term parameter. 
   *
   * <p>WARNING: the provided BytesRef is not copied, but used directly.
   * Therefore the bytes should not be modified after construction, for
   * example, you should clone a copy by {@link BytesRef#deepCopyOf}
   * rather than pass reused bytes from a TermsEnum.
   */
  public Term(String fld, BytesRef bytes) {
    field = fld;
    this.bytes = bytes;
  }

  /** Constructs a Term with the given field and text.
   * <p>Note that a null field or null text value results in undefined
   * behavior for most Lucene APIs that accept a Term parameter. */
  public Term(String fld, String text) {
    this(fld, new BytesRef(text));
  }

  /** Constructs a Term with the given field and empty text.
   * This serves two purposes: 1) reuse of a Term with the same field.
   * 2) pattern for a query.
   * 
   * @param fld field's name
   */
  public Term(String fld) {
    this(fld, new BytesRef());
  }

  /** Returns the field of this term.   The field indicates
    the part of a document which this term came from. */
  public final String field() { return field; }

  /** Returns the text of this term.  In the case of words, this is simply the
    text of the word.  In the case of dates and other types, this is an
    encoding of the object as a string.  */
  public final String text() { 
    return toString(bytes);
  }
  
  /** Returns human-readable form of the term text. If the term is not unicode,
   * the raw bytes will be printed instead. */
  public static final String toString(BytesRef termText) {
    // the term might not be text, but usually is. so we make a best effort
    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT);
    try {
      return decoder.decode(ByteBuffer.wrap(termText.bytes, termText.offset, termText.length)).toString();
    } catch (CharacterCodingException e) {
      return termText.toString();
    }
  }

  /** Returns the bytes of this term. */
  public final BytesRef bytes() { return bytes; }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Term other = (Term) obj;
    if (field == null) {
      if (other.field != null)
        return false;
    } else if (!field.equals(other.field))
      return false;
    if (bytes == null) {
      if (other.bytes != null)
        return false;
    } else if (!bytes.equals(other.bytes))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    result = prime * result + ((bytes == null) ? 0 : bytes.hashCode());
    return result;
  }

  /** Compares two terms, returning a negative integer if this
    term belongs before the argument, zero if this term is equal to the
    argument, and a positive integer if this term belongs after the argument.

    The ordering of terms is first by field, then by text.*/
  @Override
  public final int compareTo(Term other) {
    if (field.equals(other.field)) {
      return bytes.compareTo(other.bytes);
    } else {
      return field.compareTo(other.field);
    }
  }

  /** 
   * Resets the field and text of a Term. 
   * <p>WARNING: the provided BytesRef is not copied, but used directly.
   * Therefore the bytes should not be modified after construction, for
   * example, you should clone a copy rather than pass reused bytes from
   * a TermsEnum.
   */
  final void set(String fld, BytesRef bytes) {
    field = fld;
    this.bytes = bytes;
  }

  @Override
  public final String toString() { return field + ":" + text(); }
}
