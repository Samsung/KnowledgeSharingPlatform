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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.LongBitSet;

/**
 * Rewrites MultiTermQueries into a filter, using DocValues for term enumeration.
 * <p>
 * This can be used to perform these queries against an unindexed docvalues field.
 * @lucene.experimental
 */
public final class DocValuesRewriteMethod extends MultiTermQuery.RewriteMethod {
  
  @Override
  public Query rewrite(IndexReader reader, MultiTermQuery query) {
    Query result = new ConstantScoreQuery(new MultiTermQueryDocValuesWrapperFilter(query));
    result.setBoost(query.getBoost());
    return result;
  }
  
  static class MultiTermQueryDocValuesWrapperFilter extends Filter {
    
    protected final MultiTermQuery query;
    
    /**
     * Wrap a {@link MultiTermQuery} as a Filter.
     */
    protected MultiTermQueryDocValuesWrapperFilter(MultiTermQuery query) {
      this.query = query;
    }
    
    @Override
    public String toString() {
      // query.toString should be ok for the filter, too, if the query boost is 1.0f
      return query.toString();
    }
    
    @Override
    public final boolean equals(final Object o) {
      if (o==this) return true;
      if (o==null) return false;
      if (this.getClass().equals(o.getClass())) {
        return this.query.equals( ((MultiTermQueryDocValuesWrapperFilter)o).query );
      }
      return false;
    }
    
    @Override
    public final int hashCode() {
      return query.hashCode();
    }
    
    /** Returns the field name for this query */
    public final String getField() { return query.getField(); }
    
    /**
     * Returns a DocIdSet with documents that should be permitted in search
     * results.
     */
    @Override
    public DocIdSet getDocIdSet(LeafReaderContext context, final Bits acceptDocs) throws IOException {
      final SortedDocValues fcsi = DocValues.getSorted(context.reader(), query.field);
      // Cannot use FixedBitSet because we require long index (ord):
      final LongBitSet termSet = new LongBitSet(fcsi.getValueCount());
      TermsEnum termsEnum = query.getTermsEnum(new Terms() {
        
        @Override
        public TermsEnum iterator(TermsEnum reuse) {
          return fcsi.termsEnum();
        }

        @Override
        public long getSumTotalTermFreq() {
          return -1;
        }

        @Override
        public long getSumDocFreq() {
          return -1;
        }

        @Override
        public int getDocCount() {
          return -1;
        }

        @Override
        public long size() {
          return -1;
        }

        @Override
        public boolean hasFreqs() {
          return false;
        }

        @Override
        public boolean hasOffsets() {
          return false;
        }

        @Override
        public boolean hasPositions() {
          return false;
        }
        
        @Override
        public boolean hasPayloads() {
          return false;
        }
      });
      
      assert termsEnum != null;
      if (termsEnum.next() != null) {
        // fill into a bitset
        do {
          long ord = termsEnum.ord();
          if (ord >= 0) {
            termSet.set(ord);
          }
        } while (termsEnum.next() != null);
      } else {
        return null;
      }
      
      return new DocValuesDocIdSet(context.reader().maxDoc(), acceptDocs) {
        @Override
        protected final boolean matchDoc(int doc) throws ArrayIndexOutOfBoundsException {
          int ord = fcsi.getOrd(doc);
          if (ord == -1) {
            return false;
          }
          return termSet.get(ord);
        }
      };
    }
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    return 641;
  }
}
