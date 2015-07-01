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
import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.util.BitDocIdSet;
import org.apache.lucene.util.BitSet;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.Bits.MatchAllBits;
import org.apache.lucene.util.Bits.MatchNoBits;

/**
 * A {@link Filter} that accepts all documents that have one or more values in a
 * given field. This {@link Filter} request {@link Bits} from
 * {@link org.apache.lucene.index.LeafReader#getDocsWithField}
 */
public class FieldValueFilter extends Filter {
  private final String field;
  private final boolean negate;

  /**
   * Creates a new {@link FieldValueFilter}
   * 
   * @param field
   *          the field to filter
   */
  public FieldValueFilter(String field) {
    this(field, false);
  }

  /**
   * Creates a new {@link FieldValueFilter}
   * 
   * @param field
   *          the field to filter
   * @param negate
   *          iff <code>true</code> all documents with no value in the given
   *          field are accepted.
   * 
   */
  public FieldValueFilter(String field, boolean negate) {
    this.field = field;
    this.negate = negate;
  }
  
  /**
   * Returns the field this filter is applied on.
   * @return the field this filter is applied on.
   */
  public String field() {
    return field;
  }
  
  /**
   * Returns <code>true</code> iff this filter is negated, otherwise <code>false</code> 
   * @return <code>true</code> iff this filter is negated, otherwise <code>false</code>
   */
  public boolean negate() {
    return negate; 
  }

  @Override
  public DocIdSet getDocIdSet(LeafReaderContext context, Bits acceptDocs)
      throws IOException {
    final Bits docsWithField = DocValues.getDocsWithField(
        context.reader(), field);
    if (negate) {
      if (docsWithField instanceof MatchAllBits) {
        return null;
      }
      return new DocValuesDocIdSet(context.reader().maxDoc(), acceptDocs) {
        @Override
        protected final boolean matchDoc(int doc) {
          return !docsWithField.get(doc);
        }
      };
    } else {
      if (docsWithField instanceof MatchNoBits) {
        return null;
      }
      if (docsWithField instanceof BitSet) {
        // UweSays: this is always the case for our current impl - but who knows
        // :-)
        return BitsFilteredDocIdSet.wrap(new BitDocIdSet((BitSet) docsWithField), acceptDocs);
      }
      return new DocValuesDocIdSet(context.reader().maxDoc(), acceptDocs) {
        @Override
        protected final boolean matchDoc(int doc) {
          return docsWithField.get(doc);
        }
      };
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((field == null) ? 0 : field.hashCode());
    result = prime * result + (negate ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FieldValueFilter other = (FieldValueFilter) obj;
    if (field == null) {
      if (other.field != null)
        return false;
    } else if (!field.equals(other.field))
      return false;
    if (negate != other.negate)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "FieldValueFilter [field=" + field + ", negate=" + negate + "]";
  }

}
