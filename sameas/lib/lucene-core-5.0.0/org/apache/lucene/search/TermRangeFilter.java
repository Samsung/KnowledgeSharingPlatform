package org.apache.lucene.search;

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

/**
 * A Filter that restricts search results to a range of term
 * values in a given field.
 *
 * <p>This filter matches the documents looking for terms that fall into the
 * supplied range according to {@link
 * Byte#compareTo(Byte)},  It is not intended
 * for numerical ranges; use {@link NumericRangeFilter} instead.
 *
 * <p>If you construct a large number of range filters with different ranges but on the 
 * same field, {@link DocValuesRangeFilter} may have significantly better performance. 
 * @since 2.9
 */
public class TermRangeFilter extends MultiTermQueryWrapperFilter<TermRangeQuery> {
    
  /**
   * @param fieldName The field this range applies to
   * @param lowerTerm The lower bound on this range
   * @param upperTerm The upper bound on this range
   * @param includeLower Does this range include the lower bound?
   * @param includeUpper Does this range include the upper bound?
   * @throws IllegalArgumentException if both terms are null or if
   *  lowerTerm is null and includeLower is true (similar for upperTerm
   *  and includeUpper)
   */
  public TermRangeFilter(String fieldName, BytesRef lowerTerm, BytesRef upperTerm,
                     boolean includeLower, boolean includeUpper) {
      super(new TermRangeQuery(fieldName, lowerTerm, upperTerm, includeLower, includeUpper));
  }

  /**
   * Factory that creates a new TermRangeFilter using Strings for term text.
   */
  public static TermRangeFilter newStringRange(String field, String lowerTerm, String upperTerm, boolean includeLower, boolean includeUpper) {
    BytesRef lower = lowerTerm == null ? null : new BytesRef(lowerTerm);
    BytesRef upper = upperTerm == null ? null : new BytesRef(upperTerm);
    return new TermRangeFilter(field, lower, upper, includeLower, includeUpper);
  }
  
  /**
   * Constructs a filter for field <code>fieldName</code> matching
   * less than or equal to <code>upperTerm</code>.
   */
  public static TermRangeFilter Less(String fieldName, BytesRef upperTerm) {
      return new TermRangeFilter(fieldName, null, upperTerm, false, true);
  }

  /**
   * Constructs a filter for field <code>fieldName</code> matching
   * greater than or equal to <code>lowerTerm</code>.
   */
  public static TermRangeFilter More(String fieldName, BytesRef lowerTerm) {
      return new TermRangeFilter(fieldName, lowerTerm, null, true, false);
  }
  
  /** Returns the lower value of this range filter */
  public BytesRef getLowerTerm() { return query.getLowerTerm(); }

  /** Returns the upper value of this range filter */
  public BytesRef getUpperTerm() { return query.getUpperTerm(); }
  
  /** Returns <code>true</code> if the lower endpoint is inclusive */
  public boolean includesLower() { return query.includesLower(); }
  
  /** Returns <code>true</code> if the upper endpoint is inclusive */
  public boolean includesUpper() { return query.includesUpper(); }
}
