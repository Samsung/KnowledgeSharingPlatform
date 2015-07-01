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
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.SortedSetDocValues;

/** 
 * SortField for {@link SortedSetDocValues}.
 * <p>
 * A SortedSetDocValues contains multiple values for a field, so sorting with
 * this technique "selects" a value as the representative sort value for the document.
 * <p>
 * By default, the minimum value in the set is selected as the sort value, but
 * this can be customized. Selectors other than the default do have some limitations
 * to ensure that all selections happen in constant-time for performance.
 * <p>
 * Like sorting by string, this also supports sorting missing values as first or last,
 * via {@link #setMissingValue(Object)}.
 * <p>
 * @see SortedSetSelector
 */
public class SortedSetSortField extends SortField {
  
  private final SortedSetSelector.Type selector;
  
  /**
   * Creates a sort, possibly in reverse, by the minimum value in the set 
   * for the document.
   * @param field Name of field to sort by.  Must not be null.
   * @param reverse True if natural order should be reversed.
   */
  public SortedSetSortField(String field, boolean reverse) {
    this(field, reverse, SortedSetSelector.Type.MIN);
  }

  /**
   * Creates a sort, possibly in reverse, specifying how the sort value from 
   * the document's set is selected.
   * @param field Name of field to sort by.  Must not be null.
   * @param reverse True if natural order should be reversed.
   * @param selector custom selector type for choosing the sort value from the set.
   * <p>
   * NOTE: selectors other than {@link SortedSetSelector.Type#MIN} require optional codec support.
   */
  public SortedSetSortField(String field, boolean reverse, SortedSetSelector.Type selector) {
    super(field, SortField.Type.CUSTOM, reverse);
    if (selector == null) {
      throw new NullPointerException();
    }
    this.selector = selector;
  }
  
  /** Returns the selector in use for this sort */
  public SortedSetSelector.Type getSelector() {
    return selector;
  }

  @Override
  public int hashCode() {
    return 31 * super.hashCode() + selector.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    SortedSetSortField other = (SortedSetSortField) obj;
    if (selector != other.selector) return false;
    return true;
  }
  
  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<sortedset" + ": \"").append(getField()).append("\">");
    if (getReverse()) buffer.append('!');
    if (missingValue != null) {
      buffer.append(" missingValue=");
      buffer.append(missingValue);
    }
    buffer.append(" selector=");
    buffer.append(selector);

    return buffer.toString();
  }

  /**
   * Set how missing values (the empty set) are sorted.
   * <p>
   * Note that this must be {@link #STRING_FIRST} or {@link #STRING_LAST}.
   */
  @Override
  public void setMissingValue(Object missingValue) {
    if (missingValue != STRING_FIRST && missingValue != STRING_LAST) {
      throw new IllegalArgumentException("For SORTED_SET type, missing value must be either STRING_FIRST or STRING_LAST");
    }
    this.missingValue = missingValue;
  }
  
  @Override
  public FieldComparator<?> getComparator(int numHits, int sortPos) throws IOException {
    return new FieldComparator.TermOrdValComparator(numHits, getField(), missingValue == STRING_LAST) {
      @Override
      protected SortedDocValues getSortedDocValues(LeafReaderContext context, String field) throws IOException {
        SortedSetDocValues sortedSet = DocValues.getSortedSet(context.reader(), field);
        return SortedSetSelector.wrap(sortedSet, selector);
      }
    };
  }
}
