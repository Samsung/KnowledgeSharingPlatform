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
import org.apache.lucene.index.DocsEnum; // javadoc @link
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.FixedBitSet;

/**
 * A {@link Filter} that only accepts documents whose single
 * term value in the specified field is contained in the
 * provided set of allowed terms.
 * 
 * <p/>
 * 
 * This is the same functionality as TermsFilter (from
 * queries/), except this filter requires that the
 * field contains only a single term for all documents.
 * Because of drastically different implementations, they
 * also have different performance characteristics, as
 * described below.
 * 
 * 
 * <p/>
 * 
 * With each search, this filter translates the specified
 * set of Terms into a private {@link FixedBitSet} keyed by
 * term number per unique {@link IndexReader} (normally one
 * reader per segment).  Then, during matching, the term
 * number for each docID is retrieved from the cache and
 * then checked for inclusion using the {@link FixedBitSet}.
 * Since all testing is done using RAM resident data
 * structures, performance should be very fast, most likely
 * fast enough to not require further caching of the
 * DocIdSet for each possible combination of terms.
 * However, because docIDs are simply scanned linearly, an
 * index with a great many small documents may find this
 * linear scan too costly.
 * 
 * <p/>
 * 
 * In contrast, TermsFilter builds up an {@link FixedBitSet},
 * keyed by docID, every time it's created, by enumerating
 * through all matching docs using {@link DocsEnum} to seek
 * and scan through each term's docID list.  While there is
 * no linear scan of all docIDs, besides the allocation of
 * the underlying array in the {@link FixedBitSet}, this
 * approach requires a number of "disk seeks" in proportion
 * to the number of terms, which can be exceptionally costly
 * when there are cache misses in the OS's IO cache.
 * 
 * <p/>
 * 
 * Generally, this filter will be slower on the first
 * invocation for a given field, but subsequent invocations,
 * even if you change the allowed set of Terms, should be
 * faster than TermsFilter, especially as the number of
 * Terms being matched increases.  If you are matching only
 * a very small number of terms, and those terms in turn
 * match a very small number of documents, TermsFilter may
 * perform faster.
 *
 * <p/>
 *
 * Which filter is best is very application dependent.
 */

public class DocValuesTermsFilter extends Filter {
  private String field;
  private BytesRef[] terms;

  public DocValuesTermsFilter(String field, BytesRef... terms) {
    this.field = field;
    this.terms = terms;
  }

  public DocValuesTermsFilter(String field, String... terms) {
    this.field = field;
    this.terms = new BytesRef[terms.length];
    for (int i = 0; i < terms.length; i++)
      this.terms[i] = new BytesRef(terms[i]);
  }

  @Override
  public DocIdSet getDocIdSet(LeafReaderContext context, Bits acceptDocs) throws IOException {
    final SortedDocValues fcsi = DocValues.getSorted(context.reader(), field);
    final FixedBitSet bits = new FixedBitSet(fcsi.getValueCount());
    for (int i=0;i<terms.length;i++) {
      int ord = fcsi.lookupTerm(terms[i]);
      if (ord >= 0) {
        bits.set(ord);
      }
    }
    return new DocValuesDocIdSet(context.reader().maxDoc(), acceptDocs) {
      @Override
      protected final boolean matchDoc(int doc) {
        int ord = fcsi.getOrd(doc);
        if (ord == -1) {
          // missing
          return false;
        } else {
          return bits.get(ord);
        }
      }
    };
  }
}
