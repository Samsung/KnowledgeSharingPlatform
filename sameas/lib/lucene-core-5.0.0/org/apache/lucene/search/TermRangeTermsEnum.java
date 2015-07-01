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

import org.apache.lucene.index.FilteredTermsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 * Subclass of FilteredTermEnum for enumerating all terms that match the
 * specified range parameters.  Each term in the enumeration is
 * greater than all that precede it.</p>
 */
public class TermRangeTermsEnum extends FilteredTermsEnum {

  final private boolean includeLower;
  final private boolean includeUpper;
  final private BytesRef lowerBytesRef;
  final private BytesRef upperBytesRef;

  /**
   * Enumerates all terms greater/equal than <code>lowerTerm</code>
   * but less/equal than <code>upperTerm</code>. 
   * 
   * If an endpoint is null, it is said to be "open". Either or both 
   * endpoints may be open.  Open endpoints may not be exclusive 
   * (you can't select all but the first or last term without 
   * explicitly specifying the term to exclude.)
   * 
   * @param tenum
   *          TermsEnum to filter
   * @param lowerTerm
   *          The term text at the lower end of the range
   * @param upperTerm
   *          The term text at the upper end of the range
   * @param includeLower
   *          If true, the <code>lowerTerm</code> is included in the range.
   * @param includeUpper
   *          If true, the <code>upperTerm</code> is included in the range.
   */
  public TermRangeTermsEnum(TermsEnum tenum, BytesRef lowerTerm, BytesRef upperTerm, 
    boolean includeLower, boolean includeUpper) {
    super(tenum);

    // do a little bit of normalization...
    // open ended range queries should always be inclusive.
    if (lowerTerm == null) {
      this.lowerBytesRef = new BytesRef();
      this.includeLower = true;
    } else {
      this.lowerBytesRef = lowerTerm;
      this.includeLower = includeLower;
    }

    if (upperTerm == null) {
      this.includeUpper = true;
      upperBytesRef = null;
    } else {
      this.includeUpper = includeUpper;
      upperBytesRef = upperTerm;
    }

    setInitialSeekTerm(lowerBytesRef);
  }

  @Override
  protected AcceptStatus accept(BytesRef term) {
    if (!this.includeLower && term.equals(lowerBytesRef))
      return AcceptStatus.NO;
    
    // Use this field's default sort ordering
    if (upperBytesRef != null) {
      final int cmp = upperBytesRef.compareTo(term);
      /*
       * if beyond the upper term, or is exclusive and this is equal to
       * the upper term, break out
       */
      if ((cmp < 0) ||
          (!includeUpper && cmp==0)) {
        return AcceptStatus.END;
      }
    }

    return AcceptStatus.YES;
  }
}
