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
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.similarities.DefaultSimilarity; // javadocs
import org.apache.lucene.search.similarities.Similarity; // javadocs
import org.apache.lucene.util.BytesRef;

// TODO: how to handle versioning here...?

/** Represents a single field for indexing.  IndexWriter
 *  consumes Iterable&lt;IndexableField&gt; as a document.
 *
 *  @lucene.experimental */

public interface IndexableField {

  /** Field name */
  public String name();

  /** {@link IndexableFieldType} describing the properties
   * of this field. */
  public IndexableFieldType fieldType();
  
  /** 
   * Returns the field's index-time boost.
   * <p>
   * Only fields can have an index-time boost, if you want to simulate
   * a "document boost", then you must pre-multiply it across all the
   * relevant fields yourself. 
   * <p>The boost is used to compute the norm factor for the field.  By
   * default, in the {@link Similarity#computeNorm(FieldInvertState)} method, 
   * the boost value is multiplied by the length normalization factor and then
   * rounded by {@link DefaultSimilarity#encodeNormValue(float)} before it is stored in the
   * index.  One should attempt to ensure that this product does not overflow
   * the range of that encoding.
   * <p>
   * It is illegal to return a boost other than 1.0f for a field that is not
   * indexed ({@link IndexableFieldType#indexOptions()} is IndexOptions.NONE) or
   * omits normalization values ({@link IndexableFieldType#omitNorms()} returns true).
   *
   * @see Similarity#computeNorm(FieldInvertState)
   * @see DefaultSimilarity#encodeNormValue(float)
   */
  public float boost();

  /** Non-null if this field has a binary value */
  public BytesRef binaryValue();

  /** Non-null if this field has a string value */
  public String stringValue();

  /** Non-null if this field has a Reader value */
  public Reader readerValue();

  /** Non-null if this field has a numeric value */
  public Number numericValue();

  /**
   * Creates the TokenStream used for indexing this field.  If appropriate,
   * implementations should use the given Analyzer to create the TokenStreams.
   *
   * @param analyzer Analyzer that should be used to create the TokenStreams from
   * @param reuse TokenStream for a previous instance of this field <b>name</b>. This allows
   *              custom field types (like StringField and NumericField) that do not use
   *              the analyzer to still have good performance. Note: the passed-in type
   *              may be inappropriate, for example if you mix up different types of Fields
   *              for the same field name. So its the responsibility of the implementation to
   *              check.
   * @return TokenStream value for indexing the document.  Should always return
   *         a non-null value if the field is to be indexed
   * @throws IOException Can be thrown while creating the TokenStream
   */
  public TokenStream tokenStream(Analyzer analyzer, TokenStream reuse) throws IOException;
}
