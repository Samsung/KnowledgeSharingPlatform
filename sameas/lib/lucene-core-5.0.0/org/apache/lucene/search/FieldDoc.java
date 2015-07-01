package org.apache.lucene.search;

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

import java.util.Arrays;

/**
 * Expert: A ScoreDoc which also contains information about
 * how to sort the referenced document.  In addition to the
 * document number and score, this object contains an array
 * of values for the document from the field(s) used to sort.
 * For example, if the sort criteria was to sort by fields
 * "a", "b" then "c", the <code>fields</code> object array
 * will have three elements, corresponding respectively to
 * the term values for the document in fields "a", "b" and "c".
 * The class of each element in the array will be either
 * Integer, Float or String depending on the type of values
 * in the terms of each field.
 *
 * <p>Created: Feb 11, 2004 1:23:38 PM
 *
 * @since   lucene 1.4
 * @see ScoreDoc
 * @see TopFieldDocs
 */
public class FieldDoc extends ScoreDoc {

  /** Expert: The values which are used to sort the referenced document.
   * The order of these will match the original sort criteria given by a
   * Sort object.  Each Object will have been returned from
   * the <code>value</code> method corresponding
   * FieldComparator used to sort this field.
   * @see Sort
   * @see IndexSearcher#search(Query,Filter,int,Sort)
   */
  public Object[] fields;

  /** Expert: Creates one of these objects with empty sort information. */
  public FieldDoc(int doc, float score) {
    super (doc, score);
  }

  /** Expert: Creates one of these objects with the given sort information. */
  public FieldDoc(int doc, float score, Object[] fields) {
    super (doc, score);
    this.fields = fields;
  }
  
  /** Expert: Creates one of these objects with the given sort information. */
  public FieldDoc(int doc, float score, Object[] fields, int shardIndex) {
    super (doc, score, shardIndex);
    this.fields = fields;
  }
  
  // A convenience method for debugging.
  @Override
  public String toString() {
    // super.toString returns the doc and score information, so just add the
    // fields information
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append(" fields=");
    sb.append(Arrays.toString(fields));
    return sb.toString();
  }
}
